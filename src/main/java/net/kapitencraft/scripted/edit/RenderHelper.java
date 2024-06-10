package net.kapitencraft.scripted.edit;

import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.client.RenderData;
import net.minecraft.resources.ResourceLocation;

public interface RenderHelper {
    ResourceLocation CODE_VISUALS = Scripted.res("gui/code_visuals.png");

    RenderData BLOCK_LEFT = RenderData.of(CODE_VISUALS, 0, 0, 21, 22);
    RenderData BLOCK_LEFT_WITH_BODY = RenderData.of(CODE_VISUALS, 0, 22, 27, 38);
    RenderData BLOCK_RIGHT = RenderData.of(CODE_VISUALS, 63, 0, 4, 19);
    RenderData BODY_EXTENSION = RenderData.of(CODE_VISUALS, 0, 41, 6, 19);
    RenderData BODY_BODY_END_EXTENSION = RenderData.of(CODE_VISUALS, 0, 41, 6, 14);
    RenderData BLOCK_WITH_BODY_END_LEFT = RenderData.of(CODE_VISUALS, 0, 60, 27, 14);
    RenderData BLOCK_WITH_BODY_END_MIDDLE = RenderData.of(CODE_VISUALS, 27, 60, 2, 14);
    RenderData BLOCK_WITH_BODY_END_RIGHT = RenderData.of(CODE_VISUALS, 29, 60, 2, 14);

    RenderData EMPTY_BOOL_START = RenderData.of(CODE_VISUALS, 21, 0, 6, 19);
    RenderData EMPTY_BOOL_MIDDLE = RenderData.of(CODE_VISUALS, 27, 0, 2, 19);
    RenderData EMPTY_BOOL_END = RenderData.of(CODE_VISUALS, 29, 0, 6, 19);
    RenderData EMPTY_DEFAULT_START = RenderData.of(CODE_VISUALS, 35, 0, 6, 19);
    RenderData EMPTY_DEFAULT_MIDDLE = RenderData.of(CODE_VISUALS, 41, 0, 2, 19);
    RenderData EMPTY_DEFAULT_END = RenderData.of(CODE_VISUALS, 43, 0, 6, 19);
    RenderData EMPTY_PRIMITIVE_START = RenderData.of(CODE_VISUALS, 49, 0, 6, 19);
    RenderData EMPTY_PRIMITIVE_MIDDLE = RenderData.of(CODE_VISUALS, 55, 0, 2, 19);
    RenderData EMPTY_PRIMITIVE_END = RenderData.of(CODE_VISUALS, 57, 0, 6, 19);

    int METHOD_TEXT_DRAW_OFFSET = 5;

    static void renderFunction(IFunctionCode code, ) {

    }
}
