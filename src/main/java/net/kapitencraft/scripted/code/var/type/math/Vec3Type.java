package net.kapitencraft.scripted.code.var.type.math;

import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class Vec3Type extends VarType<Vec3> {

    public Vec3Type() {
        super("Vec3", Vec3::add, Vec3::multiply, Vec3Type::makeDivide, Vec3::subtract,
                ((vec3, vec32) -> new Vec3(vec3.x % vec32.x, vec3.y % vec32.y, vec3.z % vec32.z)),
                Comparator.comparingDouble(Vec3::length));
        this.addMethod(this::x);
        this.addMethod(this::y);
        this.addMethod(this::z);
    }

    private static Vec3 makeDivide(Vec3 a, Vec3 b) {
        return new Vec3(a.x / b.x, a.y / b.y, a.z/ b.z);
    }

    private Axis x() {
        return new Axis("getX", Direction.Axis.X);
    }

    private Axis y() {
        return new Axis("getY", Direction.Axis.Y);
    }

    private Axis z() {
        return new Axis("getZ", Direction.Axis.Z);
    }

    private class Axis extends SimpleInstanceMethod<Double> {

        private final Direction.Axis axis;

        private Axis(String name, Direction.Axis axis) {
            super(ParamSet.empty(), name);
            this.axis = axis;
        }

        @Override
        public Double call(VarMap map, Vec3 inst) {
            return inst.get(axis);
        }

        @Override
        public VarType<Double> getType(IVarAnalyser analyser) {
            return VarTypes.DOUBLE.get();
        }
    }
}
