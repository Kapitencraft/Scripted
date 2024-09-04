package net.kapitencraft.scripted.code.exe.methods.builder.container;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.methods.builder.Parenter;
import net.kapitencraft.scripted.code.exe.methods.builder.Returning;
import net.kapitencraft.scripted.code.exe.methods.builder.node.ReturningNode;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MethodContainer {
    private final ArrayList<ReturningNode<?>> baked = new ArrayList<>();
    private final ArrayList<Returning<?>> unbaked = new ArrayList<>();



    public void bake(String varTypeName, String methodName) {
        int i = 0;
        for (Returning<?> returning : unbaked) {
            try {
                if (returning instanceof Parenter<?> parenter) {
                    returning = parenter.getRootParent();
                }
                returning.applyNodes(this::checkVarTypeDupeAndAdd);
            } catch (Exception e) {
                CrashReport report = CrashReport.forThrowable(e, "Baking Methods of " + varTypeName);
                report.addCategory("MethodInfo")
                        .setDetail("Name", methodName)
                        .setDetail("Index", i);

                throw new ReportedException(report);
            }
            i++;
        }
    }

    private void checkVarTypeDupeAndAdd(ReturningNode<?> node) {
        this.baked.stream().filter(node1 -> node1.getParamCount() == node.getParamCount()).filter(node1 -> node1.matchesTypes(node.getTypes()))
                .findAny()
                .ifPresent(node1 -> {
                    throw new IllegalArgumentException("duplicate method declaration with signature " + node1.getTypes().stream().map(VarType::getRegName).collect(Collectors.joining()));
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

    public void registerElement(Returning<?> method) {
        this.unbaked.add(method);
    }
}
