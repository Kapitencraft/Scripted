package net.kapitencraft.scripted.code.var.type;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.oop.Constructor;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.world.entity.Entity;

public class EntityType extends VarType<Entity> {
    public EntityType() {
        super(null, null, null, null, null, null);
        setConstructor(new InstConstructor());
    }

    public static class InstConstructor extends Constructor<Entity> {

        protected InstConstructor() {
            super(ParamSet.single(ParamSet.builder().addParam("type", ModVarTypes.ENTITY_TYPE).addParam("level", ModVarTypes.LEVEL)), "newEntity");
        }


        @Override
        public Method<Entity>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data);
        }

        @Override
        protected Method<Entity>.Instance create(ParamData data, Method<?>.Instance parent) {
            return new Instance(data);
        }

        @Override
        public Method<Entity>.Instance construct(JsonObject object, VarAnalyser analyser) {
            return new Instance(); //TODO complete
        }

        public class Instance extends Constructor<Entity>.Instance {

            protected Instance(ParamData paramData) {
                super(paramData);
            }

            @Override
            protected Entity call(VarMap params) {
                return params.getVarValue("type", ModVarTypes.ENTITY_TYPE).create(params.getVarValue("level", ModVarTypes.LEVEL));
            }

            @Override
            public VarType<Entity> getType(IVarAnalyser analyser) {
                return ModVarTypes.ENTITY.get();
            }
        }
    }
}
