package net.kapitencraft.scripted.lang.compiler.parser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.compiler.VarTypeParser;
import net.kapitencraft.scripted.lang.compiler.analyser.BytecodeVars;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.algebra.Operand;
import net.kapitencraft.scripted.lang.exe.algebra.OperationType;
import net.kapitencraft.scripted.lang.exe.natives.impl.NativeClassImpl;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.RegistryHolder;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.ScriptedField;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.tool.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;
import static net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory.*;

@SuppressWarnings("ThrowableNotThrown")
public class ExprParser extends AbstractParser {
    private final List<ClassReference> fallback;
    protected GenericStack generics = new GenericStack();
    private int anonymousCounter = 0; //counts how many anonymous classes have been created inside the class, to give each a unique name

    public ExprParser(Compiler.ErrorStorage errorStorage) {
        super(errorStorage);
        this.fallback = new ArrayList<>();
    }

    protected ClassReference currentFallback() {
        if (fallback.isEmpty()) throw new IllegalArgumentException("no fallback applied");
        return fallback.getLast();
    }

    public void pushGenerics(Holder.Generics generics) {
        generics.pushToStack(this.generics);
    }

    public void pushFallback(ClassReference fallback) {
        this.fallback.add(fallback);
    }

    public void popFallback() {
        if (this.fallback.isEmpty()) throw new IllegalStateException("fallback stack underflow");
        this.fallback.removeLast();
    }

    //a clean chain-of-responsibility behavior pattern we have here
    public Expr expression() {
        if (match(SWITCH)) {
            return switchExpr();
        }

        return when();
    }

    public Expr literalOrReference() {
        if (match(AT)) {
            SourceClassReference reference = consumeVarType(generics);
            Token errorPoint = previous();
            if (match(BRACKET_O)) {
                parseAnnotationProperties(reference, errorPoint);
            }
        }
        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous());
        }
        ClassReference target = consumeVarType(generics).getReference();
        Token name = previous();

        return new Expr.StaticGet(target, name);
    }

    public Annotation parseAnnotation(Holder.AnnotationObj obj, VarTypeParser varTypeParser) {
        this.apply(obj.properties(), varTypeParser);
        return parseAnnotationProperties(obj.type(), obj.type().getToken());
    }

    public Annotation parseAnnotationProperties(SourceClassReference typeRef, Token errorPoint) {
        ScriptedClass type = typeRef.getReference().get();

        if (!type.isAnnotation()) {
            error(typeRef.getToken(), "annotation type expected");
            return null;
        }
        Map<String, ScriptedCallable> annotationMethods = new HashMap<>();

        type.getMethods().asMap().forEach((s, dataMethodContainer) ->
                annotationMethods.put(s, dataMethodContainer.methods()[0])
        );

        List<String> abstracts = new ArrayList<>();
        annotationMethods.forEach((s, scriptedCallable) -> {
            if (scriptedCallable.isAbstract()) abstracts.add(s);
        });

        if (isAtEnd()) {
            if (!abstracts.isEmpty()) {
                errorMissingProperties(errorPoint, abstracts);
            }
            return Annotation.empty(type);
        }
        Expr singleProperty;
        if (!check(IDENTIFIER)) {
            singleProperty = literalOrReference();
        } else {
            advance();
            if (check(ASSIGN)) {
                current--;
                Map<String, Expr> properties = new HashMap<>();
                do {
                    Token propertyName = consumeIdentifier();
                    if (properties.containsKey(propertyName.lexeme())) errorStorage.errorF(propertyName, "duplicate annotation property with name %s", propertyName.lexeme());
                    consume(ASSIGN, "'=' expected");
                    Expr property = literalOrReference();
                    properties.put(propertyName.lexeme(), property);
                } while (match(COMMA));
                List<String> requiredProperties = new ArrayList<>(abstracts);
                requiredProperties.removeAll(properties.keySet());
                if (!requiredProperties.isEmpty()) errorMissingProperties(errorPoint, requiredProperties);
                return Annotation.fromPropertyMap(type, properties);
            } else {
                current--;
                singleProperty = literalOrReference();
            }
        }
        if (abstracts.size() > 1) {
            ArrayList<String> c = new ArrayList<>(abstracts);
            c.remove("value");
            errorMissingProperties(errorPoint, c);
        } else if (!abstracts.contains("value")) {
            error(previous(), "can not find annotation method 'value'");
        }
        return Annotation.fromSingleProperty(type, singleProperty);
    }

    private void errorMissingProperties(Token errorPoint, List<String> propertyNames) {
        error(errorPoint, propertyNames.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", ")) + " missing though required");
    }

    private Expr when() {
        Expr expr = castCheck();
        if (match(QUESTION_MARK)) {
            expectCondition(expr);
            Expr ifTrue = expression();
            consume(COLON, "':' expected");
            Expr ifFalse = expression();
            ClassReference ifTrueClass = finder.findRetType(ifTrue);
            ClassReference ifFalseClass = finder.findRetType(ifFalse);
            if (!(ifTrueClass.get().isParentOf(ifFalseClass.get()) || ifFalseClass.get().isParentOf(ifTrueClass.get()))) error(locFinder.find(ifTrue), "both expressions on when statement must return the same type");
            expr = new Expr.When(expr, ifTrue, ifFalse);
        }

        return expr;
    }

    private Expr castCheck() {
        Expr expr = assignment();
        if (match(INSTANCEOF)) {
            ClassReference loxClass = consumeVarType(generics).getReference();
            Token patternVar = null;
            if (match(IDENTIFIER)) {
                patternVar = previous();
                varAnalyser.add(patternVar.lexeme(), loxClass, true, false);
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

            if (expr instanceof Expr.VarRef(Token name, byte ordinal)) {

                checkVarExistence(name, assign.type() != ASSIGN,
                        false);
                checkVarType(name, value);
                Executor executor;
                if (assign.type() == ASSIGN) {
                    varAnalyser.setHasValue(ordinal);
                    executor = Executor.UNKNOWN;
                } else
                    executor = getExecutor(varAnalyser.getType(name.lexeme()), assign, value);

                return new Expr.Assign(name, value, assign, ordinal, executor.executor);
            } else if (expr instanceof Expr.Get get) {
                ClassReference target = finder.findRetType(get.object());
                ClassReference fieldType = target.get().getFieldType(get.name().lexeme());
                expectType(get.name(), fieldType, finder.findRetType(value));

                Executor executor;
                if (assign.type() != ASSIGN) executor = getExecutor(fieldType, assign, value);
                else executor = Executor.UNKNOWN;
                return new Expr.Set(get.object(), get.name(), value, assign, executor.executor);
            } else if (expr instanceof Expr.ArrayGet get) {
                Executor executor;
                if (assign.type() != ASSIGN) executor = getExecutor(get, assign, value);
                else executor = Executor.UNKNOWN;
                return new Expr.ArraySet(get.object(), get.index(), value, assign, executor.executor);
            } else if (expr instanceof Expr.StaticGet(ClassReference target, Token name)) {
                Executor executor;
                if (assign.type() != ASSIGN) executor = getExecutor(expr, assign, value);
                else executor = Executor.UNKNOWN;
                return new Expr.StaticSet(target, name, value, assign, executor.executor);
            }

            error(assign, "Invalid assignment target.");
        }

        if (match(GROW, SHRINK)) {

            Token assign = previous();

            if (expr instanceof Expr.VarRef(Token name, byte ordinal)) {

                ClassReference type = checkVarExistence(name, true, false);
                if (!type.get().isChildOf(VarTypeManager.NUMBER)) {
                    errorStorage.errorF(name, "Operator '%s' can not be applied to '%s'", assign.lexeme(), type.absoluteName());
                }
                return new Expr.SpecialAssign(name, assign, ordinal, type);
            }

            if (expr instanceof Expr.Get get) {
                ClassReference reference = finder.findRetType(get.object()).get().getFieldType(get.name().lexeme());
                if (!reference.get().isChildOf(VarTypeManager.NUMBER)) {
                    errorStorage.errorF(get.name(), "Operator '%s' can not be applied to '%s'", assign.lexeme(), reference.absoluteName());
                }
                return new Expr.SpecialSet(get.object(), get.name(), assign, reference);
            }

            if (expr instanceof Expr.ArrayGet arrayGet) {
                ClassReference reference = finder.findRetType(arrayGet.object()).get().getComponentType().reference();
                if (!reference.get().isChildOf(VarTypeManager.NUMBER)) {
                    errorStorage.errorF(locFinder.find(arrayGet.object()), "Operator '%s' can not be applied to '%s'", assign.lexeme(), reference.absoluteName());
                }
                return new Expr.ArraySpecial(arrayGet.object(), arrayGet.index(), assign, reference);
            }
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expectCondition(expr);
            expectCondition(right);
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND, XOR)) {
            Token operator = previous();
            Expr right = equality();
            expectCondition(expr);
            expectCondition(right);
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Executor getExecutor(ClassReference left, Token operator, ClassReference right) {
        OperationType operation = OperationType.of(operator.type());
        assert operation != null;
        ScriptedClass result = VarTypeManager.VOID;
        Operand operand = Operand.LEFT;
        if (left.get() instanceof PrimitiveClass || left.is(VarTypeManager.STRING.get()) || right.is(VarTypeManager.STRING.get())) {
            result = left.get().checkOperation(operation, Operand.LEFT, right);
            if (result == VarTypeManager.VOID) {
                result = right.get().checkOperation(operation, Operand.RIGHT, left);
                operand = Operand.RIGHT;
            }
        }
        //search for overloads
        if (result == VarTypeManager.VOID && operation.getMethodName() != null) {
            String signature = operation.getMethodName() + "(" + VarTypeManager.getClassName(right) + ")";
            ScriptedCallable method = left.get().getMethod(signature);
            if (method != null) {
                signature = VarTypeManager.getClassName(left) + signature;
                return new Executor(left, Operand.LEFT, method.retType(), signature);
            }
            String signatureRight = operation.getMethodName() + "(" + VarTypeManager.getClassName(left) + ")";
            method = right.get().getMethod(signatureRight);
            if (method != null) {
                signatureRight = VarTypeManager.getClassName(right) + signatureRight;
                return new Executor(right, Operand.RIGHT, method.retType(), signatureRight);
            }
        }
        if (result == VarTypeManager.VOID) {
            errorStorage.errorF(operator, "operator '%s' not possible for argument types %s and %s", operator.lexeme(), left.absoluteName(), right.absoluteName());
            return Executor.UNKNOWN;
        }
        return new Executor(left, operand, result.reference(), null);
    }

    private Executor getExecutor(Expr leftArg, Token operator, Expr rightArg) {
        return getExecutor(finder.findRetType(leftArg), operator, finder.findRetType(rightArg));
    }

    private Executor getExecutor(ClassReference left, Token operator, Expr rightArg) {
        return getExecutor(left, operator, finder.findRetType(rightArg));
    }

    private record Executor(ClassReference executor, Operand operand, ClassReference result, @Nullable String methodSignature) {
        private static final Executor UNKNOWN = new Executor(WILDCARD, Operand.LEFT, VarTypeManager.VOID.reference(), null);
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQUALITY)) {
            Token operator = previous();
            Expr right = comparison();
            expr = parseBinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        if (match(COMPARATORS)) {
            Token operator = previous();
            Expr right = term();

            if (match(COMPARATORS)) {
                List<Token> operators = new ArrayList<>();
                operators.add(operator);
                operators.add(previous());
                List<Expr> values = new ArrayList<>();
                values.add(expr);
                values.add(right);
                values.add(term());
                while (match(COMPARATORS)) {
                    operators.add(previous());
                    values.add(term());
                }
                expr = new Expr.ComparisonChain(values.toArray(Expr[]::new), operators.toArray(Token[]::new), VarTypeManager.INTEGER.reference());
            } else {
                expr = parseBinaryExpr(expr, operator, right);
            }
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(SUB, ADD)) {
            Token operator = previous();
            Expr right = factor();

            expr = parseBinaryExpr(expr, operator, right);
        }

        return expr;
    }

    private @NotNull Expr parseBinaryExpr(Expr expr, Token operator, Expr right) {
        Executor executorInfo = getExecutor(expr, operator, right);
        if (executorInfo.methodSignature != null) {
            if (executorInfo.operand == Operand.RIGHT) {
                return new Expr.InstCall(right, operator, new Expr[] {expr}, executorInfo.result, executorInfo.methodSignature);
            }
            return new Expr.InstCall(expr, operator, new Expr[] {right}, executorInfo.result, executorInfo.methodSignature);
        }
        if (executorInfo.operand == Operand.RIGHT)
            expr = new Expr.Binary(right, expr, operator, executorInfo.executor, executorInfo.result);
        else
            expr = new Expr.Binary(expr, right, operator, executorInfo.executor, executorInfo.result);
        return expr;
    }

    private Expr factor() {
        Expr expr = pow();

        while (match(DIV, MUL, MOD)) {
            Token operator = previous();
            Expr right = pow();

            expr = parseBinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr pow() {
        Expr expr = unary();

        while (match(POW)) {
            Token operator = previous();
            Expr right = unary();

            expr = parseBinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(NOT, SUB)) {
            Token operator = previous();
            Expr right = unary();
            ClassReference executor;
            if (operator.type() == NOT) {
                expectCondition(right);
                executor = VarTypeManager.BOOLEAN.reference();
            }
            else executor = expectType(right, VarTypeManager.NUMBER.reference());
            return new Expr.Unary(operator, right, executor);
        }

        return call();
    }

    private Expr switchExpr() {
        Token keyword = previous();
        consumeBracketOpen("switch");

        expectType(VarTypeManager.ENUM, VarTypeManager.STRING, VarTypeManager.INTEGER.reference(), VarTypeManager.DOUBLE.reference(), VarTypeManager.FLOAT.reference(), VarTypeManager.CHAR.reference());
        Expr provider = expression();
        popExpectation();

        ClassReference type = finder.findRetType(provider);

        if (type.get().isChildOf(VarTypeManager.ENUM.get())) {
            //is enum, wrap in ordinal access
            provider = new Expr.InstCall(provider, Token.createNative("ordinal"), new Expr[0], VarTypeManager.INTEGER.reference(), "Lscripted/lang/Enum;ordinal()I");
        }

        consumeBracketClose("switch");

        consumeCurlyOpen("switch body");
        Map<Integer, Expr> params = new LinkedHashMap<>();
        Expr def = null;

        while (!check(C_BRACKET_C)) {
            if (match(CASE)) {
                int key = literalOrEnum(type);
                if (params.containsKey(key)) errorStorage.errorF(previous(), "Duplicate case key '%s'", previous().lexeme());
                consume(LAMBDA, "not a statement");
                Expr expr = expression();
                consumeEndOfArg();
                params.put(key, expr);
            } else if (match(DEFAULT)) {
                if (def != null) error(previous(), "Duplicate default key");
                consume(LAMBDA, "not a statement");
                def = expression();
                consumeEndOfArg();
            } else {
                error(peek(), "unexpected token");
            }
        }

        consumeCurlyClose("switch body");
        return new Expr.Switch(provider, params, def, keyword);
    }

    private int literalOrEnum(ClassReference type) {
        if (check(PRIMITIVE)) {
            if (type.get().isChildOf(VarTypeManager.ENUM.get())) {
                //error wrong type
            }
            return (int) literal();
        } else {
            if (type.get().isChildOf(VarTypeManager.ENUM.get())) {
                Token identifier = consumeIdentifier();
                Holder.EnumConstant constant = type.get().getEnumConstant(identifier.lexeme());
                if (constant != null) {
                    return constant.ordinal();
                }
                return identifier.lexeme().hashCode(); //return hashcode to ensure objects are not equal
            }
            error(peek(), "unknown symbol");
        }
        return -1;
    }

    private Expr staticAssign(ClassReference target, Token name) {
        Token type = previous();
        Expr value = expression();
        Executor executor = getExecutor(target.get().getFieldType(name.lexeme()), type, value);
        return new Expr.StaticSet(target, name, value, type, executor.executor);
    }

    private Expr staticSpecialAssign(ClassReference target, Token name) {
        return new Expr.StaticSpecial(target, name, previous(), target.get().getFieldType(name.lexeme()));
    }

    public Expr[] args() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (arguments.size() > 255) error(peek(), "Can't have more than 255 arguments");
                arguments.add(expression());
            } while (match(COMMA));
        }

        return arguments.toArray(new Expr[0]);
    }

    public ClassReference[] argTypes(Expr[] args) {
        return Arrays.stream(args).map(this.finder::findRetType).toArray(ClassReference[]::new);
    }


    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(S_BRACKET_O)) {
                Token bracketO = previous();
                if (match(COLON)) {
                    Expr end = check(COLON) ? null : expression();
                    consume(COLON, "':' expected");
                    Expr interval = check(S_BRACKET_C) ? null : expression();
                    if (end == null && interval == null) error(bracketO, "slice without any definition");
                    consume(S_BRACKET_C, "']' expected");
                    //expr = new Expr.Slice(expr, null, end, interval);
                    continue;
                }
                Expr index = expression();
                if (match(COLON)) {
                    Expr end = check(COLON) ? null : expression();
                    consume(COLON, "':' expected");
                    Expr interval = check(S_BRACKET_C) ? null : expression();
                    consume(S_BRACKET_C, "']' expected");
                    expr = new Expr.Slice(expr, index, end, interval);
                    continue;
                }
                consume(S_BRACKET_C, "']' expected");
                ScriptedClass scriptedClass = finder.findRetType(expr).get();
                if (!scriptedClass.isArray()) error(bracketO, "array type expected");
                expr = new Expr.ArrayGet(expr, index, scriptedClass.getComponentType().reference());
            } else if (match(BRACKET_O)) {
                if (expr instanceof Expr.Get get)
                    expr = finishCall(get.name(), finder.findRetType(get.object()), get.object());
                else error(locFinder.find(expr), "obj expected");
            } else if (match(DOT)) {
                if (expr instanceof Expr.Literal && !check(IDENTIFIER)) continue;
                Token name = consume(IDENTIFIER, "Expect property name after '.'");
                ClassReference type = finder.findRetType(expr);
                ScriptedClass targetType = type.get();
                if (!check(BRACKET_O)) { //ensure not to check for field if it's a method
                    if (
                            !targetType.isArray() &&
                            name.lexeme().equals("length") && //ensure array length can be used
                            !targetType.hasField(name.lexeme())) error(name, "unknown symbol");
                }
                expr = new Expr.Get(expr, name, type);
            } else {
                break;
            }
        }
        return expr;
    }

    public ClassReference checkArguments(Expr[] args, @Nullable ScriptedCallable target, @Nullable ClassReference obj, Token loc) {
        ClassReference[] expectedTypes = target == null ? new ClassReference[0] : target.argTypes();
        ClassReference[] givenTypes = argTypes(args);
        if (expectedTypes.length != givenTypes.length) {
            errorStorage.errorF(loc, "method for %s cannot be applied to given types;", loc.lexeme());

            errorStorage.logError("required: " + Util.getDescriptor(expectedTypes));
            errorStorage.logError("found:    " + Util.getDescriptor(givenTypes));
            errorStorage.logError("reason: actual and formal argument lists differ in length");
        } else {
            for (int i = 0; i < givenTypes.length; i++) {
                expectType(locFinder.find(args[i]), givenTypes[i], expectedTypes[i]);
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
                    types.put(gCR.getTypeName(), givenTypes[i]);
                }
            }
            if (!types.isEmpty()) genericStack.push(types);


            return genericClassReference.unwrap(genericStack);
        }

        return type;
    }

    private Object literal() {
        if (match(FALSE)) return false;
        if (match(TRUE)) return true;
        if (match(NULL)) return null;
        RegistryHolder holder = tryParseRegistry();
        if (holder != null) return holder;

        if (match(NUM, STR)) {
            return previous().literal();
        }

        throw error(peek(), "Expected literal");
    }

    private Expr primary() {
        if (match(NEW)) {
            SourceClassReference type = consumeVarTypeNoArray(generics);
            if (match(S_BRACKET_O)) {
                Expr size = null;
                //array creation
                if (!check(S_BRACKET_C)) {
                    size = expression();
                }
                consume(S_BRACKET_C, "expected ']' after array constructor");
                Expr[] values = null;
                if (size == null) {
                    consumeCurlyOpen("array initialization");
                    values = args();
                    consumeCurlyClose("array initialization");
                }
                return new Expr.ArrayConstructor(type.getToken(), type.getReference(), size, values);
            }
            consumeBracketOpen("constructors");
            Expr[] args = args();
            consumeBracketClose("constructors");

            if (match(C_BRACKET_O)) {
                HolderParser hParser = new HolderParser(this.errorStorage);
                if (type.get().isFinal()) {
                    error(previous(), "can not extend final class");
                }
                hParser.apply(getCurlyEnclosedCode(), this.parser);
                String nameLiteral = String.valueOf(this.anonymousCounter++);
                String pck = this.currentFallback().pck();
                String outName = this.currentFallback().name() + "$" + nameLiteral;
                ClassReference typeTarget = VarTypeManager.getOrCreateClass(outName, pck);
                Token name = new Token(IDENTIFIER, outName, LiteralHolder.EMPTY, type.getToken().line(), type.getToken().lineStartIndex());
                SourceClassReference original = type;
                type = SourceClassReference.from(name, typeTarget);
                if (original.get().isInterface()) {
                    Compiler.queueRegister(
                            hParser.parseInterface(typeTarget, pck, name, null, null, null, List.of(original)),
                            this.errorStorage,
                            this.parser,
                            outName
                    );
                } else {
                    Compiler.queueRegister(
                            hParser.parseClass(typeTarget, null, null, null, pck, name, original, List.of()),
                            this.errorStorage,
                            this.parser,
                            outName
                    );
                }

                consumeCurlyClose("anonymous class");
            } else if (type.get().isAbstract()) {
                error(type.getToken(), "can not instantiate abstract class " + type.absoluteName());
            }

            String signature = null;
            Pair<ScriptedCallable, ScriptedClass> methodInfo = tryGetConstructorMethod(args, type.getReference(), type.get(), type.getToken());

            ClassReference typeRef = type.getReference();
            if (methodInfo != null) {
                ClassReference[] argTypes = methodInfo.getFirst().argTypes();
                signature = VarTypeManager.getMethodSignature(type.get(), "<init>", argTypes);
                checkArguments(args, methodInfo.getFirst(), null, type.getToken());

                Holder.Generics classGenerics = type.get().getGenerics();
                if (classGenerics != null) {
                    Map<String, ClassReference> types = new HashMap<>();
                    for (int i = 0; i < argTypes.length; i++) {
                        if (argTypes[i] instanceof GenericClassReference genericClassReference) {
                            types.put(
                                    genericClassReference.getTypeName(),
                                    finder.findRetType(args[i])
                            );
                        }
                    }
                    List<ClassReference> ordered = new ArrayList<>();
                    for (int i = 0; i < classGenerics.variables().length; i++) {
                        ordered.add(types.get(classGenerics.variables()[i].name().lexeme()));
                    }
                    typeRef = new AppliedGenericsReference(type.getReference(), new Holder.AppliedGenerics(type.getToken(), ordered.toArray(new ClassReference[0])));
                }
            }

            return new Expr.Constructor(type.getToken(), typeRef, args, signature);
        }

        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous());
        }

        if (match(SUPER)) {
            Token reference = previous(); //'super' reference
            ClassReference fallback = currentFallback();
            if (fallback.exists()) {
                consume(DOT, "expected '.' after 'super'");
                Token name = consumeIdentifier();
                ScriptedClass type = fallback.get();
                ClassReference superclass = type.superclass();
                consumeBracketOpen("super method");
                if (superclass == null) {
                    error(name, "can not access super class");
                } else if (type.hasMethod(name.lexeme())) {
                    Expr[] arguments = args();
                    ClassReference[] givenTypes = argTypes(arguments);
                    ScriptedClass targetClass = superclass.get();
                    if (!targetClass.hasMethod(name.lexeme())) {
                        error(name, "unknown method '" + name.lexeme() + "'");
                        consumeBracketClose("arguments");
                        return new Expr.StaticCall(superclass, name, arguments, WILDCARD, "?");
                    }
                    ScriptedCallable callable = Util.getStaticMethod(targetClass, name.lexeme(), givenTypes);
                    ClassReference retType = VarTypeManager.VOID.reference();
                    String signature = null;
                    if (callable != null) {
                        retType = checkArguments(arguments, callable, superclass, name);
                        signature = VarTypeManager.getMethodSignature(targetClass, name.lexeme(), callable.argTypes());
                    }

                    consumeBracketClose("arguments");

                    return new Expr.SuperCall(new Expr.VarRef(reference, (byte) 0), superclass, name, arguments, retType, signature);
                }
            }
        }

        if (match(IDENTIFIER)) {
            Token previous = previous(); //the identifier just consumed
            BytecodeVars.FetchResult result = varAnalyser.get(previous.lexeme()); //fetch variable under that name
            if (result == BytecodeVars.FetchResult.FAIL) { //check if there exists a variable under that name
                if (currentFallback().exists()) { //check if the parser has a class fallback available
                    ClassReference fallbackReference = currentFallback();
                    ScriptedClass fallback = fallbackReference.get(); //get said fallback
                    String name = previous.lexeme(); //get the literal of the identifier
                    if (match(BRACKET_O)) { //check if there's an attempt to call a method from the fallback class
                        if (fallback.hasMethod(name)) {
                            return finishCall(previous, fallbackReference, new Expr.VarRef(
                                            Token.createNative("this"),
                                            (byte) 0
                                    )
                            );
                        }
                    } else {
                        if (fallback.hasField(name)) {
                            ScriptedField field = fallback.getFields().get(name);
                            if (field.isStatic())
                                return new Expr.StaticGet(fallbackReference, previous);
                            else
                                return new Expr.Get(new Expr.VarRef(
                                    Token.createNative("this"),
                                    (byte) 0),
                                    previous, fallbackReference
                            );
                        }
                    }
                }
                current--; //un-consume the identifier for the statics to take over
                return statics();
            }
            checkVarExistence(previous, true, true);
            return new Expr.VarRef(
                    previous,
                    result.ordinal()
            );
        }

        RegistryHolder holder = tryParseRegistry();
        if (holder != null) {
            Expr expr = new Expr.RegistryAccess(
                    holder.reference(), holder.origin(),
                    holder.key().location().toString(),
                    holder.objLoc().toString()
            );

            Set<ClassReference> types = searched();

            if (holder.key() == Registries.BLOCK) {
                if (!types.contains(VarTypeManager.BLOCK) || check(S_BRACKET_O)) {
                    //extend block to be state
                    expr = new Expr.InstCall(expr, holder.origin(), new Expr[0], VarTypeManager.BLOCK_STATE, "Lnet/minecraft/world/level/block/Block;defaultBlockState()");

                    if (match(S_BRACKET_O)) {
                        StateDefinition<Block, BlockState> definition = ((Block) holder.entry()).getStateDefinition();

                        //while (!check(S_BRACKET_C)) {
                        //    Token propertyName = consumeIdentifier();

                        //    Property<?> property = definition.getProperty(propertyName.lexeme());
                        //    if (property == null)
                        //        error(propertyName, "unknown BlockState property for block " + holder.objLoc());

                        //    Expr obj = expression();

                        //    property //TODO
                        //}

                        consume(S_BRACKET_C, "expected ']' after BlockState properties");
                    }

                    return expr;
                }
            }
        }

        if (match(THIS)) return new Expr.VarRef(
                previous(),
                (byte)0
        );

        if (match(BRACKET_O)) {
            Expr expr = expression();
            consumeBracketClose("expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Illegal start of expression");
    }

    private RegistryHolder tryParseRegistry() {
        if (match(NAMESPACE)) {
            Token namespace = previous();

            consume(COLON, "expected ':' after namespace");

            List<Token> name = new ArrayList<>();

            name.add(consumeIdentifier());

            //lookup current expected argument types
            Set<ClassReference> last = searched();

            List<RegistryHolder> holders = new ArrayList<>();
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace.lexeme(), name.stream().map(Token::lexeme).collect(Collectors.joining()));
            for (ClassReference reference : last) {
                if (!reference.exists()) {
                    continue;
                }

                if (!(reference.get() instanceof NativeClassImpl nativeClass))
                    continue;

                if (nativeClass.getOwner() == null)
                    continue;

                Registry<?> registry = BuiltInRegistries.REGISTRY.get(nativeClass.getOwner().location());

                Object o = registry.get(location);

                if (o != null) {
                    RegistryHolder h = new RegistryHolder(reference, namespace, registry, nativeClass.getOwner(), location, o);

                    holders.add(h);
                }
            }

            if (holders.size() > 1) {
                error(namespace, "ambiguous registry entry. all of " + holders.stream().map(RegistryHolder::key).map(ResourceKey::location).map(ResourceLocation::toString).collect(Collectors.joining("[", ",", "]")) + " match");
            }

            if (holders.isEmpty()) {
                error(namespace, "no registry entry for " + location + " found");
            }

            return holders.getFirst();
        }
        return null;
    }

    private Pair<ScriptedCallable, ScriptedClass> tryGetConstructorMethod(Expr[] args, ClassReference type, ScriptedClass scriptedClass, Token loc) {
        DataMethodContainer container = scriptedClass.getMethods().get("<init>");
        if (container == null) {
            if (args.length > 0) {
                errorStorage.errorF(loc, "method for %s cannot be applied to given types;", loc.lexeme());

                errorStorage.logError("required: ");
                errorStorage.logError("found:    " + Util.getDescriptor(this.argTypes(args)));
                errorStorage.logError("reason: actual and formal argument lists differ in length");
            }
            return null;
        }

        return Util.getVirtualMethod(scriptedClass, "<init>", this.argTypes(args));
    }

    private Expr statics() {
        ClassReference target = consumeVarType(generics).getReference();
        consume(DOT, "'.' expected");
        Token name = consumeIdentifier();
        if (match(BRACKET_O)) return finishCall(name, target, null);
        if (match(ASSIGN) || match(OPERATION_ASSIGN)) return staticAssign(target, name);
        if (match(GROW, SHRINK)) return staticSpecialAssign(target, name);
        return new Expr.StaticGet(target, name);
    }

    private Expr finishCall(Token name, ClassReference objType, @Nullable Expr obj) {
        Expr[] arguments = args();

        ClassReference[] givenTypes = argTypes(arguments);
        ScriptedClass targetClass = objType.get();

        if (!targetClass.hasMethod(name.lexeme())) {
            error(name, "unknown method '" + name.lexeme() + "'");
            consumeBracketClose("arguments");
            return new Expr.StaticCall(objType, name, arguments, WILDCARD, "?");
        }
        Pair<ScriptedCallable, ScriptedClass> methodInfo = Util.getVirtualMethod(targetClass, name.lexeme(), givenTypes);
        ClassReference retType = VarTypeManager.VOID.reference();
        String signature = null;
        if (methodInfo != null) {
            retType = checkArguments(arguments, methodInfo.getFirst(), objType, name);
            signature = VarTypeManager.getMethodSignature(methodInfo.getSecond(), name.lexeme(), methodInfo.getFirst().argTypes());
        }

        consumeBracketClose("arguments");

        if (methodInfo == null || methodInfo.getFirst().isStatic()) {
            return new Expr.StaticCall(objType, name, arguments, retType, signature);
        } else {
            if (obj == null) {
                error(name, "Non-static method can not be referenced from a static context");
            }
            return new Expr.InstCall(obj, name, arguments, retType, signature);
        }
    }
}
