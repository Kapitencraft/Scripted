package net.kapitencraft.scripted.lang.holder.ast;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;

public interface Expr {

    interface Visitor<R> {
        R visitCallExpr(Call expr);
        R visitSetExpr(Set expr);
        R visitArraySetExpr(ArraySet expr);
        R visitArraySpecialExpr(ArraySpecial expr);
        R visitSingleIdentifierExpr(SingleIdentifier expr);
        R visitConstructorExpr(Constructor expr);
        R visitStaticSetExpr(StaticSet expr);
        R visitLogicalExpr(Logical expr);
        R visitIdentifierSpecialAssignExpr(IdentifierSpecialAssign expr);
        R visitUnaryExpr(Unary expr);
        R visitComparisonChainExpr(ComparisonChain expr);
        R visitWhenExpr(When expr);
        R visitCastCheckExpr(CastCheck expr);
        R visitStaticGetExpr(StaticGet expr);
        R visitSwitchExpr(Switch expr);
        R visitIdentifierAssignExpr(IdentifierAssign expr);
        R visitSliceExpr(Slice expr);
        R visitGetExpr(Get expr);
        R visitArrayGetExpr(ArrayGet expr);
        R visitLiteralExpr(Literal expr);
        R visitArrayConstructorExpr(ArrayConstructor expr);
        R visitBinaryExpr(Binary expr);
        R visitStaticSpecialExpr(StaticSpecial expr);
        R visitSpecialSetExpr(SpecialSet expr);
    }

    <R> R accept(Visitor<R> visitor);
    ClassReference retType();

    class Call implements Expr {
        public Expr object;
        public ClassReference declaring;
        public Token name;
        public Expr[] args;
        public String signature;
        public Boolean virtual;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Set implements Expr {
        public Expr object;
        public Token name;
        public Expr value;
        public Token assignType;
        public ClassReference executor;
        public String signature;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class ArraySet implements Expr {
        public Expr object;
        public Expr index;
        public Expr value;
        public Token assignType;
        public ClassReference executor;
        public ClassReference componentType;
        public String signature;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class ArraySpecial implements Expr {
        public Expr object;
        public Expr index;
        public Token assignType;
        public ClassReference executor;
        public String signature;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySpecialExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class SingleIdentifier implements Expr {
        public Token name;
        public byte ordinal;
        public ClassReference type;
        public boolean isStatic;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSingleIdentifierExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Constructor implements Expr {
        public Token keyword;
        public ClassReference target;
        public Expr[] args;
        public String signature;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstructorExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class StaticSet implements Expr {
        public ClassReference target;
        public Token name;
        public Expr value;
        public Token assignType;
        public ClassReference executor;
        public String signature;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSetExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Logical implements Expr {
        public Expr left;
        public Token operator;
        public Expr right;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class IdentifierSpecialAssign implements Expr {
        public Token name;
        public Token assignType;
        public int ordinal;
        public ClassReference executor;
        public String signature;
        public ClassReference type;
        public boolean isStatic;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIdentifierSpecialAssignExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Unary implements Expr {
        public Token operator;
        public Expr right;
        public ClassReference executor;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class ComparisonChain implements Expr {
        public Expr[] entries;
        public Token[] types;
        public ClassReference dataType;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitComparisonChainExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class When implements Expr {
        public Expr condition;
        public Expr ifTrue;
        public Expr ifFalse;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class CastCheck implements Expr {
        public Expr object;
        public ClassReference targetType;
        public Token patternVarName;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class StaticGet implements Expr {
        public ClassReference target;
        public Token name;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticGetExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Switch implements Expr {
        public Expr provider;
        public boolean isEnum;
        public SwitchKey[] params;
        public Expr defaulted;
        public Token keyword;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class IdentifierAssign implements Expr {
        public Token name;
        public Expr value;
        public Token type;
        public byte ordinal;
        public ClassReference executor;
        public String signature;
        public ClassReference fieldOwner;
        public boolean isStatic;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIdentifierAssignExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Slice implements Expr {
        public Expr object;
        public Expr start;
        public Expr end;
        public Expr interval;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSliceExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Get implements Expr {
        public Expr object;
        public Token name;
        public ClassReference type;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class ArrayGet implements Expr {
        public Expr object;
        public Expr index;
        public ClassReference componentType;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Literal implements Expr {
        public Token literal;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class ArrayConstructor implements Expr {
        public Token keyword;
        public ClassReference compoundType;
        public Expr size;
        public Expr[] obj;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayConstructorExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class Binary implements Expr {
        public Expr left;
        public Expr right;
        public Token operator;
        public ClassReference executor;
        public String signature;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class StaticSpecial implements Expr {
        public ClassReference target;
        public Token name;
        public Token assignType;
        public ClassReference executor;
        public String signature;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSpecialExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }

    class SpecialSet implements Expr {
        public Expr object;
        public Token name;
        public Token assignType;
        public String signature;
        public ClassReference retType;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialSetExpr(this);
        }
        @Override
        public ClassReference retType() {
            return this.retType;
}
    }
}
