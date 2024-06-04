package net.kapitencraft.scripted.code.var.type.abstracts;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kapitencraft.scripted.code.var.VarType;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;
import java.util.List;

public abstract class RegistryType<T> extends VarType<T> {
    private final IForgeRegistry<T> registry;

    protected RegistryType(IForgeRegistry<T> registry) {
        this.registry = registry;
    }

    public Collection<T> getContent() {
        return registry.getValues();
    }

    public void render(int x, int y, T value, GuiGraphics graphics) {
        Component name = Component.translatable(Util.makeDescriptionId(registry.getRegistryName().getPath(), registry.getKey(value)));
        graphics.drawString(Minecraft.getInstance().font, name, x + 1, y + 1, -1);
    }
}