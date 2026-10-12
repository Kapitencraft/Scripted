package net.kapitencraft.scripted.lang.holder.oop.clazz;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.analyser.SemanticAnalyser;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.baked.BakedClass;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.oop.attribute.*;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generic;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.clazz.skeleton.SkeletonClass;
import net.kapitencraft.scripted.lang.oop.field.CompileField;
import net.kapitencraft.scripted.lang.oop.field.SkeletonField;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.SkeletonMethod;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;

import java.util.*;

public record EnumHolder(ClassReference target, short modifiers,
                         AnnotationObj[] annotations, Generics generics, String pck, Token name,
                         SourceReference[] interfaces,
                         OperationHolder[] constructorHolders,
                         OperationHolder[] methodHolders,
                         FieldHolder[] fieldHolders,
                         EnumConstantHolder[] enumConstantHolders) implements ClassConstructor {
    /**
     * construct this enum to a baked class
     */
    public BakedClass construct(StmtParser stmtParser, VarTypeContainer parser, ErrorStorage logger) {

        List<Stmt> statics = new ArrayList<>();

        Map<Token, CompileField> fields = new HashMap<>();

        for (EnumConstantHolder decl : enumConstantHolders()) {
            Expr[] args;
            if (decl.arguments().length == 0) {
                args = new Expr[]{
                        literal(decl.name().lexemeAsLiteral()), //name
                        literal(new Token(TokenType.STR, String.valueOf(decl.ordinal()), new LiteralHolder(decl.ordinal(), VarTypeManager.INTEGER), decl.name().line(), decl.name().lineStartIndex()))
                };
                stmtParser.apply(new Token[0], parser);
            } else {
                stmtParser.apply(decl.arguments(), parser);
                args = prefixEnumConstructorCallArgs(stmtParser.args(), decl);
            }

            Stmt.Expression expression = new Stmt.Expression();
            {
                Expr.Constructor constructor = new Expr.Constructor();
                constructor.keyword = decl.name();
                constructor.target = target;
                constructor.args = args;
                Expr.StaticSet staticSet = new Expr.StaticSet();
                staticSet.target = target;
                staticSet.name = decl.name();
                staticSet.value = constructor;
                staticSet.assignType = new Token(TokenType.ASSIGN, "=", LiteralHolder.EMPTY, -1, 0);
                staticSet.executor = target;
                expression.expression = staticSet;
            }
            statics.add(expression);
        }

        //region $VALUES
        int length = enumConstantHolders().length;
        Token keyword = this.name.asIdentifier("$VALUES");
        Expr[] constants = new Expr[length];
        for (int i = 0; i < enumConstantHolders.length; i++) {
            EnumConstantHolder constant = enumConstantHolders[i];
            Expr.StaticGet get = new Expr.StaticGet();
            get.target = target;
            get.name = constant.name();
            constants[i] = get; //getting statics to store into the array
        }
        Stmt.Expression stmt = new Stmt.Expression();
        Expr.StaticSet valuesInit = new Expr.StaticSet();
        {
            Expr.ArrayConstructor constructor = new Expr.ArrayConstructor();
            constructor.keyword = this.name.asIdentifier("new");
            constructor.compoundType = target;
            constructor.size = literal(new Token(
                    TokenType.NUM,
                    String.valueOf(length),
                    new LiteralHolder(length, VarTypeManager.INTEGER),
                    this.name.line(),
                    this.name.lineStartIndex()
            ));
            constructor.obj = constants;
            valuesInit.target = target;
            valuesInit.value = constructor;
            valuesInit.assignType = new Token(TokenType.ASSIGN, "=", LiteralHolder.EMPTY, this.name.line(), this.name.lineStartIndex());
            valuesInit.executor = target;
            valuesInit.name = keyword;
            stmt.expression = valuesInit;
        }
        fields.put(keyword, new CompileField(keyword, valuesInit, target.array(), Modifiers.pack(true, true, false), new Annotation[0]));
        statics.add(stmt);
        //endregion

        List<String> finalFields = new ArrayList<>();
        List<CompileField> initializedFields = new ArrayList<>(); //store initialized Fields to add them in each constructor
        for (FieldHolder fieldHolder : fieldHolders()) {
            short mods = fieldHolder.modifiers();
            Expr initializer = null;
            if (fieldHolder.body() != null) {
                initializer = getFieldBody(stmtParser, parser, fieldHolder, statics);
            } else if (Modifiers.isFinal(mods)) finalFields.add(fieldHolder.name().lexeme());
            Annotation[] annotations = stmtParser.parseAnnotations(fieldHolder.annotations(), parser);

            CompileField fieldDecl = new CompileField(fieldHolder.name(), initializer, fieldHolder.type().getReference(), mods, annotations);
            fields.put(fieldHolder.name(), fieldDecl);
            if (!Modifiers.isStatic(mods) && initializer != null) {
                initializedFields.add(fieldDecl);
            }
        }

        List<Pair<Token, CompileCallable>> methods = new ArrayList<>();
        for (OperationHolder methodHolder : this.methodHolders()) {
            List<Stmt> body = null;
            if (!Modifiers.isAbstract(methodHolder.modifiers())) {
                stmtParser.applyOperation(methodHolder, parser);
                body = stmtParser.parse();
                stmtParser.popMethod(methodHolder.closeBracket());
            }

            Annotation[] annotations = stmtParser.parseAnnotations(methodHolder.annotations(), parser);

            CompileCallable methodDecl = new CompileCallable(
                    methodHolder.retType(),
                    methodHolder.extractParams(),
                    methodHolder.extractThrown(),
                    body, methodHolder.modifiers(), annotations
            );
            methods.add(Pair.of(methodHolder.name(), methodDecl));
        }

        Stmt.Return aReturn = new Stmt.Return();
        aReturn.keyword = this.name.asIdentifier("return");
        statics.add(aReturn);
        methods.add(Pair.of(this.name.asIdentifier("<clinit>"),
                new CompileCallable(VarTypeManager.VOID.reference(),
                        List.of(),
                        new ClassReference[0],
                        statics,
                        Modifiers.pack(true, true, false),
                        new Annotation[0]
                )
        ));

        //region #values
        Expr.StaticGet get = new Expr.StaticGet();
        get.target = target;
        get.name = keyword;
        Stmt.Return aReturn1 = new Stmt.Return();
        aReturn1.keyword = this.name.asIdentifier("return");
        aReturn1.value = get;
        methods.add(Pair.of(this.name.asIdentifier("values"),
                new CompileCallable(
                        target.array(),
                        List.of(),
                        new ClassReference[0],
                        List.of(aReturn1),
                        Modifiers.pack(false, true, false),
                        new Annotation[0]
                )
        ));
        //endregion

        List<Pair<Token, CompileCallable>> constructors = new ArrayList<>();
        for (OperationHolder enumConstructorHolder : this.constructorHolders()) {
            stmtParser.applyOperation(enumConstructorHolder, parser);
            List<Stmt> original = stmtParser.parse();
            prefixEnumConstructorCall(original);
            this.checkFinalsPopulated(original, finalFields);
            this.prefixFieldInitializers(original, initializedFields);
            Annotation[] annotations = stmtParser.parseAnnotations(enumConstructorHolder.annotations(), parser);

            CompileCallable constDecl = new CompileCallable(VarTypeManager.VOID.reference(), enumConstructorHolder.extractParams(), enumConstructorHolder.extractThrown(), original, (short) 0, annotations);
            stmtParser.popMethod(enumConstructorHolder.closeBracket());
            constructors.add(Pair.of(enumConstructorHolder.name(), constDecl));
        }
        if (constructors.isEmpty()) {
            Expr.Call body = new Expr.Call();
            body.object = varRef(Token.createNative("super"), (byte) 0);
            body.args = new Expr[] {
                    varRef(Token.createNative("$name"), (byte) 1),
                    varRef(Token.createNative("$ordinal"), (byte) 2)
            };
            body.virtual = false;
            body.declaring = VarTypeManager.ENUM;
            body.name = Token.createNative("<init>");
            Stmt.Expression stmt1 = new Stmt.Expression();
            stmt1.expression = body;
            Stmt.Return ret = new Stmt.Return();
            ret.keyword = Token.createNative("return");
            constructors.add(Pair.of(this.name, new CompileCallable(VarTypeManager.VOID.reference(), List.of(
                    Pair.of(VarTypeManager.STRING, "$name"),
                    Pair.of(VarTypeManager.INTEGER.reference(), "$ordinal")
            ), new ClassReference[0], List.of(stmt1, ret), (short) 0, new Annotation[0])));
        }

        return new BakedClass(
                logger,
                new Generics(new Generic[0]),
                target(),
                methods.toArray(Pair[]::new),
                constructors.toArray(Pair[]::new),
                fields,
                VarTypeManager.ENUM,
                name(),
                pck(),
                extractInterfaces(),
                Modifiers.pack(true, true, false),
                parseAnnotations(stmtParser, parser)
        );
    }

    public ScriptedClass createSkeleton(ErrorStorage logger) {

        //fields
        ImmutableMap.Builder<String, SkeletonField> fields = new ImmutableMap.Builder<>();
        List<Token> finalFields = new ArrayList<>();
        for (FieldHolder fieldHolder : this.fieldHolders()) {
            SkeletonField skeletonField = new SkeletonField(fieldHolder.type().getReference(), fieldHolder.modifiers());
            fields.put(fieldHolder.name().lexeme(), skeletonField);
            if (skeletonField.isFinal() && fieldHolder.body() == null) //add non-defaulted final fields to extra list to check constructors init
                finalFields.add(fieldHolder.name());
        }

        if (this.enumConstantHolders() != null) {
            for (EnumConstantHolder constant : this.enumConstantHolders()) {
                SkeletonField field = new SkeletonField(target, Modifiers.pack(true, true, false));
                fields.put(constant.name().lexeme(), field);
            }

            fields.put("$VALUES", new SkeletonField(target.array(), Modifiers.pack(true, true, false)));
        }

        //methods
        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (OperationHolder methodHolder : this.methodHolders()) {
            methods.putIfAbsent(methodHolder.name().lexeme(), new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = methods.get(methodHolder.name().lexeme());
            builder.addMethod(logger, SkeletonMethod.create(methodHolder), methodHolder.name());
        }
        methods.computeIfAbsent("values", s -> new DataMethodContainer.Builder(this.name()))
                .addMethod(logger, new SkeletonMethod(new ClassReference[0], new ClassReference[0], target.array(), Modifiers.pack(false, true, false)), Token.createNative("values"));

        //constructors
        methods.putIfAbsent("<init>", new DataMethodContainer.Builder(this.name()));
        DataMethodContainer.Builder builder = methods.get("<init>");
        for (OperationHolder constructorHolder : this.constructorHolders()) {
            builder.addMethod(logger, SkeletonMethod.create(constructorHolder, this.target), constructorHolder.name());
        }
        if (builder.isEmpty()) {
            builder.addMethod(logger, SkeletonMethod.createNative(new ClassReference[]{
                    VarTypeManager.STRING,
                    VarTypeManager.INTEGER.reference()
            }, new ClassReference[0], VarTypeManager.VOID.reference(), (short) 0), Token.createNative("<init>"));
        }

        return new SkeletonClass(
                this.generics,
                this.name().lexeme(),
                this.pck(), VarTypeManager.getClassName(VarTypeManager.ENUM),
                fields.build(),
                this.enumConstantHolders(),
                DataMethodContainer.bakeBuilders(methods),
                this.modifiers,
                Arrays.stream(this.interfaces).map(SourceReference::getReference).map(VarTypeManager::getClassName).toArray(String[]::new)
        );
    }

    private ClassReference[] extractInterfaces() {
        return Arrays.stream(interfaces).map(SourceReference::getReference).toArray(ClassReference[]::new);
    }

    private static Expr literal(Token token) {
        Expr.Literal literal = new Expr.Literal();
        literal.literal = token;
        return literal;
    }

    /**
     * @param og             the original's constructor's body code
     * @param fieldsWithInit the fields that are to be initialized
     */
    public void prefixFieldInitializers(List<Stmt> og, List<CompileField> fieldsWithInit) {
        for (int i = fieldsWithInit.size() - 1; i > 0; i--) {
            CompileField field = fieldsWithInit.get(i);
            Token fieldName = field.getName();
            Stmt.Expression expression = new Stmt.Expression();
            {
                Expr.Set set = new Expr.Set();
                set.object = varRef(fieldName, (byte) 0);
                set.name = fieldName;
                set.value = field.getInit();
                set.assignType = new Token(TokenType.ASSIGN, "=", LiteralHolder.EMPTY, fieldName.line(), fieldName.lineStartIndex());
                set.executor = field.getType();
                expression.expression = set;
            }
            og.addFirst(expression);
        }
    }

    /**
     * @param args the original constructor args
     * @param decl the enum declaration to be constructed
     * @return the new args, adding constant name and ordinal
     */
    //region enum constructor prefix
    private Expr[] prefixEnumConstructorCallArgs(Expr[] args, EnumConstantHolder decl) {
        Expr[] out = new Expr[args.length + 2];
        out[0] = literal(decl.name().lexemeAsLiteral()); //name
        out[1] = literal(new Token(
                TokenType.STR,
                String.valueOf(decl.ordinal()),
                new LiteralHolder(decl.ordinal(), VarTypeManager.INTEGER),
                decl.name().line(),
                decl.name().lineStartIndex()
        ));
        System.arraycopy(args, 0, out, 2, args.length);
        return out;
    }

    //issue: native constructor can not be called because obj isn't native
    //yiipeeeee

    /**
     * @param original the original code of the constructor
     * @return the new code of the constructor, adding a `super` call to {@code Enum;<init>}
     */
    private void prefixEnumConstructorCall(List<Stmt> original) {
        Stmt.Expression expression = new Stmt.Expression();
        {
            Expr.Call call = new Expr.Call();
            call.object = varRef(Token.createNative("super"), (byte) 0);
            call.name = this.name.asIdentifier("<init>");
            call.args = new Expr[]{
                    varRef(Token.createNative("$name"), (byte) 1),
                    varRef(Token.createNative("$ordinal"), (byte) 2)
            };
            call.declaring = VarTypeManager.ENUM;
            call.retType = VarTypeManager.VOID.reference();
            call.signature = "Lscripted/lang/Enum;<init>(Lscripted/lang/String;I)";
            expression.expression = call;
        }
        original.addFirst(expression);
    }

    private Expr varRef(Token name, byte ordinal) {
        Expr.SingleIdentifier ref = new Expr.SingleIdentifier();
        ref.name = Objects.requireNonNull(name);
        ref.ordinal = ordinal;
        return ref;
    }
    //endregion

    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        Validatable.validateNullable(interfaces, logger);
        Validatable.validateNullable(constructorHolders, logger);
        for (OperationHolder methodHolder : methodHolders) methodHolder.validate(logger);
        Validatable.validateNullable(fieldHolders, logger);
    }
}
