package net.kapitencraft.scripted.lang.exe.natives.scripted;

import net.kapitencraft.scripted.lang.exe.natives.ClassRegistration;
import net.kapitencraft.scripted.lang.exe.natives.ScriptedPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

@ScriptedPlugin
public class VanillaPlugin {

    public static void registerClasses(ClassRegistration registration) {
        registration.registerClass(Level.class);
        registration.registerClass(BlockPos.class);
        registration.registerClassWithRegistry(BlockState.class, Registries.BLOCK);
        registration.registerClassWithRegistry(Block.class, Registries.BLOCK);
        registration.registerClass(Vec3.class);
        registration.registerClass(Entity.class);
        registration.registerClass(LivingEntity.class);
        registration.registerClass(Player.class);
        registration.registerClass(Property.class);
        registration.registerClass(Blocks.class);
    }
}
