package net.kapitencraft.scripted.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.analyser.SemanticAnalyser;
import net.kapitencraft.scripted.lang.compiler.ast_optimize.ASTOptimizer;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

import java.util.Arrays;
import java.util.List;

public class CompileCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<Pair<ClassReference, String>> params;
    private final ClassReference[] thrown;
    private final List<Stmt> body;
    private final short modifiers;
    private final Annotation[] annotations;
    private Chunk compiled;

    public CompileCallable(ClassReference retType, List<Pair<ClassReference, String>> params, ClassReference[] thrown, List<Stmt> body, short modifiers, Annotation[] annotations) {
        this.retType = retType;
        this.params = params;
        this.thrown = thrown;
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("returnType", VarTypeManager.getClassName(retType.get()));
        {
            JsonArray array = new JsonArray();
            params.stream().map(Pair::getFirst).map(ClassReference::get).map(VarTypeManager::getClassName).forEach(array::add);
            object.add("params", array);
        }
        {
            JsonArray array = new JsonArray();
            Arrays.stream(thrown).map(ClassReference::get).map(VarTypeManager::getClassName).forEach(array::add);
            object.add("thrown", array);
        }
        if (!isAbstract()) {
            Chunk.Builder chunk = new Chunk.Builder();
            builder.reset();
            int rIndex = 0;
            if (!isStatic()) {
                chunk.addLocal(0, VarTypeManager.VOID.reference(), "this");
                rIndex++;
            }
            for (int i = 0; i < this.params.size(); i++) {
                Pair<? extends ClassReference, String> param = this.params.get(i);
                chunk.addLocal(rIndex + i, param.getFirst(), param.getSecond());
            }
            for (Stmt compileStmt : body) {
                builder.cache(compileStmt);
            }
            builder.build(chunk);
            this.compiled = chunk.build();
            object.add("body", compiled.save());
        }
        if (this.modifiers != 0) object.addProperty("modifiers", this.modifiers);

        object.add("annotations", builder.cacheAnnotations(this.annotations));
        return object;
    }

    public void analyseSemantics(SemanticAnalyser analyser, ClassReference declaring) {
        if (!isAbstract())
            analyser.analyseBody(body, this.retType, this.thrown, params, isStatic() ? null : declaring);
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

    @Override
    public ClassReference[] thrown() {
        return thrown;
    }

    public void optimize() {
        if (!this.isAbstract())
            ASTOptimizer.optimize(this.body);
    }

    public RuntimeCallable convert() {
        return new RuntimeCallable(
                this.retType,
                this.params.stream().map(Pair::getFirst).toList(),
                List.of(),
                this.compiled,
                this.modifiers,
                this.annotations
        );
    }
}