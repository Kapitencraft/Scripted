package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;

public class BlockStateType extends VarType<BlockState> {
    public BlockStateType() {
        super("BlockState", null, null, null, null, null, null);

        addMethod(SetProperty::new);
    }

    private class SetProperty extends SimpleInstanceMethod<BlockState> {

        private SetProperty() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("property", VarTypes.BLOCK_STATE_PROPERTY)
                    .addWildCardParam("main", "value")
            ), "setProperty");
        }

        @Override
        public BlockState call(VarMap map, BlockState inst) {
            return execute(map, inst);
        }

        private static <T extends Comparable<T>> BlockState execute(VarMap map, BlockState inst) {
            return inst.setValue((Property<T>) map.getVarValue("property", VarTypes.BLOCK_STATE_PROPERTY), (T) map.getVar("value").getValue());
        }

        @Override
        public VarType<BlockState> getType(IVarAnalyser analyser) {
            return VarTypes.BLOCK_STATE.get();
        }
    }

    public static Collection<Property<?>> getProperties(Var<Block> blockVar) {
        return blockVar.getValue().getStateDefinition().getProperties();
    }
}