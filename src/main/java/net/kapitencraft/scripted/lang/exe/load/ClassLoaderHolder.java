package net.kapitencraft.scripted.lang.exe.load;

import java.io.File;
import java.util.Objects;

public abstract class ClassLoaderHolder<T extends ClassLoaderHolder<T>> {
    public final File file;
    private final String pck;

    public ClassLoaderHolder(File file, String pck) {
        this.file = file;
        this.pck = pck;
    }

    protected String pck() {
        return pck;
    }

    public abstract void applySkeleton();

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClassLoaderHolder<T>) obj;
        return Objects.equals(this.file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String toString() {
        return "ClassHolder[" +
                "file=" + file + ']';
    }
}
