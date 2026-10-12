package net.kapitencraft.scripted.lang.exe.load;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.lang.PackageHolder;
import net.kapitencraft.scripted.lang.exe.Disassembler;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.Package;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.minecraft.util.GsonHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClassLoader {

    public static void load(File data) throws IOException {
        loadClasses(data);
        Scripted.LOGGER.info("Loading complete.");
    }

    public static void list(String classRef) {
        if ("$all".equals(classRef)) {
            VarTypeManager.listFlat();
        } else {
            ClassReference target = VarTypeManager.getClassForName(classRef);
            if (target == null) System.err.println("unable to find class for id '" + classRef + "'");
            else {
                ScriptedClass scriptedClass = target.get();
                System.out.println("==== Info ====");
                System.out.println("Name:    " + scriptedClass.name());
                System.out.println("Package: " + scriptedClass.pck());
                System.out.println("\n=== Methods ===");
                Map<String, DataMethodContainer> methods = scriptedClass.getMethods().asMap();
                methods.forEach((string, dataMethodContainer) -> {
                    for (ScriptedCallable method : dataMethodContainer.methods()) {
                        String name = string + "(" + VarTypeManager.getArgsSignature(method.argTypes()) + ")" + VarTypeManager.getClassName(method.retType().get());
                        if (method.isNative()) {
                            System.out.println("== " + name + " ==");
                            System.out.println("<Native>");
                        } else {
                            Disassembler.disassemble(method.getChunk(), name);
                        }
                        System.out.println();
                    }
                });
                System.out.println("==== Info End ====");
            }
        }
    }

    public static void loadClasses(File data) {
        PackageHolder<VMLoaderHolder> pckSkeleton = PackageHolder.load(data, ".scrc", VMLoaderHolder::new);
        pckSkeleton.useClasses((classes, pck) -> classes.forEach((name, vmLoaderHolder) -> loadHolderReference(pck, vmLoaderHolder)), VarTypeManager.rootPackage());
        generateSkeletons(pckSkeleton);
        generateClasses(pckSkeleton);
    }

    private static void loadHolderReference(Package pck, VMLoaderHolder holder) {
        pck.addClass(holder.name, holder.reference);
    }

    public static void generateSkeletons(PackageHolder<?> root) {
        root.useClasses((classes, pck) -> classes.forEach((s, classLoaderHolder) -> classLoaderHolder.applySkeleton()), VarTypeManager.rootPackage());
    }

    public static void generateClasses(PackageHolder<VMLoaderHolder> root) {
        record Entry(VMLoaderHolder holder, String name, Package pck) {
        }

        List<Entry> entries = new ArrayList<>();

        root.useClasses((classes, pck) -> classes.forEach((name, holder1) -> entries.add(new Entry(holder1, name, pck))), VarTypeManager.rootPackage());

        //elements are sorted so that the first elements have only native or no parent to ensure classes
        // with in-code parents loading after their parent and its methods
        Comparator<Entry> sortFunction = (o1, o2) -> {
            ScriptedClass o1Class = o1.holder.reference.get();
            ScriptedClass o2Class = o2.holder.reference.get();
            return o1Class.isChildOf(o2Class) ? 1 : o2Class.isChildOf(o1Class) ? -1 : o1.name.compareTo(o2.name);
        };

        Entry[] values = entries.toArray(new Entry[0]);
        Arrays.sort(values, sortFunction);

        for (Entry entry : values) {
            entry.pck.addNullableClass(entry.name, entry.holder.loadClass());
        }
    }

    public static ClassReference loadClassReference(JsonObject object, String elementName) {
        return VarTypeManager.getClassOrError(GsonHelper.getAsString(object, elementName));
    }

    public static String[] loadInterfaces(JsonObject data) {
        return GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
    }
}
