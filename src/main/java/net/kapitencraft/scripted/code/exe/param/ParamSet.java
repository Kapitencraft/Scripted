package net.kapitencraft.scripted.code.exe.param;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ParamSet {
    private final List<Entry> params = new ArrayList<>();

    public static Consumer<ParamSet> empty() {
        return set -> {};
    }

    public ParamSet addEntry(Consumer<Entry> entryBuilder) {
        Entry entry = new Entry();
        entryBuilder.accept(entry);
        this.params.add(entry);
        return this;
    }

    public Entry getEntryForData(ParamData paramData, VarAnalyser analyser) {
        return getEntryForArgMethods(paramData.getParams(), analyser);
    }

    public void analyse(VarAnalyser analyser, ParamData data) {
        if (this.params.size() == 1) {
            Entry entry = this.params.get(0);
            entry.check(data, analyser);
        }
    }

    /**
     * @param list the varTypes of the arguments applied
     * @return the applied entry that matches the arguments (nullable)
     */
    public Entry getEntryForArgs(List<? extends VarType<?>> list) {
        List<Entry> entries = new ArrayList<>(params);
        Iterator<? extends VarType<?>> iterator = list.iterator();

        final int[] i = new int[]{0};
        while (iterator.hasNext() && entries.size() >= 2) {
            entries.removeIf(entry -> entry.typeForId(i[0]) != iterator.next());
            i[0]++;
        }
        return entries.isEmpty() ? null : entries.get(0);
    }

    public Entry getEntryForArgMethods(List<Method<?>.Instance> list, VarAnalyser analyser) {
        return getEntryForArgs(list.stream().map(instance -> instance.getType(analyser)).toList());
    }

    public boolean isEmpty() {
        return this.params.isEmpty();
    }

    public static class Entry {
        private final WildCardSet wildCardSet = new WildCardSet();
        private final HashMap<String, VarType<?>> params = new HashMap<>();

        private final List<String> mandatoryParamsNameMap = new ArrayList<>();
        private final List<String> methodBodies = new ArrayList<>();
        private final List<String> optionalParamsNameMap = new ArrayList<>();

        Entry() {
        }

        public Entry addParam(String paramName, VarType<?> type) {
            params.put(paramName, type);
            mandatoryParamsNameMap.add(paramName);
            return this;
        }

        public Entry addParam(String paramName, Supplier<? extends VarType<?>> sup) {
            return addParam(paramName, sup.get());
        }

        public Entry addOptionalParam(String paramName, VarType<?> type) {
            params.put(paramName, type);
            optionalParamsNameMap.add(paramName);
            return this;
        }

        public Entry addOptionalParam(String paramName, Supplier<? extends VarType<?>> sup) {
            return addOptionalParam(paramName, sup.get());
        }


        public Entry addWildCardParam(String wildCardId, String paramName) {
            wildCardSet.addWildcard(wildCardId, paramName);
            params.put(paramName, null);
            mandatoryParamsNameMap.add(paramName);
            return this;
        }

        public Entry addOptionalWildCardParam(String wildCardId, String paramName) {
            wildCardSet.addWildcard(wildCardId, paramName);
            params.put(paramName, null);
            optionalParamsNameMap.add(paramName);
            return this;
        }


        VarType<?> typeForId(int id) {
            return params.get(id < mandatoryParamsNameMap.size() ? mandatoryParamsNameMap.get(id) :
                    optionalParamsNameMap.get(id - mandatoryParamsNameMap.size()));
        }

        public VarMap apply(ParamData paramData, VarMap parent) {
            List<Method<?>.Instance> methods = paramData.getParams();
            VarMap map = new VarMap();
            int i = 0;
            int mandatorySize = mandatoryParamsNameMap.size();
            for (; i < mandatorySize; i++) {//adding mandatory
                Method<?>.Instance method = methods.get(i);
                String name = mandatoryParamsNameMap.get(i);
                Var<?> value = method.buildVar(parent);
                if (params.get(name) == null) { //it's a wildcard
                    String wildCardName = wildCardSet.getWildCardId(name);
                    if (!paramData.hasWildCard(wildCardName)) {
                        paramData.applyWildCardType(wildCardName, value.getType());
                    } else {
                        if (paramData.getWildCardType(wildCardName) != value.getType()) {
                            throw new IllegalArgumentException("match error: '" + name + "' does not match type; expected: '" + paramData.getWildCardType(wildCardName) + "'; got: " + value.getType());
                        }
                    }
                }
                map.addValue(name, value);
            }
            for (; i < mandatorySize + optionalParamsNameMap.size(); i++) {
                if (i >= methods.size()) break; //optional param; not necessary to continue
                Method<?>.Instance method = methods.get(i);
                map.addValue(optionalParamsNameMap.get(i - mandatorySize), method.buildVar(parent));
            }
            return map;
        }

        public void check(ParamData data, VarAnalyser analyser) { //TODO fix method var types doing issues
            List<Method<?>.Instance> methods = data.getParams();
            Map<String, Method<?>.Instance> mapped = new HashMap<>();
            populateMapper(methods, mapped);
        }

        public void populateMapper(List<Method<?>.Instance> methods, Map<String, Method<?>.Instance> mapper) {
            int i = 0;
            int mandatorySize = mandatoryParamsNameMap.size();
            for (; i < mandatorySize; i++) {
                mapper.put(mandatoryParamsNameMap.get(i), methods.get(i));
            }
            for (; i < mandatorySize + optionalParamsNameMap.size(); i++) {
                if (methods.size() == i) break;
                mapper.put(optionalParamsNameMap.get(i - mandatorySize), methods.get(i));
            }
        }

        public void set(List<Pair<VarType<?>, String>> params) {
            params.forEach(varTypeStringPair -> this.addParam(varTypeStringPair.getSecond(), varTypeStringPair.getFirst()));
        }
    }
}