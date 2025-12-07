package net.kapitencraft.scripted.edit.graphical.selection;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.MethodStmtWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.WhileLoopWidget;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public interface SelectionTabs {
    ResourceKey<SelectionTab> OPERATORS = reg("operators");
    ResourceKey<SelectionTab> CONTROL = reg("control");

    private static ResourceKey<SelectionTab> reg(String operation) {
        return ResourceKey.create(ModRegistries.Keys.SELECTION_TABS, Scripted.res(operation));
    }

    static void bootstrap(BootstrapContext<SelectionTab> context) {
        context.register(OPERATORS, SelectionTab.builder()
                .withEntry(MethodStmtWidget.builder()
                        .setSignature("Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;")
                )
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