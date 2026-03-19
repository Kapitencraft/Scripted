package net.kapitencraft.scripted.lang.holder.baked;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.holder.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.scripted.lang.oop.field.CompileField;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BakedClass(
        Compiler.ErrorStorage logger,
        Holder.Generics generics,
        ClassReference target,
        Pair<Token, CompileCallable>[] methods,
        Pair<Token, CompileCallable>[] constructors,
        Map<Token, CompileField> fields,
        ClassReference superclass, Token name, String pck,
        ClassReference[] interfaces,
        short modifiers,
        Annotation[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CompileClass build() {
        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Pair<Token, CompileCallable> method : this.methods()) {
            methods.putIfAbsent(method.getFirst().lexeme(), new DataMethodContainer.Builder(this.name()));
            methods.get(method.getFirst().lexeme()).addMethod(logger, method.getSecond(), method.getFirst());
        }

        List<Token> finalFields = new ArrayList<>();
        fields.forEach((name, field) -> {
            //if (field.isFinal() && !field.hasInit()) {
            //    finalFields.add(name);
            //}
        });

        for (Pair<Token, CompileCallable> method : this.constructors()) {
            methods.putIfAbsent("<init>", new DataMethodContainer.Builder(this.name()));
            methods.get("<init>").addMethod(logger, method.getSecond(), method.getFirst());
        }

        return new CompileClass(
                DataMethodContainer.bakeBuilders(methods),
                create(this.fields()),
                this.superclass(),
                this.name().lexeme(),
                this.pck(),
                this.interfaces(),
                this.modifiers(),
                this.annotations()
        );
    }

    public static Map<String, CompileField> create(Map<Token, CompileField> fields) {
        ImmutableMap.Builder<String, CompileField> builder = new ImmutableMap.Builder<>();
        fields.forEach((token, generatedField) -> builder.put(token.lexeme(), generatedField));
        return builder.build();
    }
}
