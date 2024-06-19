package net.kapitencraft.scripted.code.var.type.math;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.core.BlockPos;

public class BlockPosType extends VarType<BlockPos> {
    public BlockPosType() {
        super(BlockPos::offset,
                (pos, pos1) -> new BlockPos(pos.getX() * pos1.getX(), pos.getY() * pos1.getY(), pos.getZ() * pos1.getZ()),
                (pos, pos1) -> new BlockPos(pos.getX() / pos1.getX(), pos.getY() / pos1.getY(), pos.getZ() / pos1.getZ()),
                BlockPos::subtract,
                (pos, pos1) -> new BlockPos(pos.getX() % pos1.getX(), pos.getY() % pos1.getY(), pos.getZ() % pos1.getZ()),
                BlockPos::asLong);
    }
}
