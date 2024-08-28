package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonPrimitive;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.var.type.collection.RegistryListType;
import net.kapitencraft.scripted.init.custom.ModCallbacks;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class RegistryType<V> extends PrimitiveType<V> {
    public static final Map<String, RegistryType<?>> TYPES_FOR_NAME = ModRegistries.VAR_TYPES.getSlaveMap(ModCallbacks.VarTypes.REGISTRIES, Map.class);

    private final IForgeRegistry<V> registry;

    protected RegistryType(String name, IForgeRegistry<V> registry) {
        super(name, null, null, null, null, null, null);
        this.registry = registry;
    }

    public static MethodInstance<?> readInstance(String value) {
        RegistryType<?> type = TYPES_FOR_NAME.get(value.substring(0, value.indexOf(":")));
        String[] separated = value.split(":");
        for (int i = 1; i < 3; i++) {
            if (separated[i].isEmpty()) return type.listOf().createInstance(value);
        }
        return type.readPrimitiveInstance(value);
    }

    public Collection<V> getContent() {
        return registry.getValues();
    }

    public void render(int x, int y, V value, GuiGraphics graphics) {
        Component name = Component.translatable(Util.makeDescriptionId(registry.getRegistryName().getPath(), registry.getKey(value)));
        graphics.drawString(Minecraft.getInstance().font, name, x + 1, y + 1, -1);
    }

    @Override
    public V loadPrimitive(String string) {
        return registry.getValue(new ResourceLocation(string));
    }

    @Override
    public JsonPrimitive saveToJson(V value) {
        return new JsonPrimitive(Objects.requireNonNull(registry.getKey(value), "value '" + value + "' is not part of registry '" + registry.getRegistryName() + "'").toString());
    }

    @Override
    public RegistryListType<V> listOf() {
        return new RegistryListType<>(this); //return custom type as it's required
    }

    public String getRegKey() {
        return this.registry.getRegistryName().getPath();
    }


    @Override
    public V loadFromJson(JsonPrimitive prim) {
        return registry.getValue(new ResourceLocation(prim.getAsString()));
    }

    public List<V> readListValue(String string) {
        String[] split = string.split(":");
        if (split.length != 3 || split[0].isEmpty()) {
            throw new IllegalStateException("not valid registry key '" + string + "'");
        }
        if (split[0].startsWith("#")) {//Tag
            ITagManager<V> manager = this.registry.tags();
            if (manager != null) {
                if (split[1].isEmpty()) {
                    split[1] = "minecraft"; //make namespace minecraft if not defined
                }
                ITag<V> tag = manager.getTag(TagKey.create(registry.getRegistryKey(), new ResourceLocation(split[1], split[2])));
                return tag.stream().toList();
            } else throw new IllegalStateException("registry " + registry.getRegistryName() + " does not support tags");
        } else {
            if (split[1].isEmpty()) {
                if (split[2].isEmpty()) {
                    return getContent().stream().toList();
                } else {
                    //noinspection DataFlowIssue; it will not be an issue
                    return getContent().stream().filter(v -> registry.getKey(v).getPath().contains(split[2])).toList();
                }
            } else {
                if (split[2].isEmpty()) {
                    //noinspection DataFlowIssue; it will not be an issue
                    return getContent().stream().filter(v -> split[1].equals(registry.getKey(v).getNamespace())).toList();
                } else {
                    throw new IllegalArgumentException("'" + string + "' is not a valid List<" + this.getName() + "> value");
                }
            }
        }
    }
}
