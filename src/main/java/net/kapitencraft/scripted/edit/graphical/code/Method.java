package net.kapitencraft.scripted.edit.graphical.code;

import net.kapitencraft.scripted.edit.graphical.RenderMap;

public record Method(String translationKey) implements Code {

    @Override
    public RenderMap getParamData() {
        return null;
    }
}
