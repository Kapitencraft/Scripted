package net.kapitencraft.scripted.code.var.type.math;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class Vec3Type extends VarType<Vec3> {

    public Vec3Type() {
        super(Vec3::add, Vec3::multiply, Vec3Type::makeDivide, Vec3::subtract,
                ((vec3, vec32) -> new Vec3(vec3.x % vec32.x, vec3.y % vec32.y, vec3.z % vec32.z)),
                Vec3::length);
        this.addMethod("getX", x());
        this.addMethod("getY", y());
        this.addMethod("getZ", z());
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

    private final class Axis extends InstanceMethod<Double> {

        private final Direction.Axis axis;

        private Axis(String name, Direction.Axis axis) {
            super(ParamSet.empty(), name);
            this.axis = axis;
        }

        @Override
        public InstanceMethod<Double>.Instance load(ParamData data, Method<Vec3>.Instance inst, JsonObject object) {
            return new Instance(inst);
        }

        @Override
        public Method<Double>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return null;
        }

        @Override
        protected Method<Double>.Instance create(ParamData data, Method<?>.Instance parent) {
            return new Instance((Method<Vec3>.Instance) parent);
        }

        public class Instance extends InstanceMethod<Double>.Instance {

            protected Instance(Method<Vec3>.Instance parent) {
                super(null, parent);
            }

            @Override
            public Double call(VarMap map, Vec3 inst) {
                return inst.get(axis);
            }

            @Override
            public VarType<Double> getType(IVarAnalyser analyser) {
                return ModVarTypes.DOUBLE.get();
            }
        }
    }
}
