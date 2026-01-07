package net.kapitencraft.scripted.edit.graphical;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kapitencraft.kap_lib.config.ClientModConfig;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.block.BlockGhostInserter;
import net.kapitencraft.scripted.edit.graphical.selection.SelectionTab;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.HeadWidget;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraphicalEditor extends AbstractWidget {
    private final Registry<SelectionTab> tabs;

    private CodeWidget draggedWidget;
    private final GhostBlockWidget ghostElement = new GhostBlockWidget();
    private BlockCodeElement ghostTargetElement;
    private BlockGhostInserter blockGhostInserter;
    private int draggedOffsetX, draggedOffsetY;

    private final Font font;
    private float scrollX, scrollY, scale = 1;
    private float selectionScroll;
    private final List<BlockCodeElement> elements = new ArrayList<>();

    public GraphicalEditor(int pX, int pY, int pWidth, int pHeight, Component pMessage, Font font, Registry<SelectionTab> tabs) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.tabs = tabs;
        this.font = font;

        this.elements.add(
                new BlockCodeElement(HeadWidget.builder()
                        .setTranslationKey("scripted.code.head.test")
                        //.setChild(
                        //        MethodStmtWidget.builder()
                        //                .setSignature("Lnet/minecraft/world/phys/Vec3;dot(Lnet/minecraft/world/phys/Vec3;)D")
                        //                .withArgument("vec3", new GetVarWidget("position"))
                        //                .setChild(VarModWidget.builder()
                        //                        .setExpr(ExprWidget.builder()
                        //                                .setTranslationKey("scripted.code.expr.test0")
                        //                                .setType(ExprCategory.BOOLEAN)
                        //                                .withParam("arg0", ExprWidget.builder()
                        //                                        .setTranslationKey("scripted.code.expr.test1")
                        //                                )
                        //                        ).setChild(WhileLoopWidget.builder()
                        //                                .setCondition(ExprWidget.builder()
                        //                                        .setTranslationKey("scripted.code.expr.test0")
                        //                                        .setType(ExprCategory.NUMBER)
                        //                                        .withParam("arg0", ExprWidget.builder()
                        //                                                .setTranslationKey("scripted.code.expr.test1")
                        //                                        )
                        //                                )
                        //                                .setBody(MethodStmtWidget.builder()
                        //                                        .setSignature("Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;")
                        //                                )
                        //                                .setChild(IfWidget.builder()
                        //                                        .setCondition(ExprWidget.builder()
                        //                                                .setTranslationKey("scripted.code.expr.test0")
                        //                                                .setType(ExprCategory.NUMBER)
                        //                                                .withParam("arg0", ExprWidget.builder()
                        //                                                        .setTranslationKey("scripted.code.expr.test1")
                        //                                                )
                        //                                        )
                        //                                        .withBranch(MethodStmtWidget.builder()
                        //                                                .setSignature("Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;"))
                        //                                        .withElseBranch(MethodStmtWidget.builder()
                        //                                                .setSignature("Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;"))
                        //                                )
                        //                        )
                        //                ).build()
                        //)
                        .build()
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

            ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("textures/block/black_wool.png");
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
        int minX = -(int) scrollX;
        int minY = -(int) scrollY;
        //width = scale * (translate + aPos) | /scale
        //width / scale = translate + aPos | - translate
        //width / scale - translate = aPos
        int maxX = (int) (getWidth() / scale - scrollX);
        int maxY = (int) (getHeight() / scale - scrollY);

        for (int i = this.elements.size() - 1; i >= 0; i--) { //render first elements last due to earlier elements being overwritten by later ones
            BlockCodeElement element = this.elements.get(i);
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
        if (this.draggedWidget != null) {
            this.draggedWidget.render(pGuiGraphics, font, pMouseX + this.draggedOffsetX, pMouseY + this.draggedOffsetY);
        }
        pose.popPose();
        //endregion

        pose.pushPose();
        pose.translate(getX() + 1, getY() + 1, 0);
        pose.scale(.75f, .75f, 1);
        Holder<SelectionTab>[] tabs = this.tabs.holders().toArray(Holder[]::new);
        for (int i = 0; i < tabs.length; i++) {
            pGuiGraphics.drawString(font, Component.translatable(Util.makeDescriptionId("selection_tab", tabs[i].getKey().location())), 0, i * 10, -1, false);
        }
        pose.popPose();

        pGuiGraphics.enableScissor(this.getX() + 50, this.getY(), this.getX() + 105, this.getY() + this.getHeight());
        pose.pushPose();
        pose.translate(getX() + 60, getY(), 0);
        pose.scale(0.75f, 0.75f, 1);
        pose.translate(0, this.selectionScroll, 0);
        int y = 1;
        for (Holder<SelectionTab> tab : tabs) {
            SelectionTab value = tab.value();
            pGuiGraphics.drawString(font, Component.translatable(Util.makeDescriptionId("selection_tab", tab.getKey().location())), 2, y, -1, false);
            y += 10;
            for (int i1 = 0; i1 < value.size(); i1++) {
                CodeWidget widget = value.get(i1);
                widget.render(pGuiGraphics, font, 0, y);
                y += widget.getHeight();
                y += 10;
            }
        }
        pose.popPose();
        pGuiGraphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isPoolAreaHovered(mouseX, mouseY)) {
            selectionScroll += (float) (scrollY * ClientModConfig.getScrollScale());
            return true;
        }
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
        if (isPoolAreaHovered(mouseX, mouseY)) {
            this.attemptGetWidgetFromPool(mouseX - this.getX() - 60, mouseY - this.getY());
        }
        int posX = (int) (mouseX / scale - scrollX) - getX();
        int posY = (int) (mouseY / scale - scrollY) - getY();
        for (int i = 0; i < this.elements.size(); i++) {
            BlockCodeElement element = this.elements.get(i);
            if (element.hovered(posX, posY)) {
                BlockWidgetFetchResult result = element.fetchAndRemoveHoveredBlockWidget(posX, posY, font);
                if (result == null) {
                    continue;
                }
                if (result.widget() != element.widget) {
                    element.recalculateSize();
                }
                this.draggedWidget = result.widget();
                this.draggedOffsetX = -result.x();
                this.draggedOffsetY = -result.y();
                if (!result.removed())
                    this.elements.remove(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void attemptGetWidgetFromPool(double x, double mouseY) {
        int uX = (int) x;
        //vPos = .75 * (translate + aPos) | / (3 / 4)
        //vPos * (4 / 3) = translate + aPos | - translate
        //vPos * (4 / 3) + translate = aPos
        int uY = (int) mouseY * 4 / 3 + (int) selectionScroll;
        Holder<SelectionTab>[] tabs = this.tabs.holders().toArray(Holder[]::new);
        for (Holder<SelectionTab> tab : tabs) {
            SelectionTab value = tab.value();
            uY -= 10;
            for (int i1 = 0; i1 < value.size(); i1++) {
                CodeWidget widget = value.get(i1);
                if (uY > 0 && uY < widget.getHeight()) {
                    this.draggedWidget = widget.copy();
                    this.draggedOffsetX = -uX;
                    this.draggedOffsetY = -uY;
                    return;
                }
                uY -= widget.getHeight();
                uY -= 10;
            }
        }
    }

    private boolean isPoolAreaHovered(double mouseX, double mouseY) {
        return mouseX > this.getX() + 60 && mouseX < this.getX() + 105 &&
                mouseY > this.getY() && mouseY < this.getY() + this.getHeight();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (draggedWidget != null && draggedWidget instanceof BlockCodeWidget && !isPoolAreaHovered(mouseX, mouseY)) {
            int draggedUiX = (int) ((mouseX + draggedOffsetX) / scale - scrollX) - getX();
            int draggedUiY = (int) ((mouseY + draggedOffsetY) / scale - scrollY) - getY();
            BlockGhostInserter inserter;
            for (BlockCodeElement element : elements) {
                if ((inserter = element.getGhostBlockWidget(draggedUiX, draggedUiY)) != null) {
                    if (!inserter.equals(blockGhostInserter)) {
                        if (blockGhostInserter != null)
                            blockGhostInserter.insert(ghostElement.getChild());
                        inserter.insertChildMiddle(ghostElement);
                        blockGhostInserter = inserter;
                        ghostTargetElement = element;
                    }
                    return;
                }
            }
            if (this.blockGhostInserter != null) {
                blockGhostInserter.insert(this.ghostElement.getChild());
                blockGhostInserter = null;
                ghostTargetElement = null;
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggedWidget != null) {
            if (blockGhostInserter != null && this.draggedWidget instanceof BlockCodeWidget blockWidget) {
                blockWidget.setBottomChild(this.ghostElement.getChild());
                this.blockGhostInserter.insert(blockWidget);
                this.blockGhostInserter = null;
                this.ghostTargetElement.recalculateSize();
                this.ghostTargetElement = null;
            } else if (!isPoolAreaHovered(mouseX, mouseY)) { //if pool is hovered, delete the widget
                int uiX = (int) (mouseX / scale - scrollX) - getX();
                int uiY = (int) (mouseY / scale - scrollY) - getY();

                this.elements.addFirst(new BlockCodeElement(this.draggedWidget, uiX + this.draggedOffsetX, uiY + this.draggedOffsetY)); //add as first view and access
            }
            this.draggedWidget = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private class BlockCodeElement {
        private final int x;
        private final int y;
        private int width;
        private int height;
        private final BlockCodeWidget widget;

        private BlockCodeElement(BlockCodeWidget widget) {
            this(widget, 100, 100);
        }

        private BlockCodeElement(BlockCodeWidget widget, int x, int y) {
            this.widget = widget;
            this.recalculateSize();
            this.x = x;
            this.y = y;
        }

        private int calculateWidgetWidth() {
            int width = 0;
            if (widget instanceof BlockCodeWidget blockWidget) {
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
            if (widget instanceof BlockCodeWidget blockWidget) {
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

        public BlockWidgetFetchResult fetchAndRemoveHoveredBlockWidget(int posX, int posY, Font font) {
            return this.widget.fetchAndRemoveHovered(posX - x, posY - y, font);
        }

        public void recalculateSize() {
            this.width = calculateWidgetWidth();
            this.height = calculateWidgetHeight();
        }

        public BlockGhostInserter getGhostBlockWidget(int draggedUiX, int draggedUiY) {
            if (!(this.widget instanceof BlockCodeWidget blockWidget))
                return null;
            return blockWidget.getGhostBlockWidgetTarget(draggedUiX - this.x, draggedUiY - this.y);
        }
    }

    private class GhostBlockWidget extends BlockCodeWidget {

        @Override
        public @NotNull Type getType() {
            return null;
        }

        @Override
        public int getWidth(Font font) {
            return draggedWidget.getWidth(font);
        }

        @Override
        public int getHeight() {
            return draggedWidget.getHeight();
        }

        @Override
        public BlockGhostInserter getGhostBlockWidgetTarget(int x, int y) {
            return null;
        }

        @Override
        public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
            int height = getHeight();
            graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, 6 + getWidth(font), 3 + height);
            graphics.drawString(font, "ghost", renderX + 6, renderY + 7, 0, false);
            super.render(graphics, font, renderX, renderY);
        }

        @Override
        public @Nullable BlockWidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
            return null;
        }

        @Override
        public BlockCodeWidget copy() {
            throw new IllegalAccessError("attempting to copy ghost widget");
        }
    }
}
