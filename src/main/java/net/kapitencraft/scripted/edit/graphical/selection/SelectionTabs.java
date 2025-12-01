package net.kapitencraft.scripted.edit.graphical.selection;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.widgets.TextWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BodyWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.LoopWidget;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public class SelectionTabs {
    public static final ResourceKey<SelectionTab> OPERATORS = reg("operators");

    private static ResourceKey<SelectionTab> reg(String operation) {
        return ResourceKey.create(ModRegistries.Keys.SELECTION_TABS, Scripted.res(operation));
    }

    public static void bootstrap(BootstrapContext<SelectionTab> context) {
        context.register(OPERATORS, new SelectionTab.Builder()
                .withEntry(BodyWidget.text("a test"))
                        .withEntry(LoopWidget.builder().withHead(new TextWidget("looooooop")))
                .build()
        );
    }
}