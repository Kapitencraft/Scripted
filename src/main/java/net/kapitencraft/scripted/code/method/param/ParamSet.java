package net.kapitencraft.scripted.code.method.param;

import net.kapitencraft.scripted.code.method.Method;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class ParamSet {
    private final List<Entry> possibles;

    public ParamSet(List<Entry> possibles) {
        this.possibles = possibles;
    }

    public static ParamSet single(Entry entry) {
        return new ParamSet(List.of(entry));
    }

    public static ParamSet multiple(Entry... entries) {
        return new ParamSet(List.of(entries));
    }

    public static ParamSet empty() {
        return new ParamSet(List.of());
    }

    public static ParamSet.Entry builder() {
        return new Entry();
    }

    public Entry getEntryForData(ParamData paramData) {
        return null;
    }

    public static class Entry {
        private final HashMap<String, VarType<?>> params = new HashMap<>();

        private final List<String> mandatoryParamsNameMap = new ArrayList<>();
        private final List<String> optionalParamsNameMap = new ArrayList<>();

        Entry() {}

        public Entry addParam(String paramName, Supplier<? extends VarType<?>> type) {
            params.put(paramName, type.get());
            mandatoryParamsNameMap.add(paramName);
            return this;
        }

        public Entry addOptionalParam(String paramName, VarType<?> type) {
            params.put(paramName, type);
            optionalParamsNameMap.add(paramName);
            return this;
        }

        public VarMap apply(ParamData paramData, VarMap parent) {
            List<Method<?>.Instance> methods = paramData.getParams();
            VarMap map = new VarMap();
            int i = 0;
            int mandatorySize = mandatoryParamsNameMap.size();
            for (; i < mandatorySize; i++) {//adding mandatory
                Method<?>.Instance method = methods.get(i);
                map.addValue(mandatoryParamsNameMap.get(i), method.callInit(parent));
            }
            for (; i < mandatorySize + optionalParamsNameMap.size(); i++) {
                if (i >= methods.size()) break; //optional param; can continue with non-existing
                Method<?>.Instance method = methods.get(i);
                map.addValue(optionalParamsNameMap.get(i - mandatorySize), method.callInit(parent));
            }
            return map;
        }

        public void analyse(ParamData in, VarAnalyser analyser) {

        }
    }
}
