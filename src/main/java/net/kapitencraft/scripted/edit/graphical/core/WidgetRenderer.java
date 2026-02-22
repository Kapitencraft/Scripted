package net.kapitencraft.scripted.edit.graphical.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class WidgetRenderer {
    private final GuiSpriteManager sprites;
    private final Matrix4f pose;
    private final BufferBuilder builder;

    public WidgetRenderer(GuiSpriteManager sprites, Matrix4f pose) {
        this.sprites = sprites;
        this.pose = pose;
        RenderSystem.setShaderTexture(0, ResourceLocation.withDefaultNamespace("textures/atlas/gui.png"));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    }

    public void draw() {
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    public void blitSprite(ResourceLocation sprite, int x, int y, int width, int height) {
        this.blitSprite(sprite, x, y, 0, width, height);
    }

    public void blitSprite(ResourceLocation sprite, int x, int y, int blitOffset, int width, int height) {
        TextureAtlasSprite textureatlassprite = this.sprites.getSprite(sprite);
        GuiSpriteScaling guispritescaling = this.sprites.getSpriteScaling(textureatlassprite);
        switch (guispritescaling) {
            case GuiSpriteScaling.Stretch ignored ->
                    this.blitSprite(textureatlassprite, x, y, blitOffset, width, height);
            case GuiSpriteScaling.Tile guispritescaling$tile -> this.blitTiledSprite(
                    textureatlassprite,
                    x,
                    y,
                    blitOffset,
                    width,
                    height,
                    0,
                    0,
                    guispritescaling$tile.width(),
                    guispritescaling$tile.height(),
                    guispritescaling$tile.width(),
                    guispritescaling$tile.height()
            );
            case GuiSpriteScaling.NineSlice guispritescaling$nineslice ->
                    this.blitNineSlicedSprite(textureatlassprite, guispritescaling$nineslice, x, y, blitOffset, width, height);
            default -> {
            }
        }
    }

    private void blitNineSlicedSprite(
            TextureAtlasSprite sprite, GuiSpriteScaling.NineSlice nineSlice, int x, int y, int blitOffset, int width, int height
    ) {
        GuiSpriteScaling.NineSlice.Border guispritescaling$nineslice$border = nineSlice.border();
        int left = Math.min(guispritescaling$nineslice$border.left(), width / 2);
        int right = Math.min(guispritescaling$nineslice$border.right(), width / 2);
        int top = Math.min(guispritescaling$nineslice$border.top(), height / 2);
        int bottom = Math.min(guispritescaling$nineslice$border.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, width, height);
        } else if (height == nineSlice.height()) {
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, left, height);
            this.blitTiledSprite(
                    sprite,
                    x + left,
                    y,
                    blitOffset,
                    width - right - left,
                    height,
                    left,
                    0,
                    nineSlice.width() - right - left,
                    nineSlice.height(),
                    nineSlice.width(),
                    nineSlice.height()
            );
            this.blitSprite(
                    sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - right, 0, x + width - right, y, blitOffset, right, height
            );
        } else if (width == nineSlice.width()) {
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, width, top);
            this.blitTiledSprite(
                    sprite,
                    x,
                    y + top,
                    blitOffset,
                    width,
                    height - bottom - top,
                    0,
                    top,
                    nineSlice.width(),
                    nineSlice.height() - bottom - top,
                    nineSlice.width(),
                    nineSlice.height()
            );
            this.blitSprite(
                    sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - bottom, x, y + height - bottom, blitOffset, width, bottom
            );
        } else {
            //top left
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, left, top);
            //top middle
            this.blitTiledSprite(
                    sprite, x + left, y, blitOffset, width - right - left, top, left, 0, nineSlice.width() - right - left, top, nineSlice.width(), nineSlice.height()
            );
            //top right
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - right, 0, x + width - right, y, blitOffset, right, top);
            //bottom left
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - bottom, x, y + height - bottom, blitOffset, left, bottom);
            this.blitTiledSprite(
                    sprite,
                    x + left,
                    y + height - bottom,
                    blitOffset,
                    width - right - left,
                    bottom,
                    left,
                    nineSlice.height() - bottom,
                    nineSlice.width() - right - left,
                    bottom,
                    nineSlice.width(),
                    nineSlice.height()
            );
            this.blitSprite(
                    sprite,
                    nineSlice.width(),
                    nineSlice.height(),
                    nineSlice.width() - right,
                    nineSlice.height() - bottom,
                    x + width - right,
                    y + height - bottom,
                    blitOffset,
                    right,
                    bottom
            );
            this.blitTiledSprite(
                    sprite,
                    x,
                    y + top,
                    blitOffset,
                    left,
                    height - bottom - top,
                    0,
                    top,
                    left,
                    nineSlice.height() - bottom - top,
                    nineSlice.width(),
                    nineSlice.height()
            );
            this.blitTiledSprite(
                    sprite,
                    x + left,
                    y + top,
                    blitOffset,
                    width - right - left,
                    height - bottom - top,
                    left,
                    top,
                    nineSlice.width() - right - left,
                    nineSlice.height() - bottom - top,
                    nineSlice.width(),
                    nineSlice.height()
            );
            this.blitTiledSprite(
                    sprite,
                    x + width - right,
                    y + top,
                    blitOffset,
                    left,
                    height - bottom - top,
                    nineSlice.width() - right,
                    top,
                    right,
                    nineSlice.height() - bottom - top,
                    nineSlice.width(),
                    nineSlice.height()
            );
        }
    }

    private void blitTiledSprite(
            TextureAtlasSprite sprite,
            int x,
            int y,
            int blitOffset,
            int width,
            int height,
            int uPosition,
            int vPosition,
            int spriteWidth,
            int spriteHeight,
            int nineSliceWidth,
            int nineSliceHeight
    ) {
        if (width > 0 && height > 0) {
            if (spriteWidth > 0 && spriteHeight > 0) {
                for (int i = 0; i < width; i += spriteWidth) {
                    int j = Math.min(spriteWidth, width - i);

                    for (int k = 0; k < height; k += spriteHeight) {
                        int l = Math.min(spriteHeight, height - k);
                        this.blitSprite(sprite, nineSliceWidth, nineSliceHeight, uPosition, vPosition, x + i, y + k, blitOffset, j, l);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + spriteWidth + "x" + spriteHeight);
            }
        }
    }

    private void blitSprite(TextureAtlasSprite sprite, int x, int y, int blitOffset, int width, int height) {
        if (width != 0 && height != 0) {
            this.innerBlit(
                    x,
                    x + width,
                    y,
                    y + height,
                    blitOffset,
                    sprite.getU0(),
                    sprite.getU1(),
                    sprite.getV0(),
                    sprite.getV1()
            );
        }
    }

    private void blitSprite(
            TextureAtlasSprite sprite,
            int textureWidth,
            int textureHeight,
            int uPosition,
            int vPosition,
            int x,
            int y,
            int blitOffset,
            int uWidth,
            int vHeight
    ) {
        if (uWidth != 0 && vHeight != 0) {
            this.innerBlit(
                    x,
                    x + uWidth,
                    y,
                    y + vHeight,
                    blitOffset,
                    sprite.getU((float)uPosition / (float)textureWidth),
                    sprite.getU((float)(uPosition + uWidth) / (float)textureWidth),
                    sprite.getV((float)vPosition / (float)textureHeight),
                    sprite.getV((float)(vPosition + vHeight) / (float)textureHeight)
            );
        }
    }

    void innerBlit(
            int x1,
            int x2,
            int y1,
            int y2,
            int blitOffset,
            float minU,
            float maxU,
            float minV,
            float maxV
    ) {
        builder.addVertex(pose, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV);
        builder.addVertex(pose, (float)x1, (float)y2, (float)blitOffset).setUv(minU, maxV);
        builder.addVertex(pose, (float)x2, (float)y2, (float)blitOffset).setUv(maxU, maxV);
        builder.addVertex(pose, (float)x2, (float)y1, (float)blitOffset).setUv(maxU, minV);
    }
}
