package net.kapitencraft.scripted.code.var.type.registry;

import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.type.abstracts.RegistryType;
import net.kapitencraft.scripted.init.custom.ModRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class VarTypeType extends RegistryType<VarType<?>> {
    public VarTypeType() {
        super(ModRegistries.VAR_TYPES);
    }
}
