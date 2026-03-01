package net.kapitencraft.scripted.lang.bytecode.compile;

import java.util.ArrayList;

public class CacheBuffer {
    private final ArrayList<Byte> buffer;

    public CacheBuffer() {
        this.buffer = new ArrayList<>();
    }

    public void writeShort(int i) {
        buffer.add((byte) ((i >> 8) & 255));
        buffer.add((byte) (i & 255));
    }
}
