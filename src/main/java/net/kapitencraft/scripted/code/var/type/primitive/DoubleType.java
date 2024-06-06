package net.kapitencraft.scripted.code.var.type.primitive;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class DoubleType extends PrimitiveType<Double> {
    public DoubleType() {
        super(Double::sum, (d, d1) -> d * d1, (d, d1) -> d / d1, (d, d1) -> d - d1, (d, d1) -> d % d1, d -> d);
    }
}
