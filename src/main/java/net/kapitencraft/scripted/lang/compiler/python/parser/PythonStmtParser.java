package net.kapitencraft.scripted.lang.compiler.python.parser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.scripted.lang.compiler.python.PythonOperationHolder;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.attribute.OperationHolder;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

public class PythonStmtParser extends PythonExprParser implements StmtParser {

    private ClassReference funcRetType = VarTypeManager.VOID.reference();
    private final List<Boolean> seenReturn = new ArrayList<>();
    private int loopIndex = 0;

    public PythonStmtParser(ErrorStorage errorStorage, SourceTree sourceSink, CompileSource source) {
        super(errorStorage, sourceSink, source);
    }

    private void seenReturn() {
        seenReturn.set(seenReturn.size() - 1, true);
    }

    private void pushScope() {
        seenReturn.add(false);
    }

    private void popScope() {
        seenReturn.removeLast();
    }

    private Stmt popScopeStmt() {
        popScope();
        return new Stmt.ClearLocals();
    }

    private Stmt declaration() {
        if (seenReturn.getLast()) {
            error(peek(), "unreachable statement");
        }
        if (matchAtIndent(FINAL)) return varDeclaration(true, consumeVarType(generics).getReference());

        skipIndent();
        Optional<SourceReference> type = tryConsumeVarType(generics);
        Stmt stmt = type.map(sourceClassReference -> {
            if (match(DOT)) {
                Stmt.Expression expression = new Stmt.Expression();
                expression.expression = parseObjAttributes(sourceClassReference.getReference());
                return expression;
            } else
                return varDeclaration(false, sourceClassReference.getReference());
        }).orElseGet(this::statement);
        if (panicMode)
            synchronize();
        return stmt;
    }

    private Stmt.VarDecl varDecl(boolean isFinal, ClassReference type, Token name) {

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        consumeEndOfArg();
        Stmt.VarDecl varDecl = new Stmt.VarDecl();
        varDecl.name = name;
        varDecl.type = type;
        varDecl.initializer = initializer;
        varDecl.isFinal = isFinal;
        return varDecl;
    }

    private Stmt varDeclaration(boolean isFinal, ClassReference type) {
        Token name = consume(IDENTIFIER, "Expected variable name.");
        return varDecl(isFinal, type, name);
    }

    private Stmt statement() {
        Stmt stmt;
        if (match(C_BRACKET_O)) {
            Stmt.Block block = new Stmt.Block();
            block.statements = block("block");
            stmt = block;
        } else if (match(RETURN)) stmt = returnStatement();
        else if (match(TRY)) stmt = tryStatement();
        else if (match(THROW)) stmt = thrStatement();
        else if (match(CONTINUE, BREAK)) stmt = loopInterruptionStatement();
        else if (match(FOR)) stmt = forStatement();
        else if (match(IF)) stmt = ifStatement();
        else if (match(WHILE)) stmt = whileStatement();
        else if (match(TRACE)) stmt = debugTrace();
        else stmt = expressionStatement();

        if (panicMode) {
            synchronize();
            panicMode = false;
        }
        return stmt;
    }

    private Stmt debugTrace() {
        Token keyword = previous();
        List<Token> locals = new ArrayList<>();
        if (match(S_BRACKET_O)) {
            do {
                locals.add(consumeIdentifier());
            } while (match(COMMA));
            consume(S_BRACKET_C, "expected ']' after trace debug");
        }
        consumeEndOfArg();
        Stmt.DebugTrace debugTrace = new Stmt.DebugTrace();
        debugTrace.keyword = keyword;
        debugTrace.localNames = locals.toArray(Token[]::new);
        return debugTrace;
    }

    private Stmt tryStatement() {
        consumeCurlyOpen("try statement");
        Stmt.Block tryBlock = new Stmt.Block();
        tryBlock.statements = block("try statement");
        Token brClose = previous();

        List<Pair<Pair<ClassReference[], Token>, Stmt.Block>> catches = new ArrayList<>(); //what an insane varType
        while (match(CATCH)) {
            List<ClassReference> targets = new ArrayList<>();
            consumeBracketOpen("catch");
            do {
                targets.add(consumeVarType(generics).getReference());
            } while (match(SINGLE_OR));
            pushScope();
            Token name = consumeIdentifier();
            consumeBracketClose("catch");
            consumeCurlyOpen("catch statement");
            Stmt.Block body = new Stmt.Block();
            body.statements = block("catch statement");
            body.statements.add(popScopeStmt());
            catches.add(Pair.of(
                    Pair.of(
                            targets.toArray(new ClassReference[0]),
                            name
                    ),
                    body
            ));
        }
        Stmt.Block finallyBlock = null;
        if (match(FINALLY)) {
            consumeCurlyOpen("finally statement");
            finallyBlock = new Stmt.Block();
            finallyBlock.statements = block("finally statement");
        } else if (catches.isEmpty()) error(brClose, "expected 'catch' or 'finally'");

        Stmt.Try aTry = new Stmt.Try();
        aTry.body = tryBlock;
        aTry.catches = catches.toArray(Pair[]::new);
        aTry.finale = finallyBlock;
        return aTry;
    }

    private Stmt thrStatement() {
        Token keyword = previous();
        Expr val = expression();
        consumeEndOfArg();
        seenReturn();
        Stmt.Throw aThrow = new Stmt.Throw();
        aThrow.keyword = keyword;
        aThrow.value = val;
        return aThrow;
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(LINE_FEED)) {
            value = expression();
        }

        consumeEndOfArg();
        seenReturn();
        Stmt.Return aReturn = new Stmt.Return();
        aReturn.keyword = keyword;
        aReturn.value = value;
        return aReturn;
    }

    private Stmt loopInterruptionStatement() {
        Token token = previous();
        if (loopIndex <= 0) error(token, "'" + token.lexeme() + "' can only be used inside loops");
        consumeEndOfArg();
        seenReturn();
        Stmt.LoopInterruption loopInterruption = new Stmt.LoopInterruption();
        loopInterruption.type = token;
        return loopInterruption;
    }

    private Stmt forStatement() {
        Token keyword = previous();

        SourceReference varType = consumeVarType(generics);

        Token name = consumeIdentifier();

        consume(IN, "'in' expected");

        Expr expression = expression();

        consumeScopeOpen("for");
        pushScope();

        Stmt body = parseIndentedStatement(++indents);
        indents--;

        loopIndex--;
        body = mergeBody(body, popScopeStmt());

        Stmt.ForEach forEach = new Stmt.ForEach();
        forEach.type = varType.getReference();
        forEach.name = name;
        forEach.body = body;
        forEach.initializer = expression;
        return forEach;
    }

    private Stmt parseIndentedStatement(int indent) {
        List<Stmt> stmts = new ArrayList<>();
        while (true) {
            int c = current;
            int iCount = 0;
            while (match(TAB) && iCount++ < indent);
            if (iCount == indent) {
                stmts.add(declaration());
            } else {
                current = c;
                break;
            }
        }

        if (stmts.size() == 1) {
            return stmts.getFirst();
        }
        Stmt.Block block = new Stmt.Block();
        block.statements = stmts;
        return block;
    }

    private Stmt ifStatement() {
        Token keyword = previous();
        Expr condition = expression();

        boolean allSeenReturn = true;
        boolean branchSeenReturn;

        pushScope();
        consumeScopeOpen("if");

        int nextIndent = ++this.indents;

        Stmt thenBranch = parseIndentedStatement(nextIndent);

        if (!(branchSeenReturn = this.seenReturn.getLast()))
            allSeenReturn = false;
        thenBranch = mergeBody(thenBranch, popScopeStmt());
        Stmt elseBranch = null;
        List<ElifBranch> elifs = new ArrayList<>();
        while (match(ELIF)) {
            consumeBracketOpen("elif");
            Expr elifCondition = expression();
            consumeBracketClose("elif condition");
            pushScope();
            consumeScopeOpen("elif");

            Stmt elifStmt = parseIndentedStatement(nextIndent);
            elifStmt = mergeBody(elifStmt, popScopeStmt());
            boolean seenReturn = this.seenReturn.getLast();
            elifs.add(new ElifBranch(elifCondition, elifStmt, seenReturn));
            if (!seenReturn && allSeenReturn)
                allSeenReturn = false;
        }

        boolean elseBranchSeenReturn = false;
        if (match(ELSE)) {
            pushScope();
            consumeScopeOpen("else");

            elseBranch = parseIndentedStatement(nextIndent);
            if (!this.seenReturn.getLast() && allSeenReturn)
                allSeenReturn = false;
            elseBranch = mergeBody(elseBranch, popScopeStmt());
        } else
            allSeenReturn = false;

        if (allSeenReturn)
            seenReturn();

        indents--;

        Stmt.If anIf = new Stmt.If();
        anIf.condition = condition;
        anIf.thenBranch = thenBranch;
        anIf.branchSeenReturn = branchSeenReturn;
        anIf.elseBranch = elseBranch;
        anIf.elseBranchSeenReturn = elseBranchSeenReturn;
        anIf.elifs = elifs.toArray(ElifBranch[]::new);
        anIf.keyword = keyword;
        return anIf;
    }

    private Stmt mergeBody(Stmt first, Stmt second) {
        if (first instanceof Stmt.Block block) {
            block.statements.add(second);
            return block;
        } else if (second instanceof Stmt.Block block) {
            block.statements.addFirst(first);
            return block;
        }
        Stmt.Block stmt = new Stmt.Block();
        stmt.statements = new ArrayList<>();
        stmt.statements.add(first);
        stmt.statements.add(second);
        return stmt;
    }

    private Stmt whileStatement() {
        Token keyword = previous();
        consumeBracketOpen("while");
        Expr condition = expression();
        consumeBracketClose("while condition");
        this.loopIndex++;
        this.pushScope();
        consumeScopeOpen("while");

        int nextIndent = ++this.indents;

        Stmt body = parseIndentedStatement(nextIndent);
        body = mergeBody(body, popScopeStmt());

        this.indents--;
        this.loopIndex--;

        Stmt.While aWhile = new Stmt.While();
        aWhile.condition = condition;
        aWhile.body = body;
        aWhile.keyword = keyword;
        return aWhile;
    }

    private List<Stmt> block(String name) {
        List<Stmt> statements = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            statements.add(declaration());
        }

        consumeCurlyClose(name);
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();

        consumeEndOfArg();
        Stmt.Expression stmt = new Stmt.Expression();
        stmt.expression = expr;
        return stmt;
    }

    public List<Stmt> parse() {
        if (tokens.length == 0) return List.of(returnStmt(Token.createNative("return")));
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) stmts.add(declaration());
        if (!seenReturn.getLast()) stmts.add(returnStmt(Token.createNative("return")));
        return stmts;
    }

    private Stmt returnStmt(Token aReturn) {
        Stmt.Return stmt = new Stmt.Return();
        stmt.keyword = aReturn;
        return stmt;
    }

    @Override
    public void applyOperation(OperationHolder holder, VarTypeContainer parser) {
        if (!(holder instanceof PythonOperationHolder pythonMethodHolder))
            throw new IllegalArgumentException("holder must be a PythonMethodHolder");
        this.indents = pythonMethodHolder.indent();
        apply(holder.body(), parser);
        if (holder.isStatic())
            applyStaticMethod(holder.retType(), holder.generics());
        else
            applyMethod(holder.retType(), holder.generics());

    }

    public void applyMethod(ClassReference retType, @Nullable Generics generics) {
        this.pushScope();
        this.funcRetType = retType;
        if (generics != null) generics.pushToStack(this.generics);
        else this.generics.push(Map.of());
    }

    public void popMethod(Token methodEnd) {
        if (!funcRetType.is(VarTypeManager.VOID) && !seenReturn.getLast())
            error(methodEnd, "missing return statement");
        this.popScope(); //ignore return value; methods that end get their locals removed either way
        this.generics.pop();
        funcRetType = VarTypeManager.VOID.reference();
    }

    public void applyStaticMethod(ClassReference retType, @Nullable Generics generics) {
        this.pushScope();
        this.funcRetType = retType;
        if (generics != null) generics.pushToStack(this.generics);
        else this.generics.push(Map.of());

    }

    public Annotation[] parseAnnotations(AnnotationObj[] annotations, VarTypeContainer container) {
        return Arrays.stream(annotations).map(annotationObj -> this.parseAnnotation(annotationObj, container)).toArray(Annotation[]::new);
    }
}
