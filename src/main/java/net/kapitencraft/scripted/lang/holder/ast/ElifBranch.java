package net.kapitencraft.scripted.lang.holder.ast;

public record ElifBranch(Expr condition, Stmt body, boolean seenReturn) {
}
