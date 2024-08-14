package net.kapitencraft.scripted.code.var.type.entity;

import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class LivingEntityType<T extends LivingEntity> extends EntityType<T> {
    public LivingEntityType(String name) {
        super(name);

        this.addMethod("getAttributeValue", this.context.returning(VarTypes.DOUBLE)
                .withParam("attribute", VarTypes.ATTRIBUTE)
                .executes(LivingEntity::getAttributeValue)
        );

        this.addMethod("setBaseValue", this.context.consumer()
                .withParam("attribute", VarTypes.ATTRIBUTE)
                .withParam("value", VarTypes.DOUBLE)
                .executes((t, attribute, aDouble) ->
                        Objects.requireNonNull(t.getAttribute(attribute), "Entity '" + t + "' does not have attribute '" + ForgeRegistries.ATTRIBUTES.getKey(attribute))
                                .setBaseValue(aDouble)
                )
        );
    }
}
