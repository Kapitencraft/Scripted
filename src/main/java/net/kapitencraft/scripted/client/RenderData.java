package net.kapitencraft.scripted.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

public record RenderData(ResourceLocation texture, Vector2i uv, Vector2i uvSize) {

    public static RenderData of(ResourceLocation texture, int u, int v, int uWidth, int vHeight) {
        return new RenderData(texture, new Vector2i(u, v), new Vector2i(uWidth, vHeight));
    }

    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(texture, x, y, uv.x, uv.y, uvSize.x, uvSize.y);
    }
}
