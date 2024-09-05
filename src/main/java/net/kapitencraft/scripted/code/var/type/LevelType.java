package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.level.Level;

public class LevelType extends VarType<Level> {

    public LevelType() {
        super("Level", null, null, null, null, null, null);

        addMethod("spawn", context -> context.consumer().withParam("entity", VarTypes.ENTITY).executes(Level::addFreshEntity));
        addMethod("setBlock", context -> context.consumer().withParam("pos", VarTypes.BLOCK_POS).withParam("state", VarTypes.BLOCK_STATE).executes(Level::setBlockAndUpdate));
    }

    @Override
    public Class<Level> getTypeClass() {
        return Level.class;
    }
}
