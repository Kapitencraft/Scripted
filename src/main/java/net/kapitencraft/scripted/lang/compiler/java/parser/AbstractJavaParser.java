package net.kapitencraft.scripted.lang.compiler.java.parser;

import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.analyser.LocationAnalyser;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.compiler.java.JavaCompilePipeline;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.AppliedGenericsSourceReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.scripted.lang.holder.oop.generic.AppliedGenerics;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generic;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.scripted.lang.oop.Package;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

@SuppressWarnings({"UnusedReturnValue"})
public class AbstractJavaParser {

    private static final Map<TokenTypeCategory, TokenType[]> categoryLookup = createCategoryLookup();

    private static Map<TokenTypeCategory, TokenType[]> createCategoryLookup() {
        Map<TokenTypeCategory, TokenType[]> lookup = new HashMap<>();
        for (TokenTypeCategory category : TokenTypeCategory.values()) {
            lookup.put(category, Arrays.stream(values()).filter(tokenType -> tokenType.isCategory(category)).toArray(TokenType[]::new));
        }
        return lookup;
    }

    protected int current;
    protected Token[] tokens;
    protected VarTypeContainer parser;
    protected final LocationAnalyser locFinder = new LocationAnalyser();
    protected final ErrorStorage errorStorage;
    protected boolean panicMode = false;
    protected final SourceTree sourceSink;
    protected final CompileSource source;

    public AbstractJavaParser(ErrorStorage errorStorage, SourceTree sourceSink, CompileSource source) {
        this.errorStorage = errorStorage;
        this.sourceSink = sourceSink;
        this.source = source;
    }

    public void addInternal(ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser) {
        SourceTree.DirectoryNode declaring = this.source.getDeclaring();
        String name = holder.name().lexeme();

        CompileSource compileSource = JavaCompilePipeline.INSTANCE.createSource(name, this.source.pck(), holder, storage, parser, declaring);
        sourceSink.addSource(declaring, name, compileSource);
        Compiler.dispatch(compileSource);
    }

    public void apply(Token[] toParse, VarTypeContainer targetAnalyser) {
        this.current = 0;
        this.tokens = toParse;
        this.parser = targetAnalyser;
        this.panicMode = false;
    }

    protected @Nullable AppliedGenerics appliedGenerics(GenericStack stack) {
        if (match(LESSER)) {
            Token r = previous();
            List<ClassReference> references = new ArrayList<>();
            do {
                references.add(consumeVarType(stack).getReference());
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new AppliedGenerics(r, references.toArray(new ClassReference[0]));
        }
        return null;
    }

    protected @Nullable Generics generics(GenericStack genericStack) {
        if (match(LESSER)) {
            List<Generic> generics = new ArrayList<>();
            do {
                generics.add(generic(genericStack));
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Generics(generics.toArray(new Generic[0]));
        }
        return null;
    }

    private Generic generic(GenericStack genericStack) {
        Token name = consumeIdentifier();
        SourceReference lowerBound = null, upperBound = null;
        if (match(EXTENDS)) {
            lowerBound = consumeVarType(genericStack);
        } else if (match(SUPER)) {
            upperBound = consumeVarType(genericStack);
        }

        return new Generic(name, lowerBound, upperBound);
    }

    protected boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    protected boolean check(TokenType... types) {
        if (isAtEnd()) return false;
        return Arrays.stream(types).anyMatch(this::check);
    }

    protected boolean check(TokenTypeCategory category) {
        return check(categoryLookup.get(category));
    }

    protected boolean match(TokenTypeCategory category) {
        return match(categoryLookup.get(category));
    }

    /**
     * same as {@link AbstractJavaParser#check(TokenType) check} but consumes token
     */
    protected boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    protected boolean isAtEnd() {
        return current >= tokens.length;
    }

    protected Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    protected Token peek() {
        return tokens[current];
    }

    protected Token previous() {
        return tokens[current - 1];
    }

    protected Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        if (isAtEnd()) {
            error(previous().after(), message);
        } else {
            error(peek(), message);
        }
        return Token.createNative("<unidentified>");
    }

    protected Token consumeNoThrow(TokenType type, String msg) {
        if (check(type)) return advance();

        Token token;
        if (isAtEnd()) {
            token = previous().after();
        } else
            token = peek();
        error(token, msg);
        return token;
    }

    protected Token[] getCurlyEnclosedCode() {
        return getScopedCode(C_BRACKET_O, C_BRACKET_C);
    }

    protected Token[] getScopedCode(TokenType increase, TokenType decrease) {
        if (peek().type() == decrease) return new Token[0];
        List<Token> tokens = new ArrayList<>();
        int i = 1;
        tokens.add(peek());
        do {
            advance();
            tokens.add(peek());
            if (peek().type() == increase) i++;
            else if (peek().type() == decrease) i--;
        } while (i > 0 && !isAtEnd());
        tokens.removeLast();
        return tokens.toArray(Token[]::new);
    }

    protected Optional<SourceReference> tryConsumeVarType(GenericStack generics) {
        Optional<ClassReference> optional = generics.getValue(peek().lexeme());
        if (optional.isPresent()) return Optional.of(SourceReference.from(advance(), optional.get()));
        if (VarTypeManager.hasPackage(peek().lexeme())) {
            Package p = VarTypeManager.getPackage(peek().lexeme());
            List<Token> consumed = new ArrayList<>();
            consumed.add(advance());
            while (match(DOT)) {
                Token t = advance();
                consumed.add(t);
                if (p.hasClass(t.lexeme())) {
                    return Optional.of(SourceReference.from(consumed.getFirst(), p.getClass(t.lexeme())));
                } else if (!p.hasPackage(t.lexeme()))
                    break; //can not find class here
            }
            current -= consumed.size() * 2 - 1;
        }
        Token t = advance();
        ClassReference reference = parser.getClass(t.lexeme());
        if (reference != null && !check(DOT)) {
            AppliedGenerics declared = appliedGenerics(generics);
            if (declared != null) reference = new AppliedGenericsReference(reference, declared);
            return Optional.of(SourceReference.from(t, reference));
        } else if (reference != null)
            return Optional.of(SourceReference.from(t, reference));
        current--;
        return Optional.empty();
    }

    @NotNull
    protected SourceReference consumeVarType(GenericStack generics) {
        SourceReference sourceReference = consumeVarTypeNoArray(generics);
        ClassReference reference = sourceReference.getReference();
        Token last = sourceReference.getToken();
        AppliedGenerics appliedGenerics = appliedGenerics(generics);
        if (appliedGenerics != null) {
            return AppliedGenericsSourceReference.create(last, reference, appliedGenerics);
        }
        while (match(S_BRACKET_O)) {
            consume(S_BRACKET_C, "']' expected");
            reference = reference.array();
            last = previous();
        }
        if (last != sourceReference.getToken()) {
            return SourceReference.from(last, reference);
        }
        return sourceReference;
    }

    protected SourceReference consumeVarTypeNoArray(GenericStack stack) {
        Token token = consumeIdentifier();
        ClassReference reference = parser.getClass(token.lexeme());
        if (reference == null) {
            Optional<ClassReference> optional = stack.getValue(token.lexeme());
            if (optional.isPresent()) return SourceReference.from(token, optional.get());
        }
        if (reference == null) {
            Package p = VarTypeManager.getPackage(token.lexeme());

            while (match(DOT) && p != null) {
                String id = consumeIdentifier().lexeme();
                if (p.hasClass(id)) {
                    reference = p.getClass(id);
                    break;
                }
                p = p.getPackage(id);
            }
        }

        Token last = previous();
        int index = current;
        while (match(DOT) && reference != null) {
            String enclosingName = consumeIdentifier().lexeme();
            ClassReference name = VarTypeManager.getClassForName(reference.absoluteName() + "." + enclosingName);
            if (name == null) {
                current = index;
                break;
            }
            reference = name;
            index = current;
        }

        if (reference == null) {
            error(token, "unknown symbol");
            return SourceReference.from(token, VarTypeManager.VOID.reference()); //skip rest
        }
        AppliedGenerics declared = appliedGenerics(stack);
        if (declared != null) return SourceReference.from(last, new AppliedGenericsReference(reference, declared));
        return SourceReference.from(last, reference);
    }

    protected Token consumeIdentifier() {
        return consume(IDENTIFIER, "<identifier> expected");
    }

    protected Token consumeBracketOpen(String method) {
        return this.consume(BRACKET_O, "Expected '(' after " + method + ".");
    }

    protected Token consumeCurlyOpen(String obj) {
        return this.consume(C_BRACKET_O, "Expected '{' after " + obj + ".");
    }

    protected Token consumeCurlyClose(String method) {
        return this.consume(C_BRACKET_C, "Expected '}' after " + method + ".");
    }

    protected Token consumeBracketClose(String method) {
        return this.consume(BRACKET_C, "Expected ')' after " + method + ".");
    }

    protected Token consumeEndOfArg() {
        return this.consumeNoThrow(EOA, "';' expected");
    }

    protected void error(Token token, String message) {
        if (!this.panicMode)
            errorStorage.error(token, message);
    }

    protected void errorF(Token token, String msg, Object... args) {
        if (!this.panicMode)
            errorStorage.errorF(token, msg, args);
    }

    protected void warn(Token token, String message) {
        errorStorage.warn(token, message);
    }

    protected void synchronize() {
        this.current--; //move back one to ensure finding EOA if they have been consumed by the underlying code
        while (!isAtEnd()) {
            switch (peek().type()) {
                case EOF:
                case CLASS:
                case IMPORT:
                case FUNC:
                case FOR:
                case IF:
                case WHILE:
                case SWITCH:
                case RETURN:
                    return;
            }
            advance();
        }
    }
}
