package net.kapitencraft.scripted.code.var.type.collection;

import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * protected, but do not inherit it's only for tags in registries
 */
@ApiStatus.Internal
public class ListType<T> extends VarType<List<T>> {
    private final VarType<T> type;

    public ListType(VarType<T> type) {
        super("List<" + type.getName() + ">", null, null, null, null, null, null);
        this.type = type;

        this.addConstructor(context -> context.constructor().executes(ArrayList::new));

        this.addMethod("get", context -> context.returning(type)
                .withParam("index", VarTypes.INTEGER)
                .executes(List::get)
        );
        this.addMethod("indexOf", context -> context.returning(VarTypes.INTEGER)
                .withParam(ParamInst.of("element", this.type))
                .executes(List::indexOf)
        );
        this.addMethod("size", context -> context.returning(VarTypes.INTEGER).executes(List::size));
        this.addMethod("add", context -> context.consumer()
                .withParam(ParamInst.of("value", type))
                .executes(List::add)
        );
        this.addMethod("clear", context -> context.consumer().executes(List::clear));
    }

    @Override
    public @NotNull String toId() {
        return String.format("List<%s>", this.type.toId());
    }

    public VarType<T> getType() {
        return type;
    }

    @Override
    public final VarType<List<List<T>>> listOf() {
        throw new IllegalAccessError("can not create list of list (yet)");
    }
}