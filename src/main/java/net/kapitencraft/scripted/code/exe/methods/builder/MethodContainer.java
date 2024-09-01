package net.kapitencraft.scripted.code.exe.methods.builder;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class MethodContainer {
    private final ArrayList<ReturningNode<?>> baked = new ArrayList<>();
    private final ArrayList<Returning<?>> unbaked = new ArrayList<>();



    public void bake() {
        unbaked.forEach(returning -> {
            if (returning instanceof Parenter<?> parenter) {
                returning = parenter.getRootParent();
            }
            returning.applyNodes(baked::add);
        });
    }

    public <R> ReturningNode<R> getByIndex(int id) {
        return (ReturningNode<R>) baked.get(id);
    }

    public Pair<ReturningNode<?>, Integer> getMethodAndId(List<? extends VarType<?>> types) {
        for (int i = 0; i < baked.size(); i++) {
            ReturningNode<?> node = baked.get(i);
            if (node.getParamCount() == types.size() && node.matchesTypes(types)) return Pair.of(node, i);
        }
        throw new IllegalArgumentException(String.format("no descriptors matching %s found", types.stream().map(VarType::getName).collect(Collectors.joining(", "))));
    }

    public ArrayList<ReturningNode<?>> getBaked() {
        return baked;
    }

    public Pair<String, ReturningNode<?>> create(IntFunction<String> nameMapper, List<VarType<?>> types) {
        return null;
    }

    public void registerElement(Returning<?> method) {
        this.unbaked.add(method);
    }
}
