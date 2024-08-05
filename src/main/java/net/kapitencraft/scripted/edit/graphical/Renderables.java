package net.kapitencraft.scripted.edit.graphical;

import net.kapitencraft.kap_lib.client.RenderData;
import net.kapitencraft.kap_lib.util.Color;
import net.kapitencraft.scripted.Scripted;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface Renderables {
    ResourceLocation CODE_VISUALS = Scripted.res("gui/code_visuals.png");

    RenderData BLOCK_LEFT = RenderData.of(CODE_VISUALS, 0, 0, 21, 22);
    RenderData BLOCK_LEFT_WITH_BODY = RenderData.of(CODE_VISUALS, 0, 22, 27, 38);
    RenderData BLOCK_LEFT_WITH_BODY_END = RenderData.of(CODE_VISUALS, 0, 76, 27, 22);
    RenderData BLOCK_LEFT_WITH_BODY_BOTH = RenderData.of(CODE_VISUALS, 27, 22, 27, 38);
    RenderData BLOCK_MIDDLE = RenderData.of(CODE_VISUALS, 21, 0, 2, 19);
    RenderData BLOCK_RIGHT = RenderData.of(CODE_VISUALS, 23, 0, 2, 19);
    RenderData BODY_EXTENSION = RenderData.of(CODE_VISUALS, 0, 41, 6, 19);
    RenderData BODY_BODY_END_EXTENSION = RenderData.of(CODE_VISUALS, 0, 41, 6, 14);
    RenderData BODY_END_LEFT = RenderData.of(CODE_VISUALS, 0, 60, 27, 16);
    RenderData BODY_END_MIDDLE = RenderData.of(CODE_VISUALS, 27, 60, 2, 16);
    RenderData BODY_END_RIGHT = RenderData.of(CODE_VISUALS, 29, 60, 2, 16);

    RenderData VAR_BOOL_START = RenderData.of(CODE_VISUALS, 0, 98, 6, 12);
    RenderData VAR_BOOL_MIDDLE = RenderData.of(CODE_VISUALS, 6, 98, 2, 12);
    RenderData VAR_BOOL_END = RenderData.of(CODE_VISUALS, 8, 98, 6, 12);
    RenderData VAR_DEFAULT_START = RenderData.of(CODE_VISUALS, 14, 98, 6, 12);
    RenderData VAR_DEFAULT_MIDDLE = RenderData.of(CODE_VISUALS, 20, 98, 2, 12);
    RenderData VAR_DEFAULT_END = RenderData.of(CODE_VISUALS, 22, 98, 6, 12);
    RenderData VAR_PRIMITIVE_START = RenderData.of(CODE_VISUALS, 28, 98, 6, 12);
    RenderData VAR_PRIMITIVE_MIDDLE = RenderData.of(CODE_VISUALS, 34, 98, 2, 12);
    RenderData VAR_PRIMITIVE_END = RenderData.of(CODE_VISUALS, 36, 98, 6, 12);

    RenderData VAR_CONNECTOR_LEFT = RenderData.of(CODE_VISUALS, 0, 110, 3, 12);
    RenderData VAR_CONNECTOR_RIGHT = RenderData.of(CODE_VISUALS, 3, 110, 4, 12);

    int METHOD_TEXT_DRAW_OFFSET = 5;

    static void renderBodyStart(int color, int x, int y, GuiGraphics graphics, boolean bodyUp, boolean bodyDown, int width) {
        setColor(graphics, color);
        int i;
        if (bodyUp) {
            if (bodyDown) {
                BLOCK_LEFT_WITH_BODY_BOTH.render(graphics, x, y);
            } else {
                BLOCK_LEFT_WITH_BODY_END.render(graphics, x, y);
            }
            i = 27;
        } else {
            if (bodyDown) {
                BLOCK_LEFT_WITH_BODY.render(graphics, x, y);
                i = 27;
            } else {
                BLOCK_LEFT.render(graphics, x, y);
                i = 21;
            }
        }
        for (; i < width - 2; i+=2) {
            BLOCK_MIDDLE.render(graphics, x + i, y);
        }
        BLOCK_RIGHT.render(graphics, x + i + 2, y);
    }

    static void renderBodyEnd(int color, int x, int y, GuiGraphics graphics, int width) {
        setColor(graphics, color);
        BODY_END_LEFT.render(graphics, x, y);
        int i = 27;
        for (; i < width - 2; i+=2) {
            BODY_END_MIDDLE.render(graphics, x + i, y);
        }
        i+=2;
        BODY_END_RIGHT.render(graphics, x + i, y);
    }

    static void renderMethod(int color, int x, int y, GuiGraphics graphics, String text, boolean connectorStart, boolean connectRight, VarType type) {
        Font font = Minecraft.getInstance().font;
        RenderData start;
        RenderData middle;
        RenderData end;
        switch (type) {
            case BOOL -> {
                start = VAR_BOOL_START;
                middle = VAR_BOOL_MIDDLE;
                end = VAR_BOOL_END;
            }
            case PRIMITIVE -> {
                start = VAR_PRIMITIVE_START;
                middle = VAR_PRIMITIVE_MIDDLE;
                end = VAR_PRIMITIVE_END;
            }
            default -> {
                start = VAR_DEFAULT_START;
                middle = VAR_DEFAULT_MIDDLE;
                end = VAR_DEFAULT_END;
            }
        }
        if (connectorStart) start = VAR_CONNECTOR_LEFT;
        if (connectRight) end = VAR_CONNECTOR_RIGHT;
        setColor(graphics, color);
        start.render(graphics, x, y);
        int i = 6;
        int width = font.width(text);
        for (; i < width; i+=2) {
            middle.render(graphics, x + i, y);
        }
        end.render(graphics, x + i + 2, y);
        graphics.drawString(font, text, x + METHOD_TEXT_DRAW_OFFSET, y + 1, 0);
    }

    enum VarType {
        BOOL,
        PRIMITIVE,
        DEFAULT
    }

    static void setColor(GuiGraphics graphics, int packedColor) {
        Color color = new Color(packedColor);
        graphics.setColor(color.r, color.g, color.b, color.a);
    }
}
