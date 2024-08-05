package net.kapitencraft.scripted.code.var.type.collection;

import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
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

        this.setConstructor(new ListConstructor());
        this.setExtendable(); //only used for Registry list's new constructor; do not override yourself

        this.addMethod(GetElement::new);
        this.addMethod(IndexOfElement::new);
        this.addMethod(SizeElement::new);
        this.addMethod(AddElement::new);
        this.addMethod(ClearFunction::new);
    }

    @Override
    public String toString() {
        return "List<" + type + ">";
    }

    public VarType<T> getType() {
        return type;
    }

    @Override
    public final VarType<List<List<T>>> listOf() {
        throw new IllegalAccessError("can not create list of list (yet)");
    }

    private class ListConstructor extends SimpleConstructor {

        protected ListConstructor() {
            super(ParamSet.empty());
        }

        @Override
        protected List<T> call(VarMap params) {
            return new ArrayList<>();
        }

        @Override
        public VarType<List<T>> getType(IVarAnalyser analyser) {
            return ListType.this;
        }
    }

    private class GetElement extends SimpleInstanceMethod<T> {

        protected GetElement() {
            super(set -> set.addEntry(entry -> entry.addParam("id", VarTypes.INTEGER)), "get");
        }

        @Override
        public T call(VarMap map, List<T> inst) {
            return inst.get(map.getVarValue("id", VarTypes.INTEGER));
        }

        @Override
        public VarType<T> getType(IVarAnalyser analyser) {
            return type;
        }
    }
    private class IndexOfElement extends SimpleInstanceMethod<Integer> {

        protected IndexOfElement() {
            super(set -> set.addEntry(entry -> entry
                    .addParam("element", ListType.this::getType)
            ), "indexOf");
        }
        @Override
        public Integer call(VarMap map, List<T> inst) {
            return inst.indexOf(map.getVarValue("element", ListType.this::getType));
        }

        @Override
        public VarType<Integer> getType(IVarAnalyser analyser) {
            return VarTypes.INTEGER.get();
        }

    }
    private class SizeElement extends SimpleInstanceMethod<Integer> {

        protected SizeElement() {
            super(ParamSet.empty(), "size");
        }

        @Override
        public Integer call(VarMap map, List<T> inst) {
            return inst.size();
        }

        @Override
        public VarType<Integer> getType(IVarAnalyser analyser) {
            return VarTypes.INTEGER.get();
        }
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