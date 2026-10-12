package net.kapitencraft.scripted.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.Optimization;
import net.kapitencraft.scripted.lang.exe.Opcode;

import java.util.ArrayList;
import java.util.List;

/**
 * removes pure (not modifying IO / calling other methods) instructions whose results will be removed from the stack due to {@code POP} or {@code POP_2} instructions
 * <br>does the same with instructions ending in a {@code RETURN} (not {@code RETURN_ARG}!)
 */
public class RemoveIgnoredResultInstructionsOptimization implements Optimization {

    //1. remove non-pure Instructions before RETURN
    //2. remove non-pure Instructions before POP and POP_2 and remove these if finding a DUP instruction
    @Override
    public void optimize(BytecodeOptimizer.OptimizationStorage instructions) {
        int i = instructions.size() - 1;
        while (i >= 0) {
            if (instructions.getInstruction(i) instanceof CodeInstruction cI) {
                if (cI.code() == Opcode.RETURN) {
                    i--;
                    while (i >= 0 && instructions.getInstruction(i) instanceof CodeInstruction cI1 && cI1.code().isPure()) {
                        instructions.removeInstruction(i);
                        i--;
                    }
                } else if (cI.code() == Opcode.POP || cI.code() == Opcode.POP_2) {
                    List<Integer> popLocations = new ArrayList<>();
                    popLocations.add(i);
                    i--;
                    while (i >= 0 && !popLocations.isEmpty() && (instructions.getInstruction(i) instanceof CodeInstruction cI1 && cI1.code().isPure())) {
                        switch (cI1.code()) {
                            case POP, POP_2 -> {
                                popLocations.add(i);
                                i--;
                                continue;
                            }
                            case DUP -> {
                                Integer last = popLocations.getLast();
                                CodeInstruction instruction = (CodeInstruction) instructions.getInstruction(last);
                                if (instruction.code() == Opcode.POP_2) {
                                    instructions.replaceInstruction(last, CodeInstruction.POP);
                                } else {
                                    instructions.removeInstruction(i);
                                    popLocations.removeLast();
                                    popLocations.replaceAll(integer -> integer - 1);
                                }
                            }
                        }
                        instructions.removeInstruction(i);
                    }
                }
            }
            i--;
        }
    }
}
