package net.kapitencraft.scripted.lang.compile.parser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.VarTypeManager;
import net.kapitencraft.scripted.lang.compile.Compiler;
import net.kapitencraft.scripted.lang.compile.VarTypeParser;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;
import net.kapitencraft.scripted.lang.oop.clazz.PreviewClass;

import java.util.ArrayList;
import java.util.List;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

@SuppressWarnings("ThrowableNotThrown")
public class SkeletonParser extends AbstractParser {

    private LoxClass consumeVarType(VarTypeParser parser) {
        Token token = consume(IDENTIFIER, "<identifier> expected");
        LoxClass loxClass = parser.getClass(token.lexeme());
        if (loxClass == null) error(token, "unknown symbol");
        return loxClass;
    }

    public SkeletonParser(Compiler.ErrorLogger errorLogger) {
        super(errorLogger);
    }

    public void parseImports() {
        while (check(IMPORT)) {
            importStmt();
        }
    }

    public ClassDecl classDecl(boolean classAbstract, boolean classFinal) {

        consume(CLASS, "'class' expected");

        Token name = consume(IDENTIFIER, "class name expected");

        PreviewClass previewClass = new PreviewClass(name.lexeme());
        parser.addClass(previewClass);
        LoxClass superClass = VarTypeManager.OBJECT;
        if (match(EXTENDS)) superClass = consumeVarType(parser);

        consumeCurlyOpen("class");

        List<MethodDecl> methods = new ArrayList<>();
        List<FieldDecl> fields = new ArrayList<>();
        List<ClassDecl> enclosed = new ArrayList<>();
        //MethodDecl constructor = null; TODO add constructor

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            boolean isStatic = false;
            boolean isFinal = false;
            boolean isAbstract = false;
            while (!check(IDENTIFIER) && !check(CLASS)) {
                if (match(STATIC)) {
                    if (isStatic) error(previous(), "duplicate static keyword");
                    isStatic = true;
                } else if (match(FINAL)) {
                    if (isFinal) error(previous(), "duplicate final keyword");
                    isFinal = true;
                } else if (match(ABSTRACT)) {
                    if (isAbstract) error(previous(), "duplicate abstract keyword");
                    if (!classAbstract) error(previous(), "abstract method on non-abstract class");
                    isAbstract = true;
                } else {
                    error(peek(), "<identifier> expected");
                }
            }
            if (check(CLASS)) {
                enclosed.add(classDecl(isAbstract, isFinal));
            } else {
                LoxClass type = consumeVarType(parser);
                Token elementName = consumeIdentifier();
                if (match(BRACKET_O)) {
                    if (isAbstract && isStatic) error(elementName, "illegal combination of modifiers abstract and static");
                    MethodDecl decl = funcDecl(parser, type, elementName, isFinal, isStatic, isAbstract);
                    methods.add(decl);
                } else {
                    if (isAbstract) error(elementName, "fields may not be abstract");
                    FieldDecl decl = fieldDecl(type, elementName, isFinal, isStatic);
                    fields.add(decl);
                }
            }
        }
        consumeCurlyClose("class");
        return new ClassDecl(classAbstract, classFinal, previewClass, name, superClass, methods.toArray(new MethodDecl[0]), fields.toArray(new FieldDecl[0]), enclosed.toArray(new ClassDecl[0]));
    }

    public ClassDecl parse() {
        parseImports();
        boolean isFinal = false;
        boolean isAbstract = false;
        while (!check(CLASS)) {
            if (match(FINAL)) {
                if (isFinal) error(previous(), "duplicate final keyword");
                isFinal = true;
            } else if (match(ABSTRACT)) {
                if (isAbstract) error(previous(), "duplicate abstract keyword");
                isAbstract = true;
            } else {
                error(peek(), "<identifier> expected");
            }
        }
        return classDecl(isAbstract, isFinal);
    }

    private MethodDecl funcDecl(VarTypeParser parser, LoxClass type, Token name, boolean isFinal, boolean isStatic, boolean isAbstract) {

        List<Pair<LoxClass, Token>> parameters = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                LoxClass pType = consumeVarType(parser);
                Token pName = consume(IDENTIFIER, "Expected parameter name.");
                parameters.add(Pair.of(pType, pName));
            } while (match(COMMA));
        }
        consumeBracketClose("parameters");

        Token[] code = null;
        Token end;

        if (!isAbstract) { //body only if method isn't abstract
            consumeCurlyOpen("method body");

            code = getMethodCode();

            end = consumeCurlyClose("method body");
        } else end = consumeEndOfArg();
        return new MethodDecl(code, name, end, parameters, type, isFinal, isStatic, isAbstract);
    }

    private FieldDecl fieldDecl(LoxClass type, Token name, boolean isFinal, boolean isStatic) {
        Token[] code = null;

        if (match(ASSIGN)) code = getFieldCode();
        else consumeEndOfArg();

        return new FieldDecl(code, name, type, isFinal, isStatic);
    }

    private void importStmt() {
        consume(IMPORT, "Expected import or class");
        List<Token> packages = readPackage();
        consumeEndOfArg();
        parser.addClass(VarTypeManager.getClass(packages, this::error));
    }

    private List<Token> readPackage() {
        List<Token> packages = new ArrayList<>();
        packages.add(consumeIdentifier());
        while (!check(EOA)) {
            consume(DOT, "unexpected name");
            packages.add(consumeIdentifier());
        }
        return packages;
    }

    private Token[] getMethodCode() {
        List<Token> tokens = new ArrayList<>();
        int i = 1;
        tokens.add(peek());
        do {
            advance();
            tokens.add(peek());
            if (peek().type() == C_BRACKET_O) i++;
            else if (peek().type() == C_BRACKET_C) i--;
        } while (i > 0);
        return tokens.toArray(Token[]::new);
    }

    private Token[] getFieldCode() {
        List<Token> tokens = new ArrayList<>();

        do {
            tokens.add(peek());
            advance();
        } while (!match(EOA));

        return tokens.toArray(Token[]::new);
    }


    public record MethodDecl(Token[] body, Token name, Token end, List<Pair<LoxClass, Token>> params, LoxClass loxClass, boolean isFinal, boolean isStatic, boolean isAbstract) {

    }

    public record FieldDecl(Token[] body, Token name, LoxClass type, boolean isFinal, boolean isStatic) {

    }

    public record ClassDecl(boolean isAbstract, boolean isFinal, PreviewClass target, Token name, LoxClass superclass, MethodDecl[] methods, FieldDecl[] fields, ClassDecl[] enclosed) {

    }
}
