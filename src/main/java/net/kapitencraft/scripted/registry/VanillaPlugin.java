package net.kapitencraft.scripted.registry;

import net.kapitencraft.scripted.lang.run.natives.ClassRegistration;
import net.kapitencraft.scripted.lang.run.natives.ScriptedPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@ScriptedPlugin
public class VanillaPlugin {

    public static void registerClasses(ClassRegistration registration) {
        registration.registerClass(Level.class);
        registration.registerClass(BlockPos.class);
        registration.registerClass(BlockState.class);
        registration.registerClass(Block.class);
        registration.registerClass(Vec3.class);
        registration.registerClass(Entity.class);
    }
}
