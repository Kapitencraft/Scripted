package net.kapitencraft.scripted.lang.compiler.parser;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.AppliedGenericsReference;
import net.kapitencraft.scripted.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.Package;
import net.kapitencraft.scripted.lang.oop.clazz.ClassType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.kapitencraft.scripted.lang.holder.token.TokenType.*;

@SuppressWarnings("ThrowableNotThrown")
public class HolderParser extends AbstractParser {
    private GenericStack activeGenerics = new GenericStack();
    private final Deque<String> activePackages = new ArrayDeque<>();

    public HolderParser(Compiler.ErrorStorage errorStorage) {
        super(errorStorage);
    }

    public void parseImports() {
        while (check(IMPORT)) {
            try {
                importStmt();
            } catch (ParseError error) {
                synchronize();
            }
        }
    }

    private void importStmt() {
        consume(IMPORT, "Expected import or class");
        List<Token> packages = readPackage();
        String nameOverride = null;
        if (match(AS)) nameOverride = consumeIdentifier().lexeme();
        consumeEndOfArg();
        SourceClassReference target = VarTypeManager.getOrCreateClass(packages);
        if (parser.hasClass(target.getReference(), nameOverride)) {
            error(packages.getLast(), "unknown class '" + packages.stream().map(Token::lexeme).collect(Collectors.joining(".")) + "'");
        }
        parser.addClass(target, nameOverride);
    }

    private List<Token> readPackage() {
        List<Token> packages = new ArrayList<>();
        packages.add(consumePackageOrClass());
        while (!check(EOA, AS)) {
            consume(DOT, "unexpected name");
            packages.add(consumePackageOrClass());
        }
        return packages;
    }

    private Holder.AnnotationObj[] parseAnnotations() {
        List<Holder.AnnotationObj> list = new ArrayList<>();
        while (match(AT)) {
            list.add(parseAnnotationObject());
        }
        return list.toArray(new Holder.AnnotationObj[0]);
    }

    private Holder.AnnotationObj parseAnnotationObject() {
        SourceClassReference cInst = consumeVarType();
        Token[] properties = new Token[0];
        if (match(BRACKET_O)) {
            properties = getBracketEnclosedCode();
            consumeBracketClose("annotation object");
        }
        return new Holder.AnnotationObj(cInst, properties);
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

    protected SourceClassReference consumeVarType() {
        StringBuilder typeName = new StringBuilder();
        Token token = consumePackageOrClass();
        typeName.append(token.lexeme());
        if (activeGenerics != null) {
            Optional<ClassReference> generic = activeGenerics.getValue(token.lexeme());
            if (generic.isPresent()) {
                return SourceClassReference.from(token, generic.get());
            }
        }
        ClassReference reference = parser.getClass(token.lexeme());
        if (reference == null) {
            if (!check(DOT)) {
                if (!this.activePackages.isEmpty()) {
                    String pckId = this.activePackages.peek();
                    if (pckId != null) {
                        Package p = VarTypeManager.getOrCreatePackage(pckId);
                        reference = p.getOrCreateClass(token.lexeme());
                    }
                }
            } else {
                Package p = VarTypeManager.getPackage(token.lexeme());
                while (match(DOT) && p != null) {
                    String id = consumePackageOrClass().lexeme();
                    typeName.append(".").append(id);
                    if (check(DOT)) p = p.getPackage(id);
                    else reference = p.getOrCreateClass(id);
                }
            }
        }

        if (reference == null && !this.activePackages.isEmpty()) {
            reference = VarTypeManager.getOrCreateClass(typeName.toString(), activePackages.getLast());
        }
        Holder.AppliedGenerics generics = appliedGenerics();
        while (match(S_BRACKET_O)) {
            consume(S_BRACKET_C, "']' expected");
            if (reference != null) reference = reference.array();
        }
        if (reference == null) return null;
        if (generics != null) reference = new AppliedGenericsReference(reference, generics);
        return SourceClassReference.from(token, reference);
    }

    private Token[] getBracketEnclosedCode() {
        return getScopedCode(BRACKET_O, BRACKET_C);
    }

    private void checkFileName(Token name, String fileId) {
        if (fileId != null && !Objects.equals(name.lexeme(), fileId)) {
            error(name, "file and class name must match");
        }
    }

    public Holder.Class parseFile(String fileName) {
        List<Token> pck = new ArrayList<>();
        try {
            consume(PACKAGE, "package expected!");
            pck.add(consumePackageOrClass()); //TODO reset to only identifier instead
            while (!check(EOA)) {
                consume(DOT, "unexpected token");
                pck.add(consumePackageOrClass());
            }
            consumeEndOfArg();
        } catch (ParseError error) {
            synchronize();
        }

        parseImports();

        String pckId = pck.stream().map(Token::lexeme).collect(Collectors.joining("."));

        ModifiersParser parser = MODS_NO_GENERICS;
        parser.parse();

        try {
            return switch (advance().type()) {
                case CLASS -> classDecl(parser, null, pckId, fileName);
                case ENUM -> enumDecl(parser, null, pckId, fileName);
                case ANNOTATION -> annotationDecl(parser, null, pckId, fileName);
                case INTERFACE -> interfaceDecl(parser, null, pckId, fileName);
                default -> throw error(peek(), "'interface', 'class', 'enum' or 'annotation' expected");
            };
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private static ClassReference getOrCreate(String name, String pck) {
        return VarTypeManager.getOrCreateClass(name, pck);
    }

    public void parseClassProperties(ModifierScope.Group scope, List<Holder.Method> methods, @Nullable List<Holder.Constructor> constructors, List<Holder.Field> fields, ClassReference target, String pckId, Token name, boolean asEnum) {
        String constructorName = constructors != null ? name.lexeme().contains("$") ? name.lexeme().substring(name.lexeme().lastIndexOf('$') + 1) : name.lexeme() : null;
        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            modifiers.parse();
            Holder.AnnotationObj[] annotations = modifiers.getAnnotations();
            if (readClass(pckId, name.lexeme(), modifiers)) {
                modifiers.generics.pushToStack(activeGenerics);
                if (Objects.equals(advance().lexeme(), constructorName) && !check(IDENTIFIER)) {
                    Token constName = previous();
                    consumeBracketOpen("constructors");
                    Holder.Constructor decl = constructorDecl(annotations, modifiers.getGenerics(), constName, asEnum);
                    constructors.add(decl);
                } else {
                    current--; //reset after advancing in the `if`
                    SourceClassReference type = consumeVarType();
                    Token elementName = consumeIdentifier();
                    if (match(BRACKET_O)) {
                        scope.method.check(this, modifiers);
                        Holder.Method decl = funcDecl(type, modifiers, elementName);
                        methods.add(decl);
                    } else {
                        if (modifiers.generics.variables().length > 0) {
                            error(modifiers.generics.variables()[0].name(), "generics not allowed here");
                        }
                        scope.field.check(this, modifiers);
                        if (modifiers.isAbstract()) error(elementName, "fields may not be abstract");
                        fields.addAll(fieldDecl(type, annotations, elementName, modifiers.packModifiers()));
                    }
                }
                activeGenerics.pop();
            }
        }
    }

    public List<Pair<SourceClassReference, String>> parseParams() {
        List<Pair<SourceClassReference, String>> parameters = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 params.");
                }

                SourceClassReference pType = consumeVarType();
                Token pName = consume(IDENTIFIER, "Expected parameter name.");
                parameters.add(Pair.of(pType, pName.lexeme()));
            } while (match(COMMA));
        }
        return parameters;
    }


    private Holder.Constructor constructorDecl(Holder.AnnotationObj[] annotation, Holder.Generics generics, Token origin, boolean asEnum) {
        List<Pair<SourceClassReference, String>> parameters = parseParams();
        if (asEnum) { //add name and ordinal access
            parameters.add(0, Pair.of(SourceClassReference.from(origin, VarTypeManager.STRING), "?"));
            parameters.add(1, Pair.of(SourceClassReference.from(origin, VarTypeManager.INTEGER.reference()), "?"));
        }

        consumeBracketClose("params");

        consumeCurlyOpen("method body");

        Token[] code = getCurlyEnclosedCode();

        Token endToken = consumeCurlyClose("method body");

        return new Holder.Constructor(annotation, generics, origin, endToken, parameters, code);
    }

    private Holder.Method funcDecl(SourceClassReference type, ModifiersParser modifiers, Token name) {
        List<Pair<SourceClassReference, String>> parameters = parseParams();
        consumeBracketClose("params");

        short mods = modifiers.packModifiers();

        Token[] code = null;
        Token endClose = null;

        if (!Modifiers.isAbstract(mods)) { //body only if method isn't abstract
            consumeCurlyOpen("method body");

            GenericStack shadowed = null;
            if (Modifiers.isStatic(mods)) {
                shadowed = activeGenerics;
                activeGenerics = new GenericStack();
            }

            code = getCurlyEnclosedCode();

            if (shadowed != null) activeGenerics = shadowed;

            endClose = consumeCurlyClose("method body");
        } else consumeEndOfArg();
        return new Holder.Method(modifiers.packModifiers(), modifiers.getAnnotations(), modifiers.getGenerics(), type, name, endClose, parameters, code);
    }

    private List<Holder.Field> fieldDecl(SourceClassReference type, Holder.AnnotationObj[] annotations, Token name, short modifiers) {
        Token[] code = null;
        Token assign = null;

        List<Holder.Field> fields = new ArrayList<>();

        do {
            if (!fields.isEmpty()) name = consumeIdentifier(); //only consume a new name if the name comes after a `,`
            if (match(ASSIGN)) {
                assign = previous();
                code = getFieldCode();
            }
            fields.add(new Holder.Field(modifiers, annotations, type, name, assign, code));
        } while (match(COMMA));
        consumeEndOfArg();

        return fields;
    }

    /**
     * @return true if it didn't read a class, false otherwise
     */
    private boolean readClass(String pckID, String name, ModifiersParser modifiers) {
        if (match(CLASS)) {
            ModifierScope.CLASS.check(this, modifiers);
            Compiler.queueRegister(classDecl(modifiers, name, pckID, null), this.errorStorage, this.parser, name);
        } else if (match(INTERFACE)) {
            ModifierScope.INTERFACE.check(this, modifiers);
            Compiler.queueRegister(interfaceDecl(modifiers, name, pckID, null), this.errorStorage, this.parser, name);
        } else if (match(ENUM)) {
            ModifierScope.ENUM.check(this, modifiers);
            Compiler.queueRegister(enumDecl(modifiers, name, pckID, null), this.errorStorage, this.parser, name);
        } else if (match(ANNOTATION)) {
            ModifierScope.ANNOTATION.check(this, modifiers);
            Compiler.queueRegister(annotationDecl(modifiers, name, pckID, null), this.errorStorage, this.parser, name);
        } else return true;
        return false;
    }

    private Holder.Class classDecl(ModifiersParser mods, @Nullable String namePrefix, String pckID, @Nullable String fileId) {

        Token name = consumeIdentifier();
        String originalName = name.lexeme();
        if (namePrefix != null) name = name.withPrefix(namePrefix + "$");

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceClassReference.from(name, target), originalName);
        SourceClassReference superClass = SourceClassReference.from(null, VarTypeManager.OBJECT);
        Holder.Generics classGenerics = generics();

        GenericStack stack = null;
        if (classGenerics != null) {
            if (mods.isStatic()) {
                stack = activeGenerics;
                activeGenerics = new GenericStack();
            }
            classGenerics.pushToStack(activeGenerics);
        }

        if (match(EXTENDS)) superClass = consumeVarType();

        List<SourceClassReference> implemented = new ArrayList<>();

        if (match(IMPLEMENTS)) {
            do {
                implemented.add(consumeVarType());
            } while (match(COMMA));
        }

        consumeCurlyOpen("class");

        activePackages.push(pckID + "." + name.lexeme());

        Holder.Class h = parseClass(target, mods, stack, classGenerics, pckID, name, superClass, implemented);
        consumeCurlyClose("class");
        activePackages.pop();
        return h;
    }

    public Holder.Class parseClass(ClassReference target, @Nullable ModifiersParser mods, @Nullable GenericStack stack, @Nullable Holder.Generics classGenerics, String pckID, Token name, SourceClassReference superClass, List<SourceClassReference> implemented) {
        List<Holder.Method> methods = new ArrayList<>();
        List<Holder.Constructor> constructors = new ArrayList<>();
        List<Holder.Field> fields = new ArrayList<>();

        short modifiers = mods != null ? mods.packModifiers() : 0;
        Holder.AnnotationObj[] annotations = mods != null ? mods.getAnnotations() : new Holder.AnnotationObj[0];

        parseClassProperties(Modifiers.isAbstract(modifiers) ? ModifierScope.Group.ABSTRACT_CLASS : ModifierScope.Group.CLASS, methods, constructors, fields, target, pckID, name, false);

        if (stack != null) activeGenerics = stack;
        return new Holder.Class(ClassType.CLASS,
                target,
                modifiers,
                annotations,
                classGenerics,
                pckID, name,
                superClass,
                implemented.toArray(new SourceClassReference[0]),
                constructors.toArray(new Holder.Constructor[0]),
                methods.toArray(new Holder.Method[0]),
                fields.toArray(new Holder.Field[0]),
                null
        );
    }

    protected @Nullable Holder.AppliedGenerics appliedGenerics() {
        if (match(LESSER)) {
            Token t = previous();
            List<ClassReference> references = new ArrayList<>();
            do {
                references.add(consumeVarType().getReference());
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Holder.AppliedGenerics(t, references.toArray(new ClassReference[0]));
        }
        return null;
    }

    protected @Nullable Holder.Generics generics() {
        if (match(LESSER)) {
            List<Holder.Generic> generics = new ArrayList<>();
            do {
                generics.add(generic());
            } while (match(COMMA));
            consume(GREATER, "unclosed generic declaration");
            return new Holder.Generics(generics.toArray(new Holder.Generic[0]));
        }
        return null;
    }

    private Holder.Generic generic() {
        Token name = consumeIdentifier();
        SourceClassReference lowerBound = null, upperBound = null;
        if (match(EXTENDS)) {
            lowerBound = consumeVarType();
        } else if (match(SUPER)) {
            upperBound = consumeVarType();
        }

        return new Holder.Generic(name, lowerBound, upperBound);
    }

    private Holder.Class enumDecl(ModifiersParser modifiers, String namePrefix, String pckID, String fileId) {

        Token name = consumeIdentifier();
        String originalName = name.lexeme();
        if (namePrefix != null) name = name.withPrefix(namePrefix + "$");

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceClassReference.from(name, target), originalName);

        List<SourceClassReference> interfaces = new ArrayList<>();

        if (match(IMPLEMENTS)) {
            do {
                interfaces.add(consumeVarType());
            } while (match(COMMA));
        }

        consumeCurlyOpen("enum");
        activePackages.push(pckID + "." + name.lexeme());

        List<Holder.EnumConstant> enumConstants = new ArrayList<>();

        if (!check(C_BRACKET_C, EOA)) {
            int ordinal = 0;
            do {
                Token constName = consumeIdentifier();
                Token[] args;
                if (match(BRACKET_O)) {
                    args = getBracketEnclosedCode();
                    consumeBracketClose("enum constant");
                } else args = new Token[0];
                enumConstants.add(new Holder.EnumConstant(constName, ordinal++, args));
            } while (match(COMMA));
        }

        if (!check(C_BRACKET_C)) consumeEndOfArg();

        List<Holder.Constructor> constructors = new ArrayList<>();
        List<Holder.Method> methods = new ArrayList<>();
        List<Holder.Field> fields = new ArrayList<>();

        parseClassProperties(ModifierScope.Group.ENUM, methods, constructors, fields, target, pckID, name, true);

        consumeCurlyClose("enum");
        activePackages.pop();

        return new Holder.Class(ClassType.ENUM,
                target, modifiers.packModifiers(), modifiers.getAnnotations(), modifiers.getGenerics(), pckID, name,
                SourceClassReference.from(name.asIdentifier("Enum"), VarTypeManager.ENUM),
                interfaces.toArray(new SourceClassReference[0]),
                constructors.toArray(new Holder.Constructor[0]),
                methods.toArray(new Holder.Method[0]),
                fields.toArray(new Holder.Field[0]),
                enumConstants.toArray(new Holder.EnumConstant[0])
        );
    }

    private Holder.Class annotationDecl(ModifiersParser mods, String namePrefix, String pckId, String fileId) {

        Token name = consumeIdentifier();
        String originalName = name.lexeme();
        if (namePrefix != null) name = name.withPrefix(namePrefix + "$");

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckId);

        parser.addClass(SourceClassReference.from(name, target), originalName);

        consumeCurlyOpen("annotation");
        activePackages.push(pckId + "." + name.lexeme());

        List<Holder.Method> methods = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            ModifiersParser modifiers = MODIFIERS;
            modifiers.parse();
            Holder.AnnotationObj[] annotations = modifiers.getAnnotations();
            if (readClass(pckId, name.lexeme(), modifiers)) {
                ModifierScope.ANNOTATION.check(this, modifiers);
                SourceClassReference type = consumeVarType();
                Token elementName = consumeIdentifier();
                if (match(BRACKET_O)) {
                    Holder.Method decl = annotationMethodDecl(type, annotations, elementName);
                    methods.add(decl);
                } else error(peek(), "'(' expected");
            }
        }

        consumeCurlyClose("annotation");
        activePackages.pop();
        return new Holder.Class(ClassType.ANNOTATION,
                target, mods.packModifiers(), mods.getAnnotations(), mods.getGenerics(), pckId, name,
                null, null, null,
                methods.toArray(new Holder.Method[0]),
                null, null
        );
    }

    private Holder.Method annotationMethodDecl(SourceClassReference type, Holder.AnnotationObj[] annotations, Token elementName) {
        consumeBracketClose("annotation");
        Token[] defaultCode = new Token[0];
        boolean defaulted = false;
        if (match(DEFAULT)) {
            defaultCode = getFieldCode();
            defaulted = true;
        }
        consumeEndOfArg();
        return new Holder.Method(Modifiers.pack(false, false, !defaulted), annotations, null, type, elementName, null, List.of(), defaultCode);
    }

    private Holder.Class interfaceDecl(ModifiersParser mods, @Nullable String namePrefix, String pckID, @Nullable String fileId) {

        Token name = consumeIdentifier();
        String originalName = name.lexeme();
        if (namePrefix != null) name = name.withPrefix(namePrefix + "$");

        checkFileName(name, fileId);

        ClassReference target = getOrCreate(name.lexeme(), pckID);

        parser.addClass(SourceClassReference.from(name, target), originalName);

        Holder.Generics classGenerics = generics();

        GenericStack stack = null;
        if (classGenerics != null) {
            if (mods.isStatic()) {
                stack = activeGenerics;
                activeGenerics = new GenericStack();
            } else classGenerics.pushToStack(activeGenerics);
        }

        List<SourceClassReference> parentInterfaces = new ArrayList<>();

        if (match(EXTENDS)) {
            do {
                parentInterfaces.add(consumeVarType());
            } while (match(COMMA));
        }

        activePackages.push(pckID + "." + name.lexeme());

        consumeCurlyOpen("class");

       return parseInterface(target, pckID, name, stack, classGenerics, mods, parentInterfaces);
    }

    public Holder.Class parseInterface(ClassReference target, String pckID, Token name, @Nullable GenericStack stack, @Nullable Holder.Generics classGenerics, @Nullable ModifiersParser mods, List<SourceClassReference> parentInterfaces) {
        List<Holder.Method> methods = new ArrayList<>();
        List<Holder.Field> fields = new ArrayList<>();

        parseClassProperties(ModifierScope.Group.INTERFACE, methods, null, fields, target, pckID, name, false);

        consumeCurlyClose("class");

        if (stack != null) activeGenerics = stack;
        else if (classGenerics != null) activeGenerics.pop();
        activePackages.pop();
        short modifiers = mods != null ? mods.packModifiers() : 0;
        Holder.AnnotationObj[] annotations = mods != null ? mods.getAnnotations() : new Holder.AnnotationObj[0];
        return new Holder.Class(ClassType.INTERFACE, target, modifiers,
                annotations, classGenerics, pckID, name, null,
                parentInterfaces.toArray(new SourceClassReference[0]),
                null,
                methods.toArray(new Holder.Method[0]),
                fields.toArray(new Holder.Field[0]),
                null
        );
    }

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
        private Holder.Generics generics;
        private Holder.AnnotationObj[] annotations;
        private final Map<TokenType, List<TokenType>> illegalCombinations = new HashMap<>();
        private final TokenType[] interrupt = {IDENTIFIER, CLASS, INTERFACE, ANNOTATION, ENUM, EOF};
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
            List<Holder.Generic> generics = new ArrayList<>();
            a: while (!check(interrupt) && !isAtEnd()) {
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
            this.generics = new Holder.Generics(generics.toArray(new Holder.Generic[0]));
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

        public Holder.AnnotationObj[] getAnnotations() {
            return annotations;
        }

        public void setDefaultAbstract(boolean b) {
            this.defaultAbstract = b;
        }

        public Holder.Generics getGenerics() {
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


        public void check(HolderParser holderParser, ModifiersParser parser) {
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
}
