package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.var.VarType;
import net.minecraft.world.entity.Entity;

import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

public class EntityType extends VarType<Entity> {
    public EntityType() {
        super(null, null, null, null, null, null);
    }

}
