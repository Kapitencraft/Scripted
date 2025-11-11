package net.kapitencraft.scripted.edit.graphical;

import net.minecraft.resources.ResourceLocation;

public enum ExprType {
    NUMBER(CodeWidgetSprites.NUMBER_EXPR),
    BOOLEAN(CodeWidgetSprites.BOOL_EXPR),
    OTHER(CodeWidgetSprites.GENERIC_EXPR);

    private final ResourceLocation spriteLocation;

    ExprType(ResourceLocation spriteLocation) {
        this.spriteLocation = spriteLocation;
    }

    public ResourceLocation getSpriteLocation() {
        return spriteLocation;
    }
}
