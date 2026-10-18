package net.kapitencraft.scripted.edit.graphical.selection;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.*;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.binary.BinaryNumOperationWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.binary.ComparisonWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.*;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.loop.ForEachStmtWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.loop.ForRangeStmtWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.loop.WhileStmtWidget;
import net.kapitencraft.scripted.registry.ModRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public interface SelectionTabs {
    ResourceKey<SelectionTab> OPERATORS = reg("operators");
    ResourceKey<SelectionTab> CONTROL = reg("control");
    ResourceKey<SelectionTab> WORLD = reg("world");
    ResourceKey<SelectionTab> PLAYER = reg("player");
    ResourceKey<SelectionTab> VARIABLES = reg("variables");

    private static ResourceKey<SelectionTab> reg(String operation) {
        return ResourceKey.create(ModRegistries.Keys.SELECTION_TABS, Scripted.res(operation));
    }

    static void bootstrap(BootstrapContext<SelectionTab> context) {
        context.register(WORLD, SelectionTab.builder()
                .withEntry(new CompoundStmtWidget.Builder()
                        .setKey("compound_stmts.scripted.world.set_block")
                        .setTarget(MethodStmtWidget.builder()
                                .setSignature("Lscripted/exe/CommandData;getLevel();Lnet/minecraft/world/level/Level;")
                                .setMethodChild(MethodWidget.builder()
                                        .setSignature("Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
                                        .withParam("pos", new GetVarWidget("data").setChainChild(MethodWidget.builder()
                                                .setSignature("Lscripted/exe/CommandData;getPosition()")
                                                .setType(ExprCategory.OTHER)
                                                .build()
                                        ))
                                        .withParam("newState", new GetVarWidget("type"))
                                        .withParam("flags", new ParamWidget(ExprCategory.NUMBER))
                                        .setType(ExprCategory.BOOLEAN)
                                        .build())
                                .build())
                        .addArgument("type", new BlockSelectWidget())
                        .build()
                ).withEntry(new CompoundStmtWidget.Builder()
                        .setKey("compound_stmts.scripted.world.set_block_offset")
                        .setTarget(MethodStmtWidget.builder()
                                .setSignature("Lscripted/exe/CommandData;getLevel();Lnet/minecraft/world/level/Level;")
                                .setMethodChild(MethodWidget.builder()
                                        .setSignature("Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
                                        .withParam("pos", new GetVarWidget("data").setChainChild(MethodWidget.builder()
                                                .setSignature("Lscripted/exe/CommandData;getPosition()")
                                                .setChild(MethodWidget.builder()
                                                        .setType(ExprCategory.OTHER)
                                                        .setSignature("Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;")
                                                        .withParam("dx", new GetVarWidget("x"))
                                                        .withParam("dy", new GetVarWidget("y"))
                                                        .withParam("dz", new GetVarWidget("z"))
                                                        .build())
                                                .setType(ExprCategory.OTHER)
                                                .build()
                                        ))
                                        .withParam("newState", new GetVarWidget("type"))
                                        .withParam("flags", new ParamWidget(ExprCategory.NUMBER))
                                        .setType(ExprCategory.BOOLEAN)
                                        .build())
                                .build())
                        .addArgument("type", new BlockSelectWidget())
                        .addArgument("x", ParamWidget.NUM)
                        .addArgument("y", ParamWidget.NUM)
                        .addArgument("z", ParamWidget.NUM)
                        .build()
                )
                .withEntry(MethodWidget.builder()
                        .setType(ExprCategory.OTHER)
                        .setSignature("Lnet/minecraft/core/BlockPos;<init>(III)V")
                        .withParam("x", ParamWidget.NUM)
                        .withParam("y", ParamWidget.NUM)
                        .withParam("z", ParamWidget.NUM)
                        .build()
                )
                .withEntry(MethodWidget.builder()
                        .setType(ExprCategory.OTHER)
                        .setSignature("Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
                        .withParam("pos", ParamWidget.OBJ)
                        .build())
                .withEntry(MethodWidget.builder()
                        .setType(ExprCategory.BOOLEAN)
                        .setSignature("Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
                        .withParam("block", new BlockSelectWidget())
                        .build())
                .withEntry(MethodWidget.builder()
                        .setType(ExprCategory.NUMBER)
                        .setSignature("Lnet/minecraft/world/phys/Vec3;x")
                        .build())
                .withEntry(MethodWidget.builder()
                        .setType(ExprCategory.NUMBER)
                        .setSignature("Lnet/minecraft/world/phys/Vec3;y")
                        .build())
                .withEntry(MethodWidget.builder()
                        .setType(ExprCategory.NUMBER)
                        .setSignature("Lnet/minecraft/world/phys/Vec3;z")
                        .build())
                .build()
        );
        context.register(PLAYER, SelectionTab.builder()
                .withEntry(MethodWidget.builder()
                        .setType(ExprCategory.OTHER)
                        .setSignature("Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z")
                        .withParam("stack", new ItemSelectWidget())
                        .build())
                .build()
        );
        context.register(CONTROL, SelectionTab.builder()
                .withEntry(IfStmtWidget.builder()
                        .hideElse()
                ).withEntry(IfStmtWidget.builder())
                .withEntry(IfStmtWidget.builder().withElseIfNoCondition())
                .withEntry(WhileStmtWidget.builder())
                .withEntry(ForRangeStmtWidget.builder())
                .withEntry(ForEachStmtWidget.builder())
                .withEntry(SimpleScopeEndWidget.CONTINUE)
                .withEntry(SimpleScopeEndWidget.BREAK)
                .withEntry(SimpleScopeEndWidget.RETURN)
                .build()
        );
        context.register(VARIABLES, SelectionTab.builder()
                .withEntry(AssignVarWidget.builder().doesCreateVar())
                .withEntry(AssignVarWidget.builder().setExpr(ParamWidget.OBJ))
                .withEntry(new GetVarWidget("something"))
                .withEntry(new GetVarWidget("i"))
                .build()
        );
        context.register(OPERATORS, SelectionTab.builder()
                .withEntry(new UnaryOperationWidget())
                .withEntry(new BinaryNumOperationWidget())
                .withEntry(new ComparisonWidget())
                .build()
        );
    }
}