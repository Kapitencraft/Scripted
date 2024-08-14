package net.kapitencraft.scripted.code.var.type.collection;

import com.google.gson.JsonObject;
import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;

import java.util.Map;

public class MapType<K, V> extends VarType<Map<K, V>> {
    private static final DoubleMap<VarType<?>, VarType<?>, MapType<?, ?>> CACHE = new DoubleMap<>();

    public static <K, V> MapType<K, V> getOrCache(VarType<K> key, VarType<V> value) {
        return (MapType<K, V>) CACHE.computeIfAbsent(key, value, MapType::new);
    }


    private final VarType<K> key;
    private final VarType<V> value;

    public MapType(VarType<K> key, VarType<V> value) {
        super("Map<" + key.getName() +"," + value.getName() + ">", null, null, null, null, null, null);
        this.key = key;
        this.value = value;

        this.addMethod("put", context -> context.consumer());
        this.addMethod("get", context -> context.returning(value)
                .withParam(ParamInst.of("key", key))
                .executes(Map::get)
        );
        this.addMethod("containsValue", context -> context.returning(VarTypes.BOOL)
                .withParam(ParamInst.of("value", value))
                .executes(Map::containsValue)
        );
    }

    public VarType<K> getKey() {
        return key;
    }

    public VarType<V> getValue() {
        return value;
    }

    private class Put extends InstanceFunction {
        protected Put() {
            super("put", set -> set.addEntry(entry -> entry
                    .addParam("key", MapType.this::getKey)
                    .addParam("value", MapType.this::getValue)
            ));
        }

        @Override
        public Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<Map<K, V>>.Instance inst) {
            return new Instance(ParamData.of(object, analyser, paramSet), inst);
        }

        @Override
        public VarType<Map<K, V>>.InstanceMethod<Void>.Instance create(ParamData paramData, VarAnalyser analyser, Method<?>.Instance inst) {
            return new Instance(paramData, (Method<Map<K, V>>.Instance) inst);
        }

        private class Instance extends InstanceFunction.Instance {

            protected Instance(ParamData paramData, Method<Map<K, V>>.Instance parent) {
                super(paramData, parent);
            }

            @Override
            public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<Map<K, V>> instance) {

            }
        }
    }
}