package net.kapitencraft.scripted.lang.oop.clazz;

import net.kapitencraft.scripted.lang.env.core.Environment;
import net.kapitencraft.scripted.lang.func.LoxCallable;
import net.kapitencraft.scripted.lang.func.LoxFunction;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.GeneratedField;
import net.kapitencraft.scripted.lang.oop.LoxField;
import net.kapitencraft.scripted.lang.run.Interpreter;
import net.kapitencraft.scripted.tool.Util;

import java.util.*;
import java.util.stream.Collectors;

public final class GeneratedLoxClass implements LoxClass {
    private final Map<String, LoxCallable> allAbstractMethods;
    private final Map<String, LoxCallable> allMethods;
    private final Map<String, LoxCallable> allStaticMethods;
    private final Map<String, LoxField> allFields;
    private final Map<String, LoxField> allStaticFields;
    private final List<Stmt.FuncDecl> abstractMethods;
    private final List<Stmt.FuncDecl> methods;
    private final List<Stmt.FuncDecl> staticMethods;
    private final List<Stmt.VarDecl> fields;
    private final List<Stmt.VarDecl> staticFields;
    private final LoxClass superclass;
    private final Token name;
    private final List<GeneratedLoxClass> enclosing;

    private final boolean isAbstract;

    public GeneratedLoxClass(List<Stmt.FuncDecl> abstractMethods, List<Stmt.FuncDecl> methods, List<Stmt.FuncDecl> staticMethods,
                             List<Stmt.VarDecl> fields, List<Stmt.VarDecl> staticFields,
                             LoxClass superclass, Token name, List<GeneratedLoxClass> enclosing, boolean isAbstract) {
        this.allAbstractMethods = getMethods(abstractMethods);
        this.isAbstract = isAbstract;
        Map<String, LoxCallable> abstracts = new HashMap<>(allAbstractMethods);
        abstracts.putAll(getMethods(methods));
        this.allMethods = abstracts;
        this.allStaticMethods = getMethods(staticMethods);
        this.allFields = getFields(fields);
        this.allStaticFields = getFields(staticFields);
        this.abstractMethods = abstractMethods;
        this.methods = methods;
        this.staticMethods = staticMethods;
        this.fields = fields;
        this.staticFields = staticFields;
        this.superclass = superclass;
        this.name = name;
        this.enclosing = enclosing;
    }

    private static Map<String, LoxCallable> getMethods(List<Stmt.FuncDecl> methods) {
        return methods.stream().collect(Collectors.toMap(dec -> dec.name.lexeme(), LoxFunction::new));
    }

    private static Map<String, LoxField> getFields(List<Stmt.VarDecl> fields) {
        return fields.stream().collect(Collectors.toMap(dec -> dec.name.lexeme(), GeneratedField::new));
    }

    public Map<String, LoxCallable> getMethods() {
        return Util.mergeMaps(LoxClass.super.getMethods(), allMethods);
    }

    @Override
    public LoxClass getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(LoxField::getType).orElse(LoxClass.super.getFieldType(name));
    }

    @Override
    public Map<String, LoxCallable> getAbstractMethods() {
        return Util.mergeMaps(LoxClass.super.getAbstractMethods(), allAbstractMethods);
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return allStaticFields.get(name).getType();
    }

    @Override
    public boolean hasField(String name) {
        return allFields.containsKey(name) || LoxClass.super.hasField(name);
    }

    @Override
    public LoxClass getStaticMethodType(String name) {
        return allStaticMethods.get(name).type();
    }

    @Override
    public LoxClass getMethodType(String name) {
        return allMethods.get(name).type();
    }

    @Override
    public LoxCallable getStaticMethod(String name) {
        return allStaticMethods.get(name);
    }

    @Override
    public LoxCallable getMethod(String name) {
        return Util.nonNullElse(allMethods.get(name), LoxClass.super.getMethod(name));
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return allStaticMethods.containsKey(name);
    }

    @Override
    public boolean hasMethod(String name) {
        return allMethods.containsKey(name) || LoxClass.super.hasMethod(name);
    }

    @Override
    public Map<String, LoxField> getFields() {
        return Util.mergeMaps(LoxClass.super.getFields(), allFields);
    }

    @Override
    public void callConstructor(Environment environment, Interpreter interpreter, List<Object> args) {

    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    public List<Stmt.FuncDecl> methods() {
        return methods;
    }

    public List<Stmt.FuncDecl> staticMethods() {
        return staticMethods;
    }

    public List<Stmt.VarDecl> fields() {
        return fields;
    }

    public List<Stmt.VarDecl> staticFields() {
        return staticFields;
    }

    @Override
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public String name() {
        return name.lexeme();
    }

    public Token getNameToken() {
        return name;
    }

    public List<GeneratedLoxClass> enclosing() {
        return enclosing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(abstractMethods, methods, staticMethods, fields, staticFields, superclass, name, enclosing);
    }

    @Override
    public String toString() { //jesus
        return "GeneratedLoxClass{" + name.lexeme() + "}[" +
                "abstractMethods=" + allAbstractMethods + ", " +
                "methods=" + allMethods + ", " +
                "staticMethods=" + allStaticMethods + ", " +
                "fields=" + allFields + ", " +
                "staticFields=" + allStaticFields + ", " +
                "superclass=" + superclass + ", " +
                "enclosing=" + enclosing + ']';
    }
}