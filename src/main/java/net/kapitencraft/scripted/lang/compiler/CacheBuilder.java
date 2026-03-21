package net.kapitencraft.scripted.lang.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.bytecode.exe.Opcode;
import net.kapitencraft.scripted.lang.bytecode.storage.Chunk;
import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.natives.NativeClassInstance;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class CacheBuilder implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    public static final int majorVersion = 1, minorVersion = 0;

    //marks whether to keep the expr result on the stack or not
    private boolean retainExprResult = false;
    //marks whether the expr result has already been ignored and therefore no POP must be emitted
    private boolean ignoredExprResult = false;
    private final Chunk.Builder builder = new Chunk.Builder();
    private final Stack<Loop> loops = new Stack<>();

    public CacheBuilder() {
    }

    public void cache(Expr expr) {
        expr.accept(this);
    }

    private void cacheOrNull(@Nullable Expr expr) {
        if (expr == null) builder.addCode(Opcode.NULL);
        else cache(expr);
    }

    public void cache(Stmt stmt) {
        stmt.accept(this);
    }

    public void saveArgs(Expr[] args) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        for (Expr arg : args) {
            this.cache(arg);
        }
        retainExprResult = hadRetain;
    }

    public JsonObject cacheClass(CacheableClass loxClass) {
        return loxClass.save(this); //TODO convert to entirely bytecode later
    }

    public JsonArray cacheAnnotations(Annotation[] annotations) {
        JsonArray array = new JsonArray();
        for (Annotation instance : annotations) {
            Annotation retention;
            if ((retention = VarTypeManager.directParseTypeCompiler(instance.getType()).get().getAnnotation(VarTypeManager.RETENTION)) != null) {
                if (((NativeClassInstance) retention.getProperty("value")).getObject() == RetentionPolicy.SOURCE) {
                    continue;
                }
                //TODO create annotation processor
            }
            array.add(instance.toJson());
        }
        return array;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        AssignOperators result = getAssignOperators(expr.ordinal());
        assign(expr.executor(), expr.value(), expr.type(), result.get(), result.assign(), b -> {
            if (expr.ordinal() > 2) b.addArg(expr.ordinal());
        });
        return null;
    }

    private record AssignOperators(Opcode get, Opcode assign) {
    }

    private static @NotNull AssignOperators getAssignOperators(int ordinal) {
        Opcode get = Opcode.GET;
        Opcode assign = Opcode.ASSIGN;
        switch (ordinal) {
            case 0 -> {
                get = Opcode.GET_0;
                assign = Opcode.ASSIGN_0;
            }
            case 1 -> {
                get = Opcode.GET_1;
                assign = Opcode.ASSIGN_1;
            }
            case 2 -> {
                get = Opcode.GET_2;
                assign = Opcode.ASSIGN_2;
            }
        }
        return new AssignOperators(get, assign);
    }

    //TODO enable DUP if `Assign` / `VarDecl` is directly followed by a `Get`
    @Override
    public Void visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        AssignOperators operators = getAssignOperators(expr.ordinal());
        specialAssign(expr.executor(), expr.assignType(), operators.get(), operators.assign(), b -> {
            if (expr.ordinal() > 2) b.addArg(expr.ordinal());
        });
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.left());
        cache(expr.right());
        if (hadRetain) { //if the result of a binary expression is ignored, we don't need to do its calculation as it is pure without side effects
            final ClassReference executor = expr.executor();
            this.builder.changeLineIfNecessary(expr.operator());
            TokenType operator = expr.operator().type();
            Opcode opcode = switch (operator) {
                case EQUAL -> Opcode.EQUAL;
                case NEQUAL -> Opcode.NEQUAL;
                case LEQUAL -> getLequal(executor);
                case GEQUAL -> getGequal(executor);
                case LESSER -> getLesser(executor);
                case GREATER -> getGreater(executor);
                case SUB -> getSub(executor);
                case ADD -> getAdd(executor);
                case MUL -> getMul(executor);
                case DIV -> getDiv(executor);
                case POW -> getPow(executor);
                case MOD -> getMod(executor);
                default -> throw new IllegalStateException("not an operator: " + operator);
            };
            builder.addCode(opcode);
        } else {
            builder.addCode(Opcode.POP_2);
            ignoredExprResult = true;
        }
        retainExprResult = hadRetain;
        return null;
    }

    //region comparison
    private Opcode getComparator(TokenType type, ClassReference reference) {
        return switch (type) {
            case EQUAL -> Opcode.EQUAL;
            case NEQUAL -> Opcode.NEQUAL;
            case GREATER -> getGreater(reference);
            case LESSER -> getLesser(reference);
            case GEQUAL -> getGequal(reference);
            case LEQUAL -> getLequal(reference);
            default -> throw new IllegalArgumentException("not a comparator: " + type);
        };
    }

    private Opcode getGreater(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_GREATER;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_GREATER;
        throw new IllegalStateException("could not create 'greater' for: " + reference);
    }

    private Opcode getLesser(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_LESSER;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_LESSER;
        throw new IllegalStateException("could not create 'lesser' for: " + reference);
    }

    private Opcode getGequal(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_GEQUAL;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_GEQUAL;
        throw new IllegalStateException("could not create 'gequal' for: " + reference);
    }

    private Opcode getLequal(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_LEQUAL;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_LEQUAL;
        throw new IllegalStateException("could not create 'lequal' for: " + reference);
    }

    //endregion

    @Override
    public Void visitWhenExpr(Expr.When expr) {
        cache(expr.condition());
        this.builder.jumpElse(() -> cache(expr.ifTrue()), () -> cache(expr.ifFalse()));
        return null;
    }

    @Override
    public Void visitInstCallExpr(Expr.InstCall expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.callee());
         //object is NOT POPED from the stack. keep it before the args
        this.builder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        retainExprResult = hadRetain;
        builder.invokeVirtual(expr.id());
        if (expr.retType().is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitStaticCallExpr(Expr.StaticCall expr) {
        builder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        builder.invokeStatic(expr.id());
        if (expr.retType().is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitSuperCallExpr(Expr.SuperCall expr) {
        getVar(0);
        builder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        builder.invokeStatic(expr.id());
        if (expr.retType().is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitComparisonChainExpr(Expr.ComparisonChain expr) {
        //l && r -> l ? r : false
        //i < j && j < k
        //1. i, j -> bool

        List<Integer> jumps = new ArrayList<>();
        cache(expr.entries()[0]);
        for (int i = 0; i < expr.entries().length - 2; i++) {
            cache(expr.entries()[i + 1]);
            builder.addCode(Opcode.DUP_X1);
            builder.changeLineIfNecessary(expr.types()[i]);
            builder.addCode(getComparator(expr.types()[i].type(), expr.dataType()));
            jumps.add(builder.addJumpIfFalse());
        }
        cache(expr.entries()[expr.entries().length - 1]);
        Token token = expr.types()[expr.types().length - 1];
        builder.changeLineIfNecessary(token);
        builder.addCode(getComparator(token.type(), expr.dataType()));

        int jump = builder.addJump();
        jumps.forEach(builder::patchJumpCurrent);
        builder.addCode(Opcode.POP); //necessary to remove the unused DUPed parameter
        builder.addCode(Opcode.FALSE);
        builder.patchJumpCurrent(jump);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.object());
        if (hadRetain) {
            builder.changeLineIfNecessary(expr.name());
            if (expr.type().get().isArray()) { //only `.length` exists on arrays, so we can be sure
                builder.addCode(Opcode.ARRAY_LENGTH);
            } else {
                builder.addCode(Opcode.GET_FIELD);
                builder.injectString(expr.name().lexeme());
            }
        } else {
            builder.addCode(Opcode.POP);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitStaticGetExpr(Expr.StaticGet expr) {
        if (retainExprResult) {
            builder.changeLineIfNecessary(expr.name());
            builder.addCode(Opcode.GET_STATIC);
            builder.injectString(VarTypeManager.getClassName(expr.target().get()));
            builder.injectString(expr.name().lexeme());
        }
        return null;
    }

    @Override
    public Void visitArrayGetExpr(Expr.ArrayGet expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.index());
        cache(expr.object());
        if (hadRetain) {
            builder.addCode(getArrayLoad(expr.type()));
        } else {
            builder.addCode(Opcode.POP_2);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {

        ClassReference retType = expr.executor();
        TokenType type = expr.assignType().type();
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.object());
        if (type != TokenType.ASSIGN) {
            builder.addCode(Opcode.DUP); //duplicate object down so the get field
            builder.changeLineIfNecessary(expr.name());
            builder.addCode(Opcode.GET_FIELD);
            builder.injectString(expr.name().lexeme());
            cache(expr.value());
            builder.changeLineIfNecessary(expr.assignType());
            switch (type) {
                case ADD_ASSIGN -> builder.addCode(getAdd(retType));
                case SUB_ASSIGN -> builder.addCode(getSub(retType));
                case MUL_ASSIGN -> builder.addCode(getMul(retType));
                case DIV_ASSIGN -> builder.addCode(getDiv(retType));
                case POW_ASSIGN -> builder.addCode(getPow(retType));
            }
        } else {
            cache(expr.value());
            builder.changeLineIfNecessary(expr.assignType());
        }
        if (hadRetain) {
            builder.addCode(Opcode.DUP_X1); //duplicate to keep value on the stack
        } else {
            ignoredExprResult = true;
        }
        retainExprResult = hadRetain;
        builder.changeLineIfNecessary(expr.name());
        builder.addCode(Opcode.PUT_FIELD);
        builder.injectString(expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitStaticSetExpr(Expr.StaticSet expr) {
        String className = VarTypeManager.getClassName(expr.target().get());
        String fieldName = expr.name().lexeme();
        assign(expr.executor(), expr.value(), expr.assignType(), Opcode.GET_STATIC, Opcode.PUT_STATIC, b -> {
            b.injectString(className);
            b.injectString(fieldName);
        });

        return null;
    }

    @Override
    public Void visitArraySetExpr(Expr.ArraySet expr) {
        //order: arr, index, val -> val
        ClassReference retType = expr.executor();
        TokenType type = expr.assignType().type();
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        if (type != TokenType.ASSIGN) {
            cache(expr.value());
            cache(expr.object());
            cache(expr.index());
            builder.addCode(Opcode.DUP2_X1);
            builder.addCode(getArrayLoad(retType));
            builder.changeLineIfNecessary(expr.assignType());
            switch (type) {
                case ADD_ASSIGN -> builder.addCode(getAdd(retType));
                case SUB_ASSIGN -> builder.addCode(getSub(retType));
                case MUL_ASSIGN -> builder.addCode(getMul(retType));
                case DIV_ASSIGN -> builder.addCode(getDiv(retType));
                case POW_ASSIGN -> builder.addCode(getPow(retType));
            }
        } else {
            cache(expr.object());
            cache(expr.index());
            cache(expr.value());
        }
        if (hadRetain)
            builder.addCode(Opcode.DUP); //duplicate to keep the value on the stack as the ARRAY_SET does not actually keep anything on the stack
        else
            ignoredExprResult = true;
        retainExprResult = hadRetain;
        builder.addCode(getArrayStore(retType));
        return null;
    }

    @Override
    public Void visitSpecialSetExpr(Expr.SpecialSet expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.callee());
        retainExprResult = hadRetain;
        specialAssign(expr.retType(), expr.assignType(), Opcode.GET_FIELD, Opcode.PUT_FIELD, b -> b.injectString(expr.name().lexeme()));
        return null;
    }

    @Override
    public Void visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        String id = VarTypeManager.getClassName(expr.target().get());

        ClassReference reference = expr.executor();
        builder.addCode(expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference)
        );

        specialAssign(expr.executor(), expr.assignType(), Opcode.GET_STATIC, Opcode.PUT_STATIC, b -> b.injectString(id));
        return null;
    }

    @Override
    public Void visitArraySpecialExpr(Expr.ArraySpecial expr) {
        ClassReference reference = expr.executor();
        builder.changeLineIfNecessary(expr.assignType());
        builder.addCode(expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference)
        );
        builder.addCode(getAdd(reference));
        cache(expr.index());
        cache(expr.object());
        builder.addCode(Opcode.DUP2_X1);
        return null;
    }

    @Override
    public Void visitRegistryAccessExpr(Expr.RegistryAccess expr) {
        builder.changeLineIfNecessary(expr.origin());
        builder.addCode(Opcode.REGISTRY);
        builder.addStringConstant(expr.regKey());
        builder.addStringConstant(expr.valKey());
        return null;
    }

    private void specialAssign(ClassReference reference, Token token, Opcode get, Opcode set, Consumer<Chunk.Builder> meta) {
        builder.addCode(get);
        meta.accept(builder);
        builder.changeLineIfNecessary(token);
        builder.addCode(token.type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference)
        );
        builder.addCode(getAdd(reference));
        if (retainExprResult)
            builder.addCode(Opcode.DUP); //duplicate value to emit it onto the object stack
        else
            ignoredExprResult = true;
        builder.addCode(set);
        meta.accept(builder);
    }

    //region special assign
    private Opcode getMinusOne(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_M1;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_M1;
        throw new IllegalStateException();
    }

    private Opcode getPlusOne(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_1;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_1;
        throw new IllegalStateException("");
    }
    //endregion

    @Override
    public Void visitSliceExpr(Expr.Slice expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.object());
        cacheOrNull(expr.start());
        cacheOrNull(expr.end());
        cacheOrNull(expr.interval());
        builder.addCode(Opcode.SLICE);
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitSwitchExpr(Expr.Switch expr) {
        cache(expr.provider());
        builder.addCode(Opcode.SWITCH);
        int defaultPatch = builder.currentCodeIndex();
        builder.addArg(0);
        builder.addArg(0);
        builder.add2bArg(expr.params().size()); //length of pairs

        //compile entries to add sorted
        List<Integer> keys = new ArrayList<>(expr.params().keySet());
        keys.sort(Integer::compareTo);
        record SwitchEntry(int key, int opcode, Expr entry) {}

        List<SwitchEntry> entries = new ArrayList<>();
        for (Integer key : keys) {
            Expr expr1 = expr.params().get(key);
            builder.add4bArg(key);
            entries.add(new SwitchEntry(key, builder.currentCodeIndex(), expr1));
            builder.addArg(0);
            builder.addArg(0);
        }
        List<Integer> continueJumps = new ArrayList<>();

        //cache entries
        for (SwitchEntry entry : entries) {
            builder.patchJumpCurrent(entry.opcode);
            cache(entry.entry);
            continueJumps.add(builder.addJump()); //TODO remove jump if default does not exist
        }
        builder.patchJumpCurrent(defaultPatch);
        if (expr.defaulted() != null) {
            cache(expr.defaulted());
        }

        continueJumps.forEach(builder::patchJumpCurrent);
        //https://docs.oracle.com/javase/specs/jvms/se25/html/jvms-6.html#jvms-6.5.lookupswitch
        return null;
    }

    @Override
    public Void visitCastCheckExpr(Expr.CastCheck expr) {
        //TODO
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        cache(expr.expression());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        if (!retainExprResult) {
            ignoredExprResult = true;
            return null;
        }
        builder.changeLineIfNecessary(expr.literal());
        LiteralHolder literal = expr.literal().literal();
        ScriptedClass scriptedClass = literal.type();
        Object value = literal.value();
        if (scriptedClass == VarTypeManager.DOUBLE) {
            double v = (double) value;
            if (v == 1d)
                builder.addCode(Opcode.D_1);
            else if (v == -1d) {
                builder.addCode(Opcode.D_M1);
            } else
                builder.addDoubleConstant(v);
        } else if (scriptedClass == VarTypeManager.INTEGER) {
            int v = (int) value;
            builder.addInt(v);
        } else if (VarTypeManager.STRING.is(scriptedClass))
            builder.addStringConstant((String) value);
        else if (VarTypeManager.FLOAT.is(scriptedClass)) {
            float v = (float) value;
            if (v == 1f)
                builder.addCode(Opcode.F_1);
            else if (v == -1f)
                builder.addCode(Opcode.F_M1);
            else
                builder.addFloatConstant(v);
        }
        return null;
    }

    @Override
    public Void visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        if (expr.size() != null) {
            cache(expr.size());
        } else {
            builder.addIntConstant(expr.obj().length);
        }
        builder.changeLineIfNecessary(expr.keyword());
        builder.addCode(getArrayNew(expr.compoundType()));
        //builder.injectString(VarTypeManager.getClassName(expr.compoundType().get()));
        Expr[] objects = expr.obj();
        Opcode store = getArrayStore(expr.compoundType());
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                builder.addCode(Opcode.DUP);
                builder.addInt(i);
                cache(objects[i]);
                builder.addCode(store);
            }
        }
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        //l || r -> l ? true : r
        //l && r -> l ? r : false
        //l ^ r  -> l ? r : !r
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.left());
        int jumpPatch = builder.addJumpIfFalse();
        switch (expr.operator().type()) {
            case XOR -> {
                cache(expr.right());
                builder.addCode(Opcode.NOT);
                int jumpRPatch = builder.addJump();
                builder.patchJumpCurrent(jumpPatch);
                cache(expr.right());
                builder.patchJumpCurrent(jumpRPatch);
            }
            case OR -> {
                builder.addCode(Opcode.TRUE);
                int jumpRPatch = builder.addJump();
                builder.patchJumpCurrent(jumpPatch);
                cache(expr.right());
                builder.patchJumpCurrent(jumpRPatch);
            }
            case AND -> {
                cache(expr.right());
                int jumpRPatch = builder.addJump();
                builder.patchJumpCurrent(jumpPatch);
                builder.addCode(Opcode.FALSE);
                builder.patchJumpCurrent(jumpRPatch);
            }
        }
        if (!hadRetain) {
            builder.addCode(Opcode.POP);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.right());
        if (hadRetain) {
            builder.changeLineIfNecessary(expr.operator());
            if (expr.operator().type() == TokenType.NOT) builder.addCode(Opcode.NOT);
            else builder.addCode(getNeg(expr.executor()));
        }
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitVarRefExpr(Expr.VarRef expr) {
        if (retainExprResult) {
            builder.changeLineIfNecessary(expr.name());
            getVar(expr.ordinal());
        } else
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitConstructorExpr(Expr.Constructor expr) {
        builder.changeLineIfNecessary(expr.keyword());
        builder.addCode(Opcode.NEW);
        ScriptedClass target = expr.target().get();
        builder.injectString(VarTypeManager.getClassName(target));

        if (expr.signature() != null) {
            if (retainExprResult) {
                builder.addCode(Opcode.DUP);
            } else {
                ignoredExprResult = true;
            }
            saveArgs(expr.args());
            builder.invokeVirtual(expr.signature());
        }

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements()) {
            cache(statement);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.expression());
        if (!ignoredExprResult)
            builder.addCode(Opcode.POP);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        retainExprResult = true;
        builder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.condition());
        int jumpPatch = builder.addJumpIfFalse();
        retainExprResult = false;
        cache(stmt.thenBranch());
        if (stmt.elifs().length > 0 || stmt.elseBranch() != null) {
            List<Integer> branches = new ArrayList<>();
            branches.add(builder.addJump()); //jump from branch past the IF_STMT
            for (int i = 0; i < stmt.elifs().length; i++) {
                builder.patchJumpCurrent(jumpPatch);
                ElifBranch branch = stmt.elifs()[i];
                cache(branch.condition());
                jumpPatch = builder.addJumpIfFalse();
                retainExprResult = false;
                cache(branch.body());
                if (!branch.ended())
                    branches.add(builder.addJump());
            }
            if (stmt.elseBranch() != null) {
                builder.patchJumpCurrent(jumpPatch);
                retainExprResult = false;
                cache(stmt.elseBranch());
            }
            for (int branch : branches) {
                builder.patchJumpCurrent(branch);
            }
        } else builder.patchJumpCurrent(jumpPatch);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value() != null) {
            retainExprResult = true;
            builder.changeLineIfNecessary(stmt.keyword());
            cache(stmt.value());
            builder.addCode(Opcode.RETURN_ARG);
        } else
            builder.addCode(Opcode.RETURN);
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        retainExprResult = true;
        cache(stmt.value());
        builder.changeLineIfNecessary(stmt.keyword());
        builder.addCode(Opcode.THROW);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        retainExprResult = true;
        builder.changeLineIfNecessary(stmt.name());
        cacheOrNull(stmt.initializer()); //adding a value to the stack without removing it automatically adds it as a local variable
        builder.addLocal(builder.currentCodeIndex(), stmt.localId(), stmt.type(), stmt.name().lexeme());
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        int index = builder.currentCodeIndex();
        retainExprResult = true;
        builder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.condition());
        int skip = builder.addJumpIfFalse();
        loops.add(new Loop());
        retainExprResult = false;
        cache(stmt.body());
        int returnIndex = builder.addJump();
        loops.pop().patchBoth(index);
        builder.patchJumpCurrent(skip);
        builder.patchJump(returnIndex, (short) index);
        return null;
    }

    //clears locals off the stack when they move out of scope
    @Override
    public Void visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        int amount = stmt.amount();
        while (amount >= 2) {
            builder.addCode(Opcode.POP_2);
            amount -= 2;
        }
        if (amount > 0)
            builder.addCode(Opcode.POP);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        builder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.init()); //synthesise initializer
        int result = builder.currentCodeIndex();
        retainExprResult = true;
        ignoredExprResult = false;
        cache(stmt.condition()); //synthesise loop-condition
        int jump1 = builder.addJumpIfFalse();
        loops.add(new Loop()); //push loop for continue & break entries
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.body()); //synthesise loop body
        retainExprResult = false;
        ignoredExprResult = false;
        int increment = builder.currentCodeIndex();
        cache(stmt.increment()); //synthesise increment
        if (!ignoredExprResult)
            builder.addCode(Opcode.POP); //pop the result of the increment
        int returnIndex = builder.addJump();
        loops.pop().patchBoth(increment);
        builder.patchJumpCurrent(jump1);
        builder.patchJump(returnIndex, (short) result);

        int amount = stmt.popVarCount();
        while (amount >= 2) {
            builder.addCode(Opcode.POP_2);
            amount -= 2;
        }
        if (amount > 0)
            builder.addCode(Opcode.POP);

        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        builder.addLocal(builder.currentCodeIndex(), stmt.baseVar() + 1, stmt.type(), stmt.name().lexeme());
        retainExprResult = true;
        builder.changeLineIfNecessary(stmt.name());
        cache(stmt.initializer()); //create array variable
        builder.addCode(Opcode.I_0); //create iteration variable
        int baseVarIndex = stmt.baseVar();

        int curIndex = builder.currentCodeIndex(); //link to jump back when loop is completed

        //region condition
        getVar(baseVarIndex + 1); //get iteration var
        getVar(baseVarIndex); //get array var
        builder.addCode(Opcode.ARRAY_LENGTH); //get length of array
        builder.addCode(Opcode.I_LESSER); //check if iteration var is less than the length of the array
        int result = builder.addJumpIfFalse(); //create jump out of the loop if check fails
        //endregion
        loops.add(new Loop()); //push loop

        //region load iteration object
        getVar(baseVarIndex + 1); //load iteration var
        getVar(baseVarIndex); //load array var
        builder.addCode(getArrayLoad(stmt.type()));  //create entry var by loading array element
        //endregion

        retainExprResult = false;
        cache(stmt.body()); //cache loop body

        //region increase iteration var
        int increase = builder.currentCodeIndex();
        getVar(baseVarIndex + 1); //get iteration var
        builder.addCode(Opcode.I_1); //load 1
        builder.addCode(Opcode.I_ADD); //add 1 to the iteration var
        assignVar(baseVarIndex + 1);
        //endregion
        int returnIndex = builder.addJump();
        loops.pop().patchBoth(increase);
        builder.patchJumpCurrent(result);
        builder.patchJump(returnIndex, (short) curIndex);
        return null;
    }

    @Override
    public Void visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        builder.addTraceDebug(stmt.locals());
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        builder.changeLineIfNecessary(stmt.type());
        Loop loop = loops.peek();
        switch (stmt.type().type()) {
            case BREAK -> loop.addBreak(builder.addJump());
            case CONTINUE -> loop.addContinue(builder.addJump());
        }
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        int handlerStart = builder.currentCodeIndex();
        retainExprResult = false;
        cache(stmt.body());
        int handlerEnd = builder.currentCodeIndex();
        List<Integer> jumps = new ArrayList<>();
        jumps.add(builder.addJump());
        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches()) {
            for (ClassReference reference : aCatch.getFirst().getFirst()) {
                builder.addExceptionHandler(handlerStart, handlerEnd, builder.currentCodeIndex(), builder.injectStringNoArg(VarTypeManager.getClassName(reference.get())));
            }
            retainExprResult = false;
            cache(aCatch.getSecond());
            jumps.add(builder.addJump());
        }
        if (stmt.finale() != null) {
            builder.addExceptionHandler(handlerStart, handlerEnd, builder.currentCodeIndex(), 0);
            retainExprResult = false;
            cache(stmt.finale());
        }
        jumps.forEach(builder::patchJumpCurrent);

        //TODO add https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.10
        //also read this: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.3
        return null;
    }

    private void assignVar(int i) {
        switch (i) { //save the iteration var
            case 0 -> builder.addCode(Opcode.ASSIGN_0);
            case 1 -> builder.addCode(Opcode.ASSIGN_1);
            case 2 -> builder.addCode(Opcode.ASSIGN_2);
            default -> {
                builder.addCode(Opcode.ASSIGN);
                builder.addArg(i);
            }
        }
    }

    private void getVar(int i) {
        switch (i) {
            case 0 -> builder.addCode(Opcode.GET_0);
            case 1 -> builder.addCode(Opcode.GET_1);
            case 2 -> builder.addCode(Opcode.GET_2);
            default -> {
                builder.addCode(Opcode.GET);
                builder.addArg(i);
            }
        }

    }

    public Chunk.Builder setup() {
        this.builder.clear();
        return this.builder;
    }

    //order: idx, arr -> val
    private Opcode getArrayLoad(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.IA_LOAD;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.DA_LOAD;
        if (reference.is(VarTypeManager.CHAR)) return Opcode.CA_LOAD;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.FA_LOAD;
        return Opcode.RA_LOAD;
    }

    private Opcode getArrayStore(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.IA_STORE;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.DA_STORE;
        if (reference.is(VarTypeManager.CHAR)) return Opcode.CA_STORE;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.FA_STORE;
        return Opcode.RA_STORE;
    }

    private Opcode getArrayNew(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.IA_NEW;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.DA_NEW;
        if (reference.is(VarTypeManager.CHAR)) return Opcode.CA_NEW;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.FA_NEW;
        return Opcode.RA_NEW;
    }

    private Opcode getDiv(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_DIV;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_DIV;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.F_DIV;
        throw new IllegalStateException("could not create 'div' for: " + reference);
    }

    private Opcode getMul(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_MUL;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_MUL;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.F_MUL;
        throw new IllegalStateException("could not create 'mul' for: " + reference);
    }

    private Opcode getSub(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_SUB;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_SUB;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.F_SUB;
        throw new IllegalStateException("could not create 'sub' for: " + reference);
    }

    private Opcode getAdd(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_ADD;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_ADD;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.F_ADD;
        if (reference.is(VarTypeManager.STRING.get())) return Opcode.CONCENTRATION;
        throw new IllegalStateException("could not create 'add' for: " + reference);
    }

    private Opcode getPow(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_POW;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_POW;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.F_POW;
        throw new IllegalStateException("could not create 'pow' for: " + reference);
    }

    private Opcode getMod(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_MOD;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_MOD;
        throw new IllegalStateException("could not create 'pow' for: " + reference);
    }


    private Opcode getNeg(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_NEGATION;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_NEGATION;
        if (reference.is(VarTypeManager.FLOAT)) return Opcode.F_NEGATION;
        throw new IllegalStateException("could not create 'negation' for: " + reference);
    }

    private void assign(ClassReference retType, Expr value, Token type, Opcode get, Opcode assign, Consumer<Chunk.Builder> meta) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(value);
        builder.changeLineIfNecessary(type);
        if (type.type() != TokenType.ASSIGN) {
            builder.addCode(get);
            meta.accept(builder);
            switch (type.type()) {
                case ADD_ASSIGN -> builder.addCode(getAdd(retType));
                case SUB_ASSIGN -> builder.addCode(getSub(retType));
                case MUL_ASSIGN -> builder.addCode(getMul(retType));
                case DIV_ASSIGN -> builder.addCode(getDiv(retType));
                case POW_ASSIGN -> builder.addCode(getPow(retType));
            }
        }
        if (hadRetain) {
            builder.addCode(Opcode.DUP);
        } else
            ignoredExprResult = true;
        retainExprResult = hadRetain;
        builder.addCode(assign);
        meta.accept(builder);
    }

    private final class Loop {
        private final List<Integer> breakIndices;
        private final List<Integer> continueIndices;

        private Loop() {
            this.breakIndices = new ArrayList<>();
            this.continueIndices = new ArrayList<>();
        }

        public void addContinue(int patchIndex) {
            this.continueIndices.add(patchIndex);
        }

        public void patchContinues(short idx) {
            this.continueIndices.forEach(i -> builder.patchJump(i, idx));
        }

        public void addBreak(int patchIndex) {
            this.breakIndices.add(patchIndex);
        }

        public void patchBreaks() {
            this.breakIndices.forEach(builder::patchJumpCurrent);
        }

        public void patchBoth(int continueIndex) {
            this.patchBreaks();
            this.patchContinues((short) continueIndex);
        }
    }
}
