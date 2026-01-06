package net.kapitencraft.scripted.edit.graphical.inserter.expr;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.WhileLoopWidget;

public class WhileConditionInserter implements ExprGhostInserter {
    private final WhileLoopWidget owner;

    public WhileConditionInserter(WhileLoopWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(CodeWidget target) {
        this.owner.setCondition(target);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WhileConditionInserter iCI && this.owner == iCI.owner;
    }
}
