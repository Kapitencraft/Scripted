package net.kapitencraft.scripted.lang.exe.natives.scripted.exe;

import net.kapitencraft.scripted.lang.exe.natives.NativeClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@NativeClass(pck = "scripted.exe")
public class CommandData {
    private final CommandSourceStack stack;

    public CommandData(CommandSourceStack stack) {
        this.stack = stack;
    }

    public Player getPlayer() {
        return stack.getPlayer();
    }

    public ServerLevel getLevel() {
        return stack.getLevel();
    }

    public Vec3 getPos() {
        return stack.getPosition();
    }

    public BlockPos getBlockPos() {
        return BlockPos.containing(stack.getPosition());
    }
}
