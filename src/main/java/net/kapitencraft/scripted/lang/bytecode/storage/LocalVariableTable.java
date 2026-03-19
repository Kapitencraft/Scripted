package net.kapitencraft.scripted.lang.bytecode.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.kap_lib.core.helpers.CollectorHelper;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.tool.StringReader;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record LocalVariableTable(Entry[] entries) {

    public JsonElement save() {
        return Arrays.stream(entries).map(Entry::toJson).collect(CollectorHelper.toJsonArray());
    }

    public static LocalVariableTable read(JsonArray array) {
        return new LocalVariableTable(array.asList().stream().map(JsonElement::getAsJsonObject)
                .map(Entry::read).toArray(Entry[]::new));
    }

    public Pair<String, ClassReference> get(int pc, int i) {
        for (Entry entry : entries) {
            if (entry.index == i && entry.startPc <= pc && entry.startPc + entry.length >= pc)
                return Pair.of(entry.name, entry.type);
        }
        return Pair.of("UNKNOWN", VarTypeManager.VOID.reference());
    }

    private record Entry(int startPc, int length, String name, ClassReference type, int index) {

        public JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("startPc", startPc);
            object.addProperty("length", length);
            object.addProperty("name", name);
            object.addProperty("type", VarTypeManager.getClassName(type.get()));
            object.addProperty("index", index);
            return object;
        }

        public static Entry read(JsonObject object) {
            return new Entry(
                    GsonHelper.getAsInt(object, "startPc"),
                    GsonHelper.getAsInt(object, "length"),
                    GsonHelper.getAsString(object, "name"),
                    VarTypeManager.parseType(new StringReader(GsonHelper.getAsString(object, "type"))),
                    GsonHelper.getAsInt(object, "index")
            );
        }
    }

    public static class Builder {
        private final List<Stub> stubs = new ArrayList<>();
        private final List<Entry> entries = new ArrayList<>();

        public void addLocal(int position, int index, ClassReference type, String name) {
            stubs.add(new Stub(position, index, type, name));
        }

        public void endLocal(int id) {

        }

        public LocalVariableTable build(int codePos) {
            this.stubs.forEach(stub -> entries.add(stub.end(codePos))); //end the rest of the lines
            return new LocalVariableTable(this.entries.toArray(Entry[]::new));
        }

        public void clear() {
            this.stubs.clear();
            this.entries.clear();
        }

        private record Stub(int position, int index, ClassReference type, String name) {

            public Entry end(int codePos) {
                return new Entry(position, codePos - position, name, type, index);
            }
        }
    }
}
