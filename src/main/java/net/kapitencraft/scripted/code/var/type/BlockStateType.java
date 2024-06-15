package net.kapitencraft.scripted.code.var.type;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class BlockStateType extends VarType<BlockState> {
    public BlockStateType() {
        super(null, null, null, null, null, null);

        addMethod("setProperty", new SetProperty());
    }

    public class SetProperty extends InstanceMethod<BlockState> {

        protected SetProperty() {
            super(ParamSet.single(ParamSet.builder().addParam("property", ModVarTypes.BLOCK_STATE_PROPERTY).addWildCardParam("value")), "setProperty");
        }

        @Override
        public InstanceMethod<BlockState>.Instance load(ParamData data, Method<BlockState>.Instance inst, JsonObject object) {
            return new Instance(data, inst);
        }

        @Override
        protected Method<BlockState>.Instance create(ParamData data, Method<?>.Instance parent) {
            return new Instance(data, (Method<BlockState>.Instance) parent);
        }

        public class Instance extends InstanceMethod<BlockState>.Instance {

            private Instance(ParamData paramData, Method<BlockState>.@NotNull Instance parent) {
                super(paramData, parent);
            }

            @Override
            public BlockState call(VarMap map, BlockState inst) {
                return execute(map, inst);
            }

            private static <T extends Comparable<T>> BlockState execute(VarMap map, BlockState inst) {
                return inst.setValue((Property<T>) map.getVarValue("property", ModVarTypes.BLOCK_STATE_PROPERTY), (T) map.getVar("value").getValue());
            }

            @Override
            public VarType<BlockState> getType(IVarAnalyser analyser) {
                return ModVarTypes.BLOCK_STATE.get();
            }
        }
    }

    public static Collection<Property<?>> getProperties(Var<Block> blockVar) {
        return blockVar.getValue().getStateDefinition().getProperties();
    }
}