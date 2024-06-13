package net.kapitencraft.scripted.edit.client.text;

import net.kapitencraft.scripted.code.exe.IExecutable;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.mapper.PrimitiveReference;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
    private static final Pattern PIPELINE_MATCH = Pattern.compile("\\{([\\w,_();{}]+)}");
    private static final Pattern METHOD_MATCH = Pattern.compile("(\\w+)\\(((\\w+,?)*)\\)");
    private static final Pattern FIELD_MATCH = Pattern.compile("(\\w+)\\.(\\w+)");
    private static final Pattern VAR_INITIALIZER = Pattern.compile("(((final)? \\w+)?) (\\w+) =");

    public static void compileText(String in, VarAnalyser analyser) {
        in = in.replaceAll("[\\s[^ ]]+", ""); //remove any whitespace besides space (cuz we don't need it)
        String[]
    }

    public static void compilePipeline(String in) {
        String[] methods = in.split(";");
        for (String method : methods) {
            compileMethod(method);
        }
    }

    public static IExecutable compileFunction(String string, VarAnalyser analyser) {
        Matcher matcher = VAR_INITIALIZER.matcher(string);
        if (matcher.matches()) {
            String def
        }
    }

    public static Method<?>.Instance compileMethod(String in) {
        Method<?>.Instance instance = PrimitiveReference.loadFromString(in);
        if (instance != null) return instance;

    }
}
