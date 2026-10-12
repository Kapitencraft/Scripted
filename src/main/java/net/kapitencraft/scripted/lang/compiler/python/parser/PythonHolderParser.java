package net.kapitencraft.scripted.lang.compiler.python.parser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.compiler.exe.text.HolderParser;
import net.kapitencraft.scripted.lang.compiler.python.PythonConstructorHolder;
import net.kapitencraft.scripted.lang.compiler.python.PythonMethodHolder;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.AppliedGenericsSourceReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.attribute.*;
import net.kapitencraft.scripted.lang.holder.oop.clazz.*;
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
import java.util.stream.Collectors;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

public class PythonHolderParser extends AbstractPythonParser implements HolderParser {
    private GenericStack activeGenerics = new GenericStack();
    private final String pck;
    private final ArrayDeque<String> anonymousNames = new ArrayDeque<>();

    public PythonHolderParser(ErrorStorage errorStorage, SourceTree sourceSink, CompileSource source) {
        super(errorStorage, sourceSink, source);
        this.pck = source.pck();
    }

    //region imports & package
    public void parseImports() {
        while (match(IMPORT)) {
            importStmt();
        }
    }

    private void importStmt() {
        List<Token> packages = readPackage();
        String nameOverride = null;
        if (match(AS)) nameOverride = consumeIdentifier().lexeme();
        consumeEndOfArg();
        SourceReference target = VarTypeManager.getOrCreateClass(packages);
        if (parser.hasClass(target.getReference(), nameOverride)) {
            error(packages.getLast(), "unknown class '" + packages.stream().map(Token::lexeme).collect(Collectors.joining(".")) + "'");
        }
        parser.addClass(target, nameOverride);
    }

    private List<Token> readPackage() {
        List<Token> packages = new ArrayList<>();
        packages.add(consumeIdentifier());
        while (!check(LINE_FEED, AS)) {
            consume(DOT, "unexpected name");
            packages.add(consumeIdentifier());
        }
        return packages;
    }
    //endregion

    private AnnotationObj[] parseAnnotations() {
        List<AnnotationObj> list = new ArrayList<>();
        while (match(AT)) {
            list.add(parseAnnotationObject());
        }
        return list.toArray(new AnnotationObj[0]);
    }

    private AnnotationObj parseAnnotationObject() {
        SourceReference cInst = consumeVarTypeNoArray(GenericStack.EMPTY);
        Token[] properties = new Token[0];
        if (match(BRACKET_O)) {
            properties = getBracketEnclosedCode();
            consumeBracketClose("annotation object");
        }
        return new AnnotationObj(cInst, properties);
    }

    private Token[] getFieldCode() {
        List<Token> tokens = new ArrayList<>();
        int sBracket = 0;
        int cBracket = 0;
        int bracket = 0; //necessary to allow parameter calls, and anonymous classes / lambda
        do {
            Token advance = advance();
            switch (advance.type()) {
                case S_BRACKET_O -> sBracket++;
                case S_BRACKET_C -> sBracket--;
                case C_BRACKET_O -> cBracket++;
                case C_BRACKET_C -> cBracket--;
                case BRACKET_O -> bracket++;
                case BRACKET_C -> bracket--;
            }
            tokens.add(advance);
        } while ((sBracket > 0 || cBracket > 0 || bracket > 0) || !check(EOA));
        return tokens.toArray(Token[]::new);
    }

    private Token[] getBracketEnclosedCode() {
        return getScopedCode(BRACKET_O, BRACKET_C);
    }

    private void checkFileName(Token name, String fileId) {
        if (fileId != null && !Objects.equals(name.lexeme(), fileId)) {
            error(name, "file and class name must match");
        }
    }

    public ClassConstructor parseFile(String fileName, String declaredPck) {
        parseImports();

        ClassReference target = getOrCreate(fileName, declaredPck);

        List<Token> mainMethodCode = new ArrayList<>();
        List<PythonMethodHolder> methods = new ArrayList<>();
        List<FieldHolder> fields = new ArrayList<>();

        this.anonymousNames.push(fileName);
        while (!isAtEnd()) {
            skipIndent();

            if (check(TokenTypeCategory.ATT_MODIFIER) || check(TokenTypeCategory.ATT_KEYWORD)) {
                ModifiersParser parser = MODS_NO_GENERICS;
                parser.parse();
                if (match(FUNC)) {
                    SourceReference reference = consumeVarType();
                    Token name = consumeIdentifier();
                    methods.add(funcDecl(reference, parser, name));
                } else if (match(GLOBAL)) {
                    Token name = consumeIdentifier();
                    consumeColon("attribute");
                    SourceReference type = consumeVarType();

                    fields.addAll(fieldDecl(type, parser.annotations, name, parser.packModifiers()));
                } else {
                    readClass(declaredPck, fileName, parser);
                }
                continue;
            } else {
                while (!isAtEnd() && !check(LINE_FEED))
                    mainMethodCode.add(advance());
                if (!isAtEnd()) {
                    mainMethodCode.add(advance()); //add line feed
                }
            }
        }
        if (!mainMethodCode.isEmpty()) {
            mainMethodCode.removeLast();
            methods.add(new PythonMethodHolder(
                    Modifiers.pack(false, true, false),
                    new AnnotationObj[0],
                    new Generics(new Generic[0]),
                    SourceReference.from(null, VarTypeManager.VOID.reference()),
                    Token.createNative("main"),
                    tokens[tokens.length - 1].after(),
                    List.of(Pair.of(SourceReference.from(null, VarTypeManager.STRING.array()), "args")),
                    List.of(),
                    mainMethodCode.toArray(new Token[0]),
                    0
            ));
        }
        this.anonymousNames.pop();
        return new ClassHolder(
                target,
                (short) 0,
                new AnnotationObj[0],
                new Generics(new Generic[0]),
                declaredPck,
                Token.createNative(fileName),
                SourceReference.from(null, VarTypeManager.OBJECT),
                new SourceReference[0],
                new ConstructorHolder[0],
                methods.toArray(new PythonMethodHolder[0]),
                fields.toArray(new FieldHolder[0])
        );
    }

    private static ClassReference getOrCreate(String name, String pck) {
        return VarTypeManager.getOrCreateClass(name, pck);
    }

    public void parseClassProperties(ModifierScope.Group scope, List<PythonMethodHolder> methodHolders, @Nullable List<PythonConstructorHolder> constructorHolders, List<FieldHolder> fieldHolders, ClassReference target, String pckId, Token name, boolean asEnum) {
        String constructorName = constructorHolders != null ? name.lexeme().contains("$") ? name.lexeme().substring(name.lexeme().lastIndexOf('$') + 1) : name.lexeme() : null;
        while (!isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            if (skipIndent()) {
                modifiers.parse();
                AnnotationObj[] annotations = modifiers.getAnnotations();
                if (readClass(pckId, name.lexeme(), modifiers)) {
                    modifiers.generics.pushToStack(activeGenerics);

                    if (match(FUNC)) {
                        if (Objects.equals(advance().lexeme(), constructorName) && !check(IDENTIFIER)) {
                            Token constName = previous();
                            consumeBracketOpen("constructors");
                            indents++;
                            PythonConstructorHolder decl = constructorDecl(annotations, modifiers.getGenerics(), constName, asEnum);
                            indents--;
                            constructorHolders.add(decl);
                            continue;
                        }
                        current--; //reset after advancing in line 205
                        SourceReference type = consumeVarType(activeGenerics);
                        Token elementName = consumeIdentifier();
                        scope.method.check(this, modifiers);
                        PythonMethodHolder decl = funcDecl(type, modifiers, elementName);
                        methodHolders.add(decl);
                    } else {
                        consume(GLOBAL, "'global' expected");
                        SourceReference type = consumeVarType(activeGenerics);
                        Token elementName = consumeIdentifier();
                        if (modifiers.generics.variables().length > 0) {
                            error(modifiers.generics.variables()[0].name(), "generics not allowed here");
                        }
                        scope.field.check(this, modifiers);
                        if (modifiers.isAbstract()) error(elementName, "fields may not be abstract");
                        fieldHolders.addAll(fieldDecl(type, annotations, elementName, modifiers.packModifiers()));
                    }
                    activeGenerics.pop();
                }
            } else
                break;
        }
    }

    public List<Pair<SourceReference, String>> parseParams() {
        List<Pair<SourceReference, String>> parameters = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 params.");
                }

                SourceReference pType = consumeVarType(activeGenerics);
                Token pName = consume(IDENTIFIER, "Expected parameter name.");
                parameters.add(Pair.of(pType, pName.lexeme()));
            } while (match(COMMA));
        }
        return parameters;
    }

    //region attribute decl
    private PythonConstructorHolder constructorDecl(AnnotationObj[] annotation, Generics generics, Token origin, boolean asEnum) {
        List<Pair<SourceReference, String>> parameters = parseParams();
        if (asEnum) { //add name and ordinal access
            parameters.add(0, Pair.of(SourceReference.from(origin, VarTypeManager.STRING), "$name"));
            parameters.add(1, Pair.of(SourceReference.from(origin, VarTypeManager.INTEGER.reference()), "$ordinal"));
        }

        consumeBracketClose("params");

        List<SourceReference> thrown = new ArrayList<>();
        if (match(THROWS)) {
            do {
                thrown.add(consumeVarType(activeGenerics));
            } while (match(COMMA));
        }

        consumeScopeOpen("method body");

        Token[] code = getIndentedCode(indents);

        Token endToken = peek();

        return new PythonConstructorHolder(annotation, generics, origin, endToken, parameters, thrown, code, indents);
    }

    private PythonMethodHolder funcDecl(SourceReference type, ModifiersParser modifiers, Token name) {
        consumeBracketOpen("params");
        List<Pair<SourceReference, String>> parameters = parseParams();
        consumeBracketClose("params");

        List<SourceReference> thrown = new ArrayList<>();
        if (match(THROWS)) {
            do {
                thrown.add(consumeVarType(activeGenerics));
            } while (match(COMMA));
        }

        short mods = modifiers.packModifiers();

        Token[] code = null;
        Token endClose = null;

        if (!Modifiers.isAbstract(mods)) { //body only if method isn't abstract
            consumeColon("method body");
            consumeLineFeed("method body");

            GenericStack shadowed = null;
            if (Modifiers.isStatic(mods)) {
                shadowed = activeGenerics;
                activeGenerics = new GenericStack();
            }

            code = getIndentedCode(++indents);
            indents--;

            if (shadowed != null) activeGenerics = shadowed;

        } else consumeEndOfArg();
        return new PythonMethodHolder(modifiers.packModifiers(), modifiers.getAnnotations(), modifiers.getGenerics(), type, name, endClose, parameters, thrown, code, indents + 1);
    }

    private List<FieldHolder> fieldDecl(SourceReference type, AnnotationObj[] annotations, Token name, short modifiers) {
        Token[] code = null;
        Token assign = null;

        List<FieldHolder> fieldHolders = new ArrayList<>();

        do {
            if (!fieldHolders.isEmpty())
                name = consumeIdentifier(); //only consume a new name if the name comes after a `,`
            if (match(ASSIGN)) {
                assign = previous();
                code = getFieldCode();
            }
            fieldHolders.add(new FieldHolder(modifiers, annotations, type, name, assign, code));
        } while (match(COMMA));
        consumeEndOfArg();

        return fieldHolders;
    }
    //endregion

    /**
     * @return true if it didn't read a class, false otherwise
     */
    private boolean readClass(String pckID, String name, ModifiersParser modifiers) {
        if (match(CLASS)) {
            ModifierScope.CLASS.check(this, modifiers);
            addInternal(classDecl(modifiers, name, pckID, null), this.errorStorage, this.parser);
        } else if (match(INTERFACE)) {
            ModifierScope.INTERFACE.check(this, modifiers);
            addInternal(interfaceDecl(modifiers, name, pckID, null), this.errorStorage, this.parser);
        } else if (match(ENUM)) {
            ModifierScope.ENUM.check(this, modifiers);
            addInternal(enumDecl(modifiers, name, pckID, null), this.errorStorage, this.parser);
        } else if (match(ANNOTATION)) {
            ModifierScope.ANNOTATION.check(this, modifiers);
            addInternal(annotationDecl(modifiers, name, pckID, null), this.errorStorage, this.parser);
        } else return true;
        return false;
    }

    private ClassHolder classDecl(ModifiersParser mods, @Nullable String namePrefix, String pckID, @Nullable String fileId) {

        Token name = consumeIdentifier();
        String originalName = name.lexeme();
        if (namePrefix != null) name = name.withPrefix(namePrefix + "$");

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceReference.from(name, target), originalName);
        SourceReference superClass = SourceReference.from(null, VarTypeManager.OBJECT);
        Generics classGenerics = generics();

        GenericStack stack = null;
        if (classGenerics != null) {
            if (mods.isStatic()) {
                stack = activeGenerics;
                activeGenerics = new GenericStack();
            }
            classGenerics.pushToStack(activeGenerics);
        }

        if (match(EXTENDS)) superClass = consumeVarTypeNoArray();

        List<SourceReference> implemented = new ArrayList<>();

        if (match(IMPLEMENTS)) {
            do {
                implemented.add(consumeVarTypeNoArray());
            } while (match(COMMA));
        }

        consumeScopeOpen("classBody");
        indents++;

        anonymousNames.push(name.lexeme());

        ClassHolder h = parseClass(target, mods, stack, classGenerics, pckID, name, superClass, implemented);
        indents--;
        anonymousNames.pop();
        return h;
    }

    public ClassHolder parseClass(ClassReference target, @Nullable ModifiersParser mods, @Nullable GenericStack stack, @Nullable Generics classGenerics, String pckID, Token name, SourceReference superClass, List<SourceReference> implemented) {
        List<PythonMethodHolder> methodHolders = new ArrayList<>();
        List<PythonConstructorHolder> constructorHolders = new ArrayList<>();
        List<FieldHolder> fieldHolders = new ArrayList<>();

        short modifiers = mods != null ? mods.packModifiers() : 0;
        AnnotationObj[] annotations = mods != null ? mods.getAnnotations() : new AnnotationObj[0];

        parseClassProperties(Modifiers.isAbstract(modifiers) ? ModifierScope.Group.ABSTRACT_CLASS : ModifierScope.Group.CLASS, methodHolders, constructorHolders, fieldHolders, target, pckID, name, false);

        if (stack != null) activeGenerics = stack;
        return new ClassHolder(target,
                modifiers,
                annotations,
                classGenerics,
                pckID, name,
                superClass,
                implemented.toArray(new SourceReference[0]),
                constructorHolders.toArray(PythonConstructorHolder[]::new),
                methodHolders.toArray(new PythonMethodHolder[0]),
                fieldHolders.toArray(new FieldHolder[0])
        );
    }

    //region generics
    protected @Nullable AppliedGenerics appliedGenerics() {
        if (match(LESSER)) {
            Token t = previous();
            List<ClassReference> references = new ArrayList<>();
            do {
                references.add(consumeVarType().getReference());
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new AppliedGenerics(t, references.toArray(new ClassReference[0]));
        }
        return null;
    }

    protected @Nullable Generics generics() {
        if (match(LESSER)) {
            List<Generic> generics = new ArrayList<>();
            do {
                generics.add(generic());
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Generics(generics.toArray(new Generic[0]));
        }
        return null;
    }

    private Generic generic() {
        Token name = consumeIdentifier();
        SourceReference lowerBound = null, upperBound = null;
        if (match(EXTENDS)) {
            lowerBound = consumeVarTypeNoArray();
        } else if (match(SUPER)) {
            upperBound = consumeVarTypeNoArray();
        }

        return new Generic(name, lowerBound, upperBound);
    }
    //endregion

    private EnumHolder enumDecl(ModifiersParser modifiers, String namePrefix, String pckID, String fileId) {

        Token name = consumeIdentifier();
        String originalName = name.lexeme();
        if (namePrefix != null) name = name.withPrefix(namePrefix + "$");

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceReference.from(name, target), originalName);

        List<SourceReference> interfaces = new ArrayList<>();

        if (match(IMPLEMENTS)) {
            do {
                interfaces.add(consumeVarTypeNoArray());
            } while (match(COMMA));
        }

        consumeScopeOpen("enum");
        anonymousNames.push(name.lexeme());

        List<EnumConstantHolder> enumConstantHolders = new ArrayList<>();

        if (!check(C_BRACKET_C, EOA)) {
            int ordinal = 0;
            do {
                Token constName = consumeIdentifier();
                Token[] args;
                if (match(BRACKET_O)) {
                    args = getBracketEnclosedCode();
                    consumeBracketClose("enum constant");
                } else args = new Token[0];
                enumConstantHolders.add(new EnumConstantHolder(constName, ordinal++, args));
            } while (match(COMMA));
        }

        if (!check(C_BRACKET_C)) consumeEndOfArg();

        List<PythonConstructorHolder> constructorHolders = new ArrayList<>();
        List<PythonMethodHolder> methodHolders = new ArrayList<>();
        List<FieldHolder> fieldHolders = new ArrayList<>();

        parseClassProperties(ModifierScope.Group.ENUM, methodHolders, constructorHolders, fieldHolders, target, pckID, name, true);

        consumeCurlyClose("enum");
        anonymousNames.pop();

        return new EnumHolder(
                target, modifiers.packModifiers(), modifiers.getAnnotations(), modifiers.getGenerics(), pckID, name,
                interfaces.toArray(new SourceReference[0]),
                constructorHolders.toArray(new PythonConstructorHolder[0]),
                methodHolders.toArray(new PythonMethodHolder[0]),
                fieldHolders.toArray(new FieldHolder[0]),
                enumConstantHolders.toArray(new EnumConstantHolder[0])
        );
    }

    private AnnotationHolder annotationDecl(ModifiersParser mods, String namePrefix, String pckId, String fileId) {

        Token name = consumeIdentifier();
        String originalName = name.lexeme();
        if (namePrefix != null) name = name.withPrefix(namePrefix + "$");

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckId);

        parser.addClass(SourceReference.from(name, target), originalName);

        consumeCurlyOpen("annotation");
        anonymousNames.push(name.lexeme());

        List<MethodHolder> methodHolders = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            modifiers.parse();
            AnnotationObj[] annotations = modifiers.getAnnotations();
            if (readClass(pckId, name.lexeme(), modifiers)) {
                ModifierScope.ANNOTATION.check(this, modifiers);
                SourceReference type = consumeVarType();
                Token elementName = consumeIdentifier();
                if (match(BRACKET_O)) {
                    MethodHolder decl = annotationMethodDecl(type, annotations, elementName);
                    methodHolders.add(decl);
                } else error(peek(), "'(' expected");
            }
        }

        consumeCurlyClose("annotation");
        anonymousNames.pop();
        return new AnnotationHolder(
                target, mods.packModifiers(), mods.getAnnotations(), mods.getGenerics(), pckId, name,
                methodHolders.toArray(new MethodHolder[0])
        );
    }

    private MethodHolder annotationMethodDecl(SourceReference type, AnnotationObj[] annotations, Token elementName) {
        consumeBracketClose("annotation");
        Token[] defaultCode = new Token[0];
        boolean defaulted = false;
        if (match(DEFAULT)) {
            defaultCode = getFieldCode();
            defaulted = true;
        }
        consumeEndOfArg();
        return new MethodHolder(Modifiers.pack(false, false, !defaulted), annotations, null, type, elementName, null, List.of(), List.of(), defaultCode);
    }

    private InterfaceHolder interfaceDecl(ModifiersParser mods, @Nullable String namePrefix, String pckID, @Nullable String fileId) {

        Token name = consumeIdentifier();
        String originalName = name.lexeme();
        if (namePrefix != null) name = name.withPrefix(namePrefix + "$");

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceReference.from(name, target), originalName);

        Generics classGenerics = generics();

        GenericStack stack = null;
        if (classGenerics != null) {
            if (mods.isStatic()) {
                stack = activeGenerics;
                activeGenerics = new GenericStack();
            } else classGenerics.pushToStack(activeGenerics);
        }

        List<SourceReference> parentInterfaces = new ArrayList<>();

        if (match(EXTENDS)) {
            do {
                parentInterfaces.add(consumeVarTypeNoArray());
            } while (match(COMMA));
        }

        anonymousNames.push(name.lexeme());

        consumeScopeOpen("interface");

        return parseInterface(target, pckID, name, stack, classGenerics, mods, parentInterfaces);
    }

    public InterfaceHolder parseInterface(ClassReference target, String pckID, Token name, @Nullable GenericStack stack, @Nullable Generics classGenerics, @Nullable ModifiersParser mods, List<SourceReference> parentInterfaces) {
        List<PythonMethodHolder> methodHolders = new ArrayList<>();
        List<FieldHolder> fieldHolders = new ArrayList<>();

        parseClassProperties(ModifierScope.Group.INTERFACE, methodHolders, null, fieldHolders, target, pckID, name, false);

        consumeCurlyClose("interface");

        if (stack != null) activeGenerics = stack;
        else if (classGenerics != null) activeGenerics.pop();
        anonymousNames.pop();
        short modifiers = mods != null ? mods.packModifiers() : 0;
        AnnotationObj[] annotations = mods != null ? mods.getAnnotations() : new AnnotationObj[0];
        return new InterfaceHolder(target, modifiers,
                annotations, classGenerics, pckID, name,
                parentInterfaces.toArray(new SourceReference[0]),
                methodHolders.toArray(new PythonMethodHolder[0]),
                fieldHolders.toArray(new FieldHolder[0])
        );
    }

    //region modifiers & attribute parsing
    private final ModifiersParser MODIFIERS = from(true, FINAL, ABSTRACT, STATIC, DEFAULT);
    private final ModifiersParser MODS_NO_GENERICS = from(false, FINAL, ABSTRACT, STATIC, DEFAULT);

    public ModifiersParser from(boolean generics, TokenType... accepted) {
        Map<TokenType, List<TokenType>> illegals = new HashMap<>();
        for (TokenType type : accepted) {
            List<TokenType> others = new ArrayList<>();
            for (TokenType type1 : accepted) {
                if (type != type1) others.add(type1);
            }
            illegals.put(type, others);
        }
        return new ModifiersParser(List.of(accepted), illegals, generics);
    }

    public class ModifiersParser {
        private final Map<TokenType, Token> encountered = new HashMap<>();
        private final List<TokenType> acceptable = new ArrayList<>();
        private Generics generics;
        private AnnotationObj[] annotations;
        private final Map<TokenType, List<TokenType>> illegalCombinations = new HashMap<>();
        private final TokenType[] interrupt = {IDENTIFIER, CLASS, INTERFACE, ANNOTATION, ENUM, EOF, FUNC, GLOBAL};
        private boolean defaultAbstract = false;
        private final boolean allowGenerics;

        public ModifiersParser(List<TokenType> acceptable, Map<TokenType, List<TokenType>> combinations, boolean allowGenerics) {
            this.acceptable.addAll(acceptable);
            this.illegalCombinations.putAll(combinations);
            this.allowGenerics = allowGenerics;
        }

        public void parse() {
            annotations = parseAnnotations();
            this.clear();
            List<Generic> generics = new ArrayList<>();
            a:
            while (!check(interrupt) && !isAtEnd()) {
                boolean handled = false;
                if (match(LESSER) && allowGenerics) {
                    if (!generics.isEmpty()) error(previous(), "duplicate generic declaration");
                    do {
                        generics.add(generic());
                    } while (match(COMMA));
                    consume(GREATER, "unclosed generic declaration");
                    handled = true;
                } else for (TokenType type : acceptable) {
                    if (match(type)) {
                        if (encountered.containsKey(type)) {
                            error(previous(), "duplicate modifier '" + type.id() + "'");
                            continue a;
                        }
                        encountered.put(type, previous());
                        illegalCombinations.get(type).stream()
                                .filter(encountered::containsKey)
                                .forEach(tokenType ->
                                        error(previous(), String.format(
                                                "Illegal combination of modifiers '%s' and '%s'",
                                                type.id(),
                                                tokenType.id()
                                        ))
                                );
                        handled = true;
                    }
                }
                if (!handled) error(peek(), "modifier or <identifier> expected");
            }
            this.generics = new Generics(generics.toArray(new Generic[0]));
        }

        private void clear() {
            encountered.clear();
        }

        private short packModifiers() {
            return Modifiers.pack(isFinal(), isStatic(), isAbstract() && !isDefault());
        }

        private boolean isFinal() {
            return encountered.containsKey(FINAL);
        }

        private boolean isStatic() {
            return encountered.containsKey(STATIC);
        }

        private boolean isAbstract() {
            return defaultAbstract || encountered.containsKey(ABSTRACT);
        }

        private boolean isDefault() {
            return encountered.containsKey(DEFAULT);
        }

        public Token get(TokenType type) {
            return encountered.get(type);
        }

        public AnnotationObj[] getAnnotations() {
            return annotations;
        }

        public void setDefaultAbstract(boolean b) {
            this.defaultAbstract = b;
        }

        public Generics getGenerics() {
            return generics;
        }
    }

    public enum ModifierScope {
        ABSTRACT_CLASS(List.of()),
        CLASS(List.of(), DEFAULT),
        INTERFACE(List.of(ABSTRACT), FINAL),
        INTERFACE_FIELD(List.of()),
        ENUM(List.of(FINAL), ABSTRACT, DEFAULT),
        ANNOTATION(List.of(FINAL), STATIC, DEFAULT);

        private final List<TokenType> illegalModifiers;
        private final List<TokenType> redundantModifiers;

        ModifierScope(List<TokenType> redundantModifiers, TokenType... illegalModifiers) {
            this.redundantModifiers = redundantModifiers;
            this.illegalModifiers = List.of(illegalModifiers);
        }

        public void check(PythonHolderParser holderParser, ModifiersParser parser) {
            parser.setDefaultAbstract(this == INTERFACE);
            illegalModifiers.stream()
                    .map(parser::get)
                    .filter(Objects::nonNull)
                    .forEach(token -> holderParser.error(token, String.format("modifier '%s' not allowed here", token.lexeme())));
            redundantModifiers.stream()
                    .map(parser::get)
                    .filter(Objects::nonNull)
                    .forEach(token -> holderParser.warn(token, String.format("redundant modifier '%s'", token.lexeme())));
        }

        public enum Group {
            ABSTRACT_CLASS(ModifierScope.ABSTRACT_CLASS, ModifierScope.CLASS),
            CLASS(ModifierScope.CLASS, ModifierScope.CLASS),
            INTERFACE(ModifierScope.INTERFACE, ModifierScope.INTERFACE_FIELD),
            ENUM(ModifierScope.ENUM, ModifierScope.CLASS);

            private final ModifierScope method, field;

            Group(ModifierScope method, ModifierScope field) {
                this.method = method;
                this.field = field;
            }
        }
    }
    //endregion

    @NotNull
    protected SourceReference consumeVarType() {
        SourceReference sourceReference = consumeVarTypeNoArray(activeGenerics);
        ClassReference reference = sourceReference.getReference();
        Token last = sourceReference.getToken();
        AppliedGenerics appliedGenerics = appliedGenerics(activeGenerics);
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

    protected SourceReference consumeVarTypeNoArray() {
        Token token = consumeIdentifier();
        ClassReference reference = parser.getClass(token.lexeme());
        if (reference == null) {
            Optional<ClassReference> optional = activeGenerics.getValue(token.lexeme());
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
            String enclosingName = consumeIdentifier().lexeme(); //needs to stay here for the mean-time to ensure the compiler doesn't break
            ClassReference name = VarTypeManager.getClassForName(reference.absoluteName() + "." + enclosingName);
            if (name == null) {
                current = index;
                break;
            }
            reference = name;
            index = current;
        }

        if (reference == null) {
            String name = String.join("$", anonymousNames);
            reference = VarTypeManager.getOrCreateClass(name + "$" + token.lexeme(), pck);
            SourceReference sourceReference = SourceReference.from(token, reference); //skip rest
            parser.addClass(sourceReference, token.lexeme());
        }
        AppliedGenerics declared = appliedGenerics(activeGenerics);
        if (declared != null) return SourceReference.from(last, new AppliedGenericsReference(reference, declared));
        return SourceReference.from(last, reference);
    }
}