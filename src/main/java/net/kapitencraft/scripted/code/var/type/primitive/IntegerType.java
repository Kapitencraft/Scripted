package net.kapitencraft.scripted.code.var.type.primitive;

public class IntegerType extends PrimitiveType<Integer> {

    public IntegerType() {
        super(Integer::sum, (i, i1) -> i * i1, (i, i1) -> i / i1, (i, i1) -> i - i1, (i, i1) -> i % i1, Integer::intValue);
    }
}
