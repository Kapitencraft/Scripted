package net.kapitencraft.scripted.lang.holder.ast;

public final class ElifBranch {
    public final Expr condition;
    public final Stmt body;
    public boolean ended;

    public ElifBranch(Expr condition, Stmt body, boolean ended) {
        this.condition = condition;
        this.body = body;
        this.ended = ended;
    }

    @Override
    public String toString() {
        return "ElifBranch[" +
                "condition=" + condition + ", " +
                "body=" + body + ", " +
                "ended=" + ended + ']';
    }

}
