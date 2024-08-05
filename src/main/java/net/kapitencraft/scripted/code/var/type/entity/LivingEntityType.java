package net.kapitencraft.scripted.code.var.type.entity;

import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class LivingEntityType<T extends LivingEntity> extends EntityType<T> {
    public LivingEntityType(String name) {
        super(name);

        this.addMethod(GetAttributeValue::new);

        this.addMethod(SetBaseValue::new);
    }

    private class GetAttributeValue extends SimpleInstanceMethod<Double> {

        protected GetAttributeValue() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("attribute", VarTypes.ATTRIBUTE)
            ), "getAttributeValue");
        }

        @Override
        public Double call(VarMap map, T inst) {
            return inst.getAttributeValue(map.getVarValue("attribute", VarTypes.ATTRIBUTE));
        }

        @Override
        protected VarType<Double> getType(IVarAnalyser analyser) {
            return VarTypes.DOUBLE.get();
        }
    }

    private class SetBaseValue extends SimpleInstanceFunction {

        protected SetBaseValue() {
            super("setBaseValue", set -> set.addEntry(entry -> entry
                    .addParam("attribute", VarTypes.ATTRIBUTE)
                    .addParam("value", VarTypes.DOUBLE)
            ));
        }

        @Override
        public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<T> instance) {
            LivingEntity living = instance.getValue();
            Attribute attribute = map.getVarValue("attribute", VarTypes.ATTRIBUTE);
            Objects.requireNonNull(living.getAttribute(attribute), "Entity '" + living + "' does not have attribute '" + ForgeRegistries.ATTRIBUTES.getKey(attribute)).setBaseValue(map.getVarValue("value", VarTypes.DOUBLE));
        }
    }
}
