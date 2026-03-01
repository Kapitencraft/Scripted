package net.kapitencraft.scripted.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.annotation.CompileAnnotationCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.run.VarTypeManager;

import java.util.Map;

public record BakedAnnotation(
        ClassReference target,
        Token name, String pck,
        Map<String, Holder.Class.MethodWrapper> methodWrappers,
        Annotation[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CompileClass build() {

        ImmutableMap.Builder<String, DataMethodContainer> builder = new ImmutableMap.Builder<>();
        methodWrappers.forEach((string, wrapper) -> builder.put(string, new DataMethodContainer(new ScriptedCallable[]{new CompileAnnotationCallable(wrapper.retType(), wrapper.val(), wrapper.annotations())})));

        return new CompileClass(
                builder.build(), Map.of(), VarTypeManager.OBJECT,
                this.name().lexeme(),
                this.pck(), new ClassReference[0], Modifiers.ANNOTATION, this.annotations());
    }

    @Override
    public ClassReference superclass() {
        return null;
    }

    @Override
    public Pair<Token, CompileCallable>[] methods() {
        return new Pair[0];
    }

    @Override
    public ClassReference[] interfaces() {
        return new ClassReference[0];
    }
}
