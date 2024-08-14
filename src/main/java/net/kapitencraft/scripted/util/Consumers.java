package net.kapitencraft.scripted.util;

public class Consumers {

    @FunctionalInterface
    public interface C3<P1, P2, P3> {
        void apply(P1 p1, P2 p2, P3 p3);
    }

    @FunctionalInterface
    public interface C4<P1, P2, P3, P4> {
        void apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    @FunctionalInterface
    public interface C5<P1, P2, P3, P4, P5> {
        void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    @FunctionalInterface
    public interface C6<P1, P2, P3, P4, P5, P6> {
        void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
    }

    @FunctionalInterface
    public interface C7<P1, P2, P3, P4, P5, P6, P7> {
        void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);
    }

    @FunctionalInterface
    public interface C8<P1, P2, P3, P4, P5, P6, P7, P8> {
        void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);
    }

    @FunctionalInterface
    public interface C9<P1, P2, P3, P4, P5, P6, P7, P8, P9> {
        void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9);
    }

    @FunctionalInterface
    public interface C10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> {
        void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10);
    }
}
