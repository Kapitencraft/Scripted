package net.kapitencraft.scripted.lang.compile.analyser;

import net.kapitencraft.scripted.lang.func.LoxCallable;
import net.kapitencraft.scripted.lang.oop.clazz.LoxClass;

public class EnvAnalyser {
    private final MethodAnalyser methodAnalyser;
    private final VarAnalyser varAnalyser;
    private int loopIndex = 0;

    public EnvAnalyser() {
        this.methodAnalyser = new MethodAnalyser();
        this.varAnalyser = new VarAnalyser();
    }

    public void push() {
        methodAnalyser.push();
        varAnalyser.push();
    }

    public void pop() {
        methodAnalyser.pop();
        varAnalyser.pop();
    }

    public void pushLoop() {
        loopIndex++;
    }

    public void popLoop() {
        loopIndex--;
    }

    public boolean inLoop() {
        return loopIndex > 0;
    }

    public boolean hasVar(String name) {
        return varAnalyser.has(name);
    }

    public boolean hasVarValue(String name) {
        return varAnalyser.hasValue(name);
    }

    public boolean isFinal(String name) {
        return varAnalyser.isFinal(name);
    }

    public void setHasVarValue(String name) {
        varAnalyser.setHasValue(name);
    }

    public void addVar(String name, LoxClass type, boolean value, boolean isFinal) {
        varAnalyser.add(name, type, value, isFinal);
    }

    public boolean hasMethod(String name) {
        return methodAnalyser.has(name);
    }

    public boolean addMethod(String name, LoxCallable callable) {
        return methodAnalyser.add(name, callable);
    }

    public LoxClass getVarType(String name) {
        return varAnalyser.getType(name);
    }

    public LoxClass getMethodType(String lexeme) {
        return methodAnalyser.type(lexeme);
    }

    public LoxCallable getMethod(String lexeme) {
        return methodAnalyser.callable(lexeme);
    }
}
