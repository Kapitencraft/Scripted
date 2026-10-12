package net.kapitencraft.scripted.lang.holder.ast;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.List;

public interface Stmt {

    interface Visitor<R> {
        R visitReturnStmt(Return stmt);
        R visitForStmt(For stmt);
        R visitWhileStmt(While stmt);
        R visitForEachStmt(ForEach stmt);
        R visitDebugTraceStmt(DebugTrace stmt);
        R visitExpressionStmt(Expression stmt);
        R visitVarDeclStmt(VarDecl stmt);
        R visitThrowStmt(Throw stmt);
        R visitBlockStmt(Block stmt);
        R visitTryStmt(Try stmt);
        R visitClearLocalsStmt(ClearLocals stmt);
        R visitIfStmt(If stmt);
        R visitLoopInterruptionStmt(LoopInterruption stmt);
    }

    <R> R accept(Visitor<R> visitor);

    class Return implements Stmt {
        public Token keyword;
        public Expr value;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    class For implements Stmt {
        public Stmt init;
        public Expr condition;
        public Expr increment;
        public Stmt body;
        public Token keyword;
        public int popVarCount;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    class While implements Stmt {
        public Expr condition;
        public Stmt body;
        public Token keyword;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    class ForEach implements Stmt {
        public ClassReference type;
        public Token name;
        public Expr initializer;
        public Stmt body;
        public int baseVar;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForEachStmt(this);
        }
    }

    class DebugTrace implements Stmt {
        public Token keyword;
        public Token[] localNames;
        public byte[] localOrdinals;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDebugTraceStmt(this);
        }
    }

    class Expression implements Stmt {
        public Expr expression;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    class VarDecl implements Stmt {
        public Token name;
        public ClassReference type;
        public Expr initializer;
        public boolean isFinal;
        public int localId;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclStmt(this);
        }
    }

    class Throw implements Stmt {
        public Token keyword;
        public Expr value;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThrowStmt(this);
        }
    }

    class Block implements Stmt {
        public List<Stmt> statements;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    class Try implements Stmt {
        public Block body;
        public Pair<Pair<ClassReference[],Token>,Block>[] catches;
        public Block finale;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }
    }

    class ClearLocals implements Stmt {
        public int amount;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitClearLocalsStmt(this);
        }
    }

    class If implements Stmt {
        public Expr condition;
        public Stmt thenBranch;
        public boolean branchSeenReturn;
        public Stmt elseBranch;
        public boolean elseBranchSeenReturn;
        public ElifBranch[] elifs;
        public Token keyword;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    class LoopInterruption implements Stmt {
        public Token type;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopInterruptionStmt(this);
        }
    }
}
