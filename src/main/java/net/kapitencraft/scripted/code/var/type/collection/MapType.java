package net.kapitencraft.scripted.code.var.type.collection;

import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;

import java.util.Map;
import java.util.function.Supplier;

public class MapType<K, V> extends VarType<Map<K, V>> {
    private static final DoubleMap<VarType<?>, VarType<?>, MapType<?, ?>> CACHE = new DoubleMap<>();

    public static <K, V> MapType<K, V> getOrCache(VarType<K> key, VarType<V> value) {
        return (MapType<K, V>) CACHE.computeIfAbsent(key, value, MapType::new);
    }

    public static <K, V> MapType<K, V> getOrCache(Supplier<? extends VarType<K>> keySup, Supplier<? extends VarType<V>> valSup) {
        return getOrCache(keySup.get(), valSup.get());
    }


    private final VarType<K> key;
    private final VarType<V> value;

    public MapType(VarType<K> key, VarType<V> value) {
        super("Map<" + key.getName() +"," + value.getName() + ">", null, null, null, null, null, null);
        this.key = key;
        this.value = value;

        this.addMethod("put", context -> context.consumer()
                .withParam(ParamInst.of("key", key))
                .withParam(ParamInst.of("value", value))
                .executes(Map::put)
        );
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
}