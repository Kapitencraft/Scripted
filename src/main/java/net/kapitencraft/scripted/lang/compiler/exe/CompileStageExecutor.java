package net.kapitencraft.scripted.lang.compiler.exe;

import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;

public interface CompileStageExecutor<T extends CompileSource> {

    void process(T value, SourceTree source);
}
