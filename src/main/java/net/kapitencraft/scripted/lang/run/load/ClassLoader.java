package net.kapitencraft.scripted.lang.run.load;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.bytecode.exe.Disassembler;
import net.kapitencraft.scripted.lang.bytecode.exe.VirtualMachine;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.Package;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.run.VarTypeManager;
import net.kapitencraft.scripted.lang.run.test.TestLoader;
import net.minecraft.util.GsonHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClassLoader {

    public static final File cacheLoc = new File("./run/cache");

    @SuppressWarnings("AssignmentUsedAsCondition")
    public static void main(String[] args) throws IOException {
        loadClasses();
        System.out.println("Loading complete.");
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        String line = "";
        boolean profiling = false;
        while (!"exit".equals(line)) {
            if (line != null) {
                if (line.startsWith("run ")) {
                    String data = line.substring(4);
                    String classRef;
                    if (data.contains(" ")) classRef = data.substring(0, data.indexOf(' '));
                    else classRef = data;
                    ClassReference target = VarTypeManager.getClassForName(classRef);
                    if (target == null) System.err.println("unable to find class for id '" + classRef + "'");
                    else {
                        if (data.contains(" ")) data = data.substring(data.indexOf(' ') + 1);
                        else data = "";
                        VirtualMachine.runMainMethod(target.get(), data, profiling, true);
                    }
                } else if (line.startsWith("profiler")) {
                    if (line.length() < 9) {
                        System.err.println("missing parameter for profiler command. allowed values:");
                        System.err.println("\tstart\n\tend\n\ttoggle");
                    }
                    switch (line.substring(9)) {
                        case "start" -> {
                            profiling = true;
                            System.out.println("started profiler");
                        }
                        case "end" -> {
                            profiling = false;
                            System.out.println("stopped profiler");
                        }
                        case "toggle" -> {
                            profiling = !profiling;
                            System.out.println("toggled profiler. now: " + profiling);
                        }
                        default -> System.err.println("unknown profiler operation : \"" + line.substring(10) + "\"");
                    }
                } else if (line.startsWith("debug")) {
                    if (VirtualMachine.DEBUG = !VirtualMachine.DEBUG) {
                        System.out.println("enabled debug");
                    } else {
                        System.out.println("disabled debug");
                    }
                } else if (line.startsWith("test")) {
                    TestLoader.run();
                } else if (line.startsWith("help")) {
                    System.out.println("== HELP ==");
                    System.out.println("\texit                        - Ends the Program");
                    System.out.println("\tprofiler [start|end|toggle] - Runs the appropriate profiler action");
                    System.out.println("\trun <ClassPath>             - Executes the 'main(String[])' method of that class");
                    System.out.println("\tdebug                       - toggles debug log for the VM");
                    System.out.println("\ttest                        - Runs the benchmark test");
                    System.out.println("\tlist                        - Lists all methods of the given class and their content");
                } else if (line.startsWith("list ")) {
                    String classRef = line.substring(5);
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
                else if (!line.isEmpty()) System.err.println("unknown command: \"" + line + "\"");
            }
            line = scanner.next();
        }
    }

    public static void loadClasses() {
        PackageHolder<VMLoaderHolder> pckSkeleton = load(cacheLoc, ".scrc", VMLoaderHolder::new);
        useClasses(pckSkeleton, (classes, pck) -> classes.forEach((name, vmLoaderHolder) -> loadHolderReference(pck, vmLoaderHolder)));
        generateSkeletons(pckSkeleton);
        generateClasses(pckSkeleton);
    }

    private static void loadHolderReference(Package pck, VMLoaderHolder holder) {
        pck.addClass(holder.name, holder.reference);
    }

    public static <T extends ClassLoaderHolder<T>> PackageHolder<T> load(File fileLoc, String end, Function<File, T> constructor) {
        PackageHolder<T> root = new PackageHolder<>();
        List<Pair<File, PackageHolder<T>>> pckLoader = new ArrayList<>();
        pckLoader.add(Pair.of(fileLoc, root));
        while (!pckLoader.isEmpty()) {
            Pair<File, PackageHolder<T>> pck = pckLoader.get(0);
            File file = pck.getFirst();
            PackageHolder<T> holder = pck.getSecond();
            File[] files = file.listFiles();
            if (files == null) {
                pckLoader.remove(0);
                continue;
            }
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    PackageHolder<T> child = new PackageHolder<>();
                    holder.packages.put(file1.getName(), child);
                    pckLoader.add(Pair.of(file1, child));
                } else {
                    String name = file1.getName().replace(end, "");
                    holder.classes.put(name, constructor.apply(file1));
                }
            }
            pckLoader.remove(0);
        }
        return root;
    }

    public static void generateSkeletons(PackageHolder<?> root) {
        useClasses(root, (classes, pck) -> classes.forEach((s, classLoaderHolder) -> classLoaderHolder.applySkeleton()));
    }

    public static void generateClasses(PackageHolder<VMLoaderHolder> root) {
        useClasses(root, (classes, pck) -> classes.forEach((name, holder1) -> pck.addNullableClass(name, holder1.loadClass())));
    }


    //how should I name this...
    public static <T extends ClassLoaderHolder<T>> void useClasses(PackageHolder<T> root, BiConsumer<Map<String, T>, Package> consumer) {
        List<Pair<PackageHolder<T>, Package>> packageData = new ArrayList<>();
        packageData.add(Pair.of(root, VarTypeManager.rootPackage()));
        while (!packageData.isEmpty()) {
            Pair<PackageHolder<T>, Package> data = packageData.get(0);
            PackageHolder<T> holder = data.getFirst();
            Package pck = data.getSecond();
            consumer.accept(holder.classes, pck);
            holder.packages.forEach((name, holder1) ->
                    packageData.add(Pair.of(holder1, pck.getOrCreatePackage(name))) //adding all packages back to the queue
            );
            packageData.remove(0);
        }
    }

    public static <T extends ClassLoaderHolder<T>> void useHolders(PackageHolder<T> root, Consumer<T> consumer, Executor executor) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<Pair<PackageHolder<T>, Package>> packageData = new ArrayList<>();
        packageData.add(Pair.of(root, VarTypeManager.rootPackage()));
        while (!packageData.isEmpty()) {
            Pair<PackageHolder<T>, Package> data = packageData.get(0);
            PackageHolder<T> holder = data.getFirst();
            Package pck = data.getSecond();
            holder.classes.forEach((n, o) ->
                    futures.add(CompletableFuture.runAsync(() -> consumer.accept(o), executor))
            );
            holder.packages.forEach((name, holder1) ->
                    packageData.add(Pair.of(holder1, pck.getOrCreatePackage(name))) //adding all packages back to the queue
            );
            packageData.remove(0);
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }

    public static ClassReference loadClassReference(JsonObject object, String elementName) {
        return VarTypeManager.getClassOrError(GsonHelper.getAsString(object, elementName));
    }

    public static String[] loadInterfaces(JsonObject data) {
        return GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
    }

    public static class PackageHolder<T extends ClassLoaderHolder<T>> {
        private final Map<String, PackageHolder<T>> packages = new HashMap<>();
        private final Map<String, T> classes = new HashMap<>();

        public void add(String pck, String name, T val) {
            String[] packages = pck.split("\\.");
            PackageHolder<T> holder = this.packages.get(packages[0]);
            for (int i = 1; i < packages.length; i++) {
                holder = holder.getOrCreate(packages[i]);
            }
            holder.classes.put(name, val);
        }

        public PackageHolder<T> getOrCreate(String name) {
            return packages.computeIfAbsent(name, n -> new PackageHolder<>());
        }

        public void forEach(Consumer<T> sink) {
            classes.values().forEach(sink);
            packages.values().forEach(h -> h.forEach(sink));
        }
    }

    public static String pck(File file) {
        String path = file.getPath().replace(cacheLoc.getPath(), "").replace(".scrc", "");
        List<String> pckData = new ArrayList<>(List.of(path.split("\\\\")));
        pckData = pckData.subList(1, pckData.size()-1);
        return String.join(".", pckData);
    }
}
