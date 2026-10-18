package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import org.jetbrains.annotations.Nullable;

public abstract class ChainableExprWidget implements ExprCodeWidget {
    protected @Nullable ExprCodeWidget chainChild;

    public ChainableExprWidget setChainChild(ExprCodeWidget child) {
        this.chainChild = child;
        return this;
    }

    public ExprCodeWidget getChainChild() {
        return chainChild;
    }
}
