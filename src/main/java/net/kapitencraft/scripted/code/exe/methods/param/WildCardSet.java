package net.kapitencraft.scripted.code.exe.methods.param;

import java.util.HashMap;

public class WildCardSet {
    /**
     * key: arg-name <br>
     * value: wild-card-name
     */
    private final HashMap<String, String> wildcards = new HashMap<>();

    public void addWildcard(String id, String name) {
        wildcards.put(name, id);
    }

    public String getWildCardId(String argId) {
        return wildcards.get(argId);
    }
}
