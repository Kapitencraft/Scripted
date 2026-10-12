package net.kapitencraft.scripted.lang.compiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.edit.graphical.BlocklyCompilerPipeline;
import net.kapitencraft.scripted.lang.compiler.analyser.LocationAnalyser;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.CompilePipeline;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStage;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.compiler.java.JavaCompilePipeline;
import net.kapitencraft.scripted.lang.compiler.python.PythonCompilePipeline;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import net.kapitencraft.scripted.lang.tool.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Compiler {
    public static final File ROOT = new File("./run/src");
    private static final Logger LOGGER = LoggerFactory.getLogger("Compiler");

    public static File src, cache;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final LocationAnalyser LOCATION_ANALYSER = new LocationAnalyser();

    public static final List<CompilePipeline<?>> PIPELINES = List.of(
            JavaCompilePipeline.INSTANCE,
            PythonCompilePipeline.INSTANCE,
            BlocklyCompilerPipeline.INSTANCE
    );

    public static boolean optimize = true;
    public static File source;
    private static SourceTree compileData;
    private static CompileStage activeStage;

    public static void dispatch(CompileSource holder) {
        for (int i = CompileStage.CREATE_SKELETON.ordinal(); i <= activeStage.ordinal(); i++) {
            holder.process(CompileStage.values()[i], compileData);
        }
    }

    public static SourceTree compile(boolean logInfo, boolean failFast, File data, CommandSourceStack errorSink) {
        src = new File(data, "src");
        cache = new File(data, "cache");

        compileData = SourceTree.load(src);

        if (compileData.isEmpty()) {
            if (logInfo)
                LOGGER.info("no source found. returning");
            errorSink.sendSystemMessage(Component.literal("no source found!"));
            return compileData;
        }
        LOGGER.debug("launching compile executor...");
        ExecutorService executor = Executors.newFixedThreadPool(10, new CompilerThreadFactory());
        ErrorStorage.overallErrorCount = 0;
        try {
            for (CompileStage stage : CompileStage.values()) {
                if (stage == CompileStage.CACHE && cache == null) {
                    if (logInfo)
                        LOGGER.info("Skipping step CACHING as there is no cache root provided");
                    continue;
                }

                activeStage = stage;
                if (logInfo)
                    LOGGER.info("executing step {}", stage);

                if (stage == CompileStage.CACHE && cache.exists())
                    Util.delete(cache);

                compileData.execute(stage, executor, logInfo);

                if (failFast) {
                    if (ErrorStorage.overallErrorCount > 0) {
                        compileData.forEach(CompileSource::printErrors);

                        if (logInfo) {
                            if (ErrorStorage.overallErrorCount > 100) {
                                LOGGER.error("only showing the first 100 errors out of {} total", ErrorStorage.overallErrorCount);
                            } else LOGGER.error("{} errors", ErrorStorage.overallErrorCount);
                        }
                    }
                }
            }
        } finally {
            LOGGER.debug("stopping compile executor...");
            executor.shutdownNow();
        }
        if (ErrorStorage.overallErrorCount < 1)
            errorSink.sendSystemMessage(Component.literal("successfully compiled").withStyle(ChatFormatting.GREEN));
        else
            errorSink.sendFailure(Component.literal("failed to compile with " + ErrorStorage.overallErrorCount + " errors"));
        return compileData;
    }

    public static SourceTree getCompileData() {
        return compileData;
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

    public interface ClassBuilder {

        CacheableClass build();

        ClassReference superclass();

        Token name();

        Pair<Token, CompileCallable>[] methods();

        ClassReference[] interfaces();

        void analyse();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
}