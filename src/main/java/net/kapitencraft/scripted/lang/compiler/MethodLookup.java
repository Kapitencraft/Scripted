package net.kapitencraft.scripted.lang.compiler;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.scripted.lang.tool.Util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MethodLookup {
    private final List<Pair<ScriptedClass, AbstractMethodMap>> lookup;
    private final Map<String, ScriptedCallable> exposed;

    public MethodLookup(List<Pair<ScriptedClass, AbstractMethodMap>> lookup) {
        this.lookup = lookup;
        exposed = this.createExposed();
    }

    //region compile

    public void checkFinal(Compiler.ErrorStorage logger, Pair<Token, CompileCallable>[] map) {
        for (Pair<Token, CompileCallable> pair : map) {
            for (Pair<ScriptedClass, AbstractMethodMap> lookupElement : lookup) {
                Map<String, DataMethodContainer> methodMap = lookupElement.getSecond().asMap();
                if (!methodMap.containsKey(pair.getFirst().lexeme())) continue; //no method with name found, continuing
                for (ScriptedCallable method : methodMap.get(pair.getFirst().lexeme()).methods()) {
                    if (!method.isFinal()) continue;
                    if (Util.matchArgs(method.argTypes(), pair.getSecond().argTypes())) {
                        logger.errorF(pair.getFirst(), "method '%s(%s)' can not override final method from class '%s'", pair.getFirst().lexeme(), Util.getDescriptor(pair.getSecond().argTypes()), lookupElement.getFirst().name());
                    }
                }
            }
        }
    }

    public void checkAbstract(Compiler.ErrorStorage logger, Token className, Pair<Token, CompileCallable>[] map) {
        Map<String, List<Pair<ScriptedClass, ScriptedCallable>>> abstracts = new HashMap<>();
        for (Pair<ScriptedClass, AbstractMethodMap> methods : lookup) {
            methods.getSecond().asMap().forEach((s, dataMethodContainer) -> {
                a: for (ScriptedCallable method : dataMethodContainer.methods()) {
                    List<Pair<ScriptedClass, ScriptedCallable>> classData = abstracts.computeIfAbsent(s, k -> new ArrayList<>());
                    if (method.isAbstract()) {
                        for (Pair<ScriptedClass, ScriptedCallable> pair : classData) {
                            if (Util.matchArgs(pair.getSecond().argTypes(), method.argTypes())) continue a;
                        }
                        classData.add(Pair.of(methods.getFirst(), method));
                    } else {
                        for (int i = 0; i < classData.size(); i++) {
                            Pair<ScriptedClass, ScriptedCallable> pair = classData.get(i);
                            if (Util.matchArgs(pair.getSecond().argTypes(), method.argTypes())) {
                                classData.remove(i);
                                continue a;
                            }
                        }
                    }
                }
            });
        }
        for (Pair<Token, CompileCallable> pair : map) {
            List<Pair<ScriptedClass, ScriptedCallable>> methods = abstracts.get(pair.getFirst().lexeme());
            if (methods == null) continue; //no abstract method for that name, continuing
            methods.removeIf(callablePair -> Util.matchArgs(pair.getSecond().argTypes(), callablePair.getSecond().argTypes()));
        }
        abstracts.forEach((string, pairs) -> {
            pairs.forEach(pair -> {
                String errorMsg = pair.getFirst().isInterface() ?
                        "class %s must either be declared abstract or override abstract method '%s(%s)' from interface %s" :
                        "class %s must either be declared abstract or override abstract method '%s(%s)' from class %s";
                logger.errorF(className, errorMsg, className.lexeme(), string, Util.getDescriptor(pair.getSecond().argTypes()), pair.getFirst().name());
            });
        });
    }

    //endregion

    public static MethodLookup createFromClass(ScriptedClass scriptedClass, ClassReference... interfaces) {
        List<ScriptedClass> parentMap = createParentMap(scriptedClass);
        List<ScriptedClass> allParents = new ArrayList<>();
        for (ClassReference i : interfaces) {
            addInterfaces(i.get(), allParents::add);
            allParents.add(i.get());
        }
        for (ScriptedClass parent : parentMap) {
            addInterfaces(parent, allParents::add);
            allParents.add(parent);
        }
        List<Pair<ScriptedClass, AbstractMethodMap>> lookup = allParents.stream().collect(Util.toPairList(Function.identity(), ScriptedClass::getMethods));
        return new MethodLookup(lookup);
    }

    private static void addInterfaces(ScriptedClass target, Consumer<ScriptedClass> sink) {
        if (target.interfaces() != null) for (ClassReference anInterface : target.interfaces()) {
            addInterfaces(anInterface.get(), sink);
            sink.accept(anInterface.get());
        }
    }

    private static List<ScriptedClass> createParentMap(ScriptedClass scriptedClass) {
        Objects.requireNonNull(scriptedClass, "Can not create target map for null class!");
        List<ScriptedClass> parents = new ArrayList<>();
        if (scriptedClass.superclass() != null) {
            do {
                parents.add(scriptedClass);
                scriptedClass = scriptedClass.superclass().get();
            } while (scriptedClass != null && scriptedClass.superclass() != null);
        }
        return Util.invert(parents);
    }

    private Map<String, ScriptedCallable> createExposed() {
        Map<String, ScriptedCallable> map = new HashMap<>();
        for (Pair<ScriptedClass, AbstractMethodMap> pair : lookup) {
            map.putAll(ScriptedCallable.parseMethods(pair.getSecond().asMap()));
        }
        return map;
    }

    public ScriptedCallable get(String signature) {
        return exposed.get(signature);
    }
}
