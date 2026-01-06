package net.kapitencraft.scripted.edit.graphical.selection;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.widgets.ExprWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.MethodStmtWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.WhileLoopWidget;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.Map;

public interface SelectionTabs {
    ResourceKey<SelectionTab> OPERATORS = reg("operators");
    ResourceKey<SelectionTab> CONTROL = reg("control");
    ResourceKey<SelectionTab> WORLD = reg("world");

    private static ResourceKey<SelectionTab> reg(String operation) {
        return ResourceKey.create(ModRegistries.Keys.SELECTION_TABS, Scripted.res(operation));
    }

    static void bootstrap(BootstrapContext<SelectionTab> context) {
        context.register(WORLD, SelectionTab.builder()
                .withEntry(new ExprWidget(ExprCategory.NUMBER, "Lnet/minecraft/world/phys/Vec3;x", Map.of()))
                .withEntry(new ExprWidget(ExprCategory.NUMBER, "Lnet/minecraft/world/phys/Vec3;y", Map.of()))
                .withEntry(new ExprWidget(ExprCategory.NUMBER, "Lnet/minecraft/world/phys/Vec3;z", Map.of()))
                .build()
        );
        context.register(CONTROL, SelectionTab.builder()
                .withEntry(IfWidget.builder()
                        .hideElse()
                ).withEntry(IfWidget.builder())
                .withEntry(WhileLoopWidget.builder())
                .build()
        );
    }
}