package net.kapitencraft.scripted.lang.exe.natives.scripted.exe;

import net.kapitencraft.scripted.lang.exe.natives.NativeClass;
import net.minecraft.world.entity.player.Player;

@NativeClass(pck = "scripted.exe")
public class CommandData {
    private final Player player;

    public CommandData(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
