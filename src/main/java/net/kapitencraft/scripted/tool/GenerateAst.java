package net.kapitencraft.scripted.tool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GenerateAst {
    public static final String DIRECTORY = "src/main/java/net/kapitencraft/lang/holder/ast";

    public static void main(String[] args) throws IOException {
        defineAst("Expr", List.of(
                "Assign        : Token name; Expr value; Token type",
                "SpecialAssign : Token name; Token assignType",
                "Binary        : Expr left; Token operator; Expr right",
                "When          : Expr condition; Expr ifTrue; Expr ifFalse",
                "Call          : Expr callee; Token bracket; List<Expr> args",
                "InstCall      : Expr callee; Token name; Token bracket; List<Expr> args",
                "Get           : Expr object; Token name",
                "Set           : Expr object; Token name; Expr value; Token assignType",
                "SpecialSet    : Expr callee; Token name; Token assignType",
                "Switch        : Expr provider; Map<Object,Expr> params; Expr defaulted; Token keyword",
                "CastCheck     : Expr object; LoxClass targetType; Token patternVarName",
                "Grouping      : Expr expression",
                //"Lambda   : List<Token> params, Stmt body",
                "Literal       : Token value",
                "Logical       : Expr left; Token operator; Expr right",
                "Unary         : Token operator; Expr right",
                "VarRef        : Token name",
                "FuncRef       : Token name",
                "Constructor   : LoxClass target; List<Expr> params"
        ));
        defineAst("Stmt", List.of(
                "Block            : List<Stmt> statements",
                "Expression       : Expr expression",
                "FuncDecl         : LoxClass retType; Token name; Token end; List<Pair<LoxClass,Token>> params; List<Stmt> body; boolean isFinal; boolean isAbstract",
                "If               : Expr condition; Stmt thenBranch; Stmt elseBranch; List<Pair<Expr,Stmt>> elifs; Token keyword",
                "Return           : Token keyword; Expr value",
                "VarDecl          : Token name; LoxClass type; Expr initializer; boolean isFinal",
                "While            : Expr condition; Stmt body; Token keyword",
                "For              : Stmt init; Expr condition; Expr increment; Stmt body; Token keyword",
                "LoopInterruption : Token type"
        ));
    }

    private static void defineAst(String baseName, List<String> types) throws IOException {
        String path = DIRECTORY + "/" + baseName + ".java";
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package net.kapitencraft.lang.holder.ast;");
        writer.println();
        writer.println("import java.util.Map;");
        writer.println("import java.util.List;");
        writer.println("import net.kapitencraft.lang.holder.token.Token;");
        writer.println("import net.kapitencraft.tool.Pair;");
        writer.println("import net.kapitencraft.lang.oop.clazz.LoxClass;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");
        writer.println();

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println();
        writer.println("    public static class " + className + " extends " +
                baseName + " {");

        // Fields.
        String[] fields = fieldList.split("; ");
        for (String field : fields) {
            writer.println("        public final " + field + ";");
        }
        writer.println();


        // Constructor.
        writer.print("        public " + className + "(");
        for (int i = 0; i < fields.length - 1; i++) {
            writer.print(fields[i]);
            writer.print(", ");
        }
        writer.print(fields[fields.length - 1]);
        writer.println(") {");

        // Store parameters in fields.
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");


        // Visitor pattern.
        writer.println();
        writer.println("        @Override");
        writer.println("        public <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" +
                className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");

    }
}