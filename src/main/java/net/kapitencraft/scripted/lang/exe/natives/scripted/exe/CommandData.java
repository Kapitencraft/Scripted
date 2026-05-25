package net.kapitencraft.scripted.lang.exe.natives.scripted.exe;

import net.kapitencraft.scripted.lang.exe.natives.NativeClass;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@NativeClass(pck = "scripted.exe")
public class CommandData {
    private final Player player;
    private final BlockPos blockPos;
    private final Vec3 pos;
    private final ServerLevel level;

    public CommandData(Player player, BlockPos blockPos, Vec3 pos, ServerLevel level) {
        this.player = player;
        this.blockPos = blockPos;
        this.pos = pos;
        this.level = level;
    }

    public Player getPlayer() {
        return player;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Vec3 getPos() {
        return pos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
