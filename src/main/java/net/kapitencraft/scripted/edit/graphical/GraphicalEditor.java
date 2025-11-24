package net.kapitencraft.scripted.edit.graphical;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kapitencraft.kap_lib.config.ClientModConfig;
import net.kapitencraft.scripted.edit.graphical.widgets.*;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BodyWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.HeadWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.LoopWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraphicalEditor extends AbstractWidget {
    private CodeWidget dragged;
    private BlockWidget ghostTarget;
    private int draggedOffsetX, draggedOffsetY;

    private final Font font;
    private float scrollX, scrollY, scale = 1;
    private final List<CodeElement> elements = new ArrayList<>();

    public GraphicalEditor(int pX, int pY, int pWidth, int pHeight, Component pMessage, Font font) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.font = font;

        this.elements.add(
                new CodeElement(HeadWidget.builder()
                        .withExpr(new TextWidget("First Method omg"))
                        .setChild(
                                BodyWidget.text("ABCDEFGHIJKLMNOP")
                                        .setChild(BodyWidget.builder()
                                                .withExpr(new TextWidget("Scripted!"))
                                                .withExpr(new ExprWidget(ExprType.BOOLEAN, List.of(
                                                        new TextWidget("A"),
                                                        new ExprWidget(ExprType.OTHER, List.of(
                                                                new TextWidget("abcdef")
                                                        ))
                                                )))
                                                .setChild(LoopWidget.builder()
                                                        .withHead(new TextWidget("while x"))
                                                        .setBody(BodyWidget.text("enclosed"))
                                                        .setChild(BodyWidget.text("after enclosure"))
                                                )
                                        ).build()
                        ).build()
                )
        );
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
        PoseStack pose = pGuiGraphics.pose();
        {
            pose.pushPose();
            pose.translate(this.getX(), this.getY(), 0);
            pose.pushPose();
            pose.scale(scale, scale, 1);

            ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("textures/block/deepslate_tiles.png");
            int i = Mth.floor(this.scrollX);
            int j = Mth.floor(this.scrollY);
            int k = i % 16;
            int l = j % 16;

            for (int i1 = -1; i1 <= this.width / 16f / this.scale; i1++) {
                for (int j1 = -1; j1 <= this.height / 16f / this.scale; j1++) {
                    pGuiGraphics.blit(resourcelocation, -k + 16 * i1, -l + 16 * j1, 0.0F, 0.0F, 16, 16, 16, 16);
                }
            }
            pose.popPose();
        } //background
        pose.pushPose(); //reset pose
        pose.scale(scale, scale, 1);
        pose.translate(this.scrollX, this.scrollY, 0);
        //0 = scale * (translate + aPos)
        //0 = translate + aPos | -translate
        //-translate = aPos
        int minX =  -(int)scrollX;
        int minY = -(int) scrollY;
        //width = scale * (translate + aPos) | /scale
        //width / scale = translate + aPos | - translate
        //width / scale - translate = aPos
        int maxX = (int) (getWidth() / scale - scrollX);
        int maxY = (int) (getHeight() / scale - scrollY);

        for (int i = this.elements.size() - 1; i >= 0; i--) { //render first elements last due to earlier elements being overwritten by later ones
            CodeElement element = this.elements.get(i);
            if (element.visible(minX, minY, maxX, maxY)) {
                element.widget.render(pGuiGraphics, font, element.x, element.y);
            }
        }

        pGuiGraphics.disableScissor();
        pose.popPose();
        pose.popPose();
        //region dragged
        pose.pushPose();
        pose.scale(scale, scale, 1);
        pose.translate(0, 0, 100);
        if (this.dragged != null) {
            this.dragged.render(pGuiGraphics, font, pMouseX + this.draggedOffsetX, pMouseY + this.draggedOffsetY);
        }
        pose.popPose();
        //endregion
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (Screen.hasControlDown()) {
            /*
            relativeMouseX = (mouseX - wX)
            vPos = scale * (translate + aPos)
            vPos = scale * translate + scale * aPos | /scale
            vPos / scale = translate + aPos | -translate
            vPos / scale - translate = aPos

            vPos = scaleNew * (translateNew + aPos) | /scaleNew
            vPos / scaleNew = translateNew + aPos   | -translateNew
            vPos / scaleNew - translateNew = aPos
            vPos / scale - translate = vPos / scaleNew - translateNew | + translate
            vPos / scale = vPos / scaleNew - translateNew + translate | + translateNew
            vPos / scale + translateNew = vPos / scaleNew + translate | - vPos / scale
            translateNew = vPos / scaleNew - vPos / scale + translate
            */
            float scrollOffset = (float) scrollY * .05f;
            float scaleOld = this.scale;
            this.scale = Math.clamp(this.scale + scrollOffset, .5f, 4f);
            if (scaleOld != scale) {
                float relativeX = (float) mouseX - this.getX();
                float relativeY = (float) mouseY - this.getY();
                this.scrollX += relativeX / scale - relativeX / scaleOld;
                this.scrollY += relativeY / scale - relativeY / scaleOld;
            }
        } else {
            float scrollDelta = (float) (scrollY * ClientModConfig.getScrollScale());
            if (Screen.hasShiftDown()) {
                this.scrollX += scrollDelta;
            } else
                this.scrollY += scrollDelta;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int posX = (int) (mouseX / scale - scrollX) - getX();
        int posY = (int) (mouseY / scale - scrollY) - getY();
        for (int i = 0; i < this.elements.size(); i++) {
            CodeElement element = this.elements.get(i);
            if (element.hovered(posX, posY)) {
                WidgetFetchResult result = element.fetchAndRemoveHoveredWidget(posX, posY, font);
                if (result == null) {
                    continue;
                }
                if (result.widget() != element.widget) {
                    element.recalculateSize();
                }
                this.dragged = result.widget();
                this.draggedOffsetX = -result.x();
                this.draggedOffsetY = -result.y();
                if (!result.removed())
                    this.elements.remove(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (dragged != null && dragged instanceof BlockWidget) {
            int draggedUiX = (int) ((mouseX + draggedOffsetX) / scale - scrollX) - getX();
            int draggedUiY = (int) ((mouseY + draggedOffsetY) / scale - scrollY) - getY();
            for (CodeElement element : elements) {
                
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragged != null) {
            if (this.ghostTarget != null && this.dragged instanceof BlockWidget blockWidget) {
                blockWidget.setBottomChild(this.ghostTarget.getChild());
                this.ghostTarget.setChild(blockWidget);
            }
            int uiX = (int) (mouseX / scale - scrollX) - getX();
            int uiY = (int) (mouseY / scale - scrollY) - getY();
            this.elements.addFirst(new CodeElement(this.dragged, uiX + this.draggedOffsetX, uiY + this.draggedOffsetY)); //add as first view and access
            this.dragged = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private class CodeElement {
        private final int x;
        private final int y;
        private int width;
        private int height;
        private final CodeWidget widget;

        private CodeElement(CodeWidget widget) {
            this(widget, 100, 100);
        }

        private CodeElement(CodeWidget widget, int x, int y) {
            this.widget = widget;
            this.recalculateSize();
            this.x = x;
            this.y = y;
        }

        private int calculateWidgetWidth() {
            int width = 0;
            if (widget instanceof BlockWidget blockWidget) {
                do {
                    width += blockWidget.getWidth(font);
                    blockWidget = blockWidget.getChild();
                } while (blockWidget != null);
            } else
                width = this.widget.getWidth(font);
            return width;
        }

        private int calculateWidgetHeight() {
            int height = 0;
            if (widget instanceof BlockWidget blockWidget) {
                do {
                    height += blockWidget.getHeight();
                    blockWidget = blockWidget.getChild();
                } while (blockWidget != null);
            } else
                height = widget.getHeight();
            return height;
        }

        public boolean hovered(int mouseX, int mouseY) {
            return mouseX > this.x && mouseX < this.x + this.width &&
                    mouseY > this.y && mouseY < this.y + this.height;
        }

        public boolean visible(int minX, int minY, int maxX, int maxY) {
            return this.x + width > minX || this.y + height > minY || this.x < maxX || this.y < maxY;
        }

        public WidgetFetchResult fetchAndRemoveHoveredWidget(int posX, int posY, Font font) {
            return ((Removable) this.widget).fetchAndRemoveHovered(posX - x, posY - y, font);
        }

        public void recalculateSize() {
            this.width = calculateWidgetWidth();
            this.height = calculateWidgetHeight();
        }
    }

    private class GhostBlockWidget extends BlockWidget {

        @Override
        public Type getType() {
            return null;
        }

        @Override
        public int getWidth(Font font) {
            return dragged.getWidth(font);
        }

        @Override
        public int getHeight() {
            return dragged.getHeight();
        }

        @Override
        public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
            return null;
        }
    }
}
