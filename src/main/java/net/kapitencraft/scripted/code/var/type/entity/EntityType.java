package net.kapitencraft.scripted.code.var.type.entity;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.world.entity.Entity;

public class EntityType<T extends Entity> extends VarType<T> {
    public EntityType(String name) {
        super(name, null, null, null, null, null, null);
        setExtendable(); //entities must be extendable to allow LivingEntities and Players (and more)
        setConstructor(new InstConstructor());
    }

    private class InstConstructor extends Constructor {

        protected InstConstructor() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("type", ModVarTypes.ENTITY_TYPE)
                    .addParam("level", ModVarTypes.LEVEL)
            ), "newEntity");
        }


        @Override
        public Method<T>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data);
        }

        @Override
        protected Method<T>.Instance create(ParamData data, Method<?>.Instance parent) {
            return new Instance(data);
        }

        @Override
        public Method<T>.Instance construct(JsonObject object, VarAnalyser analyser) {
            return new Instance(ParamData.of(object, analyser, this.paramSet));
        }

        public class Instance extends Constructor.Instance {

            protected Instance(ParamData paramData) {
                super(paramData);
            }

            @Override
            protected T call(VarMap params) {
                return (T) params.getVarValue("type", ModVarTypes.ENTITY_TYPE).create(params.getVarValue("level", ModVarTypes.LEVEL));
            }

            @Override
            public VarType<T> getType(IVarAnalyser analyser) {
                return (VarType<T>) ModVarTypes.ENTITY.get();
            }
        }
    }
}
