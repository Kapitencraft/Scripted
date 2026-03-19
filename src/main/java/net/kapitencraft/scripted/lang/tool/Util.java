package net.kapitencraft.scripted.lang.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Util {

    public static <K, V> Map<K, V> mergeMaps(Map<? extends K, ? extends V> base, Map<? extends K, ? extends V> extension) {
        Map<K, V> temp = new HashMap<>(base);
        temp.putAll(extension);
        return Map.copyOf(temp);
    }

    public static void delete(File file) {
        File[] files = file.listFiles();
        if (files != null) for (File subFile : files) {
            delete(subFile);
        }
        file.delete();
    }

    //that's because Objects.requireNonNullElse checks else for null
    public static <K> K nonNullElse(K main, K other) {
        return main != null ? main : other;
    }

    public static boolean matchArgs(ClassReference[] got, ClassReference[] expected) {
        if (got.length != expected.length) return false;
        if (got.length == 0) return true;
        for (int i = 0; i < got.length; i++) {
            ClassReference gotType = got[i];
            ClassReference expectedType = expected[i];
            if (gotType == null || expectedType == null) return false;
            if (!gotType.is(expectedType)) return false;
        }
        return true;
    }

    public static String getDescriptor(ClassReference[] args) {
        return Arrays.stream(args).map(ClassReference::name).collect(Collectors.joining(","));
    }

    /**
     * get all elements of a Map containing a Map
     */
    static <T, K, L, J extends Map<K, L>> List<L> values(Map<T, J> map) {
        return map.values().stream().map(Map::values).flatMap(Collection::stream).toList();
    }

    public static List<File> listResources(File file, @Nullable String fileSuffix) {
        if (!file.exists()) return List.of();
        if (!file.isDirectory()) return List.of(file);
        List<File> finals = new ArrayList<>();
        List<File> queue = new ArrayList<>();
        queue.add(file);
        while (!queue.isEmpty()) {
            File el = queue.get(0);
            if (el.isDirectory()) {
                File[] files = el.listFiles();
                if (files != null) queue.addAll(List.of(files));
            } else {
                if (fileSuffix == null || el.getPath().endsWith(fileSuffix)) finals.add(el);
            }
            queue.remove(0);
        }
        return finals;
    }

    /**
     * @param map the map to write
     * @param keyMapper a function to convert each key of the map into an JsonElement
     * @param valueMapper a function to convert each value of the map into an JsonElement
     * @return an JsonArray containing each entry of the map
     */
    public static <K, V> JsonArray writeMap(Map<K, V> map, Function<K, JsonElement> keyMapper, Function<V, JsonElement> valueMapper) {
        JsonArray array = new JsonArray(map.size());
        map.forEach((k, v) -> {
            JsonObject object = new JsonObject();
            object.add("key", keyMapper.apply(k));
            object.add("value", valueMapper.apply(v));
        });
        return array;
    }

    /**
     * @param array the json array containing all map entries
     * @param keyExtractor a function that extracts each map key out of the entry
     * @param valueExtractor a function that extracts each map value out of the entry
     * @return a MapStream containing all the entries extracted
     */
    public static <K, V> Map<K, V> readMap(JsonArray array, BiFunction<JsonObject, String, K> keyExtractor, BiFunction<JsonObject, String, V> valueExtractor) {
        HashMap<K, V> map = new HashMap<>();
        array.asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(object -> map.put(keyExtractor.apply(object, "key"), valueExtractor.apply(object, "value")));
        return map;
    }

    public static <T> List<T> invert(List<T> parents) {
        List<T> inverted = new ArrayList<>();
        for (int i = parents.size()-1; i >= 0; i--) {
            inverted.add(parents.get(i));
        }
        return inverted;
    }

    public static <K, V, L> Collector<L, ?, List<Pair<K, V>>> toPairList(Function<L, K> keyMapper, Function<L, V> valueMapper) {
        return Collector.of(
                ArrayList::new,
                (pairs, l) -> pairs.add(new Pair<>(keyMapper.apply(l), valueMapper.apply(l))),
                (pairs, pairs2) -> {
                    pairs.addAll(pairs2);
                    return pairs;
                }
        );
    }

    public static <T> boolean arrayContains(T[] array, T val) {
        for (T t : array) if (t.equals(val)) return true;
        return false;
    }

    public static Pair<ScriptedCallable, ScriptedClass> getVirtualMethod(ScriptedClass targetClass, String method, ClassReference[] args) {
        DataMethodContainer container = targetClass.getMethods().get(method);
        ScriptedCallable m;
        if (container == null || (m = container.getMethod(args)) == null) {
            return getVirtualMethod(targetClass.superclass().get(), method, args);
        }
        return Pair.of(m, targetClass);
    }

    public static ScriptedCallable getStaticMethod(ScriptedClass targetClass, String name, ClassReference[] args) {
        if (!targetClass.hasMethod(name)) {
            return null;
        }
        DataMethodContainer container = targetClass.getMethods().get(name);
        if (container == null)
            throw new IllegalStateException("unable to obtain method container for name '" + name + "' on class '" + targetClass.absoluteName() + "'");
        int match = 0;
            ScriptedCallable matchEntry = container.methods()[0];
        for (ScriptedCallable method : container.methods()) {
            ClassReference[] argTypes = method.argTypes();
            for (int i = 0; i < argTypes.length && i < args.length; i++) {
                if (argTypes[i].equals(args[i])) {
                    if (i + 1 > match) {
                        match = i;
                        matchEntry = method;
                    }
                }
            }
        }
        return matchEntry;
    }

    public static int getLargets(List<String> entries) {
        if (entries.isEmpty()) return 0;
        int width = entries.get(0).length();
        for (int i = 1; i < entries.size(); i++) {
            if (width < entries.get(i).length()) {
                width = entries.get(i).length();
            }
        }
        return width;
    }

    public static String objToString(Object o) {
        if (o == null) return "null";
        Class<?> eClass = o.getClass();
        if (eClass.isArray()) {
            if (eClass == byte[].class)
                return Arrays.toString((byte[]) o);
            else if (eClass == short[].class)
                return Arrays.toString((short[]) o);
            else if (eClass == int[].class)
                return Arrays.toString((int[]) o);
            else if (eClass == long[].class)
                return Arrays.toString((long[]) o);
            else if (eClass == char[].class)
                return Arrays.toString((char[]) o);
            else if (eClass == float[].class)
                return Arrays.toString((float[]) o);
            else if (eClass == double[].class)
                return Arrays.toString((double[]) o);
            else if (eClass == boolean[].class)
                return Arrays.toString((boolean[]) o);
            else { // element is an array of object references
                return Arrays.deepToString((Object[]) o);
            }
        }
        return o.toString();
    }
}
