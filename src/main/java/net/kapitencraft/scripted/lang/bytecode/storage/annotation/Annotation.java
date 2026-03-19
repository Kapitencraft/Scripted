package net.kapitencraft.scripted.lang.bytecode.storage.annotation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Annotation {

    private final String type;
    private final Map<String, EntryValue> entries;

    public Annotation(String type, Map<String, EntryValue> entries) {
        this.type = type;
        this.entries = entries;
    }

    public static Annotation empty(ScriptedClass type) {
        return new Annotation(VarTypeManager.getClassName(type), Map.of());
    }

    public static Annotation fromSingleProperty(ScriptedClass type, Expr singleProperty) {
        return new Annotation(VarTypeManager.getClassName(type), Map.of("value", fromExpr(singleProperty)));
    }

    public static Annotation fromPropertyMap(ScriptedClass type, Map<String, Expr> properties) {
        Map<String, EntryValue> entries = new HashMap<>();
        properties.forEach((string, expr) -> entries.put(string, fromExpr(expr)));
        return new Annotation(VarTypeManager.getClassName(type), entries);
    }

    public String getType() {
        return type;
    }

    public static Annotation[] readAnnotations(JsonObject data) {
        JsonArray array = GsonHelper.getAsJsonArray(data, "annotations");
        return array.asList().stream().map(JsonElement::getAsJsonObject).map(Annotation::readAnnotation).toArray(Annotation[]::new);
    }

    public static Annotation readAnnotation(JsonObject object) {
        String type = GsonHelper.getAsString(object, "type");
        JsonObject entries = GsonHelper.getAsJsonObject(object, "entries");
        Map<String, EntryValue> values = new HashMap<>();
        entries.asMap().forEach((string, jsonElement) -> values.put(string, EntryValue.parse(jsonElement)));
        return new Annotation(type, values);
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", this.type);
        JsonObject entries = new JsonObject();
        this.entries.forEach((string, annotationEntry) -> entries.add(string, EntryValue.toJson(annotationEntry)));
        object.add("entries", entries);
        return object;
    }

    public Object getProperty(String name) {
        return entries.get(name).value();
    }

    //region entry
    public enum EntryType {
        ANNOTATION(AnnotationValue::fromJson),
        STRING(StringValue::fromJson),
        NUMBER(NumberValue::fromJson),
        ENUM(EnumValue::fromJson),
        ARRAY(ArrayValue::fromJson);

        private final Function<JsonObject, EntryValue> constructor;

        EntryType(Function<JsonObject, EntryValue> constructor) {
            this.constructor = constructor;
        }
    }

    public interface EntryValue {
        static EntryValue parse(JsonElement jsonElement) {
            JsonObject entry = jsonElement.getAsJsonObject();

            EntryType value = EntryType.valueOf(GsonHelper.getAsString(entry, "type").toUpperCase());
            return value.constructor.apply(entry);
        }

        EntryType getType();

        Object value();

        static JsonObject toJson(EntryValue value) {
            JsonObject object = new JsonObject();
            object.addProperty("type", value.getType().name().toLowerCase());
            value.toJson(object);
            return object;
        }

        void toJson(JsonObject object);
    }

    private record AnnotationValue(Annotation annotation) implements EntryValue {
        private static AnnotationValue fromJson(JsonObject object) {
            return new AnnotationValue(readAnnotation(GsonHelper.getAsJsonObject(object, "annotation")));
        }

        @Override
        public EntryType getType() {
            return EntryType.ANNOTATION;
        }

        @Override
        public Object value() {
            return annotation;
        }

        @Override
        public void toJson(JsonObject object) {
            object.add("annotation", this.annotation.toJson());
        }
    }

    private record StringValue(String value) implements EntryValue {
        private static StringValue fromJson(JsonObject object) {
            return new StringValue(GsonHelper.getAsString(object, "value"));
        }

        @Override
        public EntryType getType() {
            return EntryType.STRING;
        }

        @Override
        public void toJson(JsonObject object) {
            object.addProperty("value", value);
        }
    }

    private record NumberValue(Number value) implements EntryValue {
        private static NumberValue fromJson(JsonObject object) {
            return new NumberValue(Objects.requireNonNull(object.getAsJsonPrimitive("value"), "missing \"value\" section in number value").getAsNumber());
        }

        @Override
        public EntryType getType() {
            return EntryType.NUMBER;
        }

        @Override
        public void toJson(JsonObject object) {
            object.addProperty("value", value);
        }
    }

    private record EnumValue(String className, String fieldName) implements EntryValue {
        private static EnumValue fromJson(JsonObject object) {
            return new EnumValue(GsonHelper.getAsString(object, "className"), GsonHelper.getAsString(object, "fieldName"));
        }

        @Override
        public EntryType getType() {
            return EntryType.ENUM;
        }

        @Override
        public Object value() {
            return VarTypeManager.directFlatParse(className).getStaticField(fieldName);
        }

        @Override
        public void toJson(JsonObject object) {
            object.addProperty("className", className);
            object.addProperty("fieldName", fieldName);
        }
    }

    private record ArrayValue(EntryValue[] entries) implements EntryValue {
        private static ArrayValue fromJson(JsonObject object) {
            JsonArray entries = GsonHelper.getAsJsonArray(object, "values");
            return new ArrayValue(entries.asList().stream().map(JsonElement::getAsJsonObject).map(EntryValue::parse).toArray(EntryValue[]::new));
        }


        @Override
        public EntryType getType() {
            return EntryType.ARRAY;
        }

        @Override
        public Object value() {
            Object[] values = new Object[entries.length];
            for (int i = 0; i < entries.length; i++) {
                values[i] = entries[i].value();
            }
            return values;
        }

        @Override
        public void toJson(JsonObject object) {
            JsonArray array = new JsonArray();
            for (EntryValue entry : entries) {
                array.add(EntryValue.toJson(entry));
            }
            object.add("values", array);
        }
    }

    public static EntryValue fromExpr(Expr expr) {
        if (expr instanceof Expr.Literal literal) {
            Object object = literal.literal().literal().value();
            if (object instanceof String s) return new StringValue(s);
            else return new NumberValue((Number) object);
        } else if (expr instanceof Expr.StaticGet get) {
            return new EnumValue(VarTypeManager.getClassName(get.target()), get.name().lexeme());
        } else if (expr instanceof Expr.ArrayConstructor constructor) {
            if (constructor.size() != null) return new ArrayValue(new EntryValue[0]);
            return new ArrayValue(Arrays.stream(constructor.obj()).map(Annotation::fromExpr).toArray(EntryValue[]::new));
        }
        return null; //TODO
    }

    //endregion
}
