package net.kapitencraft.scripted.lang.compiler.bytecode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.IncrementIntVarInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.StaticFieldAccessInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.SwitchInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.TraceDebugInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.constant.DoubleConstantInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.constant.FloatConstantInstruction;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.natives.NativeClassInstance;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.ast.SwitchKey;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CacheBuilder implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    public static final int majorVersion = 1, minorVersion = 0;

    //marks whether to keep the expr result on the stack or not
    private boolean retainExprResult = false;
    //marks whether the expr result has already been ignored and therefore no POP must be emitted
    private boolean ignoredExprResult = false;
    private final ByteCodeBuilder byteCodeBuilder;
    private final ArrayDeque<Loop> loops = new ArrayDeque<>();

    public CacheBuilder() {
        this.byteCodeBuilder = new ByteCodeBuilder();
    }

    public void cache(Expr expr) {
        expr.accept(this);
    }

    private void cacheRetained(Expr value) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(value);
        retainExprResult = hadRetain;
    }

    private void cacheOrNull(@Nullable Expr expr) {
        if (expr == null) {
            byteCodeBuilder.addSimple(Opcode.NULL);
        } else cache(expr);
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
            if ((retention = VarTypeManager.directParseType(instance.getType()).get().getAnnotation(VarTypeManager.RETENTION)) != null) {
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
    public Void visitIdentifierAssignExpr(Expr.IdentifierAssign expr) {
        if (expr.fieldOwner == null) {
            byte ordinal = expr.ordinal;
            AssignOperators result = getAssignOperators(ordinal);
            assign(expr.executor, expr.value, expr.type, result.get(), result.assign(), opcode -> {
                if ((ordinal & 255) > 2)
                    byteCodeBuilder.addLocalAccess(opcode, ordinal);
                else
                    byteCodeBuilder.addSimple(opcode);
            });
        } else {
            if (expr.isStatic) {
                assign(expr.executor, expr.value, expr.type, Opcode.GET_STATIC, Opcode.PUT_STATIC, opcode -> {
                    byteCodeBuilder.addStringInstruction(opcode, expr.name.lexeme());
                });
            }
        }
        return null;
    }

    public void build(Chunk.Builder chunk) {
        this.byteCodeBuilder.build(chunk);
    }

    public void reset() {
        this.byteCodeBuilder.reset();
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

    @Override
    public Void visitIdentifierSpecialAssignExpr(Expr.IdentifierSpecialAssign expr) {
        if (expr.type == null) {
            int ordinal = expr.ordinal;
            AssignOperators operators = getAssignOperators(ordinal);
            if (expr.executor.is(VarTypeManager.INTEGER) && !retainExprResult) {
                byteCodeBuilder.add(new IncrementIntVarInstruction(ordinal, expr.assignType.type() == TokenType.GROW ? 1 : -1));
                ignoredExprResult = true;
                return null;
            }
            specialAssign(expr.executor, expr.assignType, operators.get(), operators.assign(), o -> {
                if (ordinal > 2)
                    byteCodeBuilder.addLocalAccess(o, ordinal);
                else
                    byteCodeBuilder.addSimple(o);
            });
        } else {
            if (expr.isStatic) {
                specialAssign(expr.retType, expr.assignType, Opcode.GET_STATIC, Opcode.PUT_STATIC,
                        o -> byteCodeBuilder.addStringInstruction(o, expr.name.lexeme())
                );
            } else {
                byteCodeBuilder.addSimple(Opcode.GET_0);
                specialAssign(expr.retType, expr.assignType, Opcode.GET_FIELD, Opcode.PUT_FIELD,
                        o -> byteCodeBuilder.addStringInstruction(o, expr.name.lexeme())
                );
            }
        }
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        TokenType type = expr.operator.type();
        //if (expr.left() instanceof Expr.Literal(Token leftLiteral) && expr.right() instanceof Expr.Literal(Token rightLiteral)) {
        //    ClassReference reference = expr.returnType();
        //    Object leftValue = leftLiteral.literal().value();
        //    Object rightValue = rightLiteral.literal().value();
        //    switch (type) {
        //        case EQUAL, NEQUAL -> {
        //            if (leftValue.equals(rightValue) && type == TokenType.EQUAL) {
        //                byteCodeBuilder.addSimple(Opcode.TRUE);
        //            } else
        //                byteCodeBuilder.addSimple(Opcode.FALSE);
        //        }
        //        case LEQUAL -> {
        //        }
        //    }
        //}
        if (expr.retType().is(VarTypeManager.INTEGER)) {
            if (type == TokenType.MUL && expr.right instanceof Expr.Literal litExpr) {
                int val = ((int) litExpr.literal.literal().value());
                int exponent = 31 - Integer.numberOfLeadingZeros(val);
                if (exponent == 32 - Integer.numberOfLeadingZeros(val - 1)) { //number is power of 2
                    cache(expr.left);
                    byteCodeBuilder.addIntConstant(exponent);
                    byteCodeBuilder.addSimple(Opcode.I_SH_L);
                    return null;
                }
            } else if (type == TokenType.POW && expr.left instanceof Expr.Literal litExpr) {
                int val = ((int) litExpr.literal.literal().value());
                int exponent = 31 - Integer.numberOfLeadingZeros(val);
                if (exponent == 32 - Integer.numberOfLeadingZeros(val - 1)) { //number is power of 2
                    byteCodeBuilder.addSimple(Opcode.I_1);
                    if (exponent > 1) {
                        byteCodeBuilder.addIntConstant(exponent / 2);
                        cache(expr.right);
                        byteCodeBuilder.addSimple(Opcode.I_MUL);
                    } else
                        cache(expr.right);
                    byteCodeBuilder.addSimple(Opcode.I_SH_L);
                    return null;
                }
            }
        }

        //normal binary behavior
        cache(expr.left);
        Token operator = expr.operator;
        if (operator.type() == TokenType.ADD && expr.executor.is(VarTypeManager.STRING) && !expr.left.retType().is(VarTypeManager.STRING)) {
            byteCodeBuilder.addStringInstruction(Opcode.INVOKE_STATIC, "Lscripted/lang/String;valueOf(Lscripted/lang/Object)");
        }

        cache(expr.right);


        if (expr.signature != null) {
            byteCodeBuilder.addStringInstruction(Opcode.INVOKE_VIRTUAL, expr.signature);
        } else {
            if (hadRetain) { //if the result of a binary expression is ignored, we don't need to do its calculation as it is pure without side effects
                final ClassReference executor = expr.executor;
                byteCodeBuilder.changeLineIfNecessary(operator);
                Opcode opcode = switch (operator.type()) {
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
                byteCodeBuilder.addSimple(opcode);
            } else {
                byteCodeBuilder.addSimple(Opcode.POP_2);
                ignoredExprResult = true;
            }
        }
        retainExprResult = hadRetain;
        return null;
    }

    private void convertToStringIfNecessary(TokenType operator, ClassReference executor) {
        if (operator == TokenType.ADD && executor.is(VarTypeManager.STRING)) {
            byteCodeBuilder.addStringInstruction(Opcode.INVOKE_STATIC, "Lscripted/lang/String;valueOf(Lscripted/lang/Object)");
        }
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
        cache(expr.condition);
        this.byteCodeBuilder.jumpElse(() -> cache(expr.ifTrue), () -> cache(expr.ifFalse));
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        if (expr.object != null) {
            //object is NOT POPED from the stack. keep it before the args
            byteCodeBuilder.changeLineIfNecessary(expr.name);
            cacheRetained(expr.object);
        }

        saveArgs(expr.args);
        if (expr.virtual) {
            byteCodeBuilder.addStringInstruction(Opcode.INVOKE_VIRTUAL, expr.signature);
        } else {
            byteCodeBuilder.addStringInstruction(Opcode.INVOKE_STATIC, expr.signature);
        }
        if (expr.retType.is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitComparisonChainExpr(Expr.ComparisonChain expr) {

        List<Integer> jumps = new ArrayList<>();
        cache(expr.entries[0]);
        for (int i = 0; i < expr.entries.length - 2; i++) {
            cache(expr.entries[i + 1]);
            byteCodeBuilder.addSimple(Opcode.DUP_X1);
            byteCodeBuilder.changeLineIfNecessary(expr.types[i]);
            byteCodeBuilder.addSimple(getComparator(expr.types[i].type(), expr.dataType));
            jumps.add(byteCodeBuilder.addJumpIfFalse());
        }
        cache(expr.entries[expr.entries.length - 1]);
        Token token = expr.types[expr.types.length - 1];
        byteCodeBuilder.changeLineIfNecessary(token);
        byteCodeBuilder.addSimple(getComparator(token.type(), expr.dataType));

        int jump = byteCodeBuilder.addJump();
        jumps.forEach(byteCodeBuilder::patchJump);
        byteCodeBuilder.addSimple(Opcode.POP); //necessary to remove the unused DUPed parameter
        byteCodeBuilder.addSimple(Opcode.FALSE);
        byteCodeBuilder.patchJump(jump);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        cacheRetained(expr.object);
        if (retainExprResult) {
            byteCodeBuilder.changeLineIfNecessary(expr.name);
            if (expr.type.get().isArray()) { //only `.length` exists on arrays, so we can be sure
                byteCodeBuilder.addSimple(Opcode.ARRAY_LENGTH);
            } else {
                byteCodeBuilder.addStringInstruction(Opcode.GET_FIELD, expr.name.lexeme());
            }
        } else {
            byteCodeBuilder.addSimple(Opcode.POP);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitStaticGetExpr(Expr.StaticGet expr) {
        if (retainExprResult) {
            byteCodeBuilder.changeLineIfNecessary(expr.name);
            String className = VarTypeManager.getClassName(expr.target.get());
            String fieldName = expr.name.lexeme();
            byteCodeBuilder.addStaticFieldAccess(Opcode.GET_STATIC, className, fieldName);
        }
        return null;
    }

    @Override
    public Void visitArrayGetExpr(Expr.ArrayGet expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.index);
        cache(expr.object);
        if (hadRetain) {
            Opcode opcode = getArrayLoad(expr.componentType);
            byteCodeBuilder.addSimple(opcode);
        } else {
            byteCodeBuilder.addSimple(Opcode.POP_2);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {

        ClassReference retType = expr.executor;
        TokenType type = expr.assignType.type();
        cacheRetained(expr.object);
        if (type != TokenType.ASSIGN) {
            byteCodeBuilder.addSimple(Opcode.DUP);
            byteCodeBuilder.changeLineIfNecessary(expr.name);
            byteCodeBuilder.addStringInstruction(Opcode.GET_FIELD, expr.name.lexeme());
            cacheRetained(expr.value);
            byteCodeBuilder.changeLineIfNecessary(expr.assignType);
            Opcode opcode = switch (type) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                case MOD_ASSIGN -> getMod(retType);
                default -> throw new IllegalArgumentException("not a assign type: " + type);
            };
            byteCodeBuilder.addSimple(opcode);
        } else {
            cacheRetained(expr.value);
            byteCodeBuilder.changeLineIfNecessary(expr.assignType);
        }
        if (retainExprResult) {
            byteCodeBuilder.addSimple(Opcode.DUP_X1); //duplicate to keep value on the stack
        } else {
            ignoredExprResult = true;
        }
        byteCodeBuilder.changeLineIfNecessary(expr.name);
        byteCodeBuilder.addStringInstruction(Opcode.PUT_FIELD, expr.name.lexeme());
        return null;
    }

    @Override
    public Void visitStaticSetExpr(Expr.StaticSet expr) {
        String className = VarTypeManager.getClassName(expr.target.get());
        String fieldName = expr.name.lexeme();
        assign(expr.executor, expr.value, expr.assignType, Opcode.GET_STATIC, Opcode.PUT_STATIC, opcode -> byteCodeBuilder.addStaticFieldAccess(opcode, className, fieldName));

        return null;
    }

    @Override
    public Void visitArraySetExpr(Expr.ArraySet expr) {
        //order: arr, index, val -> val
        ClassReference retType = expr.componentType;
        TokenType type = expr.assignType.type();
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        if (type != TokenType.ASSIGN) {
            cache(expr.value);
            cache(expr.object);
            cache(expr.index);
            byteCodeBuilder.addSimple(Opcode.DUP2_X1);
            Opcode load = getArrayLoad(retType);
            byteCodeBuilder.addSimple(load);
            byteCodeBuilder.changeLineIfNecessary(expr.assignType);
            Opcode opcode = switch (type) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                case MOD_ASSIGN -> getMod(retType);
                default -> throw new IllegalStateException("unknown assign type: " + type);
            };
            byteCodeBuilder.addSimple(opcode);
        } else {
            cache(expr.object);
            cache(expr.index);
            cache(expr.value);
        }
        if (hadRetain) {
            byteCodeBuilder.addSimple(Opcode.DUP); //duplicate to keep the value on the stack as the ARRAY_SET does not actually keep anything on the stack
        } else
            ignoredExprResult = true;
        retainExprResult = hadRetain;
        Opcode store = getArrayStore(retType);
        byteCodeBuilder.addSimple(store);
        return null;
    }

    @Override
    public Void visitSpecialSetExpr(Expr.SpecialSet expr) {
        cacheRetained(expr.object);
        specialAssign(expr.retType, expr.assignType, Opcode.GET_FIELD, Opcode.PUT_FIELD,
                o -> byteCodeBuilder.addStringInstruction(o, expr.name.lexeme())
        );
        return null;
    }

    @Override
    public Void visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        String id = VarTypeManager.getClassName(expr.target.get());

        ClassReference reference = expr.executor;
        byteCodeBuilder.addSimple(expr.assignType.type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference));

        specialAssign(expr.executor, expr.assignType, Opcode.GET_STATIC, Opcode.PUT_STATIC,
                o -> byteCodeBuilder.addStringInstruction(o, id)
        );
        return null;
    }

    @Override
    public Void visitArraySpecialExpr(Expr.ArraySpecial expr) {
        ClassReference reference = expr.executor;
        byteCodeBuilder.changeLineIfNecessary(expr.assignType);
        Opcode opcode = expr.assignType.type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference);
        byteCodeBuilder.addSimple(opcode);
        Opcode add = getAdd(reference);
        byteCodeBuilder.addSimple(add);
        cache(expr.index);
        cache(expr.object);
        byteCodeBuilder.addSimple(Opcode.DUP2_X1);
        return null;
    }

    private void specialAssign(ClassReference reference, Token token, Opcode get, Opcode set, Consumer<Opcode> instructionSink) {
        instructionSink.accept(get);
        byteCodeBuilder.changeLineIfNecessary(token);
        Opcode o = token.type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference);
        byteCodeBuilder.addSimple(o);
        Opcode add = getAdd(reference);
        byteCodeBuilder.addSimple(add);
        if (retainExprResult) {
            byteCodeBuilder.addSimple(Opcode.DUP); //duplicate value to emit it onto the object stack
        } else
            ignoredExprResult = true;
        instructionSink.accept(set);
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
        cache(expr.object);
        cacheOrNull(expr.start);
        cacheOrNull(expr.end);
        cacheOrNull(expr.interval);
        byteCodeBuilder.addSimple(Opcode.SLICE);
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitSwitchExpr(Expr.Switch expr) {
        cacheRetained(expr.provider);
        if (expr.isEnum) {
            byteCodeBuilder.addStringInstruction(Opcode.INVOKE_VIRTUAL, "Lscripted/lang/Enum;ordinal()");
        }
        int instDefaultPatch = byteCodeBuilder.size();

        //compile entries to add sorted
        List<Integer> keys = Arrays.stream(expr.params).map(key -> key.index).sorted(Integer::compareTo).toList();
        record SwitchEntry(int key, Expr entry) {
        }

        List<SwitchEntry> entries = new ArrayList<>();
        List<SwitchInstruction.Entry> instEntries = new ArrayList<>();
        for (Integer key : keys) {
            Expr expr1 = null;
            for (SwitchKey param : expr.params) {
                if (param.index == key) {
                    expr1 = param.expr;
                    break;
                }
            }
            entries.add(new SwitchEntry(key, expr1));
            instEntries.add(new SwitchInstruction.Entry(key));
        }
        byteCodeBuilder.addSwitch(expr.params.length, instEntries);
        List<Integer> continueJumpInstructions = new ArrayList<>();

        //cache entries
        for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
            SwitchEntry entry = entries.get(i);
            instEntries.get(i).setIdx(byteCodeBuilder.size());
            cache(entry.entry);
            if (expr.defaulted != null || i < entriesSize - 1) {
                continueJumpInstructions.add(byteCodeBuilder.addJump());
            }
        }
        byteCodeBuilder.patchJump(instDefaultPatch);
        if (expr.defaulted != null) {
            cache(expr.defaulted);
        }

        byteCodeBuilder.addJumpMultiTargetInstruction(continueJumpInstructions);
        //https://docs.oracle.com/javase/specs/jvms/se25/html/jvms-6.html#jvms-6.5.lookupswitch
        return null;
    }

    @Override
    public Void visitCastCheckExpr(Expr.CastCheck expr) {
        cacheRetained(expr.object);
        if (expr.patternVarName != null)
            byteCodeBuilder.addSimple(Opcode.DUP);
        byteCodeBuilder.addStringInstruction(Opcode.INSTANCEOF, VarTypeManager.getClassName(expr.targetType));
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        if (!retainExprResult) {
            ignoredExprResult = true;
            return null;
        }
        Token literalToken = expr.literal;
        byteCodeBuilder.changeLineIfNecessary(literalToken);
        LiteralHolder literal = literalToken.literal();
        ScriptedClass scriptedClass = literal.type();
        Object value = literal.value();
        if (scriptedClass == VarTypeManager.DOUBLE) {
            double v = (double) value;
            if (v == 1d) {
                byteCodeBuilder.addSimple(Opcode.D_1);
            } else if (v == -1d) {
                byteCodeBuilder.addSimple(Opcode.D_M1);
            } else {
                byteCodeBuilder.add(new DoubleConstantInstruction(v));
            }
        } else if (scriptedClass == VarTypeManager.INTEGER) {
            int v = (int) value;
            byteCodeBuilder.addIntConstant(v);
        } else if (VarTypeManager.STRING.is(scriptedClass)) {
            byteCodeBuilder.addStringInstruction(Opcode.S_CONST, ((String) value));
        } else if (VarTypeManager.FLOAT.is(scriptedClass)) {
            float v = (float) value;
            if (v == 1f) {
                byteCodeBuilder.addSimple(Opcode.F_1);
            } else if (v == -1f) {
                byteCodeBuilder.addSimple(Opcode.F_M1);
            } else {
                byteCodeBuilder.add(new FloatConstantInstruction(v));
            }
        } else if (VarTypeManager.BOOLEAN.is(scriptedClass)) {
            boolean b = (boolean) value;
            if (b) {
                byteCodeBuilder.addSimple(Opcode.TRUE);
            } else {
                byteCodeBuilder.addSimple(Opcode.FALSE);
            }
        }
        return null;
    }

    @Override
    public Void visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        if (expr.size != null) {
            cache(expr.size);
        } else {
            byteCodeBuilder.addIntConstant(expr.obj.length);
        }
        byteCodeBuilder.changeLineIfNecessary(expr.keyword);
        Opcode opcode = getArrayNew(expr.compoundType);
        byteCodeBuilder.addSimple(opcode);
        //builder.injectString(VarTypeManager.getClassName(expr.compoundType().get()));
        Expr[] objects = expr.obj;
        Opcode store = getArrayStore(expr.compoundType);
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                byteCodeBuilder.addSimple(Opcode.DUP);
                byteCodeBuilder.addIntConstant(i);
                cache(objects[i]);
                byteCodeBuilder.addSimple(store);
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
        cache(expr.left);
        int jumpPatch = byteCodeBuilder.addJumpIfFalse();
        switch (expr.operator.type()) {
            case XOR -> {
                cache(expr.right);
                byteCodeBuilder.addSimple(Opcode.NOT);
                int jumpRPatch = byteCodeBuilder.addJump();
                byteCodeBuilder.patchJump(jumpPatch);
                cache(expr.right);
                byteCodeBuilder.patchJump(jumpRPatch);
            }
            case OR -> {
                byteCodeBuilder.addSimple(Opcode.TRUE);
                int jumpRPatch = byteCodeBuilder.addJump();
                byteCodeBuilder.patchJump(jumpPatch);
                cache(expr.right);
                byteCodeBuilder.patchJump(jumpRPatch);
            }
            case AND -> {
                cache(expr.right);
                int jumpRPatch = byteCodeBuilder.addJump();
                byteCodeBuilder.patchJump(jumpPatch);
                byteCodeBuilder.addSimple(Opcode.FALSE);
                byteCodeBuilder.patchJump(jumpRPatch);
            }
        }
        if (!hadRetain) {
            byteCodeBuilder.addSimple(Opcode.POP);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.right);
        if (hadRetain) {
            Token operator = expr.operator;
            byteCodeBuilder.changeLineIfNecessary(operator);
            if (operator.type() == TokenType.NOT) {
                byteCodeBuilder.addSimple(Opcode.NOT);
            } else {
                Opcode neg = getNeg(expr.executor);
                byteCodeBuilder.addSimple(neg);
            }
        }
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitSingleIdentifierExpr(Expr.SingleIdentifier expr) {
        if (retainExprResult) {
            byteCodeBuilder.changeLineIfNecessary(expr.name);
            if (expr.type != null) {
                if (expr.isStatic) {
                    byteCodeBuilder.add(new StaticFieldAccessInstruction(Opcode.GET_STATIC, VarTypeManager.getClassName(expr.type), expr.name.lexeme()));
                } else {
                    byteCodeBuilder.addSimple(Opcode.GET_0);
                    byteCodeBuilder.addStringInstruction(Opcode.GET_FIELD, expr.name.lexeme());
                }
            } else
                getVar(expr.ordinal);
        } else
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitConstructorExpr(Expr.Constructor expr) {
        byteCodeBuilder.changeLineIfNecessary(expr.keyword);
        ScriptedClass target = expr.target.get();
        byteCodeBuilder.addStringInstruction(Opcode.NEW, VarTypeManager.getClassName(target));

        if (expr.signature != null) {
            if (retainExprResult) {
                byteCodeBuilder.addSimple(Opcode.DUP);
            } else {
                ignoredExprResult = true;
            }
            saveArgs(expr.args);
            byteCodeBuilder.addStringInstruction(Opcode.INVOKE_VIRTUAL, expr.signature);
        }

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements) {
            cache(statement);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.expression);
        if (!ignoredExprResult) {
            byteCodeBuilder.addSimple(Opcode.POP);
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        retainExprResult = true;
        byteCodeBuilder.changeLineIfNecessary(stmt.keyword);
        cache(stmt.condition);
        int instPatch = byteCodeBuilder.addJumpIfFalse();
        retainExprResult = false;
        cache(stmt.thenBranch);
        if (stmt.elifs.length > 0 || stmt.elseBranch != null) {
            List<Integer> instBranches = new ArrayList<>();
            if (!stmt.branchSeenReturn)
                instBranches.add(byteCodeBuilder.addJump()); //jump over else-ifs & else
            for (int i = 0; i < stmt.elifs.length; i++) {
                byteCodeBuilder.patchJump(instPatch);
                ElifBranch branch = stmt.elifs[i];
                cache(branch.condition);
                instPatch = byteCodeBuilder.addJumpIfFalse();
                retainExprResult = false;
                cache(branch.body);
                if (!branch.ended) {
                    instBranches.add(byteCodeBuilder.addJump());
                }
            }
            if (stmt.elseBranch != null) {
                byteCodeBuilder.patchJump(instPatch);
                retainExprResult = false;
                cache(stmt.elseBranch);
            }
            byteCodeBuilder.addJumpMultiTargetInstruction(instBranches);
        } else {
            byteCodeBuilder.patchJump(instPatch);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value != null) {
            retainExprResult = true;
            byteCodeBuilder.changeLineIfNecessary(stmt.keyword);
            cache(stmt.value);
            byteCodeBuilder.addSimple(Opcode.RETURN_ARG);
        } else {
            byteCodeBuilder.addSimple(Opcode.RETURN);
        }
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        retainExprResult = true;
        cache(stmt.value);
        byteCodeBuilder.changeLineIfNecessary(stmt.keyword);
        byteCodeBuilder.addSimple(Opcode.THROW);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        retainExprResult = true;
        byteCodeBuilder.changeLineIfNecessary(stmt.name);
        cacheOrNull(stmt.initializer); //adding a value to the stack without removing it automatically adds it as a local variable
        byteCodeBuilder.registerLocal(stmt.localId, stmt.type, stmt.name.lexeme());
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        int index = byteCodeBuilder.size();
        retainExprResult = true;
        byteCodeBuilder.changeLineIfNecessary(stmt.keyword);
        cache(stmt.condition);
        int skip = byteCodeBuilder.addJumpIfFalse();
        loops.push(new Loop());
        retainExprResult = false;
        cache(stmt.body);
        byteCodeBuilder.addJump(index);
        loops.pop().patchBoth(index);
        byteCodeBuilder.patchJump(skip);
        return null;
    }

    //clears locals off the stack when they move out of scope
    @Override
    public Void visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        int amount = stmt.amount;
        while (amount >= 2) {
            byteCodeBuilder.addSimple(Opcode.POP_2);
            amount -= 2;
        }
        if (amount > 0)
            byteCodeBuilder.addSimple(Opcode.POP);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        byteCodeBuilder.changeLineIfNecessary(stmt.keyword);
        cache(stmt.init); //synthesize initializer
        int result = byteCodeBuilder.size();
        retainExprResult = true;
        ignoredExprResult = false;
        cache(stmt.condition); //synthesise loop-condition
        int jump1 = byteCodeBuilder.addJumpIfFalse();
        loops.push(new Loop()); //push loop for continue & break entries
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.body); //synthesize loop body
        retainExprResult = false;
        ignoredExprResult = false;
        int increment = byteCodeBuilder.size();
        cache(stmt.increment); //synthesize increment
        if (!ignoredExprResult)
            byteCodeBuilder.addSimple(Opcode.POP); //pop the result of the increment
        byteCodeBuilder.addJump(result);
        loops.pop().patchBoth(increment);

        byteCodeBuilder.patchJump(jump1);

        int amount = stmt.popVarCount;
        while (amount >= 2) {
            byteCodeBuilder.addSimple(Opcode.POP_2);
            amount -= 2;
        }
        if (amount > 0)
            byteCodeBuilder.addSimple(Opcode.POP);
        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        retainExprResult = true;
        byteCodeBuilder.changeLineIfNecessary(stmt.name);
        cache(stmt.initializer); //create array variable
        byteCodeBuilder.registerLocal(stmt.baseVar, stmt.type.array(), "$array");
        byteCodeBuilder.addSimple(Opcode.I_0);//create iteration variable
        byteCodeBuilder.registerLocal(stmt.baseVar + 1, VarTypeManager.INTEGER.reference(), "$i");
        int baseVarIndex = stmt.baseVar;

        int curIndex = byteCodeBuilder.size(); //link to jump back when loop is completed

        //region condition
        getVar(baseVarIndex + 1); //get iteration var
        getVar(baseVarIndex); //get array var
        byteCodeBuilder.addSimple(Opcode.ARRAY_LENGTH); //get length of array
        byteCodeBuilder.addSimple(Opcode.I_LESSER); //check if iteration var is less than the length of the array
        int result = byteCodeBuilder.addJumpIfFalse(); //create jump out of the loop if check fails
        //endregion
        loops.push(new Loop());

        //region load iteration object
        getVar(baseVarIndex + 1); //load iteration var
        getVar(baseVarIndex); //load array var
        byteCodeBuilder.addSimple(getArrayLoad(stmt.type));  //create entry var by loading array element
        byteCodeBuilder.registerLocal(stmt.baseVar + 2, stmt.type, stmt.name.lexeme());
        //endregion

        retainExprResult = false;
        cache(stmt.body); //cache loop body

        //region increase iteration var
        int increase = byteCodeBuilder.size();
        getVar(baseVarIndex + 1); //get iteration var
        byteCodeBuilder.addSimple(Opcode.I_1); //load 1
        byteCodeBuilder.addSimple(Opcode.I_ADD); //add 1 to the iteration var
        assignVar(baseVarIndex + 1);
        //endregion
        byteCodeBuilder.addJump(curIndex);
        loops.pop().patchBoth(increase);
        byteCodeBuilder.patchJump(result);
        byteCodeBuilder.addSimple(Opcode.POP_2); //remove iteration and array variable
        return null;
    }

    @Override
    public Void visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        byteCodeBuilder.add(new TraceDebugInstruction(stmt.localOrdinals));
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        byteCodeBuilder.changeLineIfNecessary(stmt.type);
        Loop loop = loops.peek();
        switch (stmt.type.type()) {
            case BREAK -> loop.addBreak(byteCodeBuilder.addJump());
            case CONTINUE -> loop.addContinue(byteCodeBuilder.addJump());
        }
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        int handlerStart = byteCodeBuilder.size();
        retainExprResult = false;
        cache(stmt.body);
        int handlerEnd = byteCodeBuilder.size();
        List<Integer> jumps = new ArrayList<>();
        jumps.add(byteCodeBuilder.addJump());
        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches) {
            for (ClassReference reference : aCatch.getFirst().getFirst()) {
                byteCodeBuilder.addExceptionHandler(handlerStart, handlerEnd, VarTypeManager.getClassName(reference.get()));
            }
            retainExprResult = false;
            cache(aCatch.getSecond());
            jumps.add(byteCodeBuilder.addJump());
        }
        if (stmt.finale != null) {
            byteCodeBuilder.addExceptionHandler(handlerStart, handlerEnd, null);
            retainExprResult = false;
            cache(stmt.finale);
        }
        byteCodeBuilder.addJumpMultiTargetInstruction(jumps);

        //https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.10
        //also read this: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.3
        return null;
    }

    private void assignVar(int i) {
        switch (i) { //save the iteration var
            case 0 -> byteCodeBuilder.addSimple(Opcode.ASSIGN_0);
            case 1 -> byteCodeBuilder.addSimple(Opcode.ASSIGN_1);
            case 2 -> byteCodeBuilder.addSimple(Opcode.ASSIGN_2);
            default -> {
                byteCodeBuilder.addLocalAccess(Opcode.ASSIGN, i);
            }
        }
    }

    private void getVar(int i) {
        switch (i) {
            case 0 -> byteCodeBuilder.addSimple(Opcode.GET_0);
            case 1 -> byteCodeBuilder.addSimple(Opcode.GET_1);
            case 2 -> byteCodeBuilder.addSimple(Opcode.GET_2);
            default -> byteCodeBuilder.addLocalAccess(Opcode.GET, i);
        }
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

    private void assign(ClassReference retType, Expr value, Token type, Opcode get, Opcode assign, Consumer<Opcode> instSink) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(value);
        byteCodeBuilder.changeLineIfNecessary(type);
        if (type.type() != TokenType.ASSIGN) {
            instSink.accept(get);
            Opcode operation = switch (type.type()) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                case MOD_ASSIGN -> getMod(retType);
                default -> throw new IllegalStateException("no operation for type: " + type.type());
            };
            byteCodeBuilder.addSimple(operation);
        }
        if (hadRetain) {
            byteCodeBuilder.addSimple(Opcode.DUP);
        } else
            ignoredExprResult = true;
        retainExprResult = hadRetain;
        instSink.accept(assign);
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
            this.continueIndices.forEach(i -> byteCodeBuilder.patchJump(i, idx));
        }

        public void addBreak(int patchIndex) {
            this.breakIndices.add(patchIndex);
        }

        public void patchBreaks() {
            byteCodeBuilder.addJumpMultiTargetInstruction(this.breakIndices);
        }

        public void patchBoth(int continueIndex) {
            this.patchBreaks();
            this.patchContinues((short) continueIndex);
        }
    }
}
