package net.kapitencraft.scripted.code.var.type.data;

import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.function.Consumer;

public class DataListType extends VarType<ListTag> {
    public DataListType() {
        super("DataList", null, null, null, null, null, null);
    }

    public class GetIntList extends SimpleInstanceMethod<List<Integer>> {

        protected GetIntList(Consumer<ParamSet> builder, String name) {
            super(set -> set.addEntry(entry -> entry
                    .addParam("test", VarTypes.INTEGER.get().listOf())), name);
        }

        @Override
        protected List<Integer> call(VarMap map, ListTag inst) {
            return List.of();
        }

        @Override
        protected VarType<List<Integer>> getType(IVarAnalyser analyser) {
            return null;
        }
    }
}
