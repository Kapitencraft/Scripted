package net.kapitencraft.scripted.code.var.type.collection;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.core.Method;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.exe.param.ParamData;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.util.GsonHelper;

import java.util.List;

public class RegistryListType<V> extends ListType<V> {
    public RegistryListType(RegistryType<V> type) {
        super(type);
        this.setConstructor(new Constructor());
    }

    @Override
    public RegistryType<V> getType() {
        return (RegistryType<V>) super.getType();
    }

    public MethodInstance<?> createInstance(String value) {
        return ((Constructor) this.constructor).createInstance(value);
    }

    public class Constructor extends RegistryType<List<V>>.Constructor {

        protected Constructor() {
            super(ParamSet.empty());
        }

        @Override
        public Method<List<V>>.Instance construct(JsonObject object, VarAnalyser analyser) {
            return new Instance(GsonHelper.getAsString(object, "key"));
        }

        @Override
        public Method<List<V>>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return construct(object, analyser);
        }

        public Method<?>.Instance createInstance(String value) {
            return new Instance(value);
        }

        private class Instance extends Method<List<V>>.Instance {
            private final List<V> val;
            private final String saveVal;

            protected Instance(String saveVal) {
                super(ParamData.empty());
                this.saveVal = saveVal;
                this.val = RegistryListType.this.getType().readListValue(saveVal);
            }

            @Override
            protected void saveAdditional(JsonObject object) {
                object.addProperty("key", saveVal);
            }

            @Override
            protected List<V> call(VarMap params, VarMap origin) {
                return val;
            }

            @Override
            public VarType<List<V>> getType(IVarAnalyser analyser) {
                return RegistryListType.this;
            }
        }
    }
}