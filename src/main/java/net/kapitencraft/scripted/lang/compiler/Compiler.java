package net.kapitencraft.scripted.lang.compiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.analyser.LocationAnalyser;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.run.load.ClassLoader;
import net.kapitencraft.scripted.lang.run.load.CompilerLoaderHolder;
import net.kapitencraft.scripted.lang.tool.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Compiler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static int errorCount = 0;
    private static ClassLoader.PackageHolder<CompilerLoaderHolder> compileData;
    private static final List<ClassRegister> registers = new ArrayList<>();
    private static Stage activeStage;

    public static void register(CompilerLoaderHolder holder, String pck, @Nullable String name) {
        compileData.add(pck, name, holder);
    }

    public static void dispatch(CompilerLoaderHolder holder) {
        for (int i = 1; i <= activeStage.ordinal(); i++) {
            Stage.values()[i].action.accept(holder);
        }
    }

    public static void queueRegister(Holder.Class aClass, ErrorStorage errorStorage, VarTypeParser parser, @Nullable String namePrefix) {
        String name = aClass.name().lexeme();
        ClassRegister e = ClassRegister.create(aClass, errorStorage, parser, name);
        registers.add(e);
        Compiler.dispatch(e.holder);
    }

    private record ClassRegister(CompilerLoaderHolder holder, String pck, @Nullable String name) {
        public static ClassRegister create(Holder.Class entry, ErrorStorage logger, VarTypeParser parser, @Nullable String name) {
            return new ClassRegister(new CompilerLoaderHolder(entry, logger, parser), entry.pck(), name);
        }

        private void register() {
            Compiler.register(holder, pck, name);
        }
    }

    public static void main(String[] args) {
        File root = new File("./run/src");
        File cache = ClassLoader.cacheLoc;

        System.out.println("Compiling...");

        compileData = ClassLoader.load(root, ".scr", CompilerLoaderHolder::new);

        ExecutorService executor = Executors.newFixedThreadPool(10, new CompilerThreadFactory());
        for (Stage stage : Stage.values()) {
            registers.forEach(ClassRegister::register);
            registers.clear();
            activeStage = stage;
            System.out.printf("executing step %s\n", stage);

            if (stage == Stage.CACHING && cache.exists())
                Util.delete(cache);

            ClassLoader.useHolders(compileData, stage.action, executor);

            if (errorCount > 0) {
                printErrors(compileData);

                if (errorCount > 100) {
                    System.err.println("only showing the first 100 errors out of " + errorCount + " total");
                } else System.err.println(errorCount + " errors");
                System.exit(65);
            }
        }
        executor.shutdownNow();
    }

    /**
     * thread factory for more reasonable names
     */
    private static class CompilerThreadFactory implements ThreadFactory {
        AtomicInteger poolNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "CompilerThread#" + poolNumber.getAndIncrement());
        }
    }

    private static void printErrors(ClassLoader.PackageHolder<CompilerLoaderHolder> compileData) {
        compileData.forEach(CompilerLoaderHolder::printErrors);
    }

    public interface ClassBuilder {

        CacheableClass build();

        ClassReference superclass();

        Token name();

        Pair<Token, CompileCallable>[] methods();

        ClassReference[] interfaces();
    }

    public static void cache(File cacheBase, CacheBuilder builder, String path, CacheableClass target, String name) throws IOException {
        JsonObject object = builder.cacheClass(target);
        File cacheTarget = new File(cacheBase, path + "/" + name + ".scrc");
        if (!cacheTarget.exists()) {
            cacheTarget.getParentFile().mkdirs();
            cacheTarget.createNewFile();
        }
        FileWriter writer = new FileWriter(cacheTarget);
        writer.write(GSON.toJson(object));
        writer.close();
    }

    public static class ErrorStorage {
        private final String[] lines;
        private final String fileLoc;
        private final LocationAnalyser finder;
        private final List<Message> messages = new ArrayList<>();

        public ErrorStorage(String[] lines, String fileLoc) {
            this.lines = lines;
            this.fileLoc = fileLoc;
            finder = new LocationAnalyser();
        }

        public void printAll() {
            for (Message msg : messages) {
                msg.print(lines, fileLoc);
            }
        }

        private interface Message {

            void print(String[] lines, String fileLoc);
        }

        private record Error(int lineIndex, int lineStartIndex, String msg, String line) implements Message {


            @Override
            public void print(String[] lines, String fileLoc) {
                Compiler.error(lineIndex, lineStartIndex, msg, fileLoc, line);
            }
        }

        private record Warn(int lineIndex, int lineStartIndex, String msg) implements Message {

            @Override
            public void print(String[] lines, String fileLoc) {
                Compiler.warn(lineIndex, lineStartIndex, msg, fileLoc, lines[lineIndex]);
            }
        }

        public void error(Token loc, String msg) {
            error(loc.line(), loc.lineStartIndex(), msg);
        }

        public void errorF(Token loc, String format, Object... args) {
            error(loc, String.format(format, args));
        }

        public void error(int lineIndex, int lineStartIndex, String msg) {
            if (errorCount++ < 100)
                messages.add(new Error(lineIndex, lineStartIndex, msg, lines[lineIndex - 1]));
        }

        public void error(Stmt loc, String msg) {
            error(finder.find(loc), msg);
        }

        public void error(Expr loc, String msg) {
            error(finder.find(loc), msg);
        }

        public void logError(String s) {
            System.err.println(s);
        }

        public void warn(int lineIndex, int lineStartIndex, String msg) {
            this.messages.add(new Warn(lineIndex, lineStartIndex, msg));
        }

        public void warn(Token loc, String msg) {
            warn(loc.line(), loc.lineStartIndex(), msg);
        }

        public void warn(Stmt loc, String msg) {
            warn(finder.find(loc), msg);
        }

        @Override
        public String toString() {
            return "ErrorStorage for '" + fileLoc + "' (errorCount: " + errorCount + ")";
        }

        public boolean hadError() {
            return errorCount > 0;
        }
    }

    public static void error(int lineIndex, int lineStartIndex, String msg, String fileId, String line) {
        report(System.err, lineIndex, msg, fileId, lineStartIndex, line);
    }

    public static void warn(int lineIndex, int lineStartIndex, String msg, String filedId, String line) {
        System.out.print("\u001B[33m"); //set output color to yellow
        report(System.out, lineIndex, msg, filedId, lineStartIndex, line);
        System.out.print("\u001B[0m"); //reset output color
    }

    public static void report(PrintStream target, int lineIndex, String message, String fileId, int startIndex, String line) {
        target.print(fileId);
        target.print(":");
        target.print(lineIndex);
        target.print(": ");
        target.println(message);

        target.println(line);
        target.println(" ".repeat(startIndex) + "^");
    }

    public enum Stage {
        PARSE_SOURCE(CompilerLoaderHolder::parseSource),
        CREATE_SKELETON(CompilerLoaderHolder::applySkeleton),
        VALIDATE(CompilerLoaderHolder::validate),
        CONSTRUCT(CompilerLoaderHolder::construct),
        FINALIZE_LOAD(CompilerLoaderHolder::finalizeLoad),
        CACHING(CompilerLoaderHolder::cache);

        private final Consumer<CompilerLoaderHolder> action;

        Stage(Consumer<CompilerLoaderHolder> action) {
            this.action = action;
        }
    }
}