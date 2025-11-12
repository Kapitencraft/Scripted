package net.kapitencraft.scripted.edit.graphical;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kapitencraft.kap_lib.config.ClientModConfig;
import net.kapitencraft.scripted.edit.graphical.widgets.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

public class GraphicalEditor extends AbstractWidget {
    private final Font font;
    private float scrollX, scrollY, scale = 1;
    private ScopeWidget widget = new ScopeWidget(
            new BodyWidget(
                    new TextWidget("ABCDEFGHIJKLMNOP")
            ),
            new BodyWidget(
                    new TextWidget("Scripted!"),
                    new ExprWidget(ExprType.BOOLEAN, List.of(
                            new TextWidget("A"),
                            new ExprWidget(ExprType.OTHER, List.of(
                                    new TextWidget("abcdef")
                            ))
                    ))
            ),
            new LoopWidget(
                    List.of(
                            new TextWidget("while x")
                    ),
                    new ScopeWidget(
                            new BodyWidget(
                                    new TextWidget("enclosed")
                            )
                    )
            ),
            new BodyWidget(
                    new TextWidget("after enclosure")
            )
    );

    public GraphicalEditor(int pX, int pY, int pWidth, int pHeight, Component pMessage, Font font) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.font = font;
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
        PoseStack pose = pGuiGraphics.pose();
        pose.pushPose();
        pose.translate(this.getX(), this.getY(), 0);
        pose.pushPose();
        pose.scale(scale, scale, 1);

        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("textures/block/black_glazed_terracotta.png");
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

        if (widget != null) {
            pose.pushPose();
            pose.translate(100, 100, 0);
            pose.scale(3, 3, 1);
            pose.translate(-100, -100, 0);
            this.widget.render(pGuiGraphics, font, 100, 100);
            //String text = ;
            //Font font = this.font;
            //RenderHelper.renderBlock(pGuiGraphics, font, text, 100, 100);
            //String text2 = ;
            //RenderHelper.renderBlock(pGuiGraphics, font, text2, 100, 119);
//
            //String loopText = "while x";
            //int loopWidth = font.width(loopText);
            //pGuiGraphics.blitSprite(CodeWidgetSprites.LOOP_HEAD, 100, 138, 6 + loopWidth, 22);
            //pGuiGraphics.drawString(font, loopText, 104, 147, 0, false);
            //pGuiGraphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE, 100, 158, 6, 18);
//
            //String enclosedText = "enclosed";
            //RenderHelper.renderBlock(pGuiGraphics, font, enclosedText, 106, 157);
//
            //pGuiGraphics.blitSprite(CodeWidgetSprites.SCOPE_END, 100, 176, 6 + loopWidth, 16);
            //pGuiGraphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, 100, 189, 40, 22);
//
            //String exprText = "ab";
            //RenderHelper.renderExpr(pGuiGraphics, font, exprText, 110, 194);
            pose.popPose();
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
