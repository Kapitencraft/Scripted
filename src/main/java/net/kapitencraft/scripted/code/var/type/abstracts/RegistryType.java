package net.kapitencraft.scripted.code.var.type.abstracts;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.ISpecialMethod;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.exe.methods.param.WildCardData;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class RegistryType<V> extends PrimitiveType<V> {
    private final IForgeRegistry<V> registry;

    protected RegistryType(String name, IForgeRegistry<V> registry) {
        super(name, null, null, null, null, null, null);
        this.registry = registry;
    }

    public Collection<V> getContent() {
        return registry.getValues();
    }

    public void render(int x, int y, V value, GuiGraphics graphics) {
        Component name = Component.translatable(Util.makeDescriptionId(registry.getRegistryName().getPath(), registry.getKey(value)));
        graphics.drawString(Minecraft.getInstance().font, name, x + 1, y + 1, -1);
    }

    @Override
    public Pattern matcher() {
        return Pattern.compile(this.registry.getRegistryName().getPath() + ":" + PrimitiveType.RESOURCE_LOCATION_MATCHER.pattern());
    }

    @Override
    public V loadPrimitive(String string) {
        return registry.getValue(new ResourceLocation(string));
    }

    @Override
    public void saveToJson(JsonObject object, V value) {
        object.addProperty("value", Objects.requireNonNull(registry.getKey(value), "value '" + value + "' is not part of registry '" + registry.getRegistryName() + "'").toString());
    }

    @Override
    public VarType<List<V>> listOf() {
        return new ListType();
    } //return custom type as it's required

    public class ListType extends VarType<V>.ListType {
        public ListType() {
            this.setConstructor(new Constructor());
        }

        private RegistryType<V>.ListType.Constructor.Instance create(String string, VarAnalyser analyser) {
            return (Constructor.Instance) ((Constructor) this.constructor).create(string, analyser, WildCardData.empty());
        }

        public class Constructor extends RegistryType<List<V>>.Constructor implements ISpecialMethod<List<V>> {

            protected Constructor() {
                super(ParamSet.empty(), "constructor");
            }

            @Override
            public Method<List<V>>.@Nullable Instance create(String in, VarAnalyser analyser, WildCardData data) {
                return new Instance(in);
            }

            @Override
            public boolean isInstance(String string) {
                return string.split(":").length == 3;
            }

            @Override
            public Method<List<V>>.Instance construct(JsonObject object, VarAnalyser analyser) {
                return null;
            }

            @Override
            public Method<List<V>>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
                return null;
            }

            @Override
            protected Method<List<V>>.Instance create(ParamData data, Method<?>.Instance parent) {
                return null;
            }

            private class Instance extends Method<List<V>>.Instance {
                private final List<V> val;
                private final String saveVal;

                protected Instance(String saveVal) {
                    super(ParamData.empty());
                    this.saveVal = saveVal;
                    this.val = RegistryType.this.readValue(saveVal);
                }

                @Override
                protected List<V> call(VarMap params) {
                    return val;
                }

                @Override
                public VarType<List<V>> getType(IVarAnalyser analyser) {
                    return RegistryType.ListType.this;
                }
            }
        }
    }


    @Override
    public V loadFromJson(JsonObject object) {
        return registry.getValue(new ResourceLocation(GsonHelper.getAsString(object, "value")));
    }

    public static <T> RegistryType<T>.Reference load(String string) {

    }

    private List<V> readValue(String string) {
        String[] split = string.split(":");
        if (split.length != 3 || split[0].isEmpty()) {
            throw new IllegalStateException("not valid registry key '" + string + "'");
        }
        if (split[0].startsWith("#")) {//Tag
            ITagManager<V> manager = this.registry.tags();
            if (split[1].isEmpty()) {
                split[1] = "minecraft"; //make namespace minecraft if not defined
            }
            if (manager != null) {
                ITag<V> tag = manager.getTag(TagKey.create(registry.getRegistryKey(), new ResourceLocation(split[1], split[2])));
                return tag.stream().toList();
            } else throw new IllegalStateException("could not find tag '" + String.join(":", split) + "'");
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
                    //noinspection DataFlowIssue
                    return getContent().stream().filter(v -> split[1].equals(registry.getKey(v).getNamespace())).toList();
                } else {
                    throw new IllegalArgumentException("'" + string + "' is not a valid List<" + this.getName() + "> value");
                }
            }
        }
    }
}
