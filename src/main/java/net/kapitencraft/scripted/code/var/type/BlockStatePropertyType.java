package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStatePropertyType<T extends Comparable<T>> extends VarType<Property<T>> {
    private final VarType<T> type;

    public BlockStatePropertyType(VarType<T> type) {
        super("Property<" + type + ">", null, null, null, null, null, null);
        this.type = type;
    }

    @Override
    public Class<Property<T>> getTypeClass() {
        return null; //TODO make f*** generics!
    }
}
