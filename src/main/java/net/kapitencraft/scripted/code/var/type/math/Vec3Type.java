package net.kapitencraft.scripted.code.var.type.math;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class Vec3Type extends VarType<Vec3> {

    public Vec3Type() {
        super("Vec3", Vec3::add, Vec3::multiply, Vec3Type::makeDivide, Vec3::subtract,
                ((vec3, vec32) -> new Vec3(vec3.x % vec32.x, vec3.y % vec32.y, vec3.z % vec32.z)),
                Comparator.comparingDouble(Vec3::length));
        this.addMethod("getX", context -> context.returning(VarTypes.DOUBLE).executes(Vec3::x));
        this.addMethod("getY", context -> context.returning(VarTypes.DOUBLE).executes(Vec3::y));
        this.addMethod("getZ", context -> context.returning(VarTypes.DOUBLE).executes(Vec3::z));
    }

    @Override
    public Class<Vec3> getTypeClass() {
        return Vec3.class;
    }

    private static Vec3 makeDivide(Vec3 a, Vec3 b) {
        return new Vec3(a.x / b.x, a.y / b.y, a.z/ b.z);
    }
}
