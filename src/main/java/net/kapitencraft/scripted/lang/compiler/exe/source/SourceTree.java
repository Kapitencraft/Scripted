package net.kapitencraft.scripted.lang.compiler.exe.source;

import com.google.common.collect.ImmutableList;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.exe.CompilePipeline;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStage;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SourceTree {
    private final DirectoryNode root = new DirectoryNode();
    private final Map<String, CompileSource> sources = new HashMap<>();

    public void addSource(DirectoryNode owner, String name, CompileSource source) {
        owner.addSource(name, source);

        sources.put(source.fullName(), source);
    }

    public boolean hasSource(String name) {
        return sources.containsKey(name);
    }

    public boolean isEmpty() {
        return sources.isEmpty();
    }

    public void forEach(Consumer<CompileSource> consumer) {
        sources.values().forEach(consumer);
    }

    public CompileSource getEntry(String target) {
        return this.sources.get(target);
    }

    public static class DirectoryNode {
        private final Map<String, DirectoryNode> children;
        private final Map<String, CompileSource> sources;

        private DirectoryNode() {
            children = new HashMap<>();
            sources = new HashMap<>();
        }

        public DirectoryNode addChild(String name) {
            DirectoryNode node = new DirectoryNode();
            this.children.put(name, node);
            return node;
        }

        public void addSource(String name, CompileSource source) {
            this.sources.put(name, source);
        }
    }

    public static SourceTree load(File fileLoc) {
        SourceTree tree = new SourceTree();
        DirectoryNode root = tree.root;
        loadRecursive(fileLoc, "", tree, root);
        return tree;
    }

    private static void loadRecursive(File root, String path, SourceTree tree, DirectoryNode holder) {
        File[] files = root.listFiles();
        if (files == null) {
            return;
        }
        for (File file1 : files) {
            String name = file1.getName();
            if (file1.isDirectory()) {

                loadRecursive(file1, path.isEmpty() ? name : path + "." + name, tree, holder.addChild(name));
            } else {
                String[] split = name.split("\\.");
                if (split.length > 2) {
                    System.err.printf("found source with illegal name: '%s'\n", name);
                    continue;
                    //error
                }
                String fileExtension = split[1];
                CompileSource processor = null;
                for (CompilePipeline<?> pipeline : Compiler.PIPELINES) {
                    if (fileExtension.equals(pipeline.getFileExtension())) {
                        processor = pipeline.createSource(file1, split[0], path, holder);
                    }
                }
                if (processor == null) {
                    System.err.printf("found source with unknown extension: '%s'\n", fileExtension);
                    continue;
                }
                tree.sources.put(path + "." + split[0], processor);
                holder.addSource(name, processor);
            }
        }
    }

    public void execute(CompileStage stage, Executor executor, boolean logInfo) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Collection<CompileSource> sources = ImmutableList.copyOf(this.sources.values()); //copy to create different reference
        AtomicInteger completed = new AtomicInteger(0);
        int total = sources.size();
        for (CompileSource source : sources) {
            if (!source.getErrorInfo().hadError())
                futures.add(CompletableFuture.runAsync(() -> source.process(stage, this), executor)
                        .whenComplete((v, ex) -> {
                            if (ex != null) {
                                System.err.printf("error in thread: %s: %s\n", source, ex.getMessage());
                            }
                            int done = completed.incrementAndGet();
                            if (logInfo)
                                printProgress(done, total);
                        }));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        if (logInfo) {
            printProgress(total, total);
            System.out.println();
        }
    }

    static void printProgress(int done, int total) {
        int width = 40; // bar width
        int progress = (int) ((done / (double) total) * width);

        String bar = "[" +
                "=".repeat(progress) +
                " ".repeat(width - progress) +
                "]";

        int percent = (int) ((done / (double) total) * 100);

        System.out.print("\r" + bar + " " + percent + "% (" + done + "/" + total + ")");
    }
}
