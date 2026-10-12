package net.kapitencraft.scripted.lang.compiler.bytecode.optimize;

public interface SimpleOptimization extends Optimization {

    void tryExecute(BytecodeOptimizer.OptimizationStorage optimizationStorage, int index);

    @Override
    default void optimize(BytecodeOptimizer.OptimizationStorage instructions) {
        for (int i = 0; i < instructions.size(); i++) {
            this.tryExecute(instructions, i);
        }
    }
}
