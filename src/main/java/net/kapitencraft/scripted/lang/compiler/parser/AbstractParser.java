package net.kapitencraft.scripted.lang.compiler.parser;

import com.google.common.collect.ImmutableList;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.compiler.VarTypeParser;
import net.kapitencraft.scripted.lang.compiler.analyser.BytecodeVars;
import net.kapitencraft.scripted.lang.compiler.analyser.LocationAnalyser;
import net.kapitencraft.scripted.lang.compiler.analyser.RetTypeAnalyser;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.scripted.lang.oop.Package;
import net.kapitencraft.scripted.lang.run.VarTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

@SuppressWarnings({"ThrowableNotThrown", "UnusedReturnValue"})
public class AbstractParser {
    protected static final ClassReference WILDCARD = new GenericClassReference("", null, null);

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
    protected VarTypeParser parser;
    protected RetTypeAnalyser finder;
    protected final LocationAnalyser locFinder = new LocationAnalyser();
    protected final Deque<List<ClassReference>> args = new ArrayDeque<>(); //TODO either use or remove
    protected final Compiler.ErrorStorage errorStorage;
    protected BytecodeVars varAnalyser;

    protected ClassReference checkVarExistence(Token name, boolean requireValue, boolean mayBeFinal) {
        String varName = name.lexeme();
        BytecodeVars.FetchResult result = varAnalyser.get(varName);
        if (result == BytecodeVars.FetchResult.FAIL) {
            error(name, "cannot find symbol");
        } else if (requireValue && !result.assigned()) {
            error(name, "Variable '" + name.lexeme() + "' might not have been initialized");
        } else if (!mayBeFinal && !result.canAssign()) {
            error(name, "Can not assign to final variable");
        }
        return result.type();
    }

    protected void checkVarType(Token name, Expr value) {
        BytecodeVars.FetchResult result = varAnalyser.get(name.lexeme());
        if (result == BytecodeVars.FetchResult.FAIL) return;
        expectType(name, value, result.type());
    }

    protected void expectType(ClassReference... types) {
        args.push(List.of(types));
    }

    protected void popExpectation() {
        args.pop();
    }

    protected boolean typeAllowed(ClassReference target) {
        return searched().contains(target);
    }

    protected List<ClassReference> searched() {
        return args.getLast();
    }

    protected void expectType(List<ClassReference> types) {
        args.push(ImmutableList.copyOf(types));
    }

    protected ClassReference expectType(Token errorLoc, Expr value, ClassReference type) {
        ClassReference got = finder.findRetType(value);
        return expectType(errorLoc, got, type);
    }

    protected ClassReference expectType(Expr value, ClassReference type) {
        return expectType(this.locFinder.find(value), value, type);
    }

    protected void expectCondition(Token errorLoc, Expr gotten) {
        expectType(errorLoc, gotten, VarTypeManager.BOOLEAN.reference());
    }

    protected void expectCondition(Expr gotten) {
        expectCondition(locFinder.find(gotten), gotten);
    }

    protected ClassReference expectType(Token errorLoc, ClassReference gotten, ClassReference expected) {
        if (gotten == null) return WILDCARD;
        if (gotten == WILDCARD) return expected;
        if (expected == VarTypeManager.OBJECT) return gotten;
        if (!gotten.get().isChildOf(expected.get()))
            errorStorage.errorF(errorLoc, "incompatible types: %s cannot be converted to %s", gotten.name(), expected.name());
        if (gotten instanceof AppliedGenericsReference reference) {
            if (expected instanceof AppliedGenericsReference reference1) {
                Holder.AppliedGenerics gottenAppliedGenerics = reference.getApplied();
                Holder.AppliedGenerics expectedAppliedGenerics = reference1.getApplied();
                ClassReference[] expectedGenerics = expectedAppliedGenerics.references();
                ClassReference[] gottenGenerics = gottenAppliedGenerics.references();

                if (expectedGenerics.length != gottenGenerics.length) {
                    errorStorage.errorF(gottenAppliedGenerics.reference(), "Wrong number of type arguments: %s; required: %s", gottenGenerics.length, expectedGenerics.length);
                } else {
                    for (int i = 0; i < expectedGenerics.length; i++) {
                        if (!expectedGenerics[i].get().isChildOf(gottenGenerics[i].get())) {
                            String name = reference1.getGenerics().variables()[i].name().lexeme();
                            errorStorage.errorF(reference.getApplied().reference(), "incompatible types: inference variable %s has incompatible bounds", name);

                            errorStorage.logError("gotten: " + gottenGenerics[i].name());
                            errorStorage.logError("lower bounds: " + expectedGenerics[i].name());
                        }
                    }
                }
            } else {
                errorStorage.errorF(reference.getApplied().reference(), "Type '%s' does not have type parameters", expected.absoluteName());
            }
        }
        return gotten;
    }

    protected byte createVar(Token name, ClassReference type, boolean hasValue, boolean isFinal) {
        BytecodeVars.FetchResult result = varAnalyser.get(name.lexeme());
        if (result != BytecodeVars.FetchResult.FAIL) {
            errorStorage.errorF(name, "Variable '%s' already defined in current scope", name.lexeme());
        }
        return varAnalyser.add(name.lexeme(), type, !isFinal, hasValue);
    }

    public AbstractParser(Compiler.ErrorStorage errorStorage) {
        this.errorStorage = errorStorage;
    }

    public void apply(Token[] toParse, VarTypeParser targetAnalyser) {
        this.current = 0;
        this.tokens = toParse;
        this.parser = targetAnalyser;
        this.varAnalyser = new BytecodeVars();
        this.finder = new RetTypeAnalyser(varAnalyser);
    }

    protected @Nullable Holder.AppliedGenerics appliedGenerics(GenericStack stack) {
        if (match(LESSER)) {
            Token r = previous();
            List<ClassReference> references = new ArrayList<>();
            do {
                references.add(consumeVarType(stack).getReference());
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Holder.AppliedGenerics(r, references.toArray(new ClassReference[0]));
        }
        return null;
    }

    protected @Nullable Holder.Generics generics(GenericStack genericStack) {
        if (match(LESSER)) {
            List<Holder.Generic> generics = new ArrayList<>();
            do {
                generics.add(generic(genericStack));
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Holder.Generics(generics.toArray(new Holder.Generic[0]));
        }
        return null;
    }

    private Holder.Generic generic(GenericStack genericStack) {
        Token name = consumeIdentifier();
        SourceClassReference lowerBound = null, upperBound = null;
        if (match(EXTENDS)) {
            lowerBound = consumeVarType(genericStack);
        } else if (match(SUPER)) {
            upperBound = consumeVarType(genericStack);
        }

        return new Holder.Generic(name, lowerBound, upperBound);
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
     * same as {@link AbstractParser#check(TokenType) check} but consumes token
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
            throw error(previous().after(), message);
        } else
            throw error(peek(), message);
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
        tokens.remove(tokens.size() - 1); //remove last
        return tokens.toArray(Token[]::new);
    }

    protected Optional<SourceClassReference> tryConsumeVarType(GenericStack generics) {
        Optional<ClassReference> optional = generics.getValue(peek().lexeme());
        if (optional.isPresent()) return Optional.of(SourceClassReference.from(advance(), optional.get()));
        if (VarTypeManager.hasPackage(peek().lexeme()) && varAnalyser.get(peek().lexeme()) == BytecodeVars.FetchResult.FAIL) {
            advance();
            if (check(DOT)) {
                current--;
                return Optional.of(consumeVarType(generics));
            }
            current--;
        }
        Token t = advance();
        ClassReference reference = parser.getClass(t.lexeme());
        if (reference != null && !check(DOT)) {
            Holder.AppliedGenerics declared = appliedGenerics(generics);
            if (declared != null) reference = new AppliedGenericsReference(reference, declared);
            return Optional.of(SourceClassReference.from(t, reference));
        } else
            current--;
        return Optional.empty();
    }

    @NotNull
    protected SourceClassReference consumeVarType(GenericStack generics) {
        SourceClassReference sourceClassReference = consumeVarTypeNoArray(generics);
        ClassReference reference = sourceClassReference.getReference();
        Token last = sourceClassReference.getToken();
        while (match(S_BRACKET_O)) {
            consume(S_BRACKET_C, "']' expected");
            reference = reference.array();
            last = previous();
        }
        if (last != sourceClassReference.getToken()) {
            return SourceClassReference.from(last, reference);
        }
        return sourceClassReference;
    }

    protected SourceClassReference consumeVarTypeNoArray(GenericStack stack) {
        Token token = consumeIdentifier();
        ClassReference reference = parser.getClass(token.lexeme());
        if (reference == null) {
            Optional<ClassReference> optional = stack.getValue(token.lexeme());
            if (optional.isPresent()) return SourceClassReference.from(token, optional.get());
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
        while (match(DOT) && reference != null) { //TODO fix token corruption due to consumption
            String enclosingName = consumeIdentifier().lexeme(); //needs to stay here for the mean-time to ensure the compiler doesn't break
        }

        if (reference == null) {
            error(token, "unknown symbol");
            return SourceClassReference.from(token, VarTypeManager.VOID.reference()); //skip rest
        }
        Holder.AppliedGenerics declared = appliedGenerics(stack);
        if (declared != null) return SourceClassReference.from(last, new AppliedGenericsReference(reference, declared));
        return SourceClassReference.from(last, reference);
    }

    protected Token consumeIdentifier() {
        return consume(IDENTIFIER, "<identifier> expected");
    }

    protected Token consumeBracketOpen(String method) {
        return this.consume(BRACKET_O, "Expected '(' after '" + method + "'.");
    }

    protected Token consumeCurlyOpen(String method) {
        return this.consume(C_BRACKET_O, "Expected '{' after '" + method + "'.");
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

    protected static class ParseError extends RuntimeException {}

    protected ParseError error(Token token, String message) {
        errorStorage.error(token, message);
        return new ParseError();
    }

    protected void warn(Token token, String message) {
        errorStorage.warn(token, message);
    }


    protected void synchronize() {
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
