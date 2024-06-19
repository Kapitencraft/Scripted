package net.kapitencraft.scripted.code.var.type.data;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.nbt.CompoundTag;

public class DataStorageType extends VarType<CompoundTag> {

    public DataStorageType() {
        super(null, null, null, null, null, null);
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

        @Override
        protected Method<CompoundTag>.Instance create(ParamData data, Method<?>.Instance parent) {
            return null;
        }

        @Override
        public Method<CompoundTag>.Instance construct(JsonObject object, VarAnalyser analyser) {
            return null;
        }

        public class Instance extends Method<CompoundTag>.Instance {

            protected Instance() {
                super(null);
            }

            @Override
            public VarType<CompoundTag> getType(IVarAnalyser analyser) {
                return ModVarTypes.DATA_STORAGE.get();
            }

            @Override
            public CompoundTag call(VarMap params) {
                return new CompoundTag();
            }
        }
    }
}