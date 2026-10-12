package net.kapitencraft.scripted.lang.holder.oop.attribute;

import net.kapitencraft.scripted.lang.holder.token.Token;

public record EnumConstantHolder(Token name, int ordinal, Token[] arguments) {
}
