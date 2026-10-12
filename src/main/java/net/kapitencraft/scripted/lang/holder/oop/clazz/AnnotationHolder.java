package net.kapitencraft.scripted.lang.holder.oop.clazz;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.analyser.SemanticAnalyser;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.baked.BakedAnnotation;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.oop.attribute.MethodHolder;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.clazz.skeleton.SkeletonAnnotation;
import net.kapitencraft.scripted.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.scripted.lang.oop.method.annotation.SkeletonAnnotationMethod;
import org.jetbrains.annotations.Nullable;

public record AnnotationHolder(ClassReference target, short modifiers,
                               AnnotationObj[] annotations, Generics generics, String pck, Token name,
                               MethodHolder[] methodHolders) implements ClassConstructor {

    public BakedAnnotation construct(StmtParser javaStmtParser, VarTypeContainer parser, ErrorStorage logger) {
        ImmutableMap.Builder<String, MethodWrapper> methods = new ImmutableMap.Builder<>();
        for (MethodHolder methodHolder : methodHolders()) {
            Expr val = null;
            if (!Modifiers.isAbstract(methodHolder.modifiers())) {
                javaStmtParser.apply(methodHolder.body(), parser);
                val = javaStmtParser.literalOrReference();
            }
            Annotation[] annotations = javaStmtParser.parseAnnotations(methodHolder.annotations(), parser);

            methods.put(methodHolder.name().lexeme(), new MethodWrapper(val, methodHolder.returnType().getReference(), annotations, methodHolder.modifiers()));
        }

        return new BakedAnnotation(
                this.target(),
                this.name(),
                this.pck(),
                methods.build(),
                parseAnnotations(javaStmtParser, parser)
        );
    }

    //annotations are not available at skeleton construction
    public ScriptedClass createSkeleton(ErrorStorage logger) {

        ImmutableMap.Builder<String, AnnotationCallable> methods = new ImmutableMap.Builder<>();
        for (MethodHolder methodHolder : methodHolders()) {
            methods.put(methodHolder.name().lexeme(), new SkeletonAnnotationMethod(methodHolder.returnType().getReference(), methodHolder.body().length > 0));
        }

        return new SkeletonAnnotation(
                this.name().lexeme(),
                this.pck(),
                methods.build()
        );
    }

    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        for (MethodHolder methodHolder : methodHolders) methodHolder.validate(logger);
    }

    public record MethodWrapper(@Nullable Expr val, ClassReference retType, Annotation[] annotations,
                                short modifiers) implements ScriptedCallable {

        @Override
        public ClassReference[] argTypes() {
            return new ClassReference[0];
        }

        @Override
        public Object call(Object[] arguments) {
            throw new IllegalAccessError("can not call method wrapper!");
        }

        @Override
        public boolean isAbstract() {
            return val == null;
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public boolean isStatic() {
            return Modifiers.isStatic(this.modifiers);
        }

        @Override
        public ClassReference[] thrown() {
            return new ClassReference[0];
        }
    }
}
