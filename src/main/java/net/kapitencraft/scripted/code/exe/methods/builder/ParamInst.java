package net.kapitencraft.scripted.code.exe.methods.builder;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.code.var.type.collection.MapType;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record ParamInst<P>(VarType<P> type, String name) {
    public static final List<String> RESERVED_PARAM_NAMES = List.of(
            "type" //used for saving method type name
    );

    public static <V> ParamInst<List<V>> listOf(String name, Supplier<? extends VarType<V>> type) {
        return new ParamInst<>(type.get().listOf(), name);
    }

    public static <P> ParamInst<P> of(Pair<VarType<P>, String> typeStringPair) {
        return of(typeStringPair.getSecond(), typeStringPair.getFirst());
    }

    public static <K, V> ParamInst<Map<K, V>> mapOf(String name, Supplier<? extends VarType<K>> keyType, Supplier<? extends VarType<V>> valueType) {
        return new ParamInst<>(MapType.getOrCache(keyType.get(), valueType.get()), name);
    }

    public static <P> ParamInst<P> create(String name, Supplier<? extends VarType<P>> type) {
        return new ParamInst<>(type.get(), name);
    }

    public static <P> ParamInst<P> of(String name, VarType<P> type) {
        return new ParamInst<>(type, name);
    }
}
