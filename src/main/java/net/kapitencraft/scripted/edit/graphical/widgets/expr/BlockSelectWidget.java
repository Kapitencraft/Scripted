package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.kap_lib.core.client.UsefulTextures;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.InteractionData;
import net.kapitencraft.scripted.edit.graphical.widgets.io.SelectBlockWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BlockSelectWidget implements ExprCodeWidget {
    public static final MapCodec<BlockSelectWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block", Blocks.AIR).forGetter(w -> w.value)
    ).apply(i, BlockSelectWidget::new));

    private Block value = Blocks.AIR;
    private ItemStack stack = ItemStack.EMPTY;

    public BlockSelectWidget(Block block) {
        this.setBlock(block);
    }

    public BlockSelectWidget() {
        this.setBlock(Blocks.STONE);
    }

    @Override
    public @NotNull Type getType() {
        return Type.SELECT_BLOCK;
    }

    public void setBlock(Block block) {
        if (this.value != block) {
            this.value = block;
            this.stack = new ItemStack(block.asItem());
        }
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(renderX, renderY, 0);
        pose.scale(.75f, .75f, 1);
        UsefulTextures.renderSlotBackground(graphics, 0, 0);
        graphics.renderItem(this.stack, 0, 0);
        pose.popPose();
    }

    @Override
    public int getWidth(Font font) {
        return 14;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public ExprCodeWidget copy() {
        return new BlockSelectWidget(this.value);
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        throw new IllegalAccessError("can not insert into block select widget");
    }

    @Override
    public CodeWidget getByName(String argName) {
        throw new IllegalAccessError("can not get from block select widget");
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {

    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return ExprWidgetFetchResult.notRemoved(this, x, y);
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        sink.accept(new Interaction(xOrigin, yOrigin, 12, 14));
    }

    private class Interaction extends CodeInteraction {

        protected Interaction(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void onClick(int mouseX, int mouseY, InteractionData data) {
            data.openWidget(new SelectBlockWidget(50, 20, data.getWidth() - 100, data.getHeight() - 40, Component.literal("Select block"), data.getFont(), data.wrapCloseWidget(BlockSelectWidget.this::setBlock)));
            //TODO select
        }
    }

    @Override
    public void update(@Nullable MethodContext context) {
    }
}
