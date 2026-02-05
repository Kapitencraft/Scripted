package net.kapitencraft.scripted.edit.graphical;

import net.kapitencraft.scripted.edit.graphical.variable.LocalVariableTable;

import java.util.Map;

public class MethodContext {
    public final LocalVariableTable lvt = new LocalVariableTable();

    public Snapshot createSnapshot() {
        return new Snapshot(lvt.active());
    }

    public static class Snapshot {
        private final Map<String, ExprCategory> lvSnapshot;

        public Snapshot(Map<String, ExprCategory> lvSnapshot) {
            this.lvSnapshot = lvSnapshot;
        }
    }
}
