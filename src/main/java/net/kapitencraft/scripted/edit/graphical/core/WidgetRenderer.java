package net.kapitencraft.scripted.edit.graphical.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
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
    private final TextureAtlasSprite boolExpr;
    private final TextureAtlasSprite elseConditionHead;
    private final TextureAtlasSprite genericExpr;
    private final TextureAtlasSprite loopHead;
    private final TextureAtlasSprite methodHead;
    private final TextureAtlasSprite numberExpr;
    private final TextureAtlasSprite scopeEnclosure;
    private final TextureAtlasSprite scopeEnd;
    private final TextureAtlasSprite scopeEndWithCode;
    private final TextureAtlasSprite simpleBlock;
    private final TextureAtlasSprite modifyIf;

    public WidgetRenderer(GuiSpriteManager sprites, Matrix4f pose) {
        this.sprites = sprites;
        this.pose = pose;
        RenderSystem.setShaderTexture(0, ResourceLocation.withDefaultNamespace("textures/atlas/gui.png"));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        this.boolExpr = sprites.getSprite(CodeWidgetSprites.BOOL_EXPR);
        this.elseConditionHead = sprites.getSprite(CodeWidgetSprites.ELSE_CONDITION_HEAD);
        this.genericExpr = sprites.getSprite(CodeWidgetSprites.GENERIC_EXPR);
        this.loopHead = sprites.getSprite(CodeWidgetSprites.LOOP_HEAD);
        this.methodHead = sprites.getSprite(CodeWidgetSprites.METHOD_HEAD);
        this.numberExpr = sprites.getSprite(CodeWidgetSprites.NUMBER_EXPR);
        this.scopeEnclosure = sprites.getSprite(CodeWidgetSprites.SCOPE_ENCLOSURE);
        this.scopeEnd = sprites.getSprite(CodeWidgetSprites.SCOPE_END);
        this.scopeEndWithCode = sprites.getSprite(CodeWidgetSprites.SCOPE_END_WITH_CODE);
        this.simpleBlock = sprites.getSprite(CodeWidgetSprites.SIMPLE_BLOCK);
        this.modifyIf = sprites.getSprite(CodeWidgetSprites.MODIFY_IF);
    }

    public void renderBoolExpr(int x, int y, int width, int height) {
        renderSprite(this.boolExpr, x, y, width, height);
    }

    public void renderElseConditionHead(int x, int y, int width, int height) {
        renderSprite(this.elseConditionHead, x, y, width, height);
    }

    public void renderGenericExpr(int x, int y, int width, int height) {
        renderSprite(this.genericExpr, x, y, width, height);
    }

    public void renderLoopHead(int x, int y, int width, int height) {
        renderSprite(this.loopHead, x, y, width, height);
    }

    public void renderMethodHead(int x, int y, int width, int height) {
        renderSprite(this.methodHead, x, y, width, height);
    }

    public void renderNumberExpr(int x, int y, int width, int height) {
        renderSprite(this.numberExpr, x, y, width, height);
    }

    public void renderScopeEnclosure(int x, int y, int width, int height) {
        renderSprite(this.scopeEnclosure, x, y, width, height);
    }

    public void renderScopeEnd(int x, int y, int width, int height) {
        renderSprite(scopeEnd, x, y, width, height);
    }

    public void renderScopeEndWithCode(int x, int y, int width, int height) {
        renderSprite(scopeEndWithCode, x, y, width, height);
    }

    public void renderSimpleBlock(int x, int y, int width, int height) {
        renderSprite(simpleBlock, x, y, width, height);
    }

    public void renderModifyIf(int x, int y, int width, int height) {
        renderSprite(modifyIf, x, y, width, height);
    }


    public void renderExpr(ExprCategory type, int x, int y, int width, int height) {
        renderSprite(switch (type) {
            case NUMBER -> numberExpr;
            case BOOLEAN -> boolExpr;
            case OTHER -> genericExpr;
        }, x, y, width, height);
    }

    private void renderSprite(TextureAtlasSprite sprite, int x, int y, int width, int height) {
        GuiSpriteScaling scaling = this.sprites.getSpriteScaling(sprite);
        switch (scaling) {
            case GuiSpriteScaling.Stretch ignored ->
                    this.blitSprite(sprite, x, y, width, height);
            case GuiSpriteScaling.Tile guispritescaling$tile -> this.blitTiledSprite(
                    sprite,
                    x,
                    y,
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
                    this.blitNineSlicedSprite(sprite, guispritescaling$nineslice, x, y, width, height);
            default -> {
            }
        }
    }

    public void draw() {
        RenderSystem.setShaderTexture(0, ResourceLocation.withDefaultNamespace("textures/atlas/gui.png"));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    private void blitNineSlicedSprite(
            TextureAtlasSprite sprite, GuiSpriteScaling.NineSlice nineSlice, int x, int y, int width, int height
    ) {
        GuiSpriteScaling.NineSlice.Border guispritescaling$nineslice$border = nineSlice.border();
        int left = Math.min(guispritescaling$nineslice$border.left(), width / 2);
        int right = Math.min(guispritescaling$nineslice$border.right(), width / 2);
        int top = Math.min(guispritescaling$nineslice$border.top(), height / 2);
        int bottom = Math.min(guispritescaling$nineslice$border.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, height);
        } else if (height == nineSlice.height()) {
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, left, height);
            this.blitTiledSprite(
                    sprite,
                    x + left,
                    y,
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
                    sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - right, 0, x + width - right, y, right, height
            );
        } else if (width == nineSlice.width()) {
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, top);
            this.blitTiledSprite(
                    sprite,
                    x,
                    y + top,
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
                    sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - bottom, x, y + height - bottom, width, bottom
            );
        } else {
            //top left
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, left, top);
            //top middle
            this.blitTiledSprite(
                    sprite, x + left, y, width - right - left, top, left, 0, nineSlice.width() - right - left, top, nineSlice.width(), nineSlice.height()
            );
            //top right
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - right, 0, x + width - right, y, right, top);
            //bottom left
            this.blitSprite(sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - bottom, x, y + height - bottom, left, bottom);
            this.blitTiledSprite(
                    sprite,
                    x + left,
                    y + height - bottom,
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
                    right,
                    bottom
            );
            this.blitTiledSprite(
                    sprite,
                    x,
                    y + top,
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
                        this.blitSprite(sprite, nineSliceWidth, nineSliceHeight, uPosition, vPosition, x + i, y + k, j, l);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + spriteWidth + "x" + spriteHeight);
            }
        }
    }

    private void blitSprite(TextureAtlasSprite sprite, int x, int y, int width, int height) {
        if (width != 0 && height != 0) {
            this.innerBlit(
                    x,
                    x + width,
                    y,
                    y + height,
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
            int uWidth,
            int vHeight
    ) {
        if (uWidth != 0 && vHeight != 0) {
            this.innerBlit(
                    x,
                    x + uWidth,
                    y,
                    y + vHeight,
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
            float minU,
            float maxU,
            float minV,
            float maxV
    ) {
        builder.addVertex(pose, (float)x1, (float)y1, 0).setUv(minU, minV);
        builder.addVertex(pose, (float)x1, (float)y2, 0).setUv(minU, maxV);
        builder.addVertex(pose, (float)x2, (float)y2, 0).setUv(maxU, maxV);
        builder.addVertex(pose, (float)x2, (float)y1, 0).setUv(maxU, minV);
    }
}
