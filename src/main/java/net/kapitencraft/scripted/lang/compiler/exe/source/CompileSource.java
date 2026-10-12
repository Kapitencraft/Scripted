package net.kapitencraft.scripted.lang.compiler.exe.source;

import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.MethodLookup;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.analyser.FinalsPopulatedAnalyser;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.CompilePipeline;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStage;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.load.ClassLoader;
import net.kapitencraft.scripted.lang.holder.baked.BakedClass;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.scripted.lang.oop.clazz.generated.RuntimeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class CompileSource {
    private final SourceTree.DirectoryNode declaring;
    protected final String name, pck;
    protected final ClassReference reference;
    protected final File file;
    protected final String content;
    protected final ErrorStorage storage;
    protected final VarTypeContainer varTypeContainer;
    protected ClassConstructor holder;
    protected Compiler.ClassBuilder builder;
    protected CacheableClass target;

    public CompileSource(String name, String pck, File file, SourceTree.DirectoryNode declaring) {
        this.name = name;
        this.pck = pck;
        this.file = file;
        try {
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.storage = new ErrorStorage(
                content.split("\n", Integer.MAX_VALUE), //second param required to not skip empty lines
                file.getAbsolutePath().replace(".\\", "") //remove '\.\'
        );
        this.varTypeContainer = new VarTypeContainer();
        this.reference = VarTypeManager.getOrCreateClass(name, pck);
        this.declaring = declaring;
    }

    public CompileSource(String name, String pck, ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser,  SourceTree.DirectoryNode declaring) {
        this.name = name;
        this.pck = pck;
        this.file = null;
        this.content = null; //not necessary with the holder already present
        this.storage = storage;
        this.holder = holder;
        this.varTypeContainer = parser;
        this.reference = VarTypeManager.getOrCreateClass(name, pck);
        this.declaring = declaring;
    }

    public void validate(SourceTree sourceTree) {
        this.varTypeContainer.validate(this.storage);
        this.holder.validate(this.storage);
    }

    public final void cache(SourceTree sourceTree) {
        try {
            Compiler.cache(
                    Compiler.cache,
                    new CacheBuilder(), //MUST under ALL CIRCUMSTANCES be thread-save. create a new object per class
                    target.pck().replace(".", "/"),
                    target,
                    target.name()
            );
        } catch (IOException e) {
            System.err.println("Error saving class '" + target.absoluteName() + "': " + e.getMessage());
        }
    }

    public void applySkeleton(SourceTree sourceTree) {
        reference.setTarget(this.holder.createSkeleton(storage));
    }

    public void analyseSemantics(SourceTree sourceTree) {
        if (builder != null)
            builder.analyse();
    }

    public void finalizeLoad(SourceTree sourceTree) {

        if (builder.superclass() != null) {
            MethodLookup lookup = MethodLookup.createFromClass(builder.superclass().get(), builder.interfaces());
            lookup.checkAbstract(storage, builder.name(), builder.methods());
            if (builder instanceof BakedClass) {
                lookup.checkFinalMethods(storage, builder.methods());
            }
        }
        //TODO get access to final fields
        FinalsPopulatedAnalyser analyser = new FinalsPopulatedAnalyser(this.storage);

        target = builder.build();
        this.holder.target().setTarget((ScriptedClass) target);
    }

    public void publish(SourceTree tree) {
        RuntimeClass runtimeClass = ((CompileClass) this.target).convert();
        this.holder.target().setTarget(runtimeClass);
    }

    public void printErrors() {
        this.storage.printAll();
    }

    public ErrorStorage getErrorInfo() {
        return storage;
    }

    public void optimize(SourceTree sourceTree) {
        this.target.optimize();
    }

    public abstract CompilePipeline<?> getPipeline();

    public <T extends CompileSource> void process(CompileStage activeStage, SourceTree source) {
        ((CompilePipeline<T>) getPipeline()).getExecutor(activeStage).process((T) this, source);
    }

    public String fullName() {
        return this.pck + "." + this.name;
    }

    public String name() {
        return this.name;
    }

    public String pck() {
        return this.pck;
    }

    public SourceTree.DirectoryNode getDeclaring() {
        return declaring;
    }
}
