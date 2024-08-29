package net.kapitencraft.scripted.edit.text.language.java;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.builtin.CreateAndSetVarFunction;
import net.kapitencraft.scripted.code.exe.functions.builtin.ForLoopFunction;
import net.kapitencraft.scripted.code.exe.functions.builtin.IfFunction;
import net.kapitencraft.scripted.code.exe.functions.builtin.WhileLoopFunction;
import net.kapitencraft.scripted.code.exe.methods.core.MethodInstance;
import net.kapitencraft.scripted.code.exe.methods.mapper.Setter;
import net.kapitencraft.scripted.code.oop.core.Object;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.code.var.type.primitive.CharType;
import net.kapitencraft.scripted.code.var.type.primitive.DoubleType;
import net.kapitencraft.scripted.code.var.type.primitive.IntegerType;
import net.kapitencraft.scripted.code.var.type.primitive.StringType;
import net.kapitencraft.scripted.edit.Token;
import net.kapitencraft.scripted.edit.text.ErrorList;
import net.kapitencraft.scripted.edit.text.language.Compiler;
import net.kapitencraft.scripted.init.ModFunctions;
import net.kapitencraft.scripted.init.ModMethods;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JavaCompiler extends Compiler {

    private final ErrorList errors;

    public static Object compileObject(String string) {
        JavaTokenizer tokenizer = new JavaTokenizer();
        JavaCompiler compiler = new JavaCompiler(tokenizer.tokenize(string));
        return compiler.castObject();
    }

    public JavaCompiler(List<Token> tokens) {
        super(tokens);
        this.errors = new ErrorList();

    }

    @Override
    public Object castObject() {
        return new InstCompiler().cast();
    }

    private class InstCompiler {
        int pos = 0;
        int line = 1;


        private Object cast() {
            assertType(Token.Type.MODIFIER);
            Object object = new Object(castName());
            assertType(Token.Type.CURLY_BRACKET_OPEN);
            if (nextType() == Token.Type.VAR_TYPE) {
                pos--;
                compileMethod(object);
            } else {
                compileConstructor(object);
            }
            return object;
        }

        private Token current() {
            return tokens.get(pos);
        }

        private Token next() {
            return tokens.get(pos + 1);
        }

        private <T> void compileMethod(Object object) {
            //TODO compile modifiers
            VarType<T> retType = castType();
            String name = castName();
            List<Pair<VarType<?>, String>> params = castParams();
            object.newMethod(name, retType, params, castPipeline(retType, VarAnalyser.of(params), false));
        }

        private void compileConstructor(Object object) {
            nextType(); //skip name
            List<Pair<VarType<?>, String>> params = castParams();
            object.setConstructor(params, castPipeline(VarTypes.VOID.get(), VarAnalyser.of(params), false));
        }

        private List<Pair<VarType<?>, String>> castParams() {
            assertType(Token.Type.BRACKET_OPEN);
            List<Pair<VarType<?>, String>> params = new ArrayList<>();
            while (next().type == Token.Type.SEPARATOR) {
                params.add(castParam());
            }
            assertType(Token.Type.BRACKET_CLOSE);
            return params;
        }

        private <T> MethodPipeline<T> castPipeline(VarType<T> ret, VarAnalyser analyser, boolean isLoop) {
            assertType(Token.Type.CURLY_BRACKET_OPEN);
            List<MethodInstance<?>> methods = new ArrayList<>();
            while (next().type != Token.Type.CURLY_BRACKET_CLOSE) {
                methods.add(castRunnable(analyser, true, ret));
            }
            assertType(Token.Type.CURLY_BRACKET_CLOSE);
            return new MethodPipeline<>(ret, methods, isLoop);
        }

        private <T> MethodInstance<?> castRunnable(VarAnalyser analyser, boolean allowLoops, VarType<T> retType) {
            return switch (next().type) {
                case IF_IDENTIFIER: yield castIf(analyser, retType);
                case FOR_IDENTIFIER: {
                    if (!allowLoops) {
                        errors.add(line, "invalid statement");
                    }
                    nextType();
                    assertType(Token.Type.BRACKET_OPEN);
                    MethodInstance<?> start = castRunnable(analyser, false, retType);
                    MethodInstance<Boolean> condition = (MethodInstance<Boolean>) castMethod(analyser);
                    MethodInstance<?> iteration = castRunnable(analyser, false, retType);
                    assertType(Token.Type.BRACKET_CLOSE);
                    MethodPipeline<?> pipeline = castPipeline(retType, analyser, true);
                    yield ForLoopFunction.create(start, condition, iteration, pipeline);
                }
                case WHILE_IDENTIFIER: {
                    nextType();
                    assertType(Token.Type.BRACKET_OPEN);
                    MethodInstance<Boolean> condition = (MethodInstance<Boolean>) castMethod(analyser);
                    assertType(Token.Type.BRACKET_CLOSE);
                    MethodPipeline<?> pipeline = castPipeline(retType, analyser, true);
                    yield WhileLoopFunction.create(condition, pipeline);
                }
                case MODIFIER:
                case VAR_TYPE: yield castCreateAndSetVarFunction(analyser);
                default: yield castMethod(analyser);
            };
        }

        private <T> IfFunction.Instance<T> castIf(VarAnalyser analyser, VarType<T> retType) {
            nextType();
            Pair<MethodInstance<Boolean>, MethodPipeline<T>> main = castIfPair(analyser, retType);
            List<Pair<MethodInstance<Boolean>, MethodPipeline<T>>> elifs = new ArrayList<>();
            MethodPipeline<T> elseBody = null;
            while (next().type == Token.Type.ELSE_IDENTIFIER) {
                nextType();
                if (next().type == Token.Type.IF_IDENTIFIER) {
                    nextType();
                    elifs.add(castIfPair(analyser, retType));
                    //cast elif
                } else {
                    if (elseBody != null) {
                        throw new IllegalStateException("'else' without if");
                    }
                    elseBody = castPipeline(retType, analyser, false);
                }
            }
            return ModFunctions.IF.get().createInst(main, elifs, elseBody, analyser);
        }

        private <T> Pair<MethodInstance<Boolean>, MethodPipeline<T>> castIfPair(VarAnalyser analyser, VarType<T> retType) {
            assertType(Token.Type.BRACKET_OPEN);
            MethodInstance<Boolean> condition = (MethodInstance<Boolean>) castMethod(analyser);
            assertType(Token.Type.BRACKET_CLOSE);
            MethodPipeline<T> pipeline = castPipeline(retType, analyser, false);
            return Pair.of(condition, pipeline);
        }

        private <T> CreateAndSetVarFunction.Instance<T> castCreateAndSetVarFunction(VarAnalyser analyser) {
            nextType();
            boolean isFinal = false;
            if (current().type == Token.Type.MODIFIER) isFinal = "final".equals(current().value);
            VarType<T> type;
            if (current().type == Token.Type.VAR_TYPE) type = (VarType<T>) VarType.NAME_MAP.get(current().value);
            else type = castType();
            String name = castName();
            assertType(Token.Type.EQUAL);
            MethodInstance<T> inst = (MethodInstance<T>) castMethod(analyser);
            return ModFunctions.CREATE_AND_SET_VAR.get().create(name, inst, type, isFinal);
        }

        private Pair<VarType<?>, String> castParam() {
            VarType<?> type = castType();
            String name = castName();
            return Pair.of(type, name);
        }

        private List<MethodInstance<?>> castParamData(VarAnalyser analyser) {
            assertType(Token.Type.BRACKET_OPEN);
            List<MethodInstance<?>> params = new ArrayList<>();
            while (next().type != Token.Type.BRACKET_CLOSE) {
                params.add(castMethod(analyser));
                if (next().type == Token.Type.NEXT_PARAM) assertType(Token.Type.NEXT_PARAM); //if not, the params are complete
            }
            assertType(Token.Type.BRACKET_CLOSE);
            return params;
        }

        private MethodInstance<?> castMethod(VarAnalyser analyser) {
            return switch (nextType()) {
                case PRIM_CHAR -> CharType.read(current().value.charAt(0));
                case PRIM_NUM -> {
                    if (current().value.contains(".")) yield DoubleType.readInstance(current().value);
                    else yield IntegerType.readInstance(current().value);
                }
                case PRIM_STRING -> StringType.readInstance(current().value);
                case PRIM_REG_ELEMENT -> RegistryType.readInstance(current().value);
                default -> {
                    MethodInstance<?> inst = null;
                    while (next().type != Token.Type.EXPR_END && next().type != Token.Type.NEXT_PARAM) {
                        switch (next().type) {
                            case ASSIGN:
                            case ASSIGN_WITH_OPERATION: inst = castAssign(inst, analyser);
                            case WHEN_CONDITION_SEPARATOR: inst = castWhenMethod(inst, analyser);
                            case OR:
                            case XOR:
                            case AND: { //bool operation
                                nextType();
                                inst = ModMethods.BOOL_OPERATION.get().create(inst, current().value, castMethod(analyser));
                            }
                            case ADD:
                            case DIV:
                            case MOD:
                            case SUB:
                            case MULT: { //math operation
                                nextType();
                                if (inst == null) {
                                    analyser.addError(Component.translatable("error.illegal_expression_start", current().value));
                                    continue;
                                }
                                inst = inst.getType(analyser).createMathOperation(current().value, inst, castMethod(analyser));
                            }
                            default: {
                                String value = castMethodName();
                                if (inst != null) {
                                    VarType<?> varType = inst.getType(analyser);
                                    if (next().type != Token.Type.BRACKET_OPEN) { //if no brackets, it's a field
                                        inst = varType.createFieldReference(value, inst);
                                    } else {
                                        inst = varType.createMethod(value, castParamData(analyser));
                                    }
                                } else {
                                    inst = ModMethods.VAR_REFERENCE.get().create(value);
                                }
                            }
                        }
                    }
                    yield inst;
                }
            };
        }

        private <T> MethodInstance<T> castAssign(@Nullable MethodInstance<T> inst, VarAnalyser analyser) {
            if (inst == null) throw new IllegalStateException("variable expected");
            VarType<T> type = inst.getType(analyser);
            Setter.Type setterType = Setter.Type.readType(next().value);
            return type.createSetVar(inst, setterType, setterType.requiresSetter() ? (MethodInstance<T>) castMethod(analyser) : null);
        }

        private <T> MethodInstance<T> castWhenMethod(MethodInstance<?> inst, VarAnalyser analyser) {
            MethodInstance<Boolean> condition = (MethodInstance<Boolean>) inst;
            nextType();
            MethodInstance<T> ifTrue = (MethodInstance<T>) castMethod(analyser);
            assertType(Token.Type.WHEN_FALSE_SEPARATOR);
            MethodInstance<T> ifFalse = (MethodInstance<T>) castMethod(analyser);
            VarType<T> type = ifTrue.getType(analyser);
            return type.createWhen(condition, ifTrue, ifFalse, analyser);
        }

        private <T> VarType<T> castType() {
            assertType(Token.Type.VAR_TYPE);
            return VarType.read(current().value);
        }

        private String castName() {
            assertType(Token.Type.VAR_NAME);
            return current().value;
        }

        private String castMethodName() {
            assertType(Token.Type.METHOD_NAME);
            return current().value;
        }

        private Token.Type nextType() {
            pos++;
            return current().type;
        }

        private void assertType(Token.Type type) {
            if (nextType() != type) {
                errors.add(line, "expected '" + type + "' but found '" + current());
            }
        }
    }
}