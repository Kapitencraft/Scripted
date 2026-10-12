package net.kapitencraft.scripted.lang.compiler.exe;

import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;

import java.io.File;

public interface CompilePipeline<T extends CompileSource> {

    CompileStageExecutor<T> getExecutor(CompileStage stage);

    T createSource(File source, String name, String pck, SourceTree.DirectoryNode owner);

    String getFileExtension();
}
