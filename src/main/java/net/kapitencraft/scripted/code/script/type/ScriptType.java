package net.kapitencraft.scripted.code.script.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.script.Script;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.util.JsonHelper;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ScriptType<T, R> {
    private final HashMap<String, VarType<?>> possibleParams = new HashMap<>();
    private final String fileSuffix;
    private final String fileDirectory;
    private final @Nullable Consumer<R> afterExecute;

    public ScriptType(String fileSuffix, String fileDirectory, @Nullable Consumer<R> afterExecute) {
        this.fileSuffix = fileSuffix;
        this.fileDirectory = fileDirectory;
        this.afterExecute = afterExecute;
    }

    public void addPossibleParam(String name, Supplier<? extends VarType<?>> type) {
        if (possibleParams.containsKey(name)) {
            throw new IllegalStateException("can not add param '" + name + "' twice");
        }
        possibleParams.put(name, type.get());
    }

    public abstract VarMap instantiate(T inst);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveScript(Script<R> script, String name) {
        File file = new File(this.fileDirectory + "/" + name + "." + fileSuffix);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append(JsonHelper.GSON.toJson(script.save()));
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(Script<R> script, T in) {
        VarMap map = instantiate(in);
        Var<R> var = script.execute(map);
        if (this.afterExecute != null) {
            this.afterExecute.accept(var.getValue());
        }
    }

    public Script<R> load(JsonObject in) {
        JsonArray array = GsonHelper.getAsJsonArray(in, "params");
        Script<R> script = new Script<>(array.asList().stream()
                .filter(JsonElement::isJsonPrimitive)
                .map(JsonElement::getAsJsonPrimitive)
                .map(JsonPrimitive::getAsString).toList(),
                this,
                MethodPipeline.load(GsonHelper.getAsJsonObject(in, "code"), new VarAnalyser(), false)
        );
        return script;
    }
}