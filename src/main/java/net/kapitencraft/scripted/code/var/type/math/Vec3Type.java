package net.kapitencraft.scripted.code.var.type.math;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.oop.InstanceMethod;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class Vec3Type extends VarType<Vec3> {

    public Vec3Type() {
        super(Vec3::add, Vec3::multiply, Vec3Type::makeDivide, Vec3::subtract, Vec3::length);
        this.addMethod("getX", Axis.x());
        this.addMethod("getY", Axis.y());
        this.addMethod("getZ", Axis.z());
    }

    private static Vec3 makeDivide(Vec3 a, Vec3 b) {
        return new Vec3(a.x / b.x, a.y / b.y, a.z/ b.z);

    }

    private static final class Axis extends InstanceMethod<Vec3, Double> {

        private final Direction.Axis axis;

        private Axis(String name, Direction.Axis axis) {
            super(ParamSet.empty(), name);
            this.axis = axis;
        }

        public static Axis x() {
            return new Axis("getX", Direction.Axis.X);
        }

        public static Axis y() {
            return new Axis("getY", Direction.Axis.Y);
        }

        public static Axis z() {
            return new Axis("getZ", Direction.Axis.Z);
        }

        @Override
        public InstanceMethod<Vec3, Double>.Instance load(ParamData set, Method<Vec3>.Instance inst, JsonObject object) {
            return new Instance(inst);
        }

        @Override
        public Method<Double>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return null;
        }

        public class Instance extends InstanceMethod<Vec3, Double>.Instance {

            protected Instance(Method<Vec3>.Instance parent) {
                super(null, parent);
            }

            @Override
            public Var<Double> call(VarMap map, Var<Vec3> inst) {
                return new Var<>(inst.getValue().get(axis));
            }

            @Override
            public VarType<Double> getType(VarAnalyser analyser) {
                return ModVarTypes.DOUBLE.get();
            }
        }
    }
}
