package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

import java.util.List;

public class SwitchInstruction extends CodeInstruction implements JumpableInstruction {
    private final int size;
    private int target;
    private final List<Entry> entries;

    public SwitchInstruction(int size, List<Entry> entries) {
        super(Opcode.SWITCH);
        this.size = size;
        this.entries = entries;
    }

    @Override
    public void setTarget(int target) {
        this.target = target;
    }

    @Override
    public int getTarget() {
        return target;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static final class Entry {
        private final int id;
        private int idx;

        public Entry(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public int idx() {
            return idx;
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.add2bArg(ips.getIp(target));
        builder.add2bArg(size);
        for (Entry entry : entries) {
            builder.add4bArg(entry.id);
            builder.add2bArg(ips.getIp(entry.idx));
        }
    }

    @Override
    public int length() {
        return 5 + entries.size() * 6;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
