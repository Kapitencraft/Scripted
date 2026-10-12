package net.kapitencraft.scripted.lang.compiler.ast_optimize;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;

import java.util.Arrays;
import java.util.List;

public interface ASTOptimization {

    void optimize(List<Stmt> code);

    default void optimizeRecursive(Stmt stmt) {
        if (stmt instanceof Stmt.If f) {
            optimize(List.of(f.thenBranch));
            Arrays.stream(f.elifs).map(b -> List.of(b.body)).forEach(this::optimize);
            if (f.elseBranch != null)
                optimize(List.of(f.elseBranch));
        } else if (stmt instanceof Stmt.ForEach forEach) {
            optimize(List.of(forEach.body));
        } else if (stmt instanceof Stmt.For f) {
            optimize(List.of(f.body));
        }
    }

    default Expr.Literal createNumber(int value) {
        Expr.Literal literal = new Expr.Literal();
        literal.literal = new Token(TokenType.NUM, String.valueOf(value), new LiteralHolder(value, VarTypeManager.INTEGER), -1, -1);
        return literal;
    }
}
