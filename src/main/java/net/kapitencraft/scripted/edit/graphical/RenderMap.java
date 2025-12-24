package net.kapitencraft.scripted.edit.graphical;

import net.kapitencraft.scripted.edit.graphical.code.Code;

import java.util.HashMap;

public class RenderMap {

    private final HashMap<String, Code> params = new HashMap<>();

    public void addParam(String name, Code renderable) {
        params.put(name, renderable);
    }

    public Code getParam(String name) {
        return params.get(name);
    }
}