package net.kapitencraft.scripted.edit.graphical.inserter.expr;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.IfWidget;

public class IfElseConditionInserter implements ExprGhostInserter {
    private final IfWidget owner;

    public IfElseConditionInserter(IfWidget owner) {
        this.owner = owner;
    }

    @Override
    public void insert(CodeWidget target) {
        this.owner.setElseCondition(target);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IfElseConditionInserter iCI && this.owner == iCI.owner;
    }
}
