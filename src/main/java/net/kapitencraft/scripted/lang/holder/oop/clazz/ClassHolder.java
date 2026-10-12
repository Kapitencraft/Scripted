package net.kapitencraft.scripted.lang.holder.oop.clazz;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.baked.BakedClass;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.oop.attribute.EnumConstantHolder;
import net.kapitencraft.scripted.lang.holder.oop.attribute.FieldHolder;
import net.kapitencraft.scripted.lang.holder.oop.attribute.OperationHolder;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.clazz.skeleton.SkeletonClass;
import net.kapitencraft.scripted.lang.oop.field.CompileField;
import net.kapitencraft.scripted.lang.oop.field.SkeletonField;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.SkeletonMethod;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;

import java.util.*;

public record ClassHolder(ClassReference target, short modifiers,
                          AnnotationObj[] annotations, Generics generics, String pck, Token name,
                          SourceReference parent,
                          SourceReference[] interfaces,
                          OperationHolder[] constructorHolders,
                          OperationHolder[] methodHolders,
                          FieldHolder[] fieldHolders) implements ClassConstructor {

    @Override
    public Compiler.ClassBuilder construct(StmtParser stmtParser, VarTypeContainer parser, ErrorStorage logger) {
        Map<Token, CompileField> fields = new HashMap<>();
        List<Stmt> statics = new ArrayList<>();
        for (FieldHolder fieldHolder : fieldHolders()) {
            Expr initializer = null;
            if (fieldHolder.body() != null) {
                initializer = getFieldBody(stmtParser, parser, fieldHolder, statics);
            }
            Annotation[] annotations = stmtParser.parseAnnotations(fieldHolder.annotations(), parser);

            short mods = fieldHolder.modifiers();
            CompileField fieldDecl = new CompileField(fieldHolder.name(), initializer, fieldHolder.type().getReference(), mods, annotations);
            fields.put(fieldHolder.name(), fieldDecl);
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

            CompileCallable methodDecl = new CompileCallable(methodHolder.retType(), methodHolder.extractParams(), methodHolder.extractThrown(), body, methodHolder.modifiers(), annotations);
            methods.add(Pair.of(methodHolder.name(), methodDecl));
        }

        if (!statics.isEmpty()) {
            ClassConstructor.addClinit(statics, methods);
        }

        List<Pair<Token, CompileCallable>> constructors = new ArrayList<>();
        for (OperationHolder constructorHolder : this.constructorHolders()) {
            stmtParser.applyOperation(constructorHolder, parser);
            List<Stmt> body = stmtParser.parse();
            Annotation[] annotations = stmtParser.parseAnnotations(constructorHolder.annotations(), parser);

            CompileCallable constDecl = new CompileCallable(VarTypeManager.VOID.reference(), constructorHolder.extractParams(), constructorHolder.extractThrown(), body, (short) 0, annotations);
            stmtParser.popMethod(constructorHolder.closeBracket());
            constructors.add(Pair.of(constructorHolder.name(), constDecl));
        }

        Annotation[] annotations = stmtParser.parseAnnotations(this.annotations, parser);

        return new BakedClass(
                logger,
                generics,
                this.target(),
                methods.toArray(new Pair[0]),
                constructors.toArray(new Pair[0]),
                fields,
                this.parent.getReference(),
                this.name(),
                this.pck(),
                this.extractInterfaces(),
                this.modifiers,
                annotations
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
        for (OperationHolder constructorHolder : this.constructorHolders()) {
            methods.putIfAbsent("<init>", new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = methods.get("<init>");
            builder.addMethod(logger, SkeletonMethod.create(constructorHolder, this.target), constructorHolder.name());
        }

        return new SkeletonClass(
                this.generics,
                this.name().lexeme(),
                this.pck(), VarTypeManager.getClassName(this.parent.getReference()),
                fields.build(),
                new EnumConstantHolder[0],
                DataMethodContainer.bakeBuilders(methods),
                this.modifiers,
                Arrays.stream(this.interfaces).map(SourceReference::getReference).map(VarTypeManager::getClassName).toArray(String[]::new)
        );
    }

    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        if (parent != null) parent.validate(logger);
        Validatable.validateNullable(interfaces, logger);
        Validatable.validateNullable(constructorHolders, logger);
        for (OperationHolder methodHolder : methodHolders) methodHolder.validate(logger);
        Validatable.validateNullable(fieldHolders, logger);
    }

    private ClassReference[] extractInterfaces() {
        return Arrays.stream(interfaces).map(SourceReference::getReference).toArray(ClassReference[]::new);
    }
}