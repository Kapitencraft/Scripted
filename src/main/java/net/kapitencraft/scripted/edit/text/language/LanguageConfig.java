package net.kapitencraft.scripted.edit.text.language;

import net.kapitencraft.kap_lib.helpers.CollectorHelper;
import net.kapitencraft.scripted.edit.Token;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.Map;

public class LanguageConfig {
    private final Map<Token.Type.Category, ForgeConfigSpec.EnumValue<ChatFormatting>> colorsForCategory;
    private final ForgeConfigSpec spec;

    public LanguageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("colors");
        colorsForCategory = Arrays.stream(Token.Type.Category.values()).collect(CollectorHelper.createMap(category ->
                builder.comment("Color of " + category.getName())
                        .defineEnum(category.getId(), category.getDefault())
        ));
        spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    public ChatFormatting getForCategory(Token.Type.Category category) {
        return colorsForCategory.get(category).get();
    }
}
