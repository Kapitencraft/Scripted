package net.kapitencraft.scripted.code.var.type.math;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.world.phys.Vec2;

public class Vec2Type extends VarType<Vec2> {

    public Vec2Type() {
        super(Vec2::add,
                (vec2, vec22) -> new Vec2(vec2.x * vec22.x, vec2.y * vec22.y),
                (vec2, vec22) -> new Vec2(vec2.x / vec22.x, vec2.y / vec22.y),
                (vec2, vec22) -> vec2.add(vec22.negated()),
                (vec2, vec22) -> new Vec2(vec2.x % vec22.x, vec2.y % vec22.y),
                null);
    }
}
