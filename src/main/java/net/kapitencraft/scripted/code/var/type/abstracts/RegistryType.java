package net.kapitencraft.scripted.code.var.type.abstracts;

import net.kapitencraft.scripted.code.var.type.primitive.PrimitiveType;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;
import java.util.regex.Pattern;

public abstract class RegistryType<T> extends PrimitiveType<T> {
    private final IForgeRegistry<T> registry;

    protected RegistryType(IForgeRegistry<T> registry) {
        super(null, null, null, null, null, null);
        this.registry = registry;
    }

    public Collection<T> getContent() {
        return registry.getValues();
    }

    public void render(int x, int y, T value, GuiGraphics graphics) {
        Component name = Component.translatable(Util.makeDescriptionId(registry.getRegistryName().getPath(), registry.getKey(value)));
        graphics.drawString(Minecraft.getInstance().font, name, x + 1, y + 1, -1);
    }

    @Override
    public Pattern matcher() {
        return PrimitiveType.RESOURCE_LOCATION_MATCHER;
    }

    @Override
    public T loadPrimitive(String string) {
        return registry.getValue(new ResourceLocation(string));
    }
}
