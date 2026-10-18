package net.kapitencraft.scripted.lang.exe.natives.scripted.exe;

import net.kapitencraft.scripted.lang.exe.natives.NativeClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@NativeClass(pck = "scripted.exe")
public class CommandData {
    private final CommandSourceStack stack;

    public CommandData(CommandSourceStack stack) {
        this.stack = stack;
    }

    public Player getPlayer() {
        return this.stack.getPlayer();
    }

    public Level getLevel() {
        return this.stack.getLevel();
    }

    public BlockPos getBlockPosition() {
        return BlockPos.containing(this.stack.getPosition());
    }

    public Vec3 getPosition() {
        return this.stack.getPosition();
    }
}
