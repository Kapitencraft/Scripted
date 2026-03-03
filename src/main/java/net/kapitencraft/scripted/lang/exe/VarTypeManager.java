package net.kapitencraft.scripted.lang.exe;

import net.kapitencraft.scripted.lang.exe.natives.NativeClassLoader;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.Package;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.scripted.lang.oop.clazz.primitive.*;
import net.kapitencraft.scripted.lang.tool.StringReader;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class VarTypeManager {
    private static final Package root = new Package("");
    private static final Package LANG_ROOT = getOrCreatePackage("scripted.lang");
    private static final Package ANNOTATION_PCK = LANG_ROOT.getOrCreatePackage("annotations");

    private static final HashMap<String, ScriptedClass> flatMap = new HashMap<>();

    private static PrimitiveClass registerPrimitive(PrimitiveClass primitiveClass) {
        LANG_ROOT.addClass(primitiveClass.name(), primitiveClass);
        return primitiveClass;
    }

    public static final PrimitiveClass NUMBER = registerPrimitive(new NumberClass());
    public static final PrimitiveClass INTEGER = registerPrimitive(new IntegerClass());
    public static final PrimitiveClass FLOAT = registerPrimitive(new FloatClass());
    public static final PrimitiveClass DOUBLE = registerPrimitive(new DoubleClass());
    public static final PrimitiveClass BOOLEAN = registerPrimitive(new BooleanClass());
    public static final PrimitiveClass CHAR = registerPrimitive(new CharacterClass());
    public static final PrimitiveClass VOID = registerPrimitive(new VoidClass());

    static {
        NativeClassLoader.load(); //load natives before actual java project
    }

    public static final ClassReference OBJECT = getMainClass("Object");

    public static final ClassReference ENUM = getOrCreateClass("Enum", "scripted.lang");

    public static final ClassReference STRING = getMainClass("String");

    public static final ClassReference THROWABLE = getMainClass("Throwable");
    public static final ClassReference STACK_OVERFLOW_ERROR = getMainClass("StackOverflowError");
    public static final ClassReference UNKNOWN_ERROR = getMainClass("UnknownError");
    public static final ClassReference MISSING_VAR_ERROR = getMainClass("MissingVarError");
    public static final ClassReference ARITHMETIC_EXCEPTION = getMainClass("ArithmeticException");
    public static final ClassReference FUNCTION_CALL_ERROR = getMainClass("FunctionCallError");
    public static final ClassReference INDEX_OUT_OF_BOUNDS_EXCEPTION = getMainClass("IndexOutOfBoundsException");
    public static final ClassReference CLASS_CAST_EXCEPTION = getMainClass("ClassCastException");

    public static final ClassReference OVERRIDE = getMainClass("Override");
    public static final ClassReference RETENTION_POLICY = getAnnotationClass("RetentionPolicy");
    public static final ClassReference RETENTION = getAnnotationClass("Retention");

    public static ClassReference getClassForName(String type) {
        int arrayCount = 0;
        while (type.charAt(type.length() - 1 - arrayCount * 2) == '[') arrayCount++;
        type = type.substring(0, type.length() - arrayCount * 2);
        String[] packages = type.split("\\.");
        Package pg = rootPackage();
        for (int i = 0; i < packages.length; i++) {
            String name = packages[i];
            if (i == packages.length - 1) {
                ClassReference reference = pg.getClass(name);
                if (reference == null) return null;
                for (; arrayCount > 0; arrayCount--) {
                    reference = reference.array();
                }
                return reference;
            } else {
                if (!pg.hasPackage(name)) return null;
                pg = pg.getPackage(name);
            }
        }
        return null;
    }

    public static ClassReference getClassOrError(String type) {
        if (type.startsWith("?")) {
            ClassReference lowerBound = null, upperBound = null;
            if (type.length() > 1) {
                if (type.substring(2).startsWith("extends")) {
                    lowerBound = getClassOrError(type.substring(10));
                } else if (type.substring(2).startsWith("super")) {
                    upperBound = getClassOrError(type.substring(8));
                }
            }
            return new GenericClassReference("?", lowerBound, upperBound);
        }
        return Objects.requireNonNull(getClassForName(type), "unknown class: " + type);
    }

    public static Package rootPackage() {
        return root;
    }

    public static boolean hasPackage(String pckName) {
        return root.hasPackage(pckName);
    }

    /**
     * gets a package
     * @param s the package, use "." to split
     * @return the package, or null if it doesn't exist
     */
    public static Package getPackage(String s) {
        String[] packages = s.split("\\.");
        Package p = rootPackage();
        for (String pck : packages) {
            p = p.getPackage(pck);
            if (p == null) break;
        }
        return p;
    }

    public static Package getOrCreatePackage(String s) {
        String[] packages = s.split("\\.");
        Package p = rootPackage();
        for (String pck : packages) {
            p = p.getOrCreatePackage(pck);
        }
        return p;
    }

    public static ClassReference getClassOrError(List<Token> s, BiConsumer<Token, String> error) {
        Package pg = rootPackage();
        for (int i = 0; i < s.size() - 1; i++) {
            Token token = s.get(i);
            String lexeme = token.lexeme();
            if (!pg.hasPackage(lexeme)) {
                error.accept(token, "unknown package '" + lexeme + "'");
                return null;
            }
            pg = pg.getPackage(lexeme);
        }
        Token token = s.getLast();
        String lexeme = token.lexeme();
        if (!pg.hasClass(lexeme)) {
            error.accept(token, "unknown class '" + lexeme + "'");
            return null;
        }
        return pg.getClass(lexeme);
    }

    public static SourceClassReference getOrCreateClass(List<Token> path) {
        Package pg = rootPackage();
        for (int i = 0; i < path.size() - 1; i++) {
            String pckName = path.get(i).lexeme();
            pg = pg.getOrCreatePackage(pckName);
        }
        Token last = path.getLast();
        return SourceClassReference.from(last, pg.getOrCreateClass(last.lexeme()));
    }

    private static ClassReference getMainClass(String name) {
        return LANG_ROOT.getClass(name);
    }

    private static ClassReference getAnnotationClass(String name) {
        return ANNOTATION_PCK.getClass(name);
    }

    public static ClassReference getOrCreateClass(String name, String pck) {
        String[] packages = pck.split("[.$]");
        Package pg = rootPackage();
        for (String aPackage : packages) {
            pg = pg.getOrCreatePackage(aPackage);
        }
        if (name.contains("$")) {
            packages = name.split("\\$");
            for (int i = 0; i < packages.length - 1; i++) {
                pg = pg.getOrCreatePackage(packages[i]);
            }
            return pg.getOrCreateClass(packages[packages.length - 1]);
        }
        return pg.getOrCreateClass(name);
    }

    public static ClassReference getClassFromObject(Object o) {
        if (o instanceof Integer) return INTEGER.reference();
        if (o instanceof Float) return FLOAT.reference();
        if (o instanceof Double) return DOUBLE.reference();
        if (o instanceof Boolean) return BOOLEAN.reference();
        if (o instanceof Character) return CHAR.reference();
        if (o instanceof DynamicClassInstance cI) {
            return cI.getType().reference();
        }
        throw new IllegalArgumentException("could not parse object to class: " + o);
    }

    public static List<ClassReference> getArgsFromObjects(List<Object> objects) {
        return objects.stream().map(VarTypeManager::getClassFromObject).toList();
    }

    public static String getClassName(ScriptedClass reference) {
        if (reference == NUMBER)
            return "N";
        else if (reference == INTEGER)
            return "I";
        else if (reference == FLOAT)
            return "F";
        else if (reference == DOUBLE)
            return "D";
        else if (reference == BOOLEAN)
            return "B"; //we can use B because we don't have bytes
        else if (reference == CHAR)
            return "C";
        else if (reference == VOID)
            return "V";
        else if (reference.isArray())
            return "[" + getClassName(reference.getComponentType());
        else
            return "L" + reference.absoluteName().replaceAll("\\.", "/") + ";";
    }

    public static String getClassName(ClassReference reference) {
        if (reference.exists()) return getClassName(reference.get());
        else return "L" + reference.absoluteName().replaceAll("\\.", "/") + ";";
    }

    public static String getMethodSignature(ScriptedClass target, String name, ClassReference[] argTypes) {
        return getClassName(target) + getMethodSignatureNoTarget(name, argTypes);
    }

    public static String getMethodSignatureNoTarget(String name, ClassReference[] argTypes) {
        return name + "(" + getArgsSignature(argTypes) + ")";
    }

    public static String getArgsSignature(ClassReference[] argTypes) {
        return Arrays.stream(argTypes).map(VarTypeManager::getClassName).collect(Collectors.joining());
    }

    public static ClassReference parseType(StringReader reader) {
        return switch (reader.read()) {
            case 'N' -> VarTypeManager.NUMBER.reference();
            case 'I' -> VarTypeManager.INTEGER.reference();
            case 'F' -> VarTypeManager.FLOAT.reference();
            case 'D' -> VarTypeManager.DOUBLE.reference();
            case 'B' -> VarTypeManager.BOOLEAN.reference();
            case 'C' -> VarTypeManager.CHAR.reference();
            case 'V' -> VarTypeManager.VOID.reference();
            case '[' -> parseType(reader).array();
            case 'L' -> {
                String type = reader.readUntil(';');
                reader.skip();
                yield getClassForName(type.replaceAll("[/$]", "."));
            }
            default -> throw new IllegalArgumentException("unknown type start: '" + reader.peek(-1) + "'");
        };
    }

    public static ClassReference directParseType(String name) {
        return switch (name) {
            case "N" -> VarTypeManager.NUMBER.reference();
            case "I" -> VarTypeManager.INTEGER.reference();
            case "F" -> VarTypeManager.FLOAT.reference();
            case "D" -> VarTypeManager.DOUBLE.reference();
            case "B" -> VarTypeManager.BOOLEAN.reference();
            case "C" -> VarTypeManager.CHAR.reference();
            case "V" -> VarTypeManager.VOID.reference();
            default -> {
                if (name.startsWith("[")) {
                    yield directParseType(name.substring(1));
                } else if (name.startsWith("L")) {
                    yield getClassForName(name.substring(1, name.length() - 1).replaceAll("/", "."));
                }
                throw new IllegalArgumentException("unknown type pattern: '" + name + "'");
            }
        };
    }

    public static ClassReference directParseTypeCompiler(String name) {
        return switch (name) {
            case "N" -> VarTypeManager.NUMBER.reference();
            case "I" -> VarTypeManager.INTEGER.reference();
            case "F" -> VarTypeManager.FLOAT.reference();
            case "D" -> VarTypeManager.DOUBLE.reference();
            case "B" -> VarTypeManager.BOOLEAN.reference();
            case "C" -> VarTypeManager.CHAR.reference();
            case "V" -> VarTypeManager.VOID.reference();
            default -> {
                if (name.startsWith("[")) {
                    yield directParseTypeCompiler(name.substring(1));
                } else if (name.startsWith("L")) {
                    yield getClassForName(name.substring(1, name.length() - 1).replaceAll("[/$]", "."));
                }
                throw new IllegalArgumentException("unknown type pattern: '" + name + "'");
            }
        };
    }

    public static ScriptedClass flatParse(StringReader reader) {
        return switch (reader.read()) {
            case 'N' -> VarTypeManager.NUMBER;
            case 'I' -> VarTypeManager.INTEGER;
            case 'F' -> VarTypeManager.FLOAT;
            case 'D' -> VarTypeManager.DOUBLE;
            case 'B' -> VarTypeManager.BOOLEAN;
            case 'C' -> VarTypeManager.CHAR;
            case 'V' -> VarTypeManager.VOID;
            case '[' -> flatParse(reader).array();
            case 'L' -> {
                String type = reader.readUntil(';');
                reader.skip();
                yield flatMap.get(type);
            }
            default -> throw new IllegalArgumentException("unknown type start: '" + reader.peek(-1) + "'");
        };
    }

    /**
     * @param s the location of the class
     * @return the class at that location or null if the class doesn't exist
     * @throws IllegalArgumentException if the parameter does not match the location format
     */
    public static ScriptedClass directFlatParse(String s) {
        return switch (s) {
            case "N" -> VarTypeManager.NUMBER;
            case "I" -> VarTypeManager.INTEGER;
            case "F" -> VarTypeManager.FLOAT;
            case "D" -> VarTypeManager.DOUBLE;
            case "B" -> VarTypeManager.BOOLEAN;
            case "C" -> VarTypeManager.CHAR;
            case "V" -> VarTypeManager.VOID;
            case "[" -> directFlatParse(s.substring(1)).array();
            default -> {
                if (s.startsWith("L")) yield flatMap.get(s.substring(1, s.length() - 1));
                throw new IllegalArgumentException("unknown type: '" + s + "'");
            }
        };
    }

    public static void registerFlat(ScriptedClass target) {
        flatMap.put(target.absoluteName().replaceAll("\\.", "/"), target);
    }

    public static void listFlat() {
        System.out.println("=== All Classes ===");
        ArrayList<String> classes = new ArrayList<>(flatMap.keySet());
        classes.sort(String::compareTo);
        classes.forEach(System.out::println);
        System.out.println("=== End Log ===");
    }

    //class loader invoke method
    public static void init() {
    }
}
