package net.kapitencraft.scripted.lang.bytecode.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.bytecode.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

public record Chunk(byte[] code, byte[] constants, ExceptionHandler[] handlers, LineNumberTable lineNumberTable, LocalVariableTable localVariableTable) {

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("code", encode(this.code));
        object.addProperty("constants", encode(this.constants));
        object.add("handlers", saveHandlers());
        object.add("line_numbers", this.lineNumberTable.save());
        object.add("locals", this.localVariableTable.save());
        return object;
    }

    private JsonElement saveHandlers() {
        JsonArray array = new JsonArray();
        for (ExceptionHandler handler : this.handlers) {
            array.add(handler.toJson());
        }
        return array;
    }

    private static String encode(byte[] in) {
        char[] chars = new char[in.length];
        for (int i = 0; i < in.length; i++) {
            chars[i] = (char) in[i];
        }
        return new String(chars);
    }

    private static byte[] decode(String in) {
        char[] chars = in.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) (chars[i] & 255);
        }
        return bytes;
    }

    public static Chunk load(JsonObject object) {
        byte[] code = decode(GsonHelper.getAsString(object, "code"));
        byte[] constants = decode(GsonHelper.getAsString(object, "constants"));
        JsonArray array = object.getAsJsonArray("handlers");
        ExceptionHandler[] handlers = array.asList().stream().map(JsonElement::getAsJsonObject).map(ExceptionHandler::fromJson).toArray(ExceptionHandler[]::new);
        LineNumberTable lineNumbers = LineNumberTable.read(object.getAsJsonArray("line_numbers"));
        LocalVariableTable localVariables = LocalVariableTable.read(object.getAsJsonArray("locals"));
        return new Chunk(code, constants, handlers, lineNumbers, localVariables);
    }

    /**
     * a builder for the chunk, used inside {@link net.kapitencraft.scripted.lang.compiler.CacheBuilder CacheBuilder} to create json format of the chunk
     */
    public static class Builder {
        private final List<ExceptionHandler> handlers;
        private final LineNumberTable.Builder lineNumbers;
        private final LocalVariableTable.Builder locals;
        private final ArrayList<Byte> code, constants;

        public Builder() {
            this.code = new ArrayList<>();
            this.constants = new ArrayList<>();
            this.handlers = new ArrayList<>();
            this.lineNumbers = new LineNumberTable.Builder();
            this.locals = new LocalVariableTable.Builder();
        }

        public void jumpElse(Runnable ifTrue, Runnable ifFalse) {
            int truePatch = addJumpIfFalse();
            ifTrue.run();
            int falsePatch = addJump();
            patchJump(truePatch, (short) currentCodeIndex());
            ifFalse.run();
            patchJump(falsePatch, (short) currentCodeIndex());
        }

        public void addLocal(int position, int index, ClassReference type, String  name) {
            this.locals.addLocal(position, index, type, name);
        }

        public void patchJump(int index, short destination) {
            this.code.set(index, (byte) ((destination >> 8) & 255));
            this.code.set(index + 1, (byte) (destination & 255));
        }

        public void patchJumpCurrent(int index) {
            int current = this.code.size();
            patchJump(index, (short) current);
        }

        public void jump(Runnable toSkip) {
            int reference = addJump();
            toSkip.run();
            patchJumpCurrent(reference);
        }

        public void addIntConstant(int constant) {
            this.addCode(Opcode.I_CONST);
            this.addConstantArg();
            for (int i = 0; i < 4; i++) {
                this.constants.add((byte) ((constant >> (8 * i)) & 255));
            }
        }

        public void addDoubleConstant(double constant) {
            this.addCode(Opcode.D_CONST);
            this.addConstantArg();
            long l = Double.doubleToLongBits(constant);
            for (int i = 0; i < 8; i++) {
                this.constants.add((byte) ((l >> (8 * i)) & 255));
            }
        }

        public void addFloatConstant(float v) {
            this.addCode(Opcode.F_CONST);
            this.addConstantArg();
            int i = Float.floatToIntBits(v);
            for (int j = 0; j < 4; j++) {
                this.constants.add((byte) ((i >> (8 * j)) & 255));
            }
        }

        public void addStringConstant(String constant) {
            this.addCode(Opcode.S_CONST);
            injectString(constant);
        }

        /**
         * links the current constant position into the code to be used by whatever
         */
        public void addConstantArg() {
            this.add2bArg(this.constants.size());
        }

        public void injectString(String constant) {
            this.addConstantArg();
            this.constants.add((byte) constant.length());
            for (byte b : constant.getBytes()) {
                this.constants.add(b);
            }
        }

        public int injectStringNoArg(String constant) {
            int loc = this.constants.size();
            this.constants.add((byte) constant.length());
            for (byte b : constant.getBytes()) {
                this.constants.add(b);
            }
            return loc;
        }

        public Chunk build() {
            byte[] code = new byte[this.code.size()];
            for (int i = 0; i < code.length; i++) {
                code[i] = this.code.get(i);
            }
            byte[] constants = new byte[this.constants.size()];
            for (int i = 0; i < constants.length; i++) {
                constants[i] = this.constants.get(i);
            }
            return new Chunk(code, constants, this.handlers.toArray(new ExceptionHandler[0]), this.lineNumbers.build(), this.locals.build(this.currentCodeIndex()));
        }

        public void addArg(byte b) {
            this.code.add(b);
        }

        public void addArg(int i) {
            this.addArg((byte) (i & 255));
        }

        public void add2bArg(int arg) {
            this.addArg((arg >> 8));
            this.addArg(arg);
        }

        public void add4bArg(int arg) {
            this.addArg((arg >> 24));
            this.addArg((arg >> 16));
            this.addArg((arg >> 8));
            this.addArg(arg);
        }

        public void addCode(Opcode opcode) {
            this.addArg(opcode.ordinal());
        }

        public int currentCodeIndex() {
            return this.code.size();
        }

        public int addJumpIfFalse() {
            this.addCode(Opcode.JUMP_IF_FALSE);
            int index = currentCodeIndex();
            this.addArg(0);
            this.addArg(0);
            return index;
        }

        public int addJump() {
            this.addCode(Opcode.JUMP);
            int index = currentCodeIndex();
            this.addArg(0);
            this.addArg(0);
            return index;
        }

        public void clear() {
            this.code.clear();
            this.constants.clear();
            this.handlers.clear();
            this.lineNumbers.clear();
            this.locals.clear();
        }

        public void addExceptionHandler(int startOp, int endOp, int handlerOp, int catchType) {
            this.handlers.add(new ExceptionHandler(startOp, endOp, handlerOp, catchType));
        }

        public void invokeStatic(String methodSignature) {
            this.addCode(Opcode.INVOKE_STATIC);
            this.injectString(methodSignature);
        }

        public void invokeVirtual(String methodSignature) {
            this.addCode(Opcode.INVOKE_VIRTUAL);
            this.injectString(methodSignature);
        }

        public void addInt(int v) {
            switch (v) {
                case -1 -> addCode(Opcode.I_M1);
                case 0 -> addCode(Opcode.I_0);
                case 1 -> addCode(Opcode.I_1);
                case 2 -> addCode(Opcode.I_2);
                case 3 -> addCode(Opcode.I_3);
                case 4 -> addCode(Opcode.I_4);
                case 5 -> addCode(Opcode.I_5);
                default -> addIntConstant(v);
            }
        }

        public void changeLineIfNecessary(Token type) {
            this.lineNumbers.changeIfNecessary(type.line(), this.currentCodeIndex());
        }

        public void addTraceDebug(@UnknownNullability List<Pair<Byte, String>> ints) {
            this.addCode(Opcode.TRACE);
            this.addConstantArg();
            this.constants.add((byte) ints.size());
            for (Pair<Byte, String> pair : ints) {
                this.constants.add(pair.getFirst());
            }
        }
    }

    /**
     * @param startOp the start ip
     * @param endOp the end ip
     * @param handlerOp the code executed when this handler matches the thrown exception
     * @param catchType the type of error to be caught
     */
    public record ExceptionHandler(int startOp, int endOp, int handlerOp, int catchType) {

        public JsonElement toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("startOp", startOp);
            object.addProperty("endOp", endOp);
            object.addProperty("handlerOp", handlerOp);
            object.addProperty("catchType", catchType);
            return object;
        }

        public static ExceptionHandler fromJson(JsonObject object) {
            return new ExceptionHandler(
                    GsonHelper.getAsInt(object, "startOp"),
                    GsonHelper.getAsInt(object, "endOp"),
                    GsonHelper.getAsInt(object, "handlerOp"),
                    GsonHelper.getAsInt(object, "catchType")
            );
        }
    }
}
