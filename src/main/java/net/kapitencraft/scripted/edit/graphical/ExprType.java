package net.kapitencraft.scripted.edit.graphical;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum ExprType implements StringRepresentable {
    NUMBER(CodeWidgetSprites.NUMBER_EXPR),
    BOOLEAN(CodeWidgetSprites.BOOL_EXPR),
    OTHER(CodeWidgetSprites.GENERIC_EXPR);

    private final ResourceLocation spriteLocation;

    public static final Codec<ExprType> CODEC = StringRepresentable.fromEnum(ExprType::values);

    ExprType(ResourceLocation spriteLocation) {
        this.spriteLocation = spriteLocation;
    }

    public ResourceLocation getSpriteLocation() {
        return spriteLocation;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
