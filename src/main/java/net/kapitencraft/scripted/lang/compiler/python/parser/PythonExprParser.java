package net.kapitencraft.scripted.lang.compiler.python.parser;

import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.SwitchKey;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.ScriptedField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;
import static net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory.*;

public class PythonExprParser extends AbstractPythonParser {
    private final List<ClassReference> fallback = new ArrayList<>();
    protected GenericStack generics = new GenericStack();
    private int anonymousCounter = 0; //counts how many anonymous classes have been created inside the class, to give each a unique name

    public PythonExprParser(ErrorStorage errorStorage, SourceTree sourceSink, CompileSource source) {
        super(errorStorage, sourceSink, source);
    }

    protected ClassReference currentFallback() {
        if (fallback.isEmpty()) throw new IllegalArgumentException("no fallback applied");
        return fallback.getLast();
    }

    public void pushFallback(ClassReference fallback) {
        this.fallback.add(fallback);
    }

    public void popFallback() {
        if (this.fallback.isEmpty()) throw new IllegalStateException("fallback stack underflow");
        this.fallback.removeLast();
    }

    public Expr expression() {
        if (match(MATCH)) {
            return switchExpr();
        }
        if (match(IF)) {
            return when();
        }

        return castCheck();
    }

    public Expr literalOrReference() {
        if (match(AT)) {
            SourceReference reference = consumeVarType(generics);
            Token errorPoint = previous();
            if (match(BRACKET_O)) {
                parseAnnotationProperties(reference, errorPoint);
            }
        }
        if (match(PRIMITIVE)) {
            Expr.Literal expr = new Expr.Literal();
            expr.literal = previous();
            return expr;
        }
        ClassReference target = consumeVarType(generics).getReference();
        consumeDot();
        Token name = consumeIdentifier();

        Expr.StaticGet staticGet = new Expr.StaticGet();
        staticGet.target = target;
        staticGet.name = name;
        return staticGet;
    }

    //region annotation
    public Annotation parseAnnotation(AnnotationObj obj, VarTypeContainer varTypeContainer) {
        this.apply(obj.properties(), varTypeContainer);
        return parseAnnotationProperties(obj.type(), obj.type().getToken());
    }

    public Annotation parseAnnotationProperties(SourceReference typeRef, Token errorPoint) {
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
                    if (properties.containsKey(propertyName.lexeme()))
                        errorStorage.errorF(propertyName, "duplicate annotation property with name %s", propertyName.lexeme());
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
    //endregion

    private Expr when() {
        Expr expr = expression();
        consume(THEN, "'then' expected");
        Expr ifTrue = expression();
        consume(ELSE, "'else' expected");
        Expr ifFalse = expression();
        Expr.When when = new Expr.When();
        when.condition = expr;
        when.ifTrue = ifTrue;
        when.ifFalse = ifFalse;
        return when;
    }

    private Expr castCheck() {
        Expr expr = assignment();
        if (match(INSTANCEOF)) {
            ClassReference loxClass = consumeVarType(generics).getReference();
            Token patternVar = null;
            if (match(IDENTIFIER)) {
                patternVar = previous();
            }
            Expr.CastCheck castCheck = new Expr.CastCheck();
            castCheck.object = expr;
            castCheck.targetType = loxClass;
            castCheck.patternVarName = patternVar;
            return castCheck;
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(ASSIGN) || match(OPERATION_ASSIGN)) {
            Token assignKeyword = previous();
            Expr value = assignment();

            if (expr instanceof Expr.SingleIdentifier varRef) {
                Token name = varRef.name;

                Expr.IdentifierAssign assign = new Expr.IdentifierAssign();
                assign.name = name;
                assign.type = assignKeyword;
                assign.value = value;
                assign.fieldOwner = varRef.type;
                return assign;
            } else if (expr instanceof Expr.Get get) {
                Expr.Set set = new Expr.Set();
                set.object = get.object;
                set.name = get.name;
                set.value = value;
                set.assignType = assignKeyword;
                return set;
            } else if (expr instanceof Expr.ArrayGet arrayGet) {

                Expr.ArraySet arraySet = new Expr.ArraySet();
                arraySet.object = arrayGet.object;
                arraySet.index = arrayGet.index;
                arraySet.value = value;
                arraySet.assignType = assignKeyword;
                arraySet.componentType = arrayGet.componentType;
                return arraySet;
            } else if (expr instanceof Expr.StaticGet staticGet) {
                Expr.StaticSet staticSet = new Expr.StaticSet();
                staticSet.target = staticGet.target;
                staticSet.name = staticGet.name;
                staticSet.value = value;
                staticSet.assignType = assignKeyword;
                return staticSet;
            }

            error(assignKeyword, "Invalid assignment target.");
        }

        if (match(GROW, SHRINK)) {

            Token assign = previous();

            if (expr instanceof Expr.SingleIdentifier varRef) {
                Token name = varRef.name;
                Expr.IdentifierSpecialAssign specialAssign = new Expr.IdentifierSpecialAssign();
                specialAssign.name = name;
                specialAssign.assignType = assign;
                specialAssign.ordinal = varRef.ordinal;
                specialAssign.type = varRef.type;
                specialAssign.isStatic = varRef.isStatic;
                return specialAssign;
            }

            if (expr instanceof Expr.Get get) {
                Expr.SpecialSet specialSet = new Expr.SpecialSet();
                specialSet.object = get.object;
                specialSet.name = get.name;
                specialSet.assignType = assign;
                return specialSet;
            }

            if (expr instanceof Expr.ArrayGet arrayGet) {
                Expr.ArraySpecial special = new Expr.ArraySpecial();
                special.object = arrayGet.object;
                special.index = arrayGet.index;
                special.assignType = assign;
                return special;
            }
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            Expr.Logical logical = new Expr.Logical();
            logical.left = expr;
            logical.operator = operator;
            logical.right = right;
            expr = logical;
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND, XOR)) {
            Token operator = previous();
            Expr right = equality();
            Expr.Logical logical = new Expr.Logical();
            logical.left = expr;
            logical.operator = operator;
            logical.right = right;
            expr = logical;
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        //while?
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
                Expr.ComparisonChain chain = new Expr.ComparisonChain();
                chain.entries = values.toArray(Expr[]::new);
                chain.types = operators.toArray(Token[]::new);
                expr = chain;
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
        Expr.Binary binary = new Expr.Binary();
        binary.left = expr;
        binary.right = right;
        binary.operator = operator;
        expr = binary;
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

            Expr.Unary unary = new Expr.Unary();
            unary.operator = operator;
            unary.right = right;
            return unary;
        }

        return call();
    }

    private Expr switchExpr() {
        Token keyword = previous();
        consumeBracketOpen("switch");

        Expr provider = expression();

        consumeBracketClose("switch");

        consumeCurlyOpen("switch body");
        List<SwitchKey> params = new ArrayList<>();
        Expr def = null;

        while (!check(C_BRACKET_C)) {
            if (match(CASE)) {
                Token value = advance();
                consume(LAMBDA, "not a statement");
                Expr expr = expression();
                consumeEndOfArg();
                SwitchKey key;
                if (!value.type().isCategory(SWITCH_KEYWORD)) {
                    key = new SwitchKey.Illegal(value, expr);
                    error(value, "number, string or identifier expected");
                } else {
                    key = switch (value.type()) {
                        case NUM -> new SwitchKey.Number(value, expr);
                        case STR -> new SwitchKey.String(value, expr);
                        case IDENTIFIER -> new SwitchKey.Identifier(value, expr);
                        default -> throw new IllegalStateException("illegal value type");
                    };
                }
                params.add(key);
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
        Expr.Switch aSwitch = new Expr.Switch();
        aSwitch.provider = provider;
        aSwitch.params = params.toArray(SwitchKey[]::new);
        aSwitch.defaulted = def;
        aSwitch.keyword = keyword;
        return aSwitch;
    }

    private Expr staticAssign(ClassReference target, Token name) {
        Token type = previous();
        Expr value = expression();
        Expr.StaticSet set = new Expr.StaticSet();
        set.target = target;
        set.name = name;
        set.value = value;
        set.assignType = type;
        return set;
    }

    private Expr staticSpecialAssign(ClassReference target, Token name) {
        Expr.StaticSpecial expr = new Expr.StaticSpecial();
        expr.target = target;
        expr.name = name;
        expr.assignType = previous();
        expr.executor = target.get().getFieldType(name.lexeme());
        return expr;
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

    private Expr call() {
        Expr expr = primary();

        while (check(S_BRACKET_O, BRACKET_O, DOT)) {
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
                    Expr.Slice slice = new Expr.Slice();
                    slice.object = expr;
                    slice.start = index;
                    slice.end = end;
                    slice.interval = interval;
                    expr = slice;
                    continue;
                }
                consume(S_BRACKET_C, "']' expected");
                Expr.ArrayGet get = new Expr.ArrayGet();
                get.object = expr;
                get.index = index;
                expr = get;
            } else if (match(BRACKET_O)) {
                if (expr instanceof Expr.Get get)
                    expr = finishCall(get.name, null, get.object);
                else error(locFinder.find(expr), "obj expected");
            } else if (match(DOT)) {
                if (expr instanceof Expr.Literal && !check(IDENTIFIER)) continue;
                Token name = consume(IDENTIFIER, "Expect property name after '.'");
                Expr.Get get = new Expr.Get();
                get.object = expr;
                get.name = name;
                expr = get;
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

        this.panicMode = true;
        error(peek(), "Expected literal");
        return null;
    }

    private Expr primary() {
        if (match(NEW)) {
            SourceReference type = consumeVarTypeNoArray(generics);
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
                Expr.ArrayConstructor constructor = new Expr.ArrayConstructor();
                constructor.keyword = type.getToken();
                constructor.compoundType = type.getReference();
                constructor.size = size;
                constructor.obj = values;

                return constructor;
            }
            consumeBracketOpen("constructors");
            Expr[] args = args();
            consumeBracketClose("constructors");

            if (match(COLON) && match(LINE_FEED)) {
                indents++;
                PythonHolderParser hParser = new PythonHolderParser(this.errorStorage, this.sourceSink, this.source);
                if (type.get().isFinal()) {
                    error(previous(), "can not extend final class");
                }
                hParser.apply(getIndentedCode(this.indents), this.parser);
                String nameLiteral = String.valueOf(this.anonymousCounter++);
                String pck = this.currentFallback().pck();
                String outName = this.currentFallback().name() + "$" + nameLiteral;
                ClassReference typeTarget = VarTypeManager.getOrCreateClass(outName, pck);
                Token name = new Token(IDENTIFIER, outName, LiteralHolder.EMPTY, type.getToken().line(), type.getToken().lineStartIndex());
                SourceReference original = type;
                type = SourceReference.from(name, typeTarget);
                if (original.get().isInterface()) {
                    addInternal(
                            hParser.parseInterface(typeTarget, pck, name, null, null, null, List.of(original)),
                            this.errorStorage,
                            this.parser
                    );
                } else {
                    addInternal(
                            hParser.parseClass(typeTarget, null, null, null, pck, name, original, List.of()),
                            this.errorStorage,
                            this.parser
                    );
                }
                indents--;
            } else if (type.get().isAbstract()) {
                error(type.getToken(), "can not instantiate abstract class " + type.absoluteName());
            }

            ClassReference typeRef = type.getReference();

            Expr.Constructor expr = new Expr.Constructor();
            expr.keyword = type.getToken();
            expr.target = typeRef;
            expr.args = args;
            return expr;
        }

        if (match(PRIMITIVE)) {
            Expr.Literal literal = new Expr.Literal();
            literal.literal = previous();
            return literal;
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
                    ScriptedClass targetClass = superclass.get();
                    if (!targetClass.hasMethod(name.lexeme())) {
                        error(name, "unknown method '" + name.lexeme() + "'");
                        consumeBracketClose("arguments");
                        Expr.Call call = new Expr.Call();
                        call.declaring = superclass;
                        call.name = name;
                        call.args = arguments;
                        call.virtual = false;
                        return call;
                    }
                    consumeBracketClose("arguments");

                    Expr.Call call = new Expr.Call();

                    Expr.SingleIdentifier callee = new Expr.SingleIdentifier();
                    callee.name = Objects.requireNonNull(reference);
                    callee.ordinal = 0;
                    call.object = callee;
                    call.declaring = superclass;
                    call.name = name;
                    call.args = arguments;
                    call.virtual = false;
                    return call;
                }
            }
        }

        if (match(IDENTIFIER)) {
            Token previous = previous(); //the identifier just consumed
            if (currentFallback().exists()) { //check if the parser has a class fallback available
                ClassReference fallbackReference = currentFallback();
                ScriptedClass fallback = fallbackReference.get(); //get said fallback
                String name = previous.lexeme(); //get the literal of the identifier
                if (match(BRACKET_O)) { //check if there's an attempt to call a method from the fallback class
                    if (fallback.hasMethod(name)) {
                        Expr.SingleIdentifier ref = new Expr.SingleIdentifier();
                        ref.name = Token.createNative("this");
                        ref.ordinal = 0;
                        return finishCall(previous, fallbackReference, ref);
                    }
                } else {
                    ScriptedClass declaring = fallback.getFieldDeclaring(name);
                    if (declaring != null) {
                        ScriptedField field = declaring.getFields().get(name);
                        Expr.SingleIdentifier identifier = new Expr.SingleIdentifier();
                        identifier.type = fallbackReference;
                        identifier.name = previous;
                        identifier.isStatic = field.isStatic();
                        return identifier;
                    }
                }
            }
            current--; //un-consume the identifier for the statics to take over
            Optional<SourceReference> reference = tryConsumeVarType(generics);
            if (reference.isPresent()) {
                ClassReference target = reference.get().getReference();
                if (match(IDENTIFIER)) {
                    error(previous, "Declaration not allowed here");
                    panicMode = true;
                    return varRef(previous, (byte) -1);
                }
                consumeDot();
                return parseObjAttributes(target);
            }
            advance();
            return varRef(
                    previous,
                    (byte) -1
            );
        }

        if (match(THIS)) return varRef(
                previous(),
                (byte) 0
        );

        if (match(BRACKET_O)) {
            Expr expr = expression();
            consumeBracketClose("expression");
            return expr; //the grouping expression mustn't exist as a real AST entry
        }

        error(peek(), "Illegal start of expression");
        this.panicMode = true;
        return varRef(Token.createNative("<unidentified>"), (byte) -1);
    }

    private void consumeDot() {
        consume(DOT, "'.' expected");
    }

    protected @NotNull Expr parseObjAttributes(ClassReference target) {
        Token name = consumeIdentifier();
        if (match(BRACKET_O)) return finishCall(name, target, null);
        if (match(ASSIGN) || match(OPERATION_ASSIGN)) return staticAssign(target, name);
        if (match(GROW, SHRINK)) return staticSpecialAssign(target, name);
        Expr.StaticGet get = new Expr.StaticGet();
        get.target = target;
        get.name = name;
        return get;
    }

    private Expr varRef(Token previous, byte ordinal) {
        Expr.SingleIdentifier ref = new Expr.SingleIdentifier();
        ref.name = previous;
        ref.ordinal = ordinal;
        return ref;
    }

    private Expr finishCall(Token name, @Nullable ClassReference objType, @Nullable Expr obj) {
        Expr[] arguments = args();

        consumeBracketClose("arguments");

        Expr.Call call = new Expr.Call();
        call.name = name;
        call.object = obj;
        call.declaring = objType;
        call.args = arguments;
        return call;
    }
}
