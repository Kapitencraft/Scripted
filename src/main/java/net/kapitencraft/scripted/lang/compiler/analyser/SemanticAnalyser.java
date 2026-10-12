package net.kapitencraft.scripted.lang.compiler.analyser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.algebra.OperationType;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.ast.SwitchKey;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.oop.attribute.EnumConstantHolder;
import net.kapitencraft.scripted.lang.holder.oop.generic.AppliedGenerics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.tool.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class SemanticAnalyser implements Stmt.Visitor<Void>, Expr.Visitor<ClassReference> {

    //region expect
    private final ArrayDeque<Set<ClassReference>> activeArgs = new ArrayDeque<>();
    private ClassReference methodReturnType = VarTypeManager.VOID.reference();
    private ArrayDeque<ClassReference[]> methodThrown = new ArrayDeque<>();

    private boolean isExpected(ClassReference reference) {
        return activeArgs.isEmpty() || activeArgs.peek().contains(reference);
    }

    private void pushExpected(Set<ClassReference> references) {
        activeArgs.push(references);
    }

    private void pushExpected(ClassReference... references) {
        pushExpected(Set.of(references));
    }

    private void popExpected() {
        activeArgs.pop();
    }

    protected ClassReference expectType(Token errorLoc, ClassReference gotten, ClassReference expected) {
        if (expected == VarTypeManager.OBJECT) return gotten;
        if (!gotten.isChildOf(expected))
            errorF(errorLoc, "incompatible types: %s cannot be converted to %s", gotten.name(), expected.name());
        if (gotten instanceof AppliedGenericsReference reference) {
            if (expected instanceof AppliedGenericsReference reference1) {
                AppliedGenerics gottenAppliedGenerics = reference.getApplied();
                AppliedGenerics expectedAppliedGenerics = reference1.getApplied();
                ClassReference[] expectedGenerics = expectedAppliedGenerics.references();
                ClassReference[] gottenGenerics = gottenAppliedGenerics.references();

                if (expectedGenerics.length != gottenGenerics.length) {
                    errorF(gottenAppliedGenerics.reference(), "Wrong number of type arguments: %s; required: %s", gottenGenerics.length, expectedGenerics.length);
                } else {
                    for (int i = 0; i < expectedGenerics.length; i++) {
                        if (!expectedGenerics[i].isChildOf(gottenGenerics[i])) {
                            String name = reference1.getGenerics().variables()[i].name().lexeme();
                            errorF(reference.getApplied().reference(), "incompatible types: inference variable %s has incompatible bounds", name);

                            errorStorage.logError("gotten: " + gottenGenerics[i].name());
                            errorStorage.logError("lower bounds: " + expectedGenerics[i].name());
                        }
                    }
                }
            } else {
                errorF(reference.getApplied().reference(), "Type '%s' does not have type parameters", expected.absoluteName());
            }
        }
        return gotten;
    }

    private void checkThrown(ClassReference thrown, Token loc) {
        if (thrown.isChildOf(VarTypeManager.RUNTIME_EXCEPTION))
            return; //ignore subclasses of the RuntimeException
        for (ClassReference[] references : methodThrown) {
            for (ClassReference reference : references) {
                if (reference.isParentOf(thrown)) {
                    return;
                }
            }
        }
        errorF(loc, "unhandled exception: %s" + thrown.absoluteName());
    }

    //endregion

    //region executor
    private OperationInfo getOperationInfo(Token operator, ClassReference left, ClassReference right) {
        OperationType operation = OperationType.of(operator.type());
        assert operation != null;
        ScriptedClass result = VarTypeManager.VOID;
        if (left.get() instanceof PrimitiveClass || left.is(VarTypeManager.STRING.get()) || right.is(VarTypeManager.STRING.get())) {
            result = left.get().checkOperation(operation, right);
        }
        //search for overloads
        if (result == VarTypeManager.VOID && operation.getMethodName() != null) {
            String signature = operation.getMethodName() + "(" + VarTypeManager.getClassName(right) + ")";
            ScriptedCallable method = left.get().getMethod(signature);
            if (method != null && !method.isStatic()) {
                signature = VarTypeManager.getClassName(left) + signature;
                return new OperationInfo(left, method.retType(), signature);
            }
        }
        if (result == VarTypeManager.VOID) {
            errorF(operator, "operator '%s' not possible for argument types %s and %s", operator.lexeme(), left.absoluteName(), right.absoluteName());
            return OperationInfo.UNKNOWN;
        }
        return new OperationInfo(left, result.reference(), null);
    }

    private OperationInfo getOperationInfo(Expr leftArg, Token operator, Expr rightArg) {
        return getOperationInfo(operator, analyseExpr(leftArg), analyseExpr(rightArg));
    }

    private OperationInfo getOperationInfo(ClassReference left, Token operator, Expr rightArg) {
        return getOperationInfo(operator, left, analyseExpr(rightArg));
    }

    private OperationInfo getOperationInfo(Token operator, ClassReference reference) {
        if (reference.get() instanceof PrimitiveClass) {
            return new OperationInfo(reference, reference, null);
        }
        String methodSig = OperationType.of(operator.type()).getMethodName() + "()";

        ScriptedCallable method = reference.get().getMethod(methodSig);

        if (method != null && !method.isStatic()) {
            return new OperationInfo(reference, method.retType(), VarTypeManager.getClassName(reference) + methodSig);
        } else {
            errorF(operator, "operation '%s' not applicable for argument type %s", OperationType.of(operator.type()).getMethodName(), reference.absoluteName());
        }

        return OperationInfo.UNKNOWN;
    }

    public void analyseBody(List<Stmt> body, ClassReference retType, ClassReference[] thrown, List<Pair<ClassReference, String>> params, @Nullable ClassReference selfClass) {
        this.methodReturnType = retType;
        this.methodThrown.clear();
        this.methodThrown.push(thrown);
        this.varAnalyser.clear();
        if (selfClass != null) this.varAnalyser.add("this", selfClass, false, true);
        for (Pair<ClassReference, String> param : params) {
            varAnalyser.add(param.getSecond(), param.getFirst(), true, true);
        }
        for (Stmt stmt : body) {
            if (stmt != null)
                this.analyseStmt(stmt);
        }
    }

    private MethodData analyseCall(Token name, ClassReference objType, Expr[] args) {
        ClassReference[] argTypes = args(args);
        ScriptedClass targetClass = objType.get();

        Pair<ScriptedClass, ScriptedCallable> callable;
        if (!targetClass.hasMethod(name.lexeme())) {
            errorF(name, "unknown method '%s' in class %s", name.lexeme(), objType.absoluteName());
            callable = null;
        } else
            callable = Util.getVirtualMethod(targetClass, name.lexeme(), argTypes);
        ClassReference retType = VarTypeManager.VOID.reference();
        String signature = null;
        if (callable != null) {
            retType = checkArguments(args, argTypes, callable.getSecond(), objType, name);
            signature = VarTypeManager.getMethodSignature(callable.getFirst(), name.lexeme(), callable.getSecond().argTypes());
            for (ClassReference reference : callable.getSecond().thrown()) {
                checkThrown(reference, name);
            }
        }
        return new MethodData(signature, retType, targetClass.reference(), callable == null || callable.getSecond().isStatic());
    }

    private record MethodData(String signature, ClassReference retType, ClassReference declaring, boolean isStatic) {
    }

    private ScriptedCallable tryGetConstructorMethod(ClassReference[] argTypes, ScriptedClass scriptedClass, Token loc) {
        DataMethodContainer container = scriptedClass.getMethods().get("<init>");
        if (container == null) {
            if (argTypes.length > 0) {
                errorF(loc, "constructor for %s cannot be applied to given types;", loc.lexeme());

                errorStorage.logError("required: ");
                errorStorage.logError("found:    " + Util.getDescriptor(argTypes));
                errorStorage.logError("reason: actual and formal argument lists differ in length");
            }
            return null;
        }

        return Util.getVirtualMethod(scriptedClass, "<init>", argTypes).getSecond();
    }

    private record OperationInfo(ClassReference executor, ClassReference result,
                                 @Nullable String methodSignature) {
        private static final OperationInfo UNKNOWN = new OperationInfo(VarTypeManager.VOID.reference(), VarTypeManager.VOID.reference(), null);
    }

    //endregion

    //region var & scope analysis
    protected byte tryCreateVar(Token name, ClassReference type, boolean hasValue, boolean isFinal) {
        byte ordinal = varAnalyser.add(name.lexeme(), type, !isFinal, hasValue);
        if (ordinal == -1)
            errorF(name, "Variable '%s' already defined in current scope", name.lexeme());
        return ordinal;
    }

    @SuppressWarnings("SameParameterValue")
    protected LocalVariableContainer.FetchResult checkVarExistence(Token name, boolean requireValue, boolean mayBeFinal) {
        String varName = name.lexeme();
        LocalVariableContainer.FetchResult result = varAnalyser.get(varName);
        if (result != LocalVariableContainer.FetchResult.FAIL) {
            if (requireValue && !result.assigned()) {
                error(name, "Variable '" + name.lexeme() + "' might not have been initialized");
            } else if (!mayBeFinal && !result.canAssign()) {
                error(name, "Can not assign to final variable");
            }
        }
        return result;
    }

    private void pushScope() {
        varAnalyser.push();
    }

    private final LocalVariableContainer varAnalyser = new LocalVariableContainer();
    //endregion

    //region error
    private boolean panicMode = false;

    private void errorIncompatibleTypes(Expr expr, ClassReference expected, ClassReference gotten) {
        errorIncompatibleTypes(Compiler.LOCATION_ANALYSER.find(expr), expected, gotten);
    }

    private void errorIncompatibleTypes(Token loc, ClassReference expected, ClassReference gotten) {
        errorF(loc, "incompatible types: %s cannot be converted to %s", gotten.absoluteName(), expected.absoluteName());
    }

    private void errorIncompatibleTypes(Token loc, ClassReference got) {
        errorF(loc, "incompatible types.");
        errorStorage.logError("expected: " + activeArgs.peek().stream().map(ClassReference::absoluteName).collect(Collectors.joining(", ")));
        errorStorage.logError("got: " + got.absoluteName());
    }

    private void error(Token loc, String msg) {
        if (!panicMode)
            errorStorage.error(loc, msg);
        panicMode = true;
    }

    public void errorF(Token loc, String formatMsg, Object... args) {
        if (!panicMode)
            errorStorage.errorF(loc, formatMsg, args);
        panicMode = true;
    }

    private final ErrorStorage errorStorage;
    //endregion

    public SemanticAnalyser(ErrorStorage errorStorage) {
        this.errorStorage = errorStorage;
    }

    public ClassReference analyseExpr(Expr expr) {
        return expr.accept(this);
    }

    public ClassReference[] args(Expr[] args) {
        return Arrays.stream(args).map(this::analyseExpr).toArray(ClassReference[]::new);
    }

    public ClassReference checkArguments(Expr[] args, ClassReference[] argTypes, @Nullable ScriptedCallable target, @Nullable ClassReference obj, Token loc) {
        ClassReference[] expectedTypes = target == null ? new ClassReference[0] : target.argTypes();
        if (expectedTypes.length != argTypes.length) {
            errorF(loc, "method for %s cannot be applied to given types;", loc.lexeme());

            errorStorage.logError("required: " + Util.getDescriptor(expectedTypes));
            errorStorage.logError("found:    " + Util.getDescriptor(argTypes));
            errorStorage.logError("reason: actual and formal argument lists differ in length");
        } else {
            for (int i = 0; i < argTypes.length; i++) {
                expectType(Compiler.LOCATION_ANALYSER.find(args[i]), argTypes[i], expectedTypes[i]);
            }
        }

        ClassReference type = target == null ? VarTypeManager.VOID.reference() : target.retType();
        //TODO figure out how to extract gotten generics
        if (type instanceof GenericClassReference genericClassReference) {
            GenericStack genericStack = new GenericStack();
            if (obj instanceof AppliedGenericsReference reference) {
                reference.push(genericStack, errorStorage);
            }

            Map<String, ClassReference> types = new HashMap<>();
            for (int i = 0; i < expectedTypes.length; i++) {
                if (expectedTypes[i] instanceof GenericClassReference gCR) {
                    types.put(gCR.getTypeName(), argTypes[i]);
                }
            }
            if (!types.isEmpty()) genericStack.push(types);


            return genericClassReference.unwrap(genericStack);
        }

        return type;
    }

    private void analyseStmt(Stmt stmt) {
        panicMode = false;
        stmt.accept(this);
    }

    @Override
    public ClassReference visitSingleIdentifierExpr(Expr.SingleIdentifier expr) {
        String varName = expr.name.lexeme();
        if ("this".equals(varName) || "super".equals(varName)) {
            LocalVariableContainer.FetchResult fetchResult = varAnalyser.get("this");
            if (fetchResult == LocalVariableContainer.FetchResult.FAIL)
                errorF(expr.name, "'%s' can not be accessed from a static context", varName);
            expr.ordinal = 0;
            expr.type = null;
            return expr.retType = fetchResult.type();
        }

        LocalVariableContainer.FetchResult fetchResult = varAnalyser.get(varName);

        if (fetchResult == LocalVariableContainer.FetchResult.FAIL) {
            if (expr.type != null) {
                ScriptedClass scriptedClass = expr.type.get();
                ScriptedClass declaring = scriptedClass.getFieldDeclaring(expr.name.lexeme());
                if (declaring != null) {
                    ClassReference fieldType = declaring.getFieldType(expr.name.lexeme());
                    return expr.retType = fieldType;
                }
            }

            errorF(expr.name, "unknown symbol: '%s'", expr.name.lexeme());
        } else {
            if (!fetchResult.assigned()) {
                errorF(expr.name, "variable '%s' might not have been initialized", expr.name.lexeme());
            }
        }
        expr.ordinal = fetchResult.ordinal();
        expr.type = null;

        return expr.retType = fetchResult.type();
    }

    @Override
    public ClassReference visitSetExpr(Expr.Set expr) {
        ClassReference objType = analyseExpr(expr.object);

        String fieldName = expr.name.lexeme();
        ClassReference fieldType = VarTypeManager.VOID.reference();
        ScriptedClass declaring = objType.get().getFieldDeclaring(fieldName);
        if (declaring != null) {
            fieldType = declaring.getFieldType(expr.name.lexeme());
        } else {
            errorF(expr.name, "unknown field in class %s: %s", objType.absoluteName(), fieldName);
        }

        ClassReference valType = analyseExpr(expr.value);

        if (!fieldType.is(valType)) {
            errorIncompatibleTypes(expr.value, fieldType, valType);
        }

        OperationInfo operationInfo;
        if (expr.assignType.type() == TokenType.ASSIGN) {
            operationInfo = OperationInfo.UNKNOWN;
        } else
            operationInfo = getOperationInfo(fieldType, expr.assignType, expr.value);
        expr.executor = operationInfo.executor;

        expr.retType = fieldType;

        return fieldType;
    }

    @Override
    public ClassReference visitArraySpecialExpr(Expr.ArraySpecial expr) {
        ClassReference reference = analyseExpr(expr.object);

        if (!reference.get().isArray())
            errorF(Compiler.LOCATION_ANALYSER.find(expr), "Array type expected; found '%s'", reference.absoluteName());

        ScriptedClass component = reference.get().getComponentType();

        OperationInfo info = getOperationInfo(expr.assignType, component.reference());

        expr.executor = info.executor;

        return expr.retType = component.reference();
    }

    @Override
    public ClassReference visitCallExpr(Expr.Call expr) {
        //declaring
        ClassReference objType = expr.declaring != null ? expr.declaring : analyseExpr(expr.object);
        MethodData methodData = analyseCall(expr.name, objType, expr.args);
        if (expr.virtual == null) {
            expr.virtual = !methodData.isStatic;
            if (expr.virtual) {
                if (expr.object == null)
                    error(expr.name, "instance method can not be accessed from static context");
            } else {
                expr.object = null; //remove object so it isn't accidentally saved
            }
        }

        if (!methodData.isStatic && expr.declaring != null && expr.object != null)
            analyseExpr(expr.object);
        if (methodData.isStatic) {
            expr.declaring = methodData.declaring;
        }
        expr.signature = methodData.signature;
        return expr.retType = methodData.retType;
    }

    @Override
    public ClassReference visitLogicalExpr(Expr.Logical expr) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(expr.left);
        analyseExpr(expr.right);
        popExpected();

        return expr.retType = VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitComparisonChainExpr(Expr.ComparisonChain expr) {
        expr.dataType = analyseExpr(expr.entries[0]);
        if (!expr.dataType.get().isChildOf(VarTypeManager.NUMBER)) {
            error(Compiler.LOCATION_ANALYSER.find(expr.entries[0]), "number expected");
        }

        for (int i = 1; i < expr.entries.length; i++) {
            ClassReference reference = analyseExpr(expr.entries[i]);
            if (!expr.dataType.is(reference)) {
                errorIncompatibleTypes(expr.entries[i], expr.dataType, reference);
            }
        }

        return expr.retType = VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitCastCheckExpr(Expr.CastCheck expr) {
        ClassReference reference = analyseExpr(expr.object);
        if (!expr.targetType.isChildOf(reference)) {
            errorF(Compiler.LOCATION_ANALYSER.find(expr.object), "inconvertible types; %s cannot be cast to %s", reference.absoluteName(), expr.targetType.absoluteName());
        }

        if (expr.patternVarName != null) {
            tryCreateVar(expr.patternVarName, expr.targetType, true, true);
        }

        return expr.retType = VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitArrayGetExpr(Expr.ArrayGet expr) {
        ClassReference reference = analyseExpr(expr.object);

        if (!reference.get().isArray())
            errorF(Compiler.LOCATION_ANALYSER.find(expr), "Array type expected; found '%s'", reference.absoluteName());

        ScriptedClass component = reference.get().getComponentType();

        ClassReference indexType = analyseExpr(expr.index);
        if (!indexType.is(VarTypeManager.INTEGER)) {
            errorF(Compiler.LOCATION_ANALYSER.find(expr.index), "integer expected; found '%s'", indexType.absoluteName());
        }

        expr.componentType = component.reference();
        return expr.retType = component.reference();
    }

    @Override
    public ClassReference visitLiteralExpr(Expr.Literal expr) {
        ScriptedClass type = expr.literal.literal().type();
        return expr.retType = type.reference();
    }

    @Override
    public ClassReference visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        pushExpected(VarTypeManager.INTEGER.reference());
        if (expr.size != null)
            analyseExpr(expr.size);
        popExpected();

        if (expr.obj != null) {
            for (Expr val : expr.obj) {
                ClassReference type = analyseExpr(val);
                if (!expr.compoundType.is(type)) {
                    errorIncompatibleTypes(val, expr.compoundType, type);
                }
            }
        }

        return expr.retType = expr.compoundType.array();
    }

    @Override
    public ClassReference visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        String fieldName = expr.name.lexeme();

        ClassReference fieldType = VarTypeManager.VOID.reference();
        ScriptedClass declaring = expr.target.get().getFieldDeclaring(fieldName);
        if (declaring != null) {
            fieldType = declaring.getFieldType(fieldName);

        } else {
            errorF(expr.name, "unknown static field in class %s: %s", expr.target.absoluteName(), fieldName);
        }

        OperationInfo operationInfo = getOperationInfo(expr.assignType, fieldType);

        expr.executor = operationInfo.executor;

        return expr.retType = operationInfo.result;
    }

    @Override
    public ClassReference visitSpecialSetExpr(Expr.SpecialSet expr) {
        ClassReference objType = analyseExpr(expr.object);

        String fieldName = expr.name.lexeme();
        ClassReference fieldType = VarTypeManager.VOID.reference();
        ScriptedClass declaring = objType.get().getFieldDeclaring(fieldName);
        if (declaring != null) {
            fieldType = declaring.getFieldType(expr.name.lexeme());
        } else {
            errorF(expr.name, "unknown field in class %s: %s", objType.absoluteName(), fieldName);
        }

        OperationInfo info = getOperationInfo(expr.assignType, fieldType);

        expr.retType = fieldType;

        return fieldType;
    }

    @Override
    public ClassReference visitArraySetExpr(Expr.ArraySet expr) {
        ClassReference reference = analyseExpr(expr.object);

        if (!reference.get().isArray())
            errorF(Compiler.LOCATION_ANALYSER.find(expr), "Array type expected; found '%s'", reference.absoluteName());

        ScriptedClass component = reference.get().getComponentType();

        ClassReference indexType = analyseExpr(expr.index);
        if (!indexType.is(VarTypeManager.INTEGER)) {
            errorF(Compiler.LOCATION_ANALYSER.find(expr.index), "integer expected; found '%s'", indexType.absoluteName());
        }

        ClassReference valType = analyseExpr(expr.value);
        if (expr.assignType.type() != TokenType.ASSIGN) {
            OperationInfo info = getOperationInfo(expr.assignType, component.reference(), valType);

            expr.executor = info.executor;
        }

        expr.componentType = component.reference();

        return expr.retType = component.reference();
    }

    @Override
    public ClassReference visitIdentifierSpecialAssignExpr(Expr.IdentifierSpecialAssign expr) {
        LocalVariableContainer.FetchResult result = checkVarExistence(expr.name, true, false);
        if (result != LocalVariableContainer.FetchResult.FAIL) {
            OperationInfo info = getOperationInfo(expr.assignType, result.type());
            expr.executor = info.executor;
            expr.ordinal = result.ordinal();
        } else {
            if (expr.type != null) {
                ScriptedClass scriptedClass = expr.type.get();
                ScriptedClass declaring = scriptedClass.getFieldDeclaring(expr.name.lexeme());
                if (declaring != null) {
                    ClassReference fieldType = declaring.getFieldType(expr.name.lexeme());

                    OperationInfo info = getOperationInfo(expr.assignType, fieldType);
                    expr.executor = info.executor;
                    expr.ordinal = result.ordinal();

                    return expr.retType = fieldType;
                }
            }

            errorF(expr.name, "unknown symbol: '%s'", expr.name.lexeme());
        }
        return expr.retType = result.type();
    }

    @Override
    public ClassReference visitConstructorExpr(Expr.Constructor expr) {
        ClassReference[] argTypes = args(expr.args);
        ScriptedCallable callable = tryGetConstructorMethod(argTypes, expr.target.get(), expr.keyword);

        if (callable != null) {
            expr.signature = VarTypeManager.getClassName(expr.target) + VarTypeManager.getMethodSignatureNoTarget("<init>", callable.argTypes());
            for (ClassReference reference : callable.thrown()) {
                checkThrown(reference, expr.keyword);
            }
        }

        return expr.retType = expr.target;
    }

    @Override
    public ClassReference visitStaticSetExpr(Expr.StaticSet expr) {
        String fieldName = expr.name.lexeme();

        ClassReference fieldType = VarTypeManager.VOID.reference();
        ScriptedClass declaring = expr.target.get().getFieldDeclaring(fieldName);
        if (declaring != null) {
            fieldType = declaring.getFieldType(fieldName);

        } else {
            errorF(expr.name, "unknown static field in class %s: %s", expr.target.absoluteName(), fieldName);
        }

        ClassReference valType = analyseExpr(expr.value);

        if (expr.assignType.type() != TokenType.ASSIGN) {
            OperationInfo info = getOperationInfo(expr.assignType, fieldType, valType);
            expr.executor = info.executor;
        }

        return expr.retType = fieldType;
    }

    @Override
    public ClassReference visitUnaryExpr(Expr.Unary expr) {
        ClassReference objType = analyseExpr(expr.right);

        OperationInfo info = getOperationInfo(expr.operator, objType);

        expr.executor = info.executor;

        return expr.retType = info.result;
    }

    @Override
    public ClassReference visitWhenExpr(Expr.When expr) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(expr.condition);
        popExpected();

        ClassReference ifTrueType = analyseExpr(expr.ifTrue);
        ClassReference ifFalseType = analyseExpr(expr.ifFalse);
        if (!ifTrueType.is(ifFalseType))
            errorF(Compiler.LOCATION_ANALYSER.find(expr.ifFalse), "ternary operator must return same type. found %s and %s", ifTrueType.absoluteName(), ifFalseType.absoluteName());
        return expr.retType = ifTrueType;
    }

    @Override
    public ClassReference visitStaticGetExpr(Expr.StaticGet expr) {
        String fieldName = expr.name.lexeme();

        ClassReference fieldType = VarTypeManager.VOID.reference();
        ScriptedClass declaring = expr.target.get().getFieldDeclaring(fieldName);
        if (declaring != null) {
            fieldType = declaring.getFieldType(fieldName);

        } else {
            errorF(expr.name, "unknown static field in class %s: %s", expr.target.absoluteName(), fieldName);
        }

        return expr.retType = fieldType;
    }

    @Override
    public ClassReference visitSwitchExpr(Expr.Switch expr) {
        pushExpected(VarTypeManager.ENUM, VarTypeManager.STRING, VarTypeManager.INTEGER.reference(), VarTypeManager.DOUBLE.reference(), VarTypeManager.FLOAT.reference(), VarTypeManager.CHAR.reference());
        ClassReference reference = analyseExpr(expr.provider);
        if (reference.isChildOf(VarTypeManager.ENUM)) {
            expr.isEnum = true;
        }
        popExpected();

        ClassReference returnType = expr.defaulted != null ? analyseExpr(expr.defaulted) : null;

        if (returnType != null && !isExpected(returnType)) {
            errorIncompatibleTypes(Compiler.LOCATION_ANALYSER.find(expr.defaulted), returnType);
        }

        for (SwitchKey param : expr.params) {
            if (!param.canMatch(reference)) {
                errorIncompatibleTypes(param.getSource(), reference, param.getType());
            }
            param.index = switch (param) {
                case SwitchKey.Number number -> ((int) number.getSource().literal().value());
                case SwitchKey.String string -> string.getSource().literal().value().hashCode();
                case SwitchKey.Identifier identifier -> {
                    EnumConstantHolder constant = reference.get().getEnumConstant(identifier.getSource().lexeme());
                    if (constant == null)
                        errorF(identifier.getSource(), "Cannot resolve symbol '%s'", identifier.getSource().lexeme());
                    yield constant != null ? constant.ordinal() : 0;
                }
                default -> throw new IllegalStateException("Unexpected value: " + param);
            };

            ClassReference retType = analyseExpr(param.expr);

            //TODO
            if (!isExpected(retType)) {
                errorIncompatibleTypes(Compiler.LOCATION_ANALYSER.find(param.expr), retType);
            }
        }

        return expr.retType = returnType;
    }

    @Override
    public ClassReference visitSliceExpr(Expr.Slice expr) {
        ClassReference objType = analyseExpr(expr.object);

        if (!objType.get().isArray()) {
            errorF(Compiler.LOCATION_ANALYSER.find(expr), "Array type expected; found '%s'", objType.absoluteName());
        }

        if (expr.start != null) {
            ClassReference reference = analyseExpr(expr.start);
            if (!reference.is(VarTypeManager.INTEGER)) {
                errorF(Compiler.LOCATION_ANALYSER.find(expr.start), "integer expected; found '%s'" + reference.absoluteName());
            }
        }
        if (expr.end != null) {
            ClassReference reference = analyseExpr(expr.end);
            if (!reference.is(VarTypeManager.INTEGER)) {
                errorF(Compiler.LOCATION_ANALYSER.find(expr.end), "integer expected; found '%s'" + reference.absoluteName());
            }
        }
        if (expr.interval != null) {
            ClassReference reference = analyseExpr(expr.interval);
            if (!reference.is(VarTypeManager.INTEGER)) {
                errorF(Compiler.LOCATION_ANALYSER.find(expr.interval), "integer expected; found '%s'" + reference.absoluteName());
            }
        }

        return objType;
    }

    @Override
    public ClassReference visitGetExpr(Expr.Get expr) {
        ClassReference objType = analyseExpr(expr.object);
        expr.type = objType;

        String fieldName = expr.name.lexeme();

        if (objType.get().isArray() && "length".equals(fieldName)) {
            return expr.retType = VarTypeManager.INTEGER.reference();
        }

        ClassReference fieldType = VarTypeManager.VOID.reference();
        ScriptedClass declaring = objType.get().getFieldDeclaring(fieldName);
        if (declaring != null) {
            fieldType = declaring.getFieldType(fieldName);
        } else {
            errorF(expr.name, "unknown field in class %s: %s", objType.absoluteName(), fieldName);
        }

        return expr.retType = fieldType;
    }

    @Override
    public ClassReference visitIdentifierAssignExpr(Expr.IdentifierAssign expr) {
        LocalVariableContainer.FetchResult result = checkVarExistence(expr.name, expr.type.type() != TokenType.ASSIGN, false);
        if (result != LocalVariableContainer.FetchResult.FAIL) {
            ClassReference type = analyseExpr(expr.value);
            if (type.isChildOf(result.type())) {
                OperationInfo operationInfo;
                if (expr.type.type() == TokenType.ASSIGN) {
                    varAnalyser.setHasValue(result.ordinal());
                    operationInfo = OperationInfo.UNKNOWN;
                } else
                    operationInfo = getOperationInfo(varAnalyser.getType(expr.name.lexeme()), expr.type, expr.value);
                expr.executor = operationInfo.executor;
            } else {
                errorIncompatibleTypes(expr.value, result.type(), type);
            }
            expr.ordinal = result.ordinal();
        } else {
            if (expr.fieldOwner != null) {
                ScriptedClass scriptedClass = expr.fieldOwner.get();
                ScriptedClass declared = scriptedClass.getFieldDeclaring(expr.name.lexeme());
                if (declared != null) {
                    ClassReference fieldType = declared.getFieldType(expr.name.lexeme());

                    if (expr.type.type() != TokenType.ASSIGN) {
                        OperationInfo info = getOperationInfo(expr.type, fieldType, fieldType);
                        expr.executor = info.executor;
                        if (!info.result.is(fieldType))
                            errorIncompatibleTypes(expr.type, fieldType, info.result);
                    }

                    return expr.retType = fieldType;
                }
            }
            errorF(expr.name, "unknown symbol: '%s'", expr.name.lexeme());
        }
        return expr.retType = result.type();
    }

    @Override
    public ClassReference visitBinaryExpr(Expr.Binary expr) {

        ClassReference leftType = analyseExpr(expr.left);
        ClassReference rightType = analyseExpr(expr.right);

        OperationInfo info = getOperationInfo(expr.operator, leftType, rightType);

        expr.executor = Objects.requireNonNull(info.executor);
        expr.signature = info.methodSignature;

        return expr.retType = info.result;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value == null) {
            if (!methodReturnType.is(VarTypeManager.VOID)) {
                error(stmt.keyword, "missing return value");
            }
        } else {
            if (methodReturnType.is(VarTypeManager.VOID)) {
                error(stmt.keyword, "cannot return value from method with void return type");
            } else {
                ClassReference retType = analyseExpr(stmt.value);
                if (!retType.is(methodReturnType)) {
                    errorIncompatibleTypes(stmt.value, methodReturnType, retType);
                }
            }
        }

        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        pushScope();
        analyseStmt(stmt.init);

        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(stmt.condition);
        popExpected();

        analyseExpr(stmt.increment);

        pushScope();

        analyseStmt(stmt.body);

        stmt.popVarCount = this.varAnalyser.pop();

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(stmt.condition);
        popExpected();

        pushScope();
        analyseStmt(stmt.body);
        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        ClassReference arrayType = stmt.type.array();
        pushExpected(arrayType);
        ClassReference reference = analyseExpr(stmt.initializer);
        if (!reference.is(arrayType))
            errorIncompatibleTypes(stmt.initializer, arrayType, reference);
        popExpected();

        pushScope();
        int baseVar = varAnalyser.add("?", arrayType, false, true); //array variable
        varAnalyser.add("?", VarTypeManager.INTEGER.reference(), false, true); //iteration variable

        pushScope();
        //add 2 synthetic vars

        varAnalyser.add(stmt.name.lexeme(), stmt.type, true, true); //named variable from sourcecode

        analyseStmt(stmt.body);
        stmt.baseVar = baseVar;

        return null;
    }

    @Override
    public Void visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        Token[] localNames = stmt.localNames;
        byte[] localOrdinals = new byte[localNames.length];

        for (int i = 0; i < localNames.length; i++) {
            Token localName = localNames[i];
            LocalVariableContainer.FetchResult fetchResult = varAnalyser.get(localName.lexeme());
            if (fetchResult == LocalVariableContainer.FetchResult.FAIL) {
                errorF(localName, "unknown local variable '%s'", localName.lexeme());
            }
            localOrdinals[i] = fetchResult.ordinal();
        }
        stmt.localOrdinals = localOrdinals;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        analyseExpr(stmt.expression);

        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        stmt.localId = varAnalyser.add(stmt.name.lexeme(), stmt.type, !stmt.isFinal, stmt.initializer != null);
        if (stmt.initializer != null) {
            ClassReference setType = analyseExpr(stmt.initializer);
            if (!setType.isChildOf(stmt.type)) {
                errorIncompatibleTypes(stmt.initializer, stmt.type, setType);
            }
        }
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        pushExpected(VarTypeManager.THROWABLE);
        ClassReference val = analyseExpr(stmt.value);
        checkThrown(val, stmt.keyword);
        popExpected();
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements) {
            analyseStmt(statement);
        }
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        pushScope();
        methodThrown.push(
                Arrays.stream(stmt.catches)
                        .map(Pair::getFirst)
                        .map(Pair::getFirst)
                        .flatMap(Arrays::stream)
                        .toArray(ClassReference[]::new)
        );

        analyseStmt(stmt.body);

        methodThrown.pop();

        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches) {
            pushScope();
            tryCreateVar(aCatch.getFirst().getSecond(), VarTypeManager.THROWABLE, true, false);
            analyseStmt(aCatch.getSecond());
        }

        if (stmt.finale != null) {
            pushScope();
            analyseStmt(stmt.finale);
        }

        return null;
    }

    @Override
    public Void visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        stmt.amount = this.varAnalyser.pop();

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        pushExpected(VarTypeManager.BOOLEAN.reference());
        analyseExpr(stmt.condition);
        popExpected();

        pushScope();
        analyseStmt(stmt.thenBranch);
        for (ElifBranch branch : stmt.elifs) {
            pushExpected(VarTypeManager.BOOLEAN.reference());
            analyseExpr(branch.condition);
            popExpected();

            pushScope();
            analyseStmt(branch.body);
        }
        if (stmt.elseBranch != null) {
            pushScope();
            analyseStmt(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return null;
    }
}
