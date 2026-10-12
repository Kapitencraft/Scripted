package net.kapitencraft.scripted.lang.oop.method;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.attribute.OperationHolder;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.tool.StringReader;
import net.kapitencraft.scripted.lang.tool.Util;
import net.minecraft.util.GsonHelper;

import java.util.List;

public class SkeletonMethod implements ScriptedCallable {
    private final ClassReference[] args;
    private final ClassReference[] thrown;
    private final ClassReference retType;
    private final short modifiers;

    public SkeletonMethod(ClassReference[] args, ClassReference[] thrown, ClassReference retType, short modifiers) {
        this.args = args;
        this.thrown = thrown;
        this.retType = retType;
        this.modifiers = modifiers;
    }

    public static SkeletonMethod create(OperationHolder decl) {
        return create(decl.params(), decl.thrown(), decl.retType(), decl.modifiers());
    }

    private static SkeletonMethod create(List<? extends Pair<SourceReference, String>> params, List<SourceReference> thrown, ClassReference type, short modifiers) {
        return new SkeletonMethod(
                params.stream()
                        .map(Pair::getFirst)
                        .map(SourceReference::getReference)
                        .toArray(ClassReference[]::new),
                thrown.stream()
                        .map(SourceReference::getReference)
                        .toArray(ClassReference[]::new),
                type,
                modifiers
        );
    }

    public static SkeletonMethod create(OperationHolder decl, ClassReference type) {
        return create(decl.params(), decl.thrown(), type, (short) 0);
    }

    public static SkeletonMethod createNative(ClassReference[] args, ClassReference[] thrown, ClassReference retType, short modifiers) {
        return new SkeletonMethod(args, thrown, retType, modifiers);
    }


    public static SkeletonMethod fromJson(JsonObject object) {
        ClassReference retType = VarTypeManager.parseType(new StringReader(GsonHelper.getAsString(object, "returnType")));
        ClassReference[] args = Util.getAsClassReferenceList(object, "params").toArray(ClassReference[]::new);

        ClassReference[] thrown = Util.getAsClassReferenceList(object, "thrown").toArray(ClassReference[]::new);

        short modifiers = object.has("modifiers") ? GsonHelper.getAsShort(object, "modifiers") : 0;
        return new SkeletonMethod(args, thrown, retType, modifiers);
    }

    public static ImmutableMap<String, DataMethodContainer> readFromCache(JsonObject data, String subElementName) {
        ImmutableMap.Builder<String, DataMethodContainer> methods = new ImmutableMap.Builder<>();
        JsonObject methodData = GsonHelper.getAsJsonObject(data, subElementName);
        methodData.asMap().forEach((s, element) -> {
            SkeletonMethod[] methodDeclarations =
                    element.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).map(SkeletonMethod::fromJson)
                            .toArray(SkeletonMethod[]::new);
            methods.put(s, new DataMethodContainer(methodDeclarations));
        });
        return methods.build();
    }

    @Override
    public ClassReference retType() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return args;
    }

    @Override
    public Object call(Object[] arguments) {
        throw new IllegalAccessError("can not call skeleton method");
    }

    @Override
    public boolean isAbstract() {
        return Modifiers.isAbstract(modifiers);
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }

    @Override
    public ClassReference[] thrown() {
        return thrown;
    }
}
