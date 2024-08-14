package net.kapitencraft.scripted.code.var.type.collection;

import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.methods.builder.ParamInst;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import org.jetbrains.annotations.ApiStatus;

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

        this.setConstructor(context -> context.constructor().executes(ArrayList::new));
        this.setExtendable(); //only used for Registry list's new constructor; do not override yourself

        this.addMethod("get", context -> context.returning(type)
                .withParam("index", VarTypes.INTEGER)
                .executes(List::get)
        );
        this.addMethod("indexOf", context -> context.returning(VarTypes.INTEGER)
                .withParam(ParamInst.of("element", this.type))
                .executes(List::indexOf)
        );
        this.addMethod("size", context -> context.returning(VarTypes.INTEGER).executes(List::size));
        this.addMethod(AddElement::new);
        this.addMethod(ClearFunction::new);
    }

    public VarType<T> getType() {
        return type;
    }

    @Override
    public final VarType<List<List<T>>> listOf() {
        throw new IllegalAccessError("can not create list of list (yet)");
    }

    private class AddElement extends SimpleInstanceFunction {

        protected AddElement() {
            super("add", set -> set.addEntry(entry -> entry
                    .addParam("value", ListType.this::getType)
            ));
        }

        @Override
        public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<List<T>> instance) {
            instance.getValue().add(map.getVarValue("value", ListType.this::getType));
        }
    }
    private class ClearFunction extends SimpleInstanceFunction {

        protected ClearFunction() {
            super("clear", ParamSet.empty());
        }

        @Override
        protected void executeInstanced(VarMap map, MethodPipeline<?> source, Var<List<T>> instance) {
            instance.getValue().clear();
        }
    }
}