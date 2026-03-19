package net.kapitencraft.scripted.lang.holder;

import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;

public record LiteralHolder(Object value, ScriptedClass type) {

    public static final LiteralHolder EMPTY = new LiteralHolder(null, null);
}
