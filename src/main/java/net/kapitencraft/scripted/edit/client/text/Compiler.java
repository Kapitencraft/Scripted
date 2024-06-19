package net.kapitencraft.scripted.edit.client.text;

import net.kapitencraft.scripted.code.exe.IExecutable;
import net.kapitencraft.scripted.code.exe.Runnable;
import net.kapitencraft.scripted.code.exe.functions.abstracts.Function;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.mapper.VarReference;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.PrimitiveType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.ModFunctions;
import net.kapitencraft.scripted.init.ModMethods;
import net.kapitencraft.scripted.io.IOHelper;
import net.kapitencraft.scripted.io.StringSegment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Compiler {
    private static final Pattern PIPELINE_MATCH = Pattern.compile("\\{([\\w,_();{} ]+)}");
    private static final Pattern METHOD_MATCH = Pattern.compile("(\\w+)\\((([\\w.()]+,?)*)\\)");
    private static final Pattern FIELD_MATCH = Pattern.compile("(\\w+)\\.(\\w+)");
    private static final Pattern CLEAR_LAST_MATCH = Pattern.compile("\\.([\\w,()_]+)$");
    private static final Pattern VAR_INITIALIZER = Pattern.compile("(((final )?\\w+)?) (\\w+)(( = )?)"); //TODO fix method invocation

    public static void compileText(String in, VarAnalyser analyser) {
        in = in.replaceAll("[\t\n]+", ""); //remove any whitespace besides space (cuz we don't need it)
    }

    public static void compilePipeline(String in) {
        String[] methods = in.split(";");
        for (String method : methods) {
        }
    }

    public static <T> Runnable compileFunction(String string, VarAnalyser analyser) {
        Matcher matcher = VAR_INITIALIZER.matcher(string);
        if (matcher.matches()) {
            //var type & final

            String definition = matcher.group(1);
            boolean isFinal = definition != null && definition.startsWith("final");
            String varTypeId = definition != null ? definition.split(" ")[1] : null;
            VarType<?> type = varTypeId != null ? IOHelper.readFromCode(varTypeId) : null;

            //name
            String varName = matcher.group(2);

            //setter
            String setter = matcher.group(3);
            Method<T>.Instance inst = setter == null ? null : (Method<T>.Instance) compileMethodChain(setter, true, analyser);

            if (inst == null) {
                return ModFunctions.CREATE_VAR.get().create(varName, Objects.requireNonNull(type, "can not create var creator, VarType '" + varTypeId + "' does not exist"), isFinal);
            } else {
                if (type == null) {
                    return ModFunctions.SET_VAR.get().create(varName, inst);
                }
                return ModFunctions.CREATE_AND_SET_VAR.get().create(varName, inst, (VarType<T>) type, isFinal);
            }
        } else {
            string = string.replaceAll(" ", ""); //remove space since we don't need it anymore
            Method<?>.Instance method = compileMethodChain(string, false, analyser);
            if (method != null) return method;
            IExecutable executable = compileFunctionChain(string, analyser);
            if (executable != null) return executable;
        }
        return null;
    }

    public static <T> Method<T>.@Nullable Instance compileMethodChain(String in, boolean allowPrimitive, VarAnalyser analyser, VarType<T> type) {
        if (allowPrimitive && type instanceof PrimitiveType<T> primitiveType) {
            Method<T>.Instance instance = PrimitiveType.loadReferenceFromString(in, primitiveType);
            if (instance != null) return instance;
        }
        String[] chain = in.split("\\.");
        Method<?>.Instance inst = null;
        for (String s : chain) {
            Matcher matcher = METHOD_MATCH.matcher(s);
            if (matcher.matches()) {
                String name = matcher.group(1);
                if (inst != null) {
                    VarType<?> varType = inst.getType(analyser);
                    inst = varType.getMethodForName(name).readFromCode(matcher.group(2), analyser, inst);
                }
            }
            if (inst == null) {
                inst = ModMethods.VAR_REFERENCE.get().create(s);
                continue;
            }
            if (!s.contains("(") && inst instanceof VarReference<?>.Instance varInstance) {
                VarType<?> varType = inst.getType(analyser);
                return varType.createFieldReference(s, varInstance);
            }
        }
        return (Method<T>.Instance) inst;
    }

    public static @Nullable IExecutable compileFunctionChain(String in, VarAnalyser analyser) {
        if (in.contains(".")) {
            Matcher matcher = CLEAR_LAST_MATCH.matcher(in);
            if (matcher.matches()) {
                Method<?>.Instance inst = compileMethodChain(in.substring(0, matcher.start() - 1), true, analyser);
                if (inst == null) return null;
                Matcher matcher1 = METHOD_MATCH.matcher(matcher.group(1));
                if (matcher1.matches()) {
                    return inst.getType(analyser).loadFunction(matcher1.group(1), matcher1.group(2), analyser);
                }
            }
        } else {
            Matcher matcher = METHOD_MATCH.matcher(in);
            if (matcher.matches()) {
                String id = matcher.group(1);
                Function function = IOHelper.readFunction(id);
                return function != null ? function.createFromCode(matcher.group(2), analyser) : null;
            }
        }
        return null;
    }

    private static final Pattern LIST_MATCHER = Pattern.compile("^List<([\\w<>]+)>$");

    public static <T> VarType<T> readType(String name) {
        Matcher matcher = LIST_MATCHER.matcher(name);
        if (matcher.matches()) {
            VarType<?> type = readType(matcher.group(1));
            return type == null ? null : (VarType<T>) type.listOf();
        }
        return (VarType<T>) VarType.NAME_MAP.get(name);
    }

    //Instance
    private final List<PrimitiveType<?>.Reference> primitivesList = new ArrayList<>();

    public Compiler(String toCompile) {
        Map<PrimitiveType<?>, List<StringSegment>> primitivesToPossiblesMap = PrimitiveType.PRIMITIVES.stream().collect(Collectors.toMap(java.util.function.Function.identity(),
                primitiveType -> IOHelper.collectBracketContent(toCompile, primitiveType.openRegex(), primitiveType.closeRegex())));
        primitivesToPossiblesMap.forEach((primitiveType, stringSegments) -> stringSegments.forEach(stringSegment -> {
            primitivesList.add(primitiveType.)
        }));
    }
}
