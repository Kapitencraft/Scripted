package net.kapitencraft.scripted.code.var.type.registry;

import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.custom.ModRegistries;

public class VarTypeType extends RegistryType<VarType<?>> {
    public VarTypeType() {
        super("VarType", ModRegistries.VAR_TYPES);
    }
}
