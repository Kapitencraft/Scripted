package net.kapitencraft.scripted.lang.compile.parser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compile.Compiler;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

import java.util.ArrayList;
import java.util.List;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

public class StmtParser extends ExprParser {

    public StmtParser(Compiler.ErrorLogger errorLogger) {
        super(errorLogger);
    }


    private Stmt declaration() {

        try {
            if (match(FINAL)) return varDeclaration(true, consumeVarType());
            if (check(IDENTIFIER)) {
                Token id = peek();
                LoxClass loxClass = parser.getClass(id.lexeme());
                if (loxClass != null) return varDeclaration(false, loxClass);
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt.VarDecl varDecl(boolean isFinal, LoxClass type, Token name) {

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        consumeEndOfArg();
        return new Stmt.VarDecl(name, type, initializer, isFinal);
    }

    private Stmt varDeclaration(boolean isFinal, LoxClass type) {
        Token name = consume(IDENTIFIER, "Expected variable name.");

        return varDecl(isFinal, type, name);
    }

    private Stmt statement() {
        try {
            if (match(RETURN)) return returnStatement();
            if (match(CONTINUE, BREAK)) return loopInterruptionStatement();
            if (match(FOR)) return forStatement();
            if (match(IF)) return ifStatement();
            if (match(WHILE)) return whileStatement();
            if (match(C_BRACKET_O)) return new Stmt.Block(block());
            if (check(IDENTIFIER)) {
                LoxClass loxClass = parser.getClass(peek().lexeme());
                if (loxClass != null) {
                    advance();
                    return varDeclaration(false, loxClass);
                }
            }

            return expressionStatement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(EOA)) {
            value = expression();
        }

        consumeEndOfArg();
        return new Stmt.Return(keyword, value);
    }

    private Stmt loopInterruptionStatement() {
        Token token = previous();
        consumeEndOfArg();
        return new Stmt.LoopInterruption(token);
    }

    private Stmt forStatement() {
        Token keyword = previous();

        consumeBracketOpen("for");

        Stmt initializer;
        if (match(EOA)) {
            initializer = null;
        } else if (match(IDENTIFIER) && parser.hasClass(previous().lexeme())) {
            initializer = varDeclaration(false, parser.getClass(previous().lexeme()));
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(EOA)) {
            condition = expression();
        }
        consumeEndOfArg();

        Expr increment = null;
        if (!check(BRACKET_C)) {
            increment = expression();
        }
        consumeBracketClose("for clauses");

        Stmt body = statement();

        return new Stmt.For(initializer, condition, increment, body, keyword);
    }

    private Stmt ifStatement() {
        Token statement = previous();
        consumeBracketOpen("if");
        Expr condition = expression();
        consumeBracketClose("if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        List<Pair<Expr, Stmt>> elifs = new ArrayList<>();
        while (match(ELIF)) {
            consumeBracketOpen("elif");
            Expr elifCondition = expression();
            consumeBracketClose("elif condition");
            Stmt elifStmt = statement();
            elifs.add(Pair.of(elifCondition, elifStmt));
        }

        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch, elifs, statement);
    }

    private Stmt whileStatement() {
        Token keyword = previous();
        consumeBracketOpen("while");
        Expr condition = expression();
        consumeBracketClose("while condition");
        Stmt body = statement();

        return new Stmt.While(condition, body, keyword);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            statements.add(declaration());
        }

        consumeCurlyClose("block");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consumeEndOfArg();
        return new Stmt.Expression(expr);
    }

    public List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) stmts.add(statement());
        return stmts;
    }
}
