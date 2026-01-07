package net.kapitencraft.scripted.edit.graphical.selection;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.WhileLoopWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.BlockSelectWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
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
                .withEntry(new ExprWidget(ExprCategory.OTHER, "Lnet/minecraft/core/BlockPos;<init>(III)V", Map.of(
                        "x", ParamWidget.NUM,
                        "y", ParamWidget.NUM,
                        "z", ParamWidget.NUM
                )))
                .withEntry(new ExprWidget(ExprCategory.OTHER, "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", Map.of("pos", ParamWidget.OBJ)))
                        .withEntry(new ExprWidget(ExprCategory.BOOLEAN, "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", Map.of("block", new BlockSelectWidget())))

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