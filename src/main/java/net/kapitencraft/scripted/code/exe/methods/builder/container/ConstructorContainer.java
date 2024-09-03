package net.kapitencraft.scripted.code.exe.methods.builder.container;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.methods.builder.BuilderContext;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConstructorContainer<T> {
    private final ArrayList<Function<BuilderContext<T>, Returning<T>>> creators = new ArrayList<>();
    private final MethodContainer container = new MethodContainer();

    public void bake(String varTypeName, String methodName) {
        this.container.bake(varTypeName, methodName);
    }

    public void create(BuilderContext<T> context) {
        creators.stream().map(func -> func.apply(context)).forEach(container::registerElement);
    }

    public void register(Function<BuilderContext<T>, Returning<T>> method) {
        creators.add(method);
    }

    public ReturningNode<T> getByIndex(int i) {
        return container.getByIndex(i);
    }

    public Pair<ReturningNode<?>, Integer> getMethodAndId(List<? extends VarType<?>> types) {
        return container.getMethodAndId(types);
    }

    public List<ReturningNode<?>> getBaked() {
        return container.getBaked();
    }

}
