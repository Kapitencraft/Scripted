package net.kapitencraft.scripted.code.var.type;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.oop.Constructor;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
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
        public Method<Entity>.Instance construct(ParamData data) {
            return new Instance(data);
        }

        @Override
        public Method<Entity>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data);
        }

        public class Instance extends Method<Entity>.Instance {

            protected Instance(ParamData paramData) {
                super(paramData);
            }

            @Override
            protected Var<Entity> call(VarMap params) {
                return new Var<>(ModVarTypes.ENTITY.get(), params.getVarValue("type", ModVarTypes.ENTITY_TYPE).create(params.getVarValue("level", ModVarTypes.LEVEL)));
            }

            @Override
            public VarType<Entity> getType(VarAnalyser analyser) {
                return ModVarTypes.ENTITY.get();
            }
        }
    }
}
