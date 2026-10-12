package net.kapitencraft.scripted.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.scripted.lang.compiler.bytecode.instruction.*;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.scripted.lang.compiler.bytecode.optimize.Optimization;
import net.kapitencraft.scripted.lang.exe.Opcode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * removes instructions that can never be reached in code
 */
public class RemoveUnreachableOpcodesOptimization implements Optimization {

    @Override
    public void optimize(BytecodeOptimizer.OptimizationStorage instructions) {
        List<ExceptionHandlerInstruction> handlers = new ArrayList<>();
        for (Instruction instruction : instructions) {
            if (instruction instanceof ExceptionHandlerInstruction handlerInstruction) {
                handlers.add(handlerInstruction);
            }
        }

        ArrayDeque<Integer> ipQueue = new ArrayDeque<>();
        ipQueue.push(0);
        boolean[] flags = new boolean[instructions.size()]; //keep a list of all reachable instructions (value = true)

        while (!ipQueue.isEmpty()) {
            int i = ipQueue.pop();
            while (i < instructions.size()) {
                if (flags[i])
                    break; //check has already happened for these instructions. no need to check again
                flags[i] = true; //mark instruction as reachable
                for (Iterator<ExceptionHandlerInstruction> iterator = handlers.iterator(); iterator.hasNext(); ) {
                    ExceptionHandlerInstruction handler = iterator.next();
                    if (handler.getHandlerStart() <= i && handler.getHandlerEnd() > i) {
                        ipQueue.add(handler.getHandlerIP());
                        iterator.remove(); //handler no longer needs to be analyzed
                    }
                }

                if (instructions.getInstruction(i) instanceof CodeInstruction cI) {
                    if (cI.code() == Opcode.RETURN || cI.code() == Opcode.RETURN_ARG) {
                        break;
                    }
                    if (cI instanceof JumpInstruction jI) {
                        ipQueue.add(jI.getTarget());
                        if (cI.code() == Opcode.JUMP) //jump instructions have no way of continuing
                            break;
                    }
                    if (cI instanceof SwitchInstruction switchInstruction) {
                        ipQueue.add(switchInstruction.getTarget());
                        switchInstruction.getEntries().stream().mapToInt(SwitchInstruction.Entry::idx).forEach(ipQueue::add);
                        break;
                    }
                }
                i++;
            }
        }
        for (int i = flags.length - 1; i >= 0; i--) {
            if (!flags[i]) {
                instructions.removeInstruction(i); //remove from the back to the front to keep the order aligned
            }
        }
    }
}
