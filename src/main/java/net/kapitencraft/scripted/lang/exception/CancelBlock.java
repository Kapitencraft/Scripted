package net.kapitencraft.scripted.lang.exception;

public class CancelBlock extends RuntimeException {
    public final Object value;

    public CancelBlock(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
