package net.kapitencraft.scripted.edit;

import net.kapitencraft.scripted.edit.graphical.GraphicalEditor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
    protected EditScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new GraphicalEditor(10, 10, width - 20, height - 20, Component.literal("hi")));
        //this.addRenderableWidget(box = Util.make(() -> {
        //    MultiLineTextBox box = new MultiLineTextBox(this.font, 10, 10, this.width-20, this.height-20, this.box, null);
        //    box.setLineRenderType(MultiLineTextBox.LineRenderType.EVERY);
        //    return box;
        //}));
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //PoseStack pose = pGuiGraphics.pose();
        //pose.pushPose();
        //pose.translate(100, 100, 0);
        //pose.scale(3, 3, 1);
        //pose.translate(-100, -100, 0);
        //String text = "ABCDEFGHIJKLMNOP";
        //Font font = this.font;
        //RenderHelper.renderBlock(pGuiGraphics, font, text, 100, 100);
        //String text2 = "Scripted!";
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
        //pose.popPose();
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
