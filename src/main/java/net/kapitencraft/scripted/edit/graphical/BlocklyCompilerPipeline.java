package net.kapitencraft.scripted.edit.graphical;

import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.CompilePipeline;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStage;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStageExecutor;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.holder.oop.clazz.ClassConstructor;

import java.io.File;
import java.util.Map;

public class BlocklyCompilerPipeline implements CompilePipeline<BlocklyCompilerPipeline.Source> {
    public static final BlocklyCompilerPipeline INSTANCE = new BlocklyCompilerPipeline();

    private static final Map<CompileStage, CompileStageExecutor<Source>> STAGES = Map.of();

    @Override
    public CompileStageExecutor<Source> getExecutor(CompileStage stage) {
        return STAGES.get(stage);
    }

    @Override
    public Source createSource(File source, String name, String pck, SourceTree.DirectoryNode owner) {
        return new Source(name, pck, source, owner);
    }

    @Override
    public String getFileExtension() {
        return "bscr";
    }

    class Source extends CompileSource {

        public Source(String name, String pck, File file, SourceTree.DirectoryNode declaring) {
            super(name, pck, file, declaring);
        }

        public Source(String name, String pck, ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser, SourceTree.DirectoryNode declaring) {
            super(name, pck, holder, storage, parser, declaring);
        }

        @Override
        public CompilePipeline<?> getPipeline() {
            return BlocklyCompilerPipeline.this;
        }
    }
}
