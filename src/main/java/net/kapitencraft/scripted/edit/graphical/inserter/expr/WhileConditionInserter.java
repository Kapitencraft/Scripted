package net.kapitencraft.scripted.edit.graphical.inserter.expr;

import net.kapitencraft.scripted.edit.graphical.widgets.block.WhileLoopWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;

public class WhileConditionInserter implements ExprGhostInserter {
    private final WhileLoopWidget owner;

    public WhileConditionInserter(WhileLoopWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(ExprCodeWidget target) {
        this.owner.setCondition(target);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WhileConditionInserter iCI && this.owner == iCI.owner;
    }
}
