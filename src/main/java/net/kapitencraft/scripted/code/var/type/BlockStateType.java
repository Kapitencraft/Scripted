package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;

public class BlockStateType extends VarType<BlockState> {
    public BlockStateType() {
        super("BlockState", null, null, null, null, null, null);

    }

    public static Collection<Property<?>> getProperties(Var<Block> blockVar) {
        return blockVar.getValue().getStateDefinition().getProperties();
    }

    @Override
    public Class<BlockState> getTypeClass() {
        return BlockState.class;
    }
}