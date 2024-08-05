package net.kapitencraft.scripted.code.var.type.entity;

import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.entity.Entity;

public class EntityType<T extends Entity> extends VarType<T> {
    public EntityType(String name) {
        super(name, null, null, null, null, null, null);
        setExtendable(); //entities must be extendable to allow LivingEntities and Players (and more)
        setConstructor(new InstConstructor());
    }

    private class InstConstructor extends SimpleConstructor {

        protected InstConstructor() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("type", VarTypes.ENTITY_TYPE)
                    .addParam("level", VarTypes.LEVEL)
            ));
        }

        @Override
        protected T call(VarMap params) {
            return (T) params.getVarValue("type", VarTypes.ENTITY_TYPE).create(params.getVarValue("level", VarTypes.LEVEL));
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return (VarType<T>) VarTypes.ENTITY.get();
        }
    }
}
