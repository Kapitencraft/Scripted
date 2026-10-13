package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.kap_lib.core.client.UsefulTextures;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.InteractionData;
import net.kapitencraft.scripted.edit.graphical.widgets.io.SelectBlockWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.io.SelectItemWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ItemSelectWidget implements ExprCodeWidget {
    public static final MapCodec<ItemSelectWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item", Items.STONE).forGetter(w -> w.stack.getItem())
    ).apply(i, ItemSelectWidget::new));

    private ItemStack stack = ItemStack.EMPTY;

    public ItemSelectWidget(Item item) {
        this.setItem(item);
    }

    public ItemSelectWidget() {
        this.setItem(Items.STONE);
    }

    @Override
    public @NotNull Type getType() {
        return Type.SELECT_ITEM;
    }

    public void setItem(ItemLike block) {
        this.stack = new ItemStack(block);
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(renderX, renderY + 1, 0);
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
        return 14;
    }

    @Override
    public ExprCodeWidget copy() {
        return new ItemSelectWidget(this.stack.getItem());
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        throw new IllegalAccessError("can not insert into block select widget");
    }

    @Override
    public CodeWidget getByName(String arg) {
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
        sink.accept(new Interaction(xOrigin, yOrigin, 14, 14));
    }

    private class Interaction extends CodeInteraction {

        protected Interaction(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void onClick(int mouseX, int mouseY, InteractionData data) {
            data.openWidget(new SelectItemWidget(50, 20, data.getWidth() - 100, data.getHeight() - 40, Component.literal("Select block"), data.getFont(), data.wrapCloseWidget(ItemSelectWidget.this::setItem)));
        }
    }

    @Override
    public void update(@Nullable MethodContext context, Font font) {
    }
}
