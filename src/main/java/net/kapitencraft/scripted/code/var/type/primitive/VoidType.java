package net.kapitencraft.scripted.code.var.type.primitive;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

/**
 * used to indicate that the method should return nothing
 */
public class VoidType extends VarType<Void> {
    public VoidType() {
        super("void", null, null, null, null, null, null);
    }

    @Override
    public Class<Void> getTypeClass() {
        return void.class; //cursed
    }
}
