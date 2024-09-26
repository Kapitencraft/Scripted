package net.kapitencraft.scripted.lang.compile;

import net.kapitencraft.scripted.lang.compile.parser.ExprParser;
import net.kapitencraft.scripted.lang.compile.parser.SkeletonParser;
import net.kapitencraft.scripted.lang.compile.parser.StmtParser;
import net.kapitencraft.scripted.lang.compile.visitor.LocationFinder;
import net.kapitencraft.scripted.lang.compile.visitor.Resolver;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.GeneratedLoxClass;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

import java.util.ArrayList;
import java.util.List;

public class Compiler {
    static boolean hadError = false;

    public static class ErrorLogger {
        private final String[] lines;
        private final LocationFinder finder;

        public ErrorLogger(String[] lines) {
            this.lines = lines;
            finder = new LocationFinder();
        }

        public void error(Token loc, String msg) {
            Compiler.error(loc, msg, lines[loc.line() - 1]);
        }

        public void error(Stmt loc, String msg) {
            error(finder.find(loc), msg);
        }

        public void error(Expr loc, String msg) {
            error(finder.find(loc), msg);
        }
    }

    public static LoxClass compile(String source, String[] lines) {
        ErrorLogger logger = new ErrorLogger(lines);
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();


        System.out.println("Parsing...");
        SkeletonParser parser = new SkeletonParser(logger);
        VarTypeParser varTypeParser = new VarTypeParser();

        parser.apply(tokens.toArray(new Token[0]), varTypeParser);

        SkeletonParser.ClassDecl decl = parser.parse();

        GeneratedLoxClass generated = compileClass(logger, decl, varTypeParser);

        // Stop if there was a syntax error.
        if (hadError) System.exit(65);

        Resolver resolver = new Resolver(logger);
        System.out.println("Resolving...");
        resolver.resolve(generated);

        if (hadError) System.exit(65);

        return generated;
    }

    private static GeneratedLoxClass compileClass(ErrorLogger logger, SkeletonParser.ClassDecl decl, VarTypeParser varTypeParser) {
        StmtParser stmtParser = new StmtParser(logger);
        ExprParser exprParser = new ExprParser(logger);

        List<Stmt.VarDecl> fields = new ArrayList<>();
        List<Stmt.VarDecl> staticFields = new ArrayList<>();
        for (SkeletonParser.FieldDecl field : decl.fields()) {
            Expr initializer = null;
            if (field.body() != null) {
                exprParser.apply(field.body(), varTypeParser);
                initializer = exprParser.expression();
            }
            Stmt.VarDecl fieldDecl = new Stmt.VarDecl(field.name(), field.type(), initializer, field.isFinal());
            if (field.isStatic()) staticFields.add(fieldDecl);
            else fields.add(fieldDecl);
        }

        List<GeneratedLoxClass> enclosed = new ArrayList<>();
        for (SkeletonParser.ClassDecl classDecl : decl.enclosed()) {
            GeneratedLoxClass loxClass = compileClass(logger, classDecl, varTypeParser);
            classDecl.target().apply(loxClass);
            enclosed.add(loxClass);
        }

        List<Stmt.FuncDecl> methods = new ArrayList<>();
        List<Stmt.FuncDecl> staticMethods = new ArrayList<>();
        List<Stmt.FuncDecl> abstracts = new ArrayList<>();
        for (SkeletonParser.MethodDecl method : decl.methods()) {
            List<Stmt> body = null;
            if (!method.isAbstract()) {
                stmtParser.apply(method.body(), varTypeParser);
                body = stmtParser.parse();
            }
            Stmt.FuncDecl methodDecl = new Stmt.FuncDecl(method.loxClass(), method.name(), method.end(), method.params(), body, method.isFinal(), method.isAbstract());
            if (method.isStatic()) staticMethods.add(methodDecl);
            else if (method.isAbstract()) abstracts.add(methodDecl);
            else methods.add(methodDecl);
        }
        GeneratedLoxClass generated = new GeneratedLoxClass(abstracts, methods, staticMethods, fields, staticFields, decl.superclass(), decl.name(), enclosed, decl.isAbstract());
        decl.target().apply(generated);
        return generated;
    }

    public static void error(Token token, String message, String line) {
        report(token.line(), message, token.lineStartIndex(), line);
    }

    private static void report(int lineIndex, String message, int startIndex, String line) {
        System.err.println("Error in line " + lineIndex + ": " + message);
        System.err.println(line);
        for (int i = 0; i < startIndex; i++) {
            System.err.print(" ");
        }
        System.err.println("^");

        hadError = true;
    }
}
