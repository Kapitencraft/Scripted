package net.kapitencraft.scripted.lang.holder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.kap_lib.core.helpers.CollectorHelper;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record LineNumberTable(Pair<Integer, Integer>[] lines) {

    public static LineNumberTable read(JsonArray data) {
        return new LineNumberTable(data.asList().stream().map(JsonElement::getAsJsonObject)
                .map(o -> Pair.of(GsonHelper.getAsInt(o, "pc"), GsonHelper.getAsInt(o, "line")))
                .toArray(Pair[]::new));
    }

    public JsonArray save() {
        return Arrays.stream(this.lines).map(p -> {
            JsonObject object = new JsonObject();
            object.addProperty("pc", p.getFirst());
            object.addProperty("line", p.getSecond());
            return object;
        }).collect(CollectorHelper.toJsonArray());
    }

    public int getLineAt(int ip) {
        int i = 0;
        while (i < lines.length - 1 && lines[i].getFirst() < ip) i++;
        return lines[i].getSecond();
    }

    public static class Builder {
        private final List<Pair<Integer, Integer>> lineChanges = new ArrayList<>();

        public void change(int pc, int lineNumber) {
            lineChanges.add(Pair.of(pc, lineNumber));
        }

        public LineNumberTable build() {
            return new LineNumberTable(lineChanges.toArray(Pair[]::new));
        }

        public void changeIfNecessary(int line, int pc) {
            if (line > -1 && (this.lineChanges.isEmpty() || this.lineChanges.get(this.lineChanges.size() - 1).getSecond() != line)) {
                this.lineChanges.add(Pair.of(pc, line));
            }
        }

        public void clear() {
            this.lineChanges.clear();
        }
    }
}
