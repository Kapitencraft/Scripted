package net.kapitencraft.scripted.edit.client;

import java.util.HashMap;

public class RenderMap {

    private final HashMap<String, IRenderable> params = new HashMap<>();

    public void addParam(String name, IRenderable renderable) {
        params.put(name, renderable);
    }

    public IRenderable getParam(String name) {
        return params.get(name);
    }
}
