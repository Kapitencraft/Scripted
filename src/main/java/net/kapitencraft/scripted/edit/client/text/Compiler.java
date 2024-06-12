package net.kapitencraft.scripted.edit.client.text;

import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.mapper.PrimitiveReference;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
    private static final Pattern PIPELINE_MATCH = Pattern.compile("\\{([\\w,_();{}]+)}");
    private static final Pattern METHOD_MATCH = Pattern.compile("(\\w+)\\(((\\w+,?)*)\\)");
    private static final Pattern FIELD_MATCH = Pattern.compile("(\\w+)\\.(\\w+)");

    public static void compileText(String in) {
        in = in.replaceAll("\\s+", ""); //remove any whitespace (cuz we don't need it)
        String[]
    }

    public static void compilePipeline(String in) {
        String[] methods = in.split(";");
        for (String method : methods) {
            compileMethod(method);
        }
    }

    public static Method<?>.Instance compileMethod(String in) {
        for (Map.Entry<Pattern, Function<String, PrimitiveReference<?>.Instance>> entry : PRIMITIVES.entrySet()) {
            Matcher matcher = entry.getKey().matcher(in);
            if (matcher.matches()) {
                return entry.getValue().apply(matcher.group(1));
            }
        }
    }
}
