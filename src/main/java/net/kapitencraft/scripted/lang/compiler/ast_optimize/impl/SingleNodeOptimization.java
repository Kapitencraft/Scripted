package net.kapitencraft.scripted.lang.compiler.ast_optimize.impl;

import net.kapitencraft.scripted.lang.compiler.ast_optimize.ASTOptimization;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SingleNodeOptimization<T extends Stmt> implements ASTOptimization {
    private final Class<T> type;

    protected SingleNodeOptimization(Class<T> type) {
        this.type = type;
    }

    @Override
    public void optimize(List<Stmt> code) {
        for (int i = 0; i < code.size(); i++) {
            Stmt stmt = code.get(i);
            if (type.isInstance(stmt)) {
                Stmt ret = optimize((T) stmt);
                if (ret != null) {
                    code.set(i, ret);
                    continue;
                }
            }
            optimizeRecursive(stmt);
        }
    }

    abstract @Nullable Stmt optimize(T value);
}