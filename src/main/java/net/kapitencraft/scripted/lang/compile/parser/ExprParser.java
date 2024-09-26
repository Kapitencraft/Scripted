package net.kapitencraft.scripted.lang.compile.parser;

import net.kapitencraft.scripted.lang.compile.Compiler;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;
import static net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory.*;

@SuppressWarnings("ThrowableNotThrown")
public class ExprParser extends AbstractParser {

    public ExprParser(Compiler.ErrorLogger errorLogger) {
        super(errorLogger);
    }

    public Expr expression() {
        if (match(SWITCH)) {
            return switchExpr();
        }

        return when();
    }

    private Expr when() {
        Expr expr = castCheck();
        if (match(WHEN_CONDITION)) {
            Expr ifTrue = expression();
            consume(WHEN_FALSE, "':' expected");
            Expr ifFalse = expression();
            expr = new Expr.When(expr, ifTrue, ifFalse);
        }

        return expr;
    }

    private Expr castCheck() {
        Expr expr = assignment();
        if (match(INSTANCEOF)) {
            LoxClass loxClass = consumeVarType();
            Token patternVar = null;
            if (match(IDENTIFIER)) {
                patternVar = previous();
            }
            return new Expr.CastCheck(expr, loxClass, patternVar);
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(ASSIGN) || match(OPERATION_ASSIGN)) {
            Token assign = previous();
            Expr value = assignment();

            if (expr instanceof Expr.VarRef variable) {
                Token name = variable.name;

                return new Expr.Assign(name, value, assign);
            } else if (expr instanceof Expr.Get get) {
                return new Expr.Set(get.object, get.name, value, assign);
            }


            error(assign, "Invalid assignment target.");
        }

        if (match(GROW, SHRINK)) {

            Token assign = previous();

            if (expr instanceof Expr.VarRef ref) {
                Token name = ref.name;

                return new Expr.SpecialAssign(name, assign);
            }

            if (expr instanceof Expr.Get get) {
                return new Expr.SpecialSet(get.object, get.name, assign);
            }

        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND, XOR)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQUALITY)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(COMPARATORS)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(SUB, ADD, POW)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(DIV, MUL, MOD)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(NOT, SUB)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr switchExpr() {
        Token keyword = previous();
        consumeBracketOpen("switch");

        Expr provider = expression();

        consumeBracketClose("switch");


        consumeCurlyOpen("switch body");
        Map<Object, Expr> params = new HashMap<>();
        Expr def = null;

        while (!check(C_BRACKET_C)) {
            if (match(CASE)) {
                Object key = literal();
                if (params.containsKey(key)) error(previous(), "Duplicate case key '" + previous().lexeme() + "'");
                consume(LAMBDA, "not a statement");
                Expr expr = expression();
                consumeEndOfArg();
                params.put(key, expr);
            } else if (match(DEFAULT)) {
                if (def != null) error(previous(), "Duplicate default key");
                consume(LAMBDA, "not a statement");
                def = expression();
                consumeEndOfArg();
            }
        }

        consumeCurlyClose("switch body");
        return new Expr.Switch(provider, params, def, keyword);
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = args();

        Token bracket = consumeBracketClose("arguments");

        return new Expr.Call(callee, bracket, arguments);
    }

    private Expr finishInstCall(Expr.Get get) {
        List<Expr> arguments = args();

        Token bracket = consumeBracketClose("arguments");

        return new Expr.InstCall(get.object, get.name, bracket, arguments);
    }

    private List<Expr> args() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (arguments.size() > 255) error(peek(), "Can't have more than 255 arguments");
                arguments.add(expression());
            } while (match(COMMA));
        }

        return arguments;
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(BRACKET_O)) {
                if (expr instanceof Expr.Get get) expr = finishInstCall(get);
                    else expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);

            } else {
                break;
            }
        }

        return expr;
    }

    private Object literal() {
        if (match(FALSE)) return false;
        if (match(TRUE)) return true;
        if (match(NULL)) return null;

        if (match(NUM, STR)) {
            return previous().literal();
        }

        throw error(peek(), "Expected literal");
    }

    private Expr primary() {
        if (match(NEW)) {
            LoxClass loxClass = consumeVarType();
            consumeBracketOpen("constructor");
            List<Expr> args = args();
            consumeBracketClose("constructor");

            return new Expr.Constructor(loxClass, args);
        }

        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous());
        }

        if (match(IDENTIFIER)) {
            Token previous = previous();
            if (peek().type() == BRACKET_O) {
                return new Expr.FuncRef(previous);
            } else {
                return new Expr.VarRef(previous);
            }
        }

        if (match(THIS)) return new Expr.VarRef(previous());

        if (match(BRACKET_O)) {
            Expr expr = expression();
            consumeBracketClose("expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expression expected.");
    }
}
