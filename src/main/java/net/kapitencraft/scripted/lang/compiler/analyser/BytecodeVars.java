package net.kapitencraft.scripted.lang.compiler.analyser;

import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.run.VarTypeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BytecodeVars {
    private final Local[] locals = new Local[256];
    private byte localCount, scopeDepth;

    public FetchResult get(String name) {
        for (int i = localCount - 1; i >= 0; i--) {
            Local local = locals[i];
            if (Objects.equals(local.name, name)) return new FetchResult((byte) i, local.canAssign, local.assigned, local.type);
        }
        return FetchResult.FAIL;
    }

    public byte add(String name, ClassReference type, boolean canAssign, boolean assigned) {
        if (name.equals("?") || //add synthetic methods no questions asked
                get(name) == FetchResult.FAIL) {
            locals[localCount] = new Local(name, scopeDepth, type, canAssign, assigned);
            return localCount++;
        }
        return -1;
    }

    public void push() {
        scopeDepth++;
    }

    public Stmt pop() {
        int i = localCount - 1;
        while (i >= 0 && locals[i].depth >= scopeDepth) {
            i--;
        }
        Stmt stmt = new Stmt.ClearLocals(localCount - (i + 1));
        localCount = (byte) (i + 1);
        scopeDepth--;
        return stmt;
    }

    public ClassReference getType(String lexeme) {
        FetchResult result = get(lexeme);
        if (result == FetchResult.FAIL) return VarTypeManager.VOID.reference();
        return result.type;
    }

    public void setHasValue(byte ordinal) {
        locals[ordinal & 255].assigned = true;
    }

    public List<String> dumpNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < localCount; i++) {
            names.add(locals[i].name);
        }
        return names;
    }

    public byte[] gatherLocalIndexes(List<String> locals) {
        byte[] indexes = new byte[locals.size()];
        for (int i = 0; i < locals.size(); i++) {
            indexes[i] = this.get(locals.get(i)).ordinal;
        }
        return indexes;
    }

    private static final class Local {
        private final String name;
        private final int depth;
        private final ClassReference type;
        private final boolean canAssign;
        private boolean assigned;

        private Local(String name, int depth, ClassReference type, boolean canAssign, boolean assigned) {
            this.name = name;
            this.depth = depth;
            this.type = type;
            this.canAssign = canAssign;
            this.assigned = assigned;
        }

        public String name() {
            return name;
        }

        public int depth() {
            return depth;
        }

        public ClassReference type() {
            return type;
        }

        public boolean canAssign() {
            return canAssign;
        }
    }

    public record FetchResult(byte ordinal, boolean canAssign, boolean assigned, ClassReference type) {
        public static final FetchResult FAIL = new FetchResult((byte) -1, false, true, VarTypeManager.VOID.reference());
    }
}
