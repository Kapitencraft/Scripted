package net.kapitencraft.scripted.lang.oop.clazz;

import net.kapitencraft.scripted.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.scripted.lang.compiler.Holder;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import net.kapitencraft.scripted.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.scripted.lang.oop.field.ScriptedField;
import net.kapitencraft.scripted.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.scripted.lang.run.Interpreter;
import net.kapitencraft.scripted.lang.run.VarTypeManager;
import net.kapitencraft.scripted.lang.run.algebra.Operand;
import net.kapitencraft.scripted.lang.run.algebra.OperationType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

public interface ScriptedClass {

    default boolean isArray() {
        return false;
    }

    default ScriptedClass array() {
        return new ArrayClass(this);
    }

    default ClassReference reference() {
        return ClassReference.of(this);
    }

    Object getStaticField(String name);

    Object setStaticField(String name, Object val);

    default Object staticSpecialAssign(String name, TokenType assignType) {
        Object val = getStaticField(name);
        if (val instanceof Integer) {
            return this.setStaticField(name, (int)val + (assignType == TokenType.GROW ? 1 : -1));
        } else if (val instanceof Float) {
            return this.setStaticField(name, (float) val + (assignType == TokenType.GROW ? 1 : -1));
        } else {
            return this.setStaticField(name, (double)val + (assignType == TokenType.GROW ? 1 : -1));
        }
    }

    //TODO move to reference?
    default ScriptedClass getComponentType() {
        return null;
    }

    /**
     * @param type the operation
     * @param other the other type
     * @return the resulting type or {@link VarTypeManager#VOID}, if this operation is not possible (must return {@link VarTypeManager#BOOLEAN} or {@link VarTypeManager#VOID} for comparators)
     * <br><br>API note: it's recommended to call {@code super.checkOperation(...)} due to the given equality check
     */
    @Deprecated
    default ScriptedClass checkOperation(OperationType type, Operand operand, ClassReference other) {
        return type == OperationType.ADDITION && "scripted.lang.String".equals(this.absoluteName()) ?
                VarTypeManager.STRING.get() :
                type.is(TokenTypeCategory.EQUALITY) && other.is(this) ?
                        VarTypeManager.BOOLEAN :
                        VarTypeManager.VOID;
    }

    default Object doOperation(OperationType type, Operand operand, Object self, Object other) {
        String selfS = Interpreter.stringify(self);
        String otherS = Interpreter.stringify(other);
        return type == OperationType.ADDITION && "scripted.lang.String".equals(this.absoluteName()) ?
                operand == Operand.LEFT ? selfS + otherS  : otherS + selfS : null;
    }

    @Contract(pure = true)
    String name();

    @Contract(pure = true)
    String pck(); //stupid keyword :agony:

    @Contract(pure = true)
    default String absoluteName() {
        return pck() + "." + name();
    }

    //TODO branch from runtime class implementations and compile class implementations
    @Contract(pure = true)
    @Nullable
    ClassReference superclass();

    default ClassReference[] interfaces() {
        return superclass() != null ? superclass().get().interfaces() : new ClassReference[0];
    }

    default ClassReference getFieldType(String name) {
        return superclass() != null ? superclass().get().getFieldType(name) : null;
    }

    default boolean hasField(String name) {
        return superclass() != null && superclass().get().hasField(name);
    }

    default Map<String, ? extends ScriptedField> getFields() {
        return superclass() != null ? superclass().get().getFields() : Map.of();
    }

    default boolean is(ScriptedClass other) {
        return other == this;
    }

    default boolean isParentOf(ScriptedClass suspectedChild) {
        if (suspectedChild.is(this) || (!suspectedChild.isInterface() && VarTypeManager.OBJECT.get().is(this))) return true;
        while (suspectedChild != null && suspectedChild.superclass() != null && suspectedChild != VarTypeManager.OBJECT.get() && !suspectedChild.is(this)) {
            suspectedChild = suspectedChild.superclass().get();
        }
        return suspectedChild != null && suspectedChild.is(this);
    }

    default boolean isChildOf(ScriptedClass suspectedParent) {
        return suspectedParent.isInterface() ?
                Arrays.stream(this.interfaces()).anyMatch(reference -> reference.get().isParentOf(suspectedParent)) || this.superclass().get().isChildOf(suspectedParent)  :
                suspectedParent.isParentOf(this);
    }

    //region method

    /**
     * @param signature the signature of the method, without declaring class or return type
     * @return the method for the signature or null if it couldn't be found
     */
    ScriptedCallable getMethod(String signature);

    default boolean hasMethod(String name) {
        return superclass() != null && superclass().get().hasMethod(name);
    }

    AbstractMethodMap getMethods();

    //endregion
    //region annotations

    Annotation[] annotations();

    default Annotation getAnnotation(ClassReference retention) {
        for (Annotation annotation : annotations()) {
            if (retention.is(VarTypeManager.directFlatParse(annotation.getType()))) {
                return annotation;
            }
        }
        return null;
    }

    //endregion
    //region MODIFIERS

    @Contract(pure = true)
    short getModifiers();

    default boolean isInterface() {
        return (getModifiers() & Modifiers.INTERFACE) != 0;
    }

    default boolean isAbstract() {
        return Modifiers.isAbstract(getModifiers());
    }

    default boolean isFinal() {
        return Modifiers.isFinal(getModifiers());
    }

    default boolean isAnnotation() {
        return (getModifiers() & Modifiers.ANNOTATION) != 0;
    }

    default @Nullable Holder.Generics getGenerics() {
        return null;
    }

    boolean isNative();

    default Holder.EnumConstant getEnumConstant(String lexeme) {
        return null;
    }

    //endregion
}