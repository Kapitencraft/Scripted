package net.kapitencraft.scripted.code.var.type.collection;

import com.google.common.collect.Multimap;
import net.kapitencraft.kap_lib.collection.DoubleMap;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

public class MultimapType<K, V> extends VarType<Multimap<K, V>> {
    private static final DoubleMap<VarType<?>, VarType<?>, MultimapType<?, ?>> CACHE = new DoubleMap<>();

    public static <K, V> MultimapType<K, V> getOrCache(VarType<K> key, VarType<V> value) {
        return (MultimapType<K, V>) CACHE.computeIfAbsent(key, value, MultimapType::new);
    }

    private final VarType<K> key;
    private final VarType<V> value;

    public MultimapType(VarType<K> key, VarType<V> value) {
        super("Multimap<" + key.getName() +"," + value.getName() + ">" , null, null, null, null, null, null);
        this.key = key;
        this.value = value;
    }

    @Override
    public Class<Multimap<K, V>> getTypeClass() {
        return null;
    }
}
