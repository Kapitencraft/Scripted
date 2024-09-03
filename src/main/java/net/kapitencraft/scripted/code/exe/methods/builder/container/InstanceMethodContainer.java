package net.kapitencraft.scripted.code.exe.methods.builder.container;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.methods.builder.BuilderContext;
import net.kapitencraft.scripted.code.exe.methods.builder.InstMapper;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class InstanceMethodContainer<T> {
    private final ArrayList<Function<BuilderContext<T>, InstMapper<T, ?>>> creators = new ArrayList<>();
    private final MethodContainer container = new MethodContainer();

    public void bake(String varTypeName, String methodName) {
        this.container.bake(varTypeName, methodName);
    }

    public void create(BuilderContext<T> context, String varTypeName, String methodName) {
        int i = 0;
        for (Function<BuilderContext<T>, InstMapper<T, ?>> function : creators) {
            try {
                container.registerElement(function.apply(context));
            } catch (Exception e) {
                CrashReport report = CrashReport.forThrowable(e, "Creating Methods of " + varTypeName);
                report.addCategory("MethodInfo")
                        .setDetail("Name", methodName)
                        .setDetail("Index", i);

                throw new ReportedException(report);
            }
            i++;
        }
    }

    public void register(Function<BuilderContext<T>, InstMapper<T, ?>> method) {
        creators.add(method);
    }

    public <R> ReturningNode<R> getByIndex(int i) {
        return container.getByIndex(i);
    }

    public Pair<ReturningNode<?>, Integer> getMethodAndId(List<? extends VarType<?>> types) {
        return container.getMethodAndId(types);
    }

    public List<ReturningNode<?>> getBaked() {
        return container.getBaked();
    }
}
