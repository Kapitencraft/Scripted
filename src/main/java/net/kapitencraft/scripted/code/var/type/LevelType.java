package net.kapitencraft.scripted.code.var.type;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LevelType extends VarType<Level> {

    public LevelType() {
        super(null, null, null, null, null, null);

        addFunction("spawnEntity", new SpawnEntity());
        addFunction("setBlock", new SetBlock());
    }

    private class SpawnEntity extends InstanceFunction {

        @Override
        public InstanceFunction.Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<Level>.Instance inst) {
            Method<Entity>.Instance method = Method.loadFromSubObject(object, "entity", analyser);
            return new Instance(inst, method);
        }

        @Override
        public Function.Instance createFromCode(String params, VarAnalyser analyser) {
            return null;
        }

        public class Instance extends InstanceFunction.Instance {
            private final Method<Entity>.Instance entity;

            protected Instance(Method<Level>.Instance supplier, Method<Entity>.Instance entity) {
                super(supplier);
                this.entity = entity;
            }

            @Override
            public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<Level> instance) {
                instance.getValue().addFreshEntity(entity.callInit(map));
            }
        }
    }
    private class SetBlock extends InstanceFunction {

        @Override
        public InstanceFunction.Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<Level>.Instance inst) {
            Method<BlockState>.Instance method = Method.loadFromSubObject(object, "stateProvider", analyser);
            Method<BlockPos>.Instance instance = Method.loadFromSubObject(object, "posProvider", analyser);
            return new Instance(inst, method, instance);
        }

        @Override
        public Function.Instance createFromCode(String params, VarAnalyser analyser) {
            return new Instance();
        }

        public class Instance extends InstanceFunction.Instance {
            private final Method<BlockState>.Instance stateProvider;
            private final Method<BlockPos>.Instance posProvider;

            protected Instance(Method<Level>.Instance supplier, Method<BlockState>.Instance stateProvider, Method<BlockPos>.Instance posProvider) {
                super(supplier);
                this.stateProvider = stateProvider;
                this.posProvider = posProvider;
            }

            @Override
            public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<Level> instance) {
                instance.getValue().setBlock(posProvider.callInit(map), stateProvider.callInit(map), 3);
            }
        }
    }
}
