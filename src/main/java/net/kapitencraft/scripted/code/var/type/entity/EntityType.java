package net.kapitencraft.scripted.code.var.type.entity;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.entity.Entity;

public class EntityType<T extends Entity> extends VarType<T> {
    public EntityType(String name) {
        super(name, null, null, null, null, null, null);
        addConstructor(context -> context.constructor().withParam("type", VarTypes.ENTITY_TYPE).withParam("level", VarTypes.LEVEL).executes((entityType, level) -> (T) entityType.create(level)));
    }
}
