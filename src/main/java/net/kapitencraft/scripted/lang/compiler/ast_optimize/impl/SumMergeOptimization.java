package net.kapitencraft.scripted.lang.compiler.ast_optimize.impl;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import org.jetbrains.annotations.Nullable;

public class SumMergeOptimization extends SingleNodeOptimization<Stmt.For> {
    public SumMergeOptimization() {
        super(Stmt.For.class);
    }

    //int sum = 0;
    //for (int i = 0; i <= n; i++) {
    //  sum += i;
    //}
    //->
    //sum = n * (n + 1) / 2

    //int sum = 0;
    //for (int i = 0; i <= n; i++) {
    //  sum += i ** 2;
    //}
    //->
    //sum = n * (n + 1) * (2n + 1) / 6

    @Override
    @Nullable Stmt optimize(Stmt.For value) {
        Stmt body = value.body;
        if (body instanceof Stmt.Block block) {
            if (block.statements.size() == 2) //can't ignore the `ClearLocals` call afterwards
                body = block.statements.getFirst();
            else
                return null;
        }
        if (value.init instanceof Stmt.VarDecl varDecl &&
                varDecl.initializer instanceof Expr.Literal literal &&
                literal.retType.is(VarTypeManager.INTEGER) &&
                (
                        ((int) literal.literal.literal().value()) == 0 ||
                                ((int) literal.literal.literal().value()) == 1
                )
        ) {
            int iteratorVarIndex = varDecl.localId;

            if (value.condition instanceof Expr.Binary condition &&
                    condition.operator.type() == TokenType.LEQUAL &&
                    condition.left instanceof Expr.SingleIdentifier sIL &&
                    sIL.type == null && sIL.ordinal == iteratorVarIndex
            ) {
                Expr end = condition.right; //upper limit
                if (value.increment instanceof Expr.IdentifierSpecialAssign assign &&
                        assign.type == null && assign.ordinal == iteratorVarIndex &&
                        assign.assignType.type() == TokenType.GROW
                ) {
                    if (body instanceof Stmt.Expression expression &&
                            expression.expression instanceof Expr.IdentifierAssign identifierAssign &&
                            identifierAssign.fieldOwner == null &&
                            identifierAssign.type.type() == TokenType.ADD_ASSIGN
                    ) {
                        if (identifierAssign.value instanceof Expr.SingleIdentifier sI &&
                                sI.type == null && sI.ordinal == iteratorVarIndex
                        ) {
                            //sum += i
                            Expr.Binary add1 = new Expr.Binary(); //(n + 1)
                            add1.left = end; //TODO copy expressions in order to preserve identity
                            add1.operator = new Token(TokenType.ADD, "+", LiteralHolder.EMPTY, -1, -1);
                            add1.right = createNumber(1);
                            add1.retType = VarTypeManager.INTEGER.reference();
                            add1.executor = VarTypeManager.INTEGER.reference();

                            Expr.Binary mul1 = new Expr.Binary(); //n *
                            mul1.left = end;
                            mul1.operator = new Token(TokenType.MUL, "*", LiteralHolder.EMPTY, -1, -1);
                            mul1.right = add1;
                            mul1.retType = VarTypeManager.INTEGER.reference();
                            mul1.executor = VarTypeManager.INTEGER.reference();

                            Expr.Binary div1 = new Expr.Binary(); //n * (n + 1) / 2
                            div1.left = mul1;
                            div1.operator = new Token(TokenType.DIV, "/", LiteralHolder.EMPTY, -1, -1);
                            div1.right = createNumber(2);
                            div1.retType = VarTypeManager.INTEGER.reference();
                            div1.executor = VarTypeManager.INTEGER.reference();
                            identifierAssign.value = div1;
                            return body;
                        }
                        if (identifierAssign.value instanceof Expr.Binary binary) {
                            if (binary.left instanceof Expr.SingleIdentifier var &&
                                    var.type == null && var.ordinal == iteratorVarIndex &&
                                    ((
                                            binary.operator.type() == TokenType.POW && //i ** 2
                                                    binary.right instanceof Expr.Literal l && ((int) l.literal.literal().value()) == 2
                                    ) || (
                                            binary.operator.type() == TokenType.MUL && //i * i
                                                    binary.right instanceof Expr.SingleIdentifier var1 &&
                                                    var1.type == null && var1.ordinal == iteratorVarIndex
                                    ))
                            ) {
                                Token add = new Token(TokenType.ADD, "+", LiteralHolder.EMPTY, -1, -1);
                                Token mul = new Token(TokenType.MUL, "*", LiteralHolder.EMPTY, -1, -1);
                                //sum = n * (n + 1) * (2n + 1) / 6

                                Expr.Binary add1 = new Expr.Binary(); //(n + 1)
                                add1.left = end; //TODO copy expressions in order to preserve identity
                                add1.operator = add;
                                add1.right = createNumber(1);
                                add1.retType = VarTypeManager.INTEGER.reference();
                                add1.executor = VarTypeManager.INTEGER.reference();

                                Expr.Binary mul1 = new Expr.Binary(); //n * (n + 1)
                                mul1.left = end;
                                mul1.operator = mul;
                                mul1.right = add1;
                                mul1.retType = VarTypeManager.INTEGER.reference();
                                mul1.executor = VarTypeManager.INTEGER.reference();

                                Expr.Binary mul3 = new Expr.Binary(); // 2n
                                mul3.left = createNumber(2);
                                mul3.operator = mul;
                                mul3.right = end;
                                mul3.retType = VarTypeManager.INTEGER.reference();
                                mul3.executor = VarTypeManager.INTEGER.reference();

                                Expr.Binary add2 = new Expr.Binary(); // 2n + 1
                                add2.left = mul3;
                                add2.operator = add;
                                add2.right = createNumber(1);
                                add2.retType = VarTypeManager.INTEGER.reference();
                                add2.executor = VarTypeManager.INTEGER.reference();

                                Expr.Binary mul2 = new Expr.Binary(); //n * (n + 1) * (2n + 1)
                                mul2.left = mul1;
                                mul2.operator = mul;
                                mul2.right = add2;
                                mul2.retType = VarTypeManager.INTEGER.reference();
                                mul2.executor = VarTypeManager.INTEGER.reference();

                                Expr.Binary div1 = new Expr.Binary(); //n * (n + 1) * (2n + 1) / 6
                                div1.left = mul2;
                                div1.operator = new Token(TokenType.DIV, "/", LiteralHolder.EMPTY, -1, -1);
                                div1.right = createNumber(6);
                                div1.retType = VarTypeManager.INTEGER.reference();
                                div1.executor = VarTypeManager.INTEGER.reference();
                                identifierAssign.value = div1;
                                return body;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
