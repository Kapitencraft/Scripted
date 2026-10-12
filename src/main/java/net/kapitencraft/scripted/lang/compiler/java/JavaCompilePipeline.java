package net.kapitencraft.scripted.lang.compiler.java;

import net.kapitencraft.scripted.lang.compiler.Lexer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.compiler.exe.text.HolderParser;
import net.kapitencraft.scripted.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.scripted.lang.compiler.exe.text.TextBasedCompilePipeline;
import net.kapitencraft.scripted.lang.compiler.java.parser.JavaHolderParser;
import net.kapitencraft.scripted.lang.compiler.java.parser.JavaStmtParser;

public class JavaCompilePipeline extends TextBasedCompilePipeline {
    public static final JavaCompilePipeline INSTANCE = new JavaCompilePipeline();

    @Override
    protected Lexer createLexer(String content, ErrorStorage storage) {
        return new JavaLexer(content, storage);
    }

    @Override
    protected HolderParser createHolderParser(ErrorStorage storage, SourceTree tree, CompileSource source) {
        return new JavaHolderParser(storage, tree, source);
    }

    @Override
    protected StmtParser createStmtParser(ErrorStorage storage, SourceTree sourceTree, CompileSource source) {
        return new JavaStmtParser(storage, sourceTree, source);
    }

    @Override
    public String getFileExtension() {
        return "scr";
    }
}
