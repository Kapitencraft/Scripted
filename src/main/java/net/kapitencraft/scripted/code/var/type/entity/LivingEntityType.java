package net.kapitencraft.scripted.code.var.type.entity;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class LivingEntityType<T extends LivingEntity> extends EntityType<T> {
    public LivingEntityType(String name) {
        super(name);
        this.addMethod("getAttributeValue", new GetAttributeValue());
    }

    private class GetAttributeValue extends InstanceMethod<Double> {

        protected GetAttributeValue() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("attribute", ModVarTypes.ATTRIBUTE)
            ), "getAttributeValue");
        }

        @Override
        public VarType<T>.InstanceMethod<Double>.Instance load(ParamData data, Method<T>.Instance inst, JsonObject object) {
            return new Instance(data, inst);
        }

        @Override
        protected Method<Double>.Instance create(ParamData data, Method<?>.Instance parent) {
            return new Instance(data, (Method<T>.Instance) parent);
        }

        private class Instance extends InstanceMethod<Double>.Instance {

            protected Instance(ParamData paramData, Method<T>.@NotNull Instance parent) {
                super(paramData, parent);
            }

            @Override
            public VarType<Double> getType(IVarAnalyser analyser) {
                return ModVarTypes.DOUBLE.get();
            }

            @Override
            public Double call(VarMap map, T inst) {
                return inst.getAttributeValue(map.getVarValue("attribute", ModVarTypes.ATTRIBUTE));
            }
        }
    }

    private class SetBaseValue extends InstanceFunction {

        protected SetBaseValue(Consumer<ParamSet> paramSet) {
            super(set -> set.addEntry(entry -> entry
                    .addParam("attribute", ModVarTypes.ATTRIBUTE)
                    .addParam("value", ModVarTypes.DOUBLE)
            ));
        }

        @Override
        public VarType<T>.InstanceFunction.Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<T>.Instance inst) {
            return new Instance(ParamData.of(object, analyser, this.paramSet), inst);
        }

        @Override
        public Function.Instance createFromCode(String params, VarAnalyser analyser) {
            return new Instance(Par);
        }

        public class Instance extends InstanceFunction.Instance {

            protected Instance(ParamData paramData, Method<T>.Instance parent) {
                super(paramData, parent);
            }

            @Override
            public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<T> instance) {
                LivingEntity living = instance.getValue();
                Attribute attribute = map.getVarValue("attribute", ModVarTypes.ATTRIBUTE);
                Objects.requireNonNull(living.getAttribute(attribute), "Entity '" + living + "' does not have attribute '" + ForgeRegistries.ATTRIBUTES.getKey(attribute)).setBaseValue(map.getVarValue("value", ModVarTypes.DOUBLE));
            }
        }
    }
}
