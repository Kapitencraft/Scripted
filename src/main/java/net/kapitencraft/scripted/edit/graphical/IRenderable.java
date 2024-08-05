package net.kapitencraft.scripted.edit.graphical;

public interface IRenderable {

    boolean allowMethodRendering();

    String translationKey();

    RenderMap getParamData();
}
