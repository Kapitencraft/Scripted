package net.kapitencraft.scripted.edit.client;

public interface IRenderable {

    boolean allowMethodRendering();

    String translationKey();

    RenderMap getParamData();
}
