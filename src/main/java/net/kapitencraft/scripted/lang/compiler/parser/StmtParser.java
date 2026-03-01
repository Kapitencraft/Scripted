package net.kapitencraft.scripted.lang.compiler.parser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.compiler.VarTypeParser;
import net.kapitencraft.scripted.lang.compiler.analyser.BytecodeVars;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

@SuppressWarnings("ThrowableNotThrown")
public class StmtParser extends ExprParser {

    public StmtParser(Compiler.ErrorStorage errorStorage) {
        super(errorStorage);
    }

    private ClassReference funcRetType = VarTypeManager.VOID.reference();
    private final Stack<Boolean> seenReturn = new Stack<>();
    private int loopIndex = 0;

    @Override
    public void apply(Token[] toParse, VarTypeParser targetAnalyser) {
        super.apply(toParse, targetAnalyser);
        seenReturn.clear(); //reset entire return stack
        seenReturn.add(false);
    }

    private void seenReturn() {
        seenReturn.set(seenReturn.size()-1, true);
    }

    private void pushScope() {
        varAnalyser.push();
        seenReturn.add(false);
    }

    private Stmt popScope() {
        seenReturn.pop();
        return varAnalyser.pop();
    }

    private Stmt declaration() {
        if (seenReturn.peek()) {
            error(peek(), "unreachable statement");
        }
        try {
            if (match(FINAL)) return varDeclaration(true, consumeVarType(generics).getReference());

            Optional<SourceClassReference> type = tryConsumeVarType(generics);
            return type.map(sourceClassReference -> varDeclaration(false, sourceClassReference.getReference())).orElseGet(this::statement);
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt.VarDecl varDecl(boolean isFinal, ClassReference type, Token name) {

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        byte index = createVar(name, type, initializer != null, isFinal);

        if (initializer != null) {
            checkVarType(name, initializer);
        }

        consumeEndOfArg();
        return new Stmt.VarDecl(name, type, initializer, isFinal, index);
    }

    private Stmt varDeclaration(boolean isFinal, ClassReference type) {
        Token name = consume(IDENTIFIER, "Expected variable name.");
        return varDecl(isFinal, type, name);
    }

    private Stmt statement() {
        try {
            if (match(C_BRACKET_O)) return new Stmt.Block(block("block"));
            if (match(RETURN)) return returnStatement();
            if (match(TRY)) return tryStatement();
            if (match(THROW)) return thrStatement();
            if (match(CONTINUE, BREAK)) return loopInterruptionStatement();
            if (match(FOR)) return forStatement();
            if (match(IF)) return ifStatement();
            if (match(WHILE)) return whileStatement();
            if (match(TRACE)) return debugTrace();

            return expressionStatement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt debugTrace() {
        Token keyword = previous();
        List<String> locals = new ArrayList<>();
        if (match(S_BRACKET_O)) {
            do {
                Token token = consumeIdentifier();
                if (this.varAnalyser.get(token.lexeme()) == BytecodeVars.FetchResult.FAIL) {
                    error(token, "no local variable named '" + token.lexeme() + "'");
                }
                locals.add(token.lexeme());
            } while (match(COMMA));
            consume(S_BRACKET_C, "expected ']' after trace debug");
        } else {
            locals = this.varAnalyser.dumpNames();
        }
        consumeEndOfArg();
        byte[] localIndexes = this.varAnalyser.gatherLocalIndexes(locals);
        return new Stmt.DebugTrace(keyword, localIndexes);
    }

    private Stmt tryStatement() {
        consumeCurlyOpen("try statement");
        Stmt.Block tryBlock = new Stmt.Block(block("try statement"));
        Token brClose = previous();

        List<Pair<Pair<ClassReference[],Token>, Stmt.Block>> catches = new ArrayList<>(); //what an insane varType
        while (match(CATCH)) {
            List<ClassReference> targets = new ArrayList<>();
            consumeBracketOpen("catch");
            do {
                targets.add(consumeVarType(generics).getReference());
            } while (match(SINGLE_OR));
            pushScope();
            Token name = consumeIdentifier();
            consumeBracketClose("catch");
            createVar(name, VarTypeManager.THROWABLE, true, false);
            consumeCurlyOpen("catch statement");
            Stmt.Block block = new Stmt.Block(block("catch statement"));
            block = new Stmt.Block(new Stmt[] {
                    block,
                    popScope()
            });
            catches.add(Pair.of(
                    Pair.of(
                            targets.toArray(new ClassReference[0]),
                            name
                    ),
                    block
            ));
        }
        Stmt.Block finallyBlock = null;
        if (match(FINALLY)) {
            consumeCurlyOpen("finally statement");
            finallyBlock = new Stmt.Block(block("finally statement"));
        } else if (catches.isEmpty()) error(brClose, "expected 'catch' or 'finally'");
        return new Stmt.Try(tryBlock, catches.toArray(Pair[]::new), finallyBlock);
    }

    private Stmt thrStatement() {
        Token keyword = previous();
        expectType(VarTypeManager.THROWABLE);
        Expr val = expression();
        expectType(val, VarTypeManager.THROWABLE);
        consumeEndOfArg();
        popExpectation();
        seenReturn();
        return new Stmt.Throw(keyword, val);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(EOA)) {
            value = expression();
        }

        if (funcRetType == VarTypeManager.VOID.reference() && value != null) error(keyword, "incompatible types: unexpected return value.");
        else if (value != null) expectType(value, funcRetType);
        else if (funcRetType != VarTypeManager.VOID.reference()) error(keyword, "incompatible types: missing return value.");

        consumeEndOfArg();
        seenReturn();
        return new Stmt.Return(keyword, value);
    }

    private Stmt loopInterruptionStatement() {
        Token token = previous();
        if (loopIndex <= 0) error(token, "'" + token.lexeme() + "' can only be used inside loops");
        consumeEndOfArg();
        seenReturn();
        return new Stmt.LoopInterruption(token);
    }

    private Stmt forStatement() {
        Token keyword = previous();

        consumeBracketOpen("for");

        Optional<SourceClassReference> type = tryConsumeVarType(generics);

        Stmt initializer;
        if (type.isPresent()) {
            Token name = consumeIdentifier();
            ClassReference reference = type.get().getReference();
            if (match(COLON)) {
                ClassReference arrayType = reference.array();
                expectType(arrayType);
                Expr init = expression();
                popExpectation();
                expectType(init, arrayType);
                consumeBracketClose("for");
                //add 2 synthetic vars
                int baseVar = varAnalyser.add("?", reference.array(), false, true); //array variable
                varAnalyser.add("?", VarTypeManager.INTEGER.reference(), false, true); //iteration variable
                pushScope();
                loopIndex++;

                varAnalyser.add(name.lexeme(), reference, true, true); //named variable from sourcecode
                Stmt stmt = statement();
                loopIndex--;
                stmt = new Stmt.Block(
                        new Stmt[] {
                                stmt,
                                popScope()
                        }
                );
                return new Stmt.ForEach(reference, name, init, stmt, baseVar);
            }
            pushScope();
            loopIndex++;
            initializer = varDecl(false, reference, name);
        } else if (match(EOA)) {
            pushScope();
            loopIndex++;
            initializer = null;
        } else if (match(IDENTIFIER) && parser.hasClass(previous().lexeme())) {
            pushScope();
            loopIndex++;
            initializer = varDeclaration(false, parser.getClass(previous().lexeme()));
        } else {
            pushScope();
            loopIndex++;
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(EOA)) {
            condition = expression();
        }
        expectCondition(condition);
        consumeEndOfArg();

        Expr increment = null;
        if (!check(BRACKET_C)) {
            increment = expression();
        }
        consumeBracketClose("for clauses");

        Stmt body = statement();

        popScope();
        loopIndex--;

        return new Stmt.For(initializer, condition, increment, body, keyword);
    }

    private Stmt ifStatement() {
        Token statement = previous();
        consumeBracketOpen("if");
        Expr condition = expression();
        this.expectCondition(condition);
        consumeBracketClose("if condition");

        this.pushScope();
        Stmt thenBranch = statement();
        boolean branchSeenReturn = seenReturn.peek();
        thenBranch = new Stmt.Block(
                new Stmt[] {
                        thenBranch,
                        popScope()
                }
        );
        Stmt elseBranch = null;
        List<ElifBranch> elifs = new ArrayList<>();
        while (match(ELIF)) {
            consumeBracketOpen("elif");
            Expr elifCondition = expression();
            this.expectCondition(elifCondition);
            consumeBracketClose("elif condition");
            this.pushScope();
            Stmt elifStmt = statement();
            boolean seenReturn = this.seenReturn.peek();
            branchSeenReturn &= seenReturn;
            elifStmt = new Stmt.Block(new Stmt[] {
                    elifStmt,
                    popScope()
            });
            elifs.add(new ElifBranch(elifCondition, elifStmt, seenReturn));
        }

        if (match(ELSE)) {
            this.pushScope();
            elseBranch = statement();
            branchSeenReturn &= seenReturn.peek();
            elseBranch = new Stmt.Block(
                    new Stmt[] {
                            elseBranch,
                            popScope()
                    }
            );
        } else
            branchSeenReturn = false;

        if (branchSeenReturn)
            seenReturn(); //current scope has seen return only if all branches have seen return and there exists an else branch

        return new Stmt.If(condition, thenBranch, elseBranch, elifs.toArray(ElifBranch[]::new), statement);
    }

    private Stmt whileStatement() {
        Token keyword = previous();
        consumeBracketOpen("while");
        Expr condition = expression();
        this.expectCondition(condition);
        consumeBracketClose("while condition");
        this.loopIndex++;
        this.pushScope();
        Stmt body = statement();
        body = new Stmt.Block(new Stmt[]{
                body,
                popScope()
        });
        this.loopIndex--;

        return new Stmt.While(condition, body, keyword);
    }

    private Stmt[] block(String name) {
        List<Stmt> statements = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            statements.add(declaration());
        }

        consumeCurlyClose(name);
        return statements.toArray(new Stmt[0]);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consumeEndOfArg();
        return new Stmt.Expression(expr);
    }

    public Stmt[] parse() {
        if (tokens.length == 0) return new Stmt[] {new Stmt.Return(Token.createNative("return"), null)};
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) stmts.add(declaration());
        if (!seenReturn.peek()) stmts.add(new Stmt.Return(Token.createNative("return"), null));
        return stmts.toArray(Stmt[]::new);
    }

    public void applyMethod(List<? extends Pair<SourceClassReference, String>> params, ClassReference targetClass, ClassReference funcRetType, @Nullable Holder.Generics generics) {
        this.pushScope();
        this.funcRetType = funcRetType;
        if (generics != null) generics.pushToStack(this.generics);
        else this.generics.push(Map.of());
        if (targetClass != null) this.varAnalyser.add("this", targetClass, false, true);
        for (Pair<SourceClassReference, String> param : params) {
            varAnalyser.add(param.getSecond(), param.getFirst().getReference(), true, true);
        }
    }

    public void popMethod(Token methodEnd) {
        if (!funcRetType.is(VarTypeManager.VOID) && !seenReturn.peek())
            error(methodEnd, "missing return statement");
        this.popScope(); //ignore return value; methods that end get their locals removed either way
        this.generics.pop();
        funcRetType = VarTypeManager.VOID.reference();
    }

    public void applyStaticMethod(List<? extends Pair<SourceClassReference, String>> params, ClassReference funcRetType, @Nullable Holder.Generics generics) {
        this.pushScope();
        this.funcRetType = funcRetType;
        if (generics != null) generics.pushToStack(this.generics);
        else this.generics.push(Map.of());

        for (Pair<SourceClassReference, String> param : params) {
            varAnalyser.add(param.getSecond(), param.getFirst().getReference(), true, true);
        }
    }
}
