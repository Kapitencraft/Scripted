package net.kapitencraft.scripted.lang.compile.visitor;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.VarTypeManager;
import net.kapitencraft.scripted.lang.compile.Compiler;
import net.kapitencraft.scripted.lang.compile.analyser.EnvAnalyser;
import net.kapitencraft.scripted.lang.func.LoxCallable;
import net.kapitencraft.scripted.lang.func.LoxFunction;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.clazz.GeneratedLoxClass;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.run.Main;

import java.util.List;
import java.util.Map;

import static net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory.*;

public class Resolver implements Expr.Visitor<LoxClass>, Stmt.Visitor<Void> {

    private final EnvAnalyser analyser = new EnvAnalyser();
    private final LocationFinder finder = new LocationFinder();
    private final ReturnScanner returnScanner;
    private FunctionType currentFunction = FunctionType.NONE;
    private LoxClass funcRetType;
    private final Compiler.ErrorLogger errorLogger;

    public Resolver(Compiler.ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
        this.returnScanner = new ReturnScanner(errorLogger);

        Main.natives.forEach(analyser::addMethod);
    }

    private void error(Token token, String message) {
        errorLogger.error(token, message);
    }

    private void checkVarExistence(Token name, boolean requireValue, boolean mayBeFinal) {
        String varName = name.lexeme();
        if (!analyser.hasVar(varName)) {
            error(name, "cannot find symbol");
        } else if (requireValue && !analyser.hasVarValue(varName)) {
            error(name, "Variable '" + name.lexeme() + "' might not have been initialized");
        } else if (!mayBeFinal && analyser.isFinal(varName)) {
            error(name, "Can not assign to final variable");
        }
    }

    private void checkVarType(Token name, Expr value) {
        if (!analyser.hasVar(name.lexeme())) return;
        resolve(name, value, analyser.getVarType(name.lexeme()));
    }

    private void createVar(Token name, LoxClass type, boolean hasValue, boolean isFinal) {
        if (analyser.hasVar(name.lexeme())) {
            error(name, "Variable '" + name.lexeme() + "' already defined");
        }
        analyser.addVar(name.lexeme(), type, hasValue, isFinal);
    }

    public void resolve(GeneratedLoxClass generated) {
        generated.enclosing().forEach(this::resolve);

        if (!generated.isAbstract()) {
            Map<String, LoxCallable> methods = generated.getMethods();

            for (Map.Entry<String, LoxCallable> entry : methods.entrySet()) {
                if (entry.getValue().isAbstract()) {
                    error(generated.getNameToken(), String.format("class %s is not declared abstract and does not override abstract method %s", generated.name(), entry.getKey()));
                }
            }
        }

        this.analyser.push();
        this.analyser.addVar("this", generated, true, true);
        generated.methods().forEach(this::resolve);
        generated.fields().forEach(this::resolve);
        this.analyser.pop();

        generated.staticMethods().forEach(this::resolve);
        generated.staticFields().forEach(this::resolve);
    }

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private LoxClass resolve(Expr expr) {
        return expr.accept(this);
    }

    private LoxClass resolveCondition(Expr condition) {
        return resolve(condition, VarTypeManager.BOOLEAN);
    }

    private LoxClass resolve(Token errorLoc, Expr expr, LoxClass expected) {
        LoxClass got = resolve(expr);
        if (expected == VarTypeManager.OBJECT) return got;
        if (expected == VarTypeManager.NUMBER && (got == VarTypeManager.INTEGER || got == VarTypeManager.FLOAT || got == VarTypeManager.DOUBLE)) return got;
        if (!expected.isParentOf(got)) error(errorLoc, "incompatible types: " + got.name() + " cannot be converted to " + expected.name());
        return expected;
    }

    private LoxClass resolve(Expr expr, LoxClass expected) {
        return resolve(finder.find(expr), expr, expected);
    }

    private void resolveFunction(Stmt.FuncDecl function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        Token name = function.name;
        if (analyser.addMethod(name.lexeme(), new LoxFunction(function))) {
            error(name, "Method '" + name.lexeme() + "' already defined");
        }
        funcRetType = function.retType;

        analyser.push();
        for (Pair<LoxClass, Token> pair : function.params) {
            createVar(pair.getSecond(), pair.getFirst(), true, false);
        }

        if (function.body == null) {
            error(function.end, "empty method");
            analyser.pop();
            currentFunction = enclosingFunction;
            funcRetType = VarTypeManager.VOID;
            return;
        }
        if (funcRetType != VarTypeManager.VOID && !returnScanner.scanList(function.body)) {
            error(function.end, "missing return statement");
        }
        function.body.forEach(this::resolve);
        analyser.pop();
        currentFunction = enclosingFunction;
        funcRetType = VarTypeManager.VOID;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        analyser.push();
        stmt.statements.forEach(this::resolve);
        analyser.pop();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFuncDeclStmt(Stmt.FuncDecl stmt) {
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolveCondition(stmt.condition);
        resolve(stmt.thenBranch);
        stmt.elifs.forEach((pair) -> {
            resolveCondition(pair.getFirst());
            resolve(pair.getSecond());
        });
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            error(stmt.keyword, "Can't return from top-level code.");
        }


        if (stmt.value != null) {
            resolve(stmt.value, funcRetType);
        } else if (funcRetType != null) {
            error(stmt.keyword, "incompatible types: unexpected return value.");
        }

        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        if (!analyser.inLoop()) error(stmt.type, "'" + stmt.type.lexeme() + "' can only be used inside loops");
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        Token name = stmt.name;
        createVar(name, stmt.type, stmt.initializer != null, stmt.isFinal);

        if (stmt.initializer != null) {
            checkVarType(name, stmt.initializer);
            resolve(stmt.initializer);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolveCondition(stmt.condition);
        analyser.push();
        analyser.pushLoop();
        resolve(stmt.body);
        analyser.popLoop();
        analyser.pop();
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        analyser.push();
        analyser.pushLoop();

        if (stmt.init != null) resolve(stmt.init);
        if (stmt.condition != null) resolve(stmt.condition);
        if (stmt.increment != null) resolve(stmt.increment);
        resolve(stmt.body);

        analyser.popLoop();
        analyser.pop();
        return null;
    }

    @Override
    public LoxClass visitAssignExpr(Expr.Assign expr) {
        checkVarExistence(expr.name, expr.type.type() != TokenType.ASSIGN,
                false);
        if (!analyser.hasVar(expr.name.lexeme())) return null;
        checkVarType(expr.name, expr.value);
        if (expr.type.type() == TokenType.ASSIGN) analyser.setHasVarValue(expr.name.lexeme());
        return analyser.getVarType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        checkVarExistence(expr.name, true, false);
        return analyser.getVarType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitBinaryExpr(Expr.Binary expr) {
        LoxClass left = resolve(expr.left);
        LoxClass right = resolve(expr.right);
        TokenType type = expr.operator.type();
        if (type == TokenType.ADD && (left == VarTypeManager.STRING || right == VarTypeManager.STRING)) return VarTypeManager.STRING; //check if at least one of the values is string
        if (type.isCategory(BOOL_BINARY) && !(left == VarTypeManager.BOOLEAN && right == VarTypeManager.BOOLEAN))
            error(expr.operator, "both values must be boolean");
        if (type.isCategory(ARITHMETIC_BINARY) && !(left.superclass() == VarTypeManager.NUMBER && right.superclass() == VarTypeManager.NUMBER))
            error(expr.operator, "both values must be numbers");
        if (left != right)
            error(expr.operator, "can not combine values of different types");
        if (type.isCategory(COMPARATORS) || type.isCategory(EQUALITY)) return VarTypeManager.BOOLEAN;
        return left;
    }

    @Override
    public LoxClass visitWhenExpr(Expr.When expr) {
        resolveCondition(expr.condition);
        LoxClass left = resolve(expr.ifTrue);
        LoxClass right = resolve(expr.ifFalse);
        if (left != right) errorLogger.error(expr.ifFalse, "When does not return the same type on both sides");
        return left;
    }

    //TODO remove as soon as all functions move to classes

    @Override
    public LoxClass visitCallExpr(Expr.Call expr) {

        resolve(expr.callee);

        try {
            Expr.FuncRef ref = (Expr.FuncRef) expr.callee;
            LoxCallable method = analyser.getMethod(ref.name.lexeme());
            List<? extends LoxClass> params = method.argTypes();
            for (int i = 0; i < method.arity(); i++) {
                Expr arg = expr.args.get(i);
                resolve(arg, params.get(i));
            }
            return method.type();
        } catch (Exception e) {
            errorLogger.error(expr.callee, "Unable to cast method: " + e.getMessage());
        }

        return null;
    }

    @Override
    public LoxClass visitInstCallExpr(Expr.InstCall expr) {
        LoxClass target = resolve(expr.callee);
        if (!target.hasMethod(expr.name.lexeme())) {
            error(expr.name, "unknown symbol");
            return VarTypeManager.VOID;
        }

        return target.getMethodType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitGetExpr(Expr.Get expr) {
        return resolve(expr.object).getFieldType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitSetExpr(Expr.Set expr) {
        LoxClass objClass = resolve(expr.object);
        if (!objClass.hasField(expr.name.lexeme())) {
            error(expr.name, "unknown symbol");
            return VarTypeManager.VOID;
        }

        LoxClass expectedType = objClass.getFieldType(expr.name.lexeme());
        return resolve(expr.value, expectedType);
    }

    @Override
    public LoxClass visitSpecialSetExpr(Expr.SpecialSet expr) {
        return resolve(expr.callee).getFieldType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitGroupingExpr(Expr.Grouping expr) {
        return resolve(expr.expression);
    }

    @Override
    public LoxClass visitLiteralExpr(Expr.Literal expr) {
        return expr.value.literal().type();
    }

    @Override
    public LoxClass visitLogicalExpr(Expr.Logical expr) {
        resolveCondition(expr.left);
        resolveCondition(expr.right);
        return VarTypeManager.BOOLEAN;
    }

    @Override
    public LoxClass visitUnaryExpr(Expr.Unary expr) {
        return expr.operator.type() == TokenType.NOT ? resolveCondition(expr.right) : resolve(expr.right, VarTypeManager.NUMBER);
    }

    @Override
    public LoxClass visitVarRefExpr(Expr.VarRef expr) {
        checkVarExistence(expr.name, true, true);

        return analyser.getVarType(expr.name.lexeme());
    }

    @Override
    public LoxClass visitFuncRefExpr(Expr.FuncRef expr) {
        Token name = expr.name;
        if (!analyser.hasMethod(name.lexeme())) error(name, "Method '" + name.lexeme() + "' not defined");
        return analyser.getMethodType(name.lexeme());
    }

    @Override
    public LoxClass visitConstructorExpr(Expr.Constructor expr) {
        expr.params.forEach(this::resolve);
        return expr.target;
    }

    @Override
    public LoxClass visitSwitchExpr(Expr.Switch stmt) {
        resolve(stmt.provider);
        List<? extends LoxClass> returns = stmt.params.values().stream().map(this::resolve).toList();
        LoxClass ret = VarTypeManager.VOID;
        for (LoxClass cl : returns) {
            if (cl != VarTypeManager.VOID && ret == VarTypeManager.VOID) {
                ret = cl;
            } else if (cl != ret) {
                error(stmt.keyword, "miss matching return types in switch statement");
                return ret;
            }
        }
        if (stmt.defaulted != null) resolve(stmt.defaulted);
        return ret;
    }

    @Override
    public LoxClass visitCastCheckExpr(Expr.CastCheck expr) {
        LoxClass providedType = resolve(expr.object);
        if (!providedType.isParentOf(expr.targetType)) error(finder.find(expr.object), expr.targetType.name() + " is no subtype of " + providedType.name());
        if (expr.patternVarName != null) {
            //TODO fix it being active inside the entire scope
            analyser.addVar(expr.patternVarName.lexeme(), expr.targetType, true, true);
        }
        return VarTypeManager.BOOLEAN;
    }
}