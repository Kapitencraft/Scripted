package net.kapitencraft.scripted.lang.compiler.ast_optimize.impl;

import net.kapitencraft.scripted.lang.compiler.ast_optimize.ASTOptimization;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

import java.util.List;

public class VarMutationMergeOptimization implements ASTOptimization {
    @Override
    public void optimize(List<Stmt> code) {
        for (int i = 0; i < code.size(); i++) {
            Stmt stmt = code.get(i);
            optimizeRecursive(stmt);


        }
    }

    //i += 5
    //i += 10
    //i += 8
    // ->
    //i += 5 + 10 + 8
}
