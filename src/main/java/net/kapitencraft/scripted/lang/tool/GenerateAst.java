package net.kapitencraft.scripted.lang.tool;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateAst {
    private static final Gson GSON = new GsonBuilder().create();

    public static final String DIRECTORY = "src/main/java/net/kapitencraft/lang/holder/ast";
    private static final String SOURCE = "src/generate_ast.json";

    public static void main(String[] args) throws IOException {
        JsonObject object = GSON.fromJson(new FileReader(SOURCE), JsonObject.class);
        Imports defaultImports = Imports.fromJsonElement(object.get("imports"));

        Map<String, Map<String, JsonElement>> obj = createObj(object.get("values"));
        obj.forEach((astType, data) -> {
            Imports imports = Imports.fromJsonElement(data.get("imports"));
            Map<String, AstDef> valueData = createValues(data.get("values"));
            defineAst(astType, valueData, imports, defaultImports);
        });
    }

    private static Map<String, AstDef> createValues(JsonElement valuesData) {
        JsonObject object = valuesData.getAsJsonObject();
        Map<String, AstDef> data = new HashMap<>();
        object.asMap().forEach((s, element) -> data.put(s, AstDef.fromJson(element)));
        return data;
    }

    private static Map<String, Map<String, JsonElement>> createObj(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        Map<String, Map<String, JsonElement>> data = new HashMap<>();
        object.asMap().forEach((s, typeData) ->
                data.put(s, typeData.getAsJsonObject().asMap()));
        return data;
    }

    private static void defineAst(String baseName, Map<String, AstDef> data, Imports imports, Imports defaultImports) {
        defineAstFile(baseName, data, imports, defaultImports);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void defineAstFile(String baseName, Map<String, AstDef> data, Imports imports, Imports defaultImports) {
        String path = DIRECTORY + "/" + baseName + ".java";
        PrintWriter writer;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            writer = new PrintWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error defining AST '" + baseName + "': " + e.getMessage());
            return;
        }

        writer.println("package net.kapitencraft.lang.holder.ast;");
        writer.println();
        for (String s : defaultImports.get()) {
            writer.print("import ");
            writer.print(s);
            writer.println(";");
        }
        for (String s : imports.get()) {
            writer.print("import ");
            writer.print(s);
            writer.println(";");
        }
        writer.println();
        writer.println("public interface " + baseName + " {");
        writer.println();

        defineVisitor(writer, baseName, data.keySet());

        writer.println();
        writer.println("    <R> R accept(Visitor<R> visitor);");

        // The AST classes.
        for (String typeId : data.keySet()) {
            defineType(writer, baseName, baseName, typeId, data.get(typeId));
        }

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, Set<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type : types) {
            writer.println("        R visit" + type + baseName + "(" +
                    type + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String extendedName, String typeId, AstDef types) {
        writer.println();
        FieldDef[] fields = types.get();
        writer.println("    record " + typeId + "(");

        writer.println(Arrays.stream(fields)
                .map(f -> "        " + f.type.get() + " " + f.name)
                .collect(Collectors.joining(", \n")));

        writer.println("    ) implements " + extendedName + " {");

        // Visitor pattern.
        writer.println();
        writer.println("        @Override");
        writer.println("        public <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" +
                typeId + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");

    }

    private record TypeDef(String compile) {

        private static TypeDef expand(String compile) {
            return new TypeDef(
                    compile
            );
        }

        public static TypeDef fromJsonElement(JsonElement element) {
            if (element.isJsonPrimitive()) {
                String val = element.getAsString();
                return expand(val);
            }
            throw new JsonParseException("don't know how to turn '" + element + "' into a TypeDef");
        }

        public String get() {
            return compile;
        }
    }

    private record Imports(String[] compile) {

        public static Imports fromJsonElement(JsonElement element) {
            if (element.isJsonArray()) {
                String[] val = collectArray(element.getAsJsonArray());
                return new Imports(val);
            }
            throw new IllegalArgumentException("don't know how to turn '" + element + "' into a TypeDef");
        }

        private static String[] collectArray(JsonArray array) {
            return array.asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
        }

        public String[] get() {
            return compile;
        }
    }

    private record FieldDef(String name, TypeDef type) {
        public static FieldDef fromJson(String name, JsonElement value) {
            return new FieldDef(name, TypeDef.fromJsonElement(value));
        }

        public FieldDef trim() {
            return new FieldDef(name.replaceAll("[$%]", ""), type);
        }
    }

    private record AstDef(FieldDef[] compileFields) {

        public FieldDef[] get() {
            return compileFields;
        }

        public static AstDef fromJson(JsonElement element) {
            List<FieldDef> compile = new ArrayList<>();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                FieldDef def = FieldDef.fromJson(entry.getKey(), entry.getValue());
                compile.add(def.trim());
            }
            return new AstDef(
                    compile.toArray(new FieldDef[0])
            );
        }
    }
}