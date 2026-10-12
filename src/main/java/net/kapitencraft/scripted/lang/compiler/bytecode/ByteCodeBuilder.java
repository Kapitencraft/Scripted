package net.kapitencraft.scripted.lang.compiler.bytecode;

import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.*;
import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.constant.IntegerConstantInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.ArrayList;
import java.util.List;

public class ByteCodeBuilder {
    private final List<Instruction> instructions = new ArrayList<>();

    public void changeLineIfNecessary(Token obj) {
        add(new ChangeLineInstruction(obj.line()));
    }

    public void addSimple(Opcode opcode) {
        add(switch (opcode) {
            case POP -> CodeInstruction.POP;
            case POP_2 -> CodeInstruction.POP_2;
            case INVOKE_STATIC, INVOKE_VIRTUAL, JUMP, JUMP_IF_FALSE,
                 D_CONST, F_CONST, I_CONST, S_CONST, SWITCH, TRACE -> throw new IllegalStateException("missing required arguments for " + opcode + " instruction");
            default -> new CodeInstruction(opcode);
        });
    }

    public void addStringInstruction(Opcode opcode, String val) {
        add(new StringArgInstruction(opcode, val));
    }

    public int addJump() {
        int i = instructions.size();
        add(new JumpInstruction(Opcode.JUMP));
        return i;
    }

    public void addJump(int target) {
        JumpInstruction i = new JumpInstruction(Opcode.JUMP);
        i.setTarget(target);
        instructions.add(i);
    }

    public int addJumpIfFalse() {
        int i = instructions.size();
        add(new JumpInstruction(Opcode.JUMP_IF_FALSE));
        return i;
    }

    public void patchJump(int jumpIdx) {
        patchJump(jumpIdx, instructions.size());
    }

    public void patchJump(int jumpIdx, int jumpOrigin) {
        ((JumpableInstruction) this.instructions.get(jumpIdx)).setTarget(jumpOrigin);
    }

    public void jumpElse(Runnable ifTrue, Runnable ifFalse) {
        int truePatch = addJumpIfFalse();
        ifTrue.run();
        int falsePatch = addJump();
        patchJump(truePatch);
        ifFalse.run();
        patchJump(falsePatch);
    }

    public void addJumpMultiTargetInstruction(List<Integer> origins) {
        if (origins.isEmpty()) return; //ignore empty
        int target = this.instructions.size();
        for (Integer origin : origins) {
            ((JumpableInstruction) this.instructions.get(origin)).setTarget(target);
        }
    }

    public int size() {
        return instructions.size();
    }

    public void addSwitch(int size, List<SwitchInstruction.Entry> entries) {
        add(new SwitchInstruction(size, entries));
    }

    public void add(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public void addIntConstant(int v) {
        add(switch (v) {
            case -1 -> new CodeInstruction(Opcode.I_M1);
            case 0 -> new CodeInstruction(Opcode.I_0);
            case 1 -> new CodeInstruction(Opcode.I_1);
            case 2 -> new CodeInstruction(Opcode.I_2);
            case 3 -> new CodeInstruction(Opcode.I_3);
            case 4 -> new CodeInstruction(Opcode.I_4);
            case 5 -> new CodeInstruction(Opcode.I_5);
            default -> new IntegerConstantInstruction(v);
        });
    }

    public void addLocalAccess(Opcode opcode, int i) {
        add(new LocalInstruction(opcode, i));
    }

    public void addStaticFieldAccess(Opcode opcode, String className, String fieldName) {
        add(new StaticFieldAccessInstruction(opcode, className, fieldName));
    }

    public void build(Chunk.Builder builder) {
        if (Compiler.optimize) {
            //int size = this.size();
            this.optimize();
            //if (this.size() < size) {
            //    System.out.println("Optimization removed " + (size - this.size()) + " (" + (size - this.size()) * 100 / size + "%) instructions");
            //}
        }
        IpContainer container = gatherStartIndexes();
        this.instructions.forEach(instruction -> instruction.save(builder, container));
    }

    public void registerLocal(int i, ClassReference type, String lexeme) {
        add(new RegisterLocalInstruction(i, type, lexeme));
    }

    public void addExceptionHandler(int handlerStart, int handlerEnd, String className) {
        add(new ExceptionHandlerInstruction(handlerStart, handlerEnd, this.size(), className));
    }

    public IpContainer gatherStartIndexes() {
        int[] startIndexes = new int[this.instructions.size()];
        int size = 0;
        for (int i = 0; i < this.instructions.size(); i++) {

            int length = this.instructions.get(i).length();
            if (length == -1) {
                startIndexes[i] = -1;
            } else {
                startIndexes[i] = size;
                size += length;
            }
        }
        return new IpContainer(startIndexes);
    }

    public void reset() {
        this.instructions.clear();
    }

    public void optimize() {
        BytecodeOptimizer.INSTANCE.optimize(this.instructions);
    }

    public static class IpContainer {
        private final int[] ips;

        private IpContainer(int[] ips) {
            this.ips = ips;
        }

        public int getIp(int startIdx) {
            while (ips[startIdx] == -1) { //jump over non-code instructions
                startIdx++;
            }
            return ips[startIdx];
        }
    }
}
