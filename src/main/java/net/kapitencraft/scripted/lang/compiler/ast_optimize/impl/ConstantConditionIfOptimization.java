package net.kapitencraft.scripted.lang.compiler.ast_optimize.impl;

import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConstantConditionIfOptimization extends SingleNodeOptimization<Stmt.If> {
    public ConstantConditionIfOptimization() {
        super(Stmt.If.class);
    }

    @Override
    @Nullable Stmt optimize(Stmt.If value) {
        if (value.condition instanceof Expr.Literal literal) {
            if (((boolean) literal.literal.literal().value())) {
                return value.thenBranch;
            }
            if (value.elifs.length > 0) {
                List<ElifBranch> remaining = new ArrayList<>();
                for (int i = 0; i < value.elifs.length; i++) {
                    ElifBranch branch = value.elifs[i];
                    if (branch.condition instanceof Expr.Literal branchLiteral) {
                        if (!((boolean) branchLiteral.literal.literal().value())) {
                            continue;
                        }
                        if (remaining.isEmpty())
                            return branch.body;
                    }
                    remaining.add(branch);
                }
                if (!remaining.isEmpty()) {
                    Stmt.If newIf = new Stmt.If();
                    ElifBranch first = remaining.getFirst();
                    newIf.condition = first.condition;
                    newIf.thenBranch = first.body;
                    newIf.branchSeenReturn = first.ended;
                    remaining.removeFirst();
                    newIf.elifs = remaining.toArray(new ElifBranch[0]);
                    newIf.elseBranch = value.elseBranch;
                    newIf.elseBranchSeenReturn = value.elseBranchSeenReturn;
                    newIf.keyword = value.keyword;
                    return newIf;
                }
            }
            return value.elseBranch;
        }
        return value;
    }
}
