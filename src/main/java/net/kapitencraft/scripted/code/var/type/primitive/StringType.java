package net.kapitencraft.scripted.code.var.type.primitive;

public class StringType extends PrimitiveType<String> {
    public StringType() {
        super((s, s1) -> s + s1, null, null, null, null, null);
    }
}
