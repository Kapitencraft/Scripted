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
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.baked.BakedInterface;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.oop.attribute.FieldHolder;
import net.kapitencraft.scripted.lang.holder.oop.attribute.MethodHolder;
import net.kapitencraft.scripted.lang.holder.oop.attribute.OperationHolder;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.clazz.skeleton.SkeletonInterface;
import net.kapitencraft.scripted.lang.oop.field.CompileField;
import net.kapitencraft.scripted.lang.oop.field.SkeletonField;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.SkeletonMethod;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;

import java.util.*;

public record InterfaceHolder(ClassReference target, short modifiers,
                              AnnotationObj[] annotations, Generics generics, String pck, Token name,
                              SourceReference[] interfaces,
                              OperationHolder[] methodHolders,
                              FieldHolder[] fieldHolders
) implements ClassConstructor {

    public BakedInterface construct(StmtParser javaStmtParser, VarTypeContainer parser, ErrorStorage logger) {
        Map<String, CompileField> staticFields = new HashMap<>();
        List<Stmt> statics = new ArrayList<>();
        for (FieldHolder fieldHolder : fieldHolders()) {
            Expr initializer = null;
            if (fieldHolder.body() != null) {
                initializer = getFieldBody(javaStmtParser, parser, fieldHolder, statics);
            }
            Annotation[] annotations = javaStmtParser.parseAnnotations(fieldHolder.annotations(), parser);

            short mods = fieldHolder.modifiers();
            CompileField fieldDecl = new CompileField(fieldHolder.name(), initializer, fieldHolder.type().getReference(), mods, annotations);
            if (Modifiers.isStatic(fieldHolder.modifiers())) staticFields.put(fieldHolder.name().lexeme(), fieldDecl);
            else logger.error(fieldHolder.name(), "fields on interfaces must be static");
        }

        List<Pair<Token, CompileCallable>> methods = new ArrayList<>();
        for (OperationHolder methodHolder : this.methodHolders()) {
            List<Stmt> body = null;
            if (!Modifiers.isAbstract(methodHolder.modifiers())) {
                javaStmtParser.apply(methodHolder.body(), parser);
                if (Modifiers.isStatic(methodHolder.modifiers()))
                    javaStmtParser.applyStaticMethod(methodHolder.retType(), methodHolder.generics());
                else
                    javaStmtParser.applyMethod(methodHolder.retType(), methodHolder.generics());
                body = javaStmtParser.parse();
                javaStmtParser.popMethod(methodHolder.closeBracket());
            }
            Annotation[] annotations = javaStmtParser.parseAnnotations(methodHolder.annotations(), parser);

            CompileCallable methodDecl = new CompileCallable(methodHolder.retType(), methodHolder.extractParams(), methodHolder.extractThrown(), body, methodHolder.modifiers(), annotations);
            methods.add(Pair.of(methodHolder.name(), methodDecl));
        }

        if (!statics.isEmpty()) {
            ClassConstructor.addClinit(statics, methods);
        }

        Annotation[] annotations = javaStmtParser.parseAnnotations(this.annotations(), parser);

        return new BakedInterface(
                logger, generics, target,
                methods.toArray(new Pair[0]),
                staticFields,
                extractInterfaces(),
                name,
                pck,
                annotations
        );
    }

    private ClassReference[] extractInterfaces() {
        return Arrays.stream(interfaces).map(SourceReference::getReference).toArray(ClassReference[]::new);
    }

    public ScriptedClass createSkeleton(ErrorStorage logger) {

        //fields
        ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
        for (FieldHolder fieldHolder : this.fieldHolders()) {
            if (Modifiers.isStatic(fieldHolder.modifiers()))
                staticFields.put(fieldHolder.name().lexeme(), new SkeletonField(fieldHolder.type().getReference(), fieldHolder.modifiers()));
            else {
                logger.error(fieldHolder.name(), "fields inside Interfaces must always be static");
            }
        }

        //methods
        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        Map<String, DataMethodContainer.Builder> staticMethods = new HashMap<>();
        for (OperationHolder methodHolder : this.methodHolders()) {
            if (Modifiers.isStatic(methodHolder.modifiers())) {
                staticMethods.putIfAbsent(methodHolder.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                DataMethodContainer.Builder builder = staticMethods.get(methodHolder.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(methodHolder), methodHolder.name());
            } else {
                methods.putIfAbsent(methodHolder.name().lexeme(), new DataMethodContainer.Builder(this.name()));
                DataMethodContainer.Builder builder = methods.get(methodHolder.name().lexeme());
                builder.addMethod(logger, SkeletonMethod.create(methodHolder), methodHolder.name());
            }
        }

        return new SkeletonInterface(
                this.name().lexeme(),
                this.pck(),
                this.extractInterfacesToString(),
                staticFields.build(),
                this.generics(),
                DataMethodContainer.bakeBuilders(methods)
        );
    }

    private String[] extractInterfacesToString() {
        return Arrays.stream(interfaces).map(SourceReference::get).map(VarTypeManager::getClassName).toArray(String[]::new);
    }

    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        Validatable.validateNullable(interfaces, logger);
        for (OperationHolder methodHolder : methodHolders) methodHolder.validate(logger);
        Validatable.validateNullable(fieldHolders, logger);
    }
}
