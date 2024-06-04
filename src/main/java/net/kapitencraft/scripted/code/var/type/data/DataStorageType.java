package net.kapitencraft.scripted.code.var.type.data;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.method.param.ParamData;
import net.kapitencraft.scripted.code.method.param.ParamSet;
import net.kapitencraft.scripted.code.oop.Constructor;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.nbt.CompoundTag;

public class DataStorageType extends VarType<CompoundTag> {

    public DataStorageType() {
        this.setConstructor(new DataStorageConstructor());
    }

    private static class DataStorageConstructor extends Constructor<CompoundTag> {

        protected DataStorageConstructor() {
            super(ParamSet.empty(), "newDataStorage");
        }

        @Override
        public Method<CompoundTag>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance();
        }

        public class Instance extends Method<CompoundTag>.Instance {

            protected Instance() {
                super(null);
            }

            @Override
            public VarType<CompoundTag> getType(VarAnalyser analyser) {
                return ModVarTypes.DATA_STORAGE.get();
            }

            @Override
            public Var<CompoundTag> call(VarMap params) {
                return new Var<>(new CompoundTag());
            }
        }
    }
}