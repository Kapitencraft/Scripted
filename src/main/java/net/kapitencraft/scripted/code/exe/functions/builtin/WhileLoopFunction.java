package net.kapitencraft.scripted.code.exe.functions.builtin;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModFunctions;
import net.minecraft.util.GsonHelper;

public class WhileLoopFunction extends Function {

    public static MethodInstance<Void> create(MethodInstance<Boolean> condition, MethodPipeline<?> pipeline) {
        return ModFunctions.WHILE.get().createInst(condition, pipeline);
    }

    private MethodInstance<Void> createInst(MethodInstance<Boolean> condition, MethodPipeline<?> pipeline) {
        return new Instance<>(condition, pipeline);
    }

    @Override
    public MethodInstance<Void> load(JsonObject object, VarAnalyser analyser) {
        MethodInstance<Boolean> method = Method.loadFromSubObject(object, "condition", analyser);
        MethodPipeline<?> pipeline = MethodPipeline.load(GsonHelper.getAsJsonObject(object, "body"), analyser, true);
        return new Instance<>(method, pipeline);
    }

    public class Instance<T> extends Function.Instance {
        private final MethodInstance<Boolean> condition;
        private final MethodPipeline<T> body;

        public Instance(MethodInstance<Boolean> condition, MethodPipeline<T> body) {
            super("while");
            this.condition = condition;
            this.body = body;
        }

        @Override
        protected void saveAdditional(JsonObject object) {
            object.add("condition", condition.toJson());
            object.add("body", body.toJson());
        }

        @Override
        public void execute(VarMap map, MethodPipeline<?> source) {
            while (condition.call(map, source) && !body.isBrokenOrCanceled()) {
                body.execute(map, (MethodPipeline<T>) source);
            }
            body.reset();
        }
    }
}