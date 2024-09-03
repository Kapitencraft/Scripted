package net.kapitencraft.scripted.code.var.type.data;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.nbt.CompoundTag;

public class DataStorageType extends VarType<CompoundTag> {

    public DataStorageType() {
        super("DataStorage", null, null, null, null, null, null);
        this.addConstructor(context -> context.constructor().executes(CompoundTag::new));
    }
}