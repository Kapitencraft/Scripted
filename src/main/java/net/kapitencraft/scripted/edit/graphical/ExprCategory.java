package net.kapitencraft.scripted.edit.graphical;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ExprCategory implements StringRepresentable {
    NUMBER(CodeWidgetSprites.NUMBER_EXPR),
    BOOLEAN(CodeWidgetSprites.BOOL_EXPR),
    OTHER(CodeWidgetSprites.GENERIC_EXPR);

    private final ResourceLocation spriteLocation;

    public static final Codec<ExprCategory> CODEC = StringRepresentable.fromEnum(ExprCategory::values);

    ExprCategory(ResourceLocation spriteLocation) {
        this.spriteLocation = spriteLocation;
    }

    public ResourceLocation getSpriteLocation() {
        return spriteLocation;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase();
    }
}
