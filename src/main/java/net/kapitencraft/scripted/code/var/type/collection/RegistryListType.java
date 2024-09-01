package net.kapitencraft.scripted.code.var.type.collection;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
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
        //this.setConstructor(new Constructor());
        //TODO fix constructor
    }

    @Override
    public RegistryType<V> getType() {
        return (RegistryType<V>) super.getType();
    }

    public MethodInstance<?> createInstance(String value) {
        return ((Constructor) this.constructor).createInstance(value);
    }

    public class Constructor extends RegistryType<List<V>>.Constructor {

        @Override
        public MethodInstance<List<V>> construct(JsonObject object, VarAnalyser analyser) {
            return new Instance(GsonHelper.getAsString(object, "key"));
        }

        @Override
        public MethodInstance<List<V>> load(JsonObject object, VarAnalyser analyser) {
            return construct(object, analyser);
        }

        public MethodInstance<?> createInstance(String value) {
            return new Instance(value);
        }

        private class Instance extends MethodInstance<List<V>> {
            private final List<V> val;
            private final String saveVal;

            protected Instance(String saveVal) {
                super("new");
                this.saveVal = saveVal;
                this.val = RegistryListType.this.getType().readListValue(saveVal);
            }

            @Override
            protected void saveAdditional(JsonObject object) {
                object.addProperty("key", saveVal);
            }

            @Override
            public List<V> call(VarMap origin, MethodPipeline<?> pipeline) {
                return val;
            }

            @Override
            public VarType<List<V>> getType(IVarAnalyser analyser) {
                return RegistryListType.this;
            }
        }
    }
}