package net.kapitencraft.scripted.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.bytecode.storage.Chunk;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.CacheBuilder;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.run.VarTypeManager;

import java.util.List;

public class CompileCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<? extends Pair<? extends ClassReference, String>> params;
    private final Stmt[] body;
    private final short modifiers;
    private final Annotation[] annotations;

    public CompileCallable(ClassReference retType, List<? extends Pair<? extends ClassReference, String>> params, Stmt[] body, short modifiers, Annotation[] annotations) {
        this.retType = retType;
        this.params = params;
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("retType", VarTypeManager.getClassName(retType.get()));
        {
            JsonArray array = new JsonArray();
            params.stream().map(Pair::getFirst).map(ClassReference::get).map(VarTypeManager::getClassName).forEach(array::add);
            object.add("params", array);
        }
        if (!isAbstract()) {
            Chunk.Builder chunk = builder.setup();
            int rIndex = 0;
            if (!isStatic()) {
                chunk.addLocal(0, 0, VarTypeManager.VOID.reference(), "this");
                rIndex++;
            }
            for (int i = 0; i < this.params.size(); i++) {
                Pair<? extends ClassReference, String> param = this.params.get(i);
                chunk.addLocal(0, rIndex + i, param.getFirst(), param.getSecond());
            }
            for (Stmt compileStmt : body) {
                builder.cache(compileStmt);
            }
            object.add("body", chunk.build().save());
        }
        if (this.modifiers != 0) object.addProperty("modifiers", this.modifiers);

        object.add("annotations", builder.cacheAnnotations(this.annotations));
        return object;
    }

    @Override
    public Object call(Object[] arguments) {
        throw new IllegalAccessError("can not run Compile Callable!");
    }

    @Override
    public boolean isAbstract() {
        return body == null;
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }

    @Override
    public ClassReference retType() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return params.stream().map(Pair::getFirst).toArray(ClassReference[]::new);
    }
}