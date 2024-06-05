package net.kapitencraft.scripted.code.exe.methods.param;

import com.google.gson.JsonSyntaxException;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.Var;
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
        private final HashMap<String, List<String>> typeMatch = new HashMap<>();

        Entry() {}

        public Entry addParam(String paramName, Supplier<? extends VarType<?>> type) {
            params.put(paramName, type.get());
            mandatoryParamsNameMap.add(paramName);
            return this;
        }

        public Entry addWildCardParam(String paramName, String... typeMatch) {
            params.put(paramName, null);
            if (typeMatch.length > 0) this.typeMatch.put(paramName, List.of(typeMatch));
            mandatoryParamsNameMap.add(paramName);
            return this;
        }

        public Entry addOptionalWildCardParam(String paramName, String... typeMatch) {
            params.put(paramName, null);
            if (typeMatch.length > 0) this.typeMatch.put(paramName, List.of(typeMatch));
            optionalParamsNameMap.add(paramName);
            return this;
        }

        public Entry addOptionalParam(String paramName, Supplier<? extends VarType<?>> type) {
            params.put(paramName, type.get());
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
                String name = mandatoryParamsNameMap.get(i);
                Var<?> value = method.callInit(parent);
                if (typeMatch.containsKey(name)) {
                    typeMatch.get(name).forEach(s -> {
                        if (!map.hasVar(s) || !value.matchesType(map.getVar(s))) {
                            throw new JsonSyntaxException("data match failed");
                        }
                    });
                }
                map.addValue(name, value);
            }
            for (; i < mandatorySize + optionalParamsNameMap.size(); i++) {
                if (i >= methods.size()) break; //optional param; can continue with non-existing
                Method<?>.Instance method = methods.get(i);
                map.addValue(optionalParamsNameMap.get(i - mandatorySize), method.callInit(parent));
            }
            return map;
        }

        public void check(ParamData data, VarAnalyser analyser) { //TODO fix method var types doing issues
            List<Method<?>.Instance> methods = data.getParams();
            VarMap map = new VarMap();
            int i = 0;
            int mandatorySize = mandatoryParamsNameMap.size();
            for (; i < mandatorySize; i++) {//adding mandatory
                Method<?>.Instance method = methods.get(i);
                String name = mandatoryParamsNameMap.get(i);
                if (typeMatch.containsKey(name)) {
                    typeMatch.get(name).forEach(s -> {
                        if (!map.hasVar(s) || method.getType(analyser).matches()) {
                            throw new JsonSyntaxException("data match failed");
                        }
                    });
                }
                map.addValue(name, );
            }
            for (; i < mandatorySize + optionalParamsNameMap.size(); i++) {
                if (i >= methods.size()) break; //optional param; can continue with non-existing
                Method<?>.Instance method = methods.get(i);
                method.getType(analyser);
            }
            return map;
        }
    }
}
