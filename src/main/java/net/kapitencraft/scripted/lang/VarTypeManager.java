package net.kapitencraft.scripted.lang;

import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.scripted.lang.oop.Package;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.oop.clazz.PrimitiveClass;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

public class VarTypeManager {
    private static final Package root = new Package();
    private static final Map<String, TokenType> keywords;

    public static final LoxClass NUMBER = new PrimitiveClass("num" , null);
    public static final LoxClass INTEGER = new PrimitiveClass(NUMBER, "int", 0);
    public static final LoxClass FLOAT = new PrimitiveClass(NUMBER, "float", 0f);
    public static final LoxClass DOUBLE = new PrimitiveClass(NUMBER, "double", 0d);
    public static final LoxClass BOOLEAN = new PrimitiveClass("bool", false);
    public static final LoxClass CHAR = new PrimitiveClass("char", ' ');
    public static final LoxClass STRING = new PrimitiveClass("String", "");

    public static final LoxClass VOID = new PrimitiveClass("void", null);
    //TODO move Object away from primitive class given that it isn't actually one
    public static final LoxClass OBJECT = new PrimitiveClass("Object", null);

    static {
        keywords = Arrays.stream(values()).filter(tokenType -> tokenType.isCategory(TokenTypeCategory.KEY_WORD)).collect(Collectors.toMap(tokenType -> tokenType.name().toLowerCase(Locale.ROOT), Function.identity()));
        initialize();
    }

    private static void initialize() {
        registerMain(OBJECT);
        registerMain(NUMBER);
        registerMain(INTEGER);
        registerMain(FLOAT);
        registerMain(BOOLEAN);
        registerMain(DOUBLE);
        registerMain(CHAR);
        registerMain(STRING);
        registerMain(VOID);
    }

    public static void registerMain(LoxClass clazz) {
        root.getOrCreatePackage("scripted").getOrCreatePackage("lang").addClass(clazz.name(), clazz);
    }

    public static TokenType getType(String name) {
        if (keywords.containsKey(name)) return keywords.get(name);

        return IDENTIFIER;
    }

    public static LoxClass getClassForName(String type) {
        String[] packages = type.split("\\.");
        Package pg = rootPackage();
        for (int i = 0; i < packages.length; i++) {
            String lexeme = packages[i];
            if (i == packages.length - 1) {
                if (!pg.hasClass(lexeme)) {
                    return null;
                }
                return pg.getClass(lexeme);
            } else {
                if (!pg.hasPackage(lexeme)) {
                    return null;
                }
                pg = pg.getPackage(lexeme);
            }
        }
        return null;
    }

    public static Package rootPackage() {
        return root;
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

    public static LoxClass getClass(List<Token> s, BiConsumer<Token, String> error) {
        Package pg = VarTypeManager.rootPackage();
        for (int i = 0; i < s.size(); i++) {
            Token token = s.get(i);
            String lexeme = token.lexeme();
            if (i == s.size() - 1) {
                if (!pg.hasClass(lexeme)) {
                    error.accept(token, "unknown symbol");
                    return null;
                }
                return pg.getClass(lexeme);
            } else {
                if (!pg.hasPackage(lexeme)) {
                    error.accept(token, "unknown symbol");
                    return null;
                }
                pg = pg.getPackage(lexeme);
            }
        }
        return null;
    }
}
