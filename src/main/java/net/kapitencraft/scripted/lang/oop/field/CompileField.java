package net.kapitencraft.scripted.lang.oop.field;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.CacheBuilder;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

public class CompileField implements ScriptedField {
    private final Token name;
    private final Expr init;
    private final ClassReference type;
    private final short modifiers;
    private final Annotation[] annotations;

    public CompileField(Token name, Expr init, ClassReference type, short modifiers, Annotation[] annotations) {
        this.name = name;
        this.init = init;
        this.type = type;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    public JsonElement cache(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("type", VarTypeManager.getClassName(type));
        object.addProperty("modifiers", this.modifiers);
        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));
        return object;
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(this.modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(this.modifiers);
    }

    @Override
    public short modifiers() {
        return modifiers;
    }

    public ClassReference getType() {
        return type;
    }

    public Token getName() {
        return name;
    }

    public Expr getInit() {
        return init;
    }
}
