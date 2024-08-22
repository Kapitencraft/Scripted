package net.kapitencraft.scripted.code;

import net.kapitencraft.scripted.edit.Token;
import net.kapitencraft.scripted.edit.text.language.LanguageConfig;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fml.config.IConfigSpec;

public abstract class LanguageProvider {
    public abstract LanguageConfig getConfig();

    public Style getFormatting(Token.Type type) {
        return Style.EMPTY.withColor(getConfig().getForCategory(type.getCategory()));
    }
}
