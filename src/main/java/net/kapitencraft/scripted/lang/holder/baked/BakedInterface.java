package net.kapitencraft.scripted.lang.holder.baked;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.holder.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.scripted.lang.oop.field.CompileField;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;

import java.util.HashMap;
import java.util.Map;

public record BakedInterface(Compiler.ErrorStorage logger, Holder.Generics generics, ClassReference target,
                             Pair<Token, CompileCallable>[] methods,
                             Map<String, CompileField> staticFields, ClassReference[] interfaces,
                             Token name, String pck,
                             Annotation[] annotations
) implements Compiler.ClassBuilder {

    @Override
    public CacheableClass build() {

        Map<String, DataMethodContainer.Builder> methods = new HashMap<>();
        for (Pair<Token, CompileCallable> method : this.methods()) {
            methods.putIfAbsent(method.getFirst().lexeme(), new DataMethodContainer.Builder(this.name()));
            DataMethodContainer.Builder builder = methods.get(method.getFirst().lexeme());
            builder.addMethod(logger, method.getSecond(), method.getFirst());
        }

        return new CompileClass(
                DataMethodContainer.bakeBuilders(methods),
                staticFields(),
                VarTypeManager.OBJECT,
                name().lexeme(),
                pck(),
                interfaces(),
                Modifiers.INTERFACE,
                annotations()
        );
    }

    @Override
    public ClassReference superclass() {
        return null;
    }
}