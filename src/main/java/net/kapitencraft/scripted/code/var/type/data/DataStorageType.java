package net.kapitencraft.scripted.code.var.type.data;

import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.nbt.CompoundTag;

public class DataStorageType extends VarType<CompoundTag> {

    public DataStorageType() {
        super("DataStorage", null, null, null, null, null, null);
        this.setConstructor(new DataStorageConstructor());
    }

    private class DataStorageConstructor extends SimpleConstructor {

        protected DataStorageConstructor() {
            super(ParamSet.empty());
        }

        @Override
        public VarType<CompoundTag> getType(IVarAnalyser analyser) {
            return VarTypes.DATA_STORAGE.get();
        }

        @Override
        public CompoundTag call(VarMap params) {
            return new CompoundTag();
        }
    }
}