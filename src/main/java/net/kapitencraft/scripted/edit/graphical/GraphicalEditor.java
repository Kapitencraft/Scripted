package net.kapitencraft.scripted.edit.graphical;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kapitencraft.kap_lib.config.ClientModConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GraphicalEditor extends AbstractWidget {
    private float scrollX, scrollY, scale = 1;

    public GraphicalEditor(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
        PoseStack pose = pGuiGraphics.pose();
        pose.pushPose();
        pose.translate(this.getX(), this.getY(), 0);
        pose.pushPose();
        pose.scale(scale, scale, 1);

        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("textures/block/netherrack.png");
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
        pose.pushPose(); //reset pose
        pose.scale(scale, scale, 1);
        pose.translate(this.scrollX, this.scrollY, 0);

        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 20; y++) {
                pGuiGraphics.fill(40 * x, 40 * y, 40 * x + 10, 40 * y + 10, -1);
            }
        }

        pGuiGraphics.disableScissor();
        pose.popPose();
        pose.popPose();
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
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
