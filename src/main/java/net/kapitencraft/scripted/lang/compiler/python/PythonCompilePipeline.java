package net.kapitencraft.scripted.lang.compiler.python;

import net.kapitencraft.scripted.lang.compiler.Lexer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.compiler.exe.text.HolderParser;
import net.kapitencraft.scripted.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.scripted.lang.compiler.exe.text.TextBasedCompilePipeline;
import net.kapitencraft.scripted.lang.compiler.python.parser.PythonHolderParser;
import net.kapitencraft.scripted.lang.compiler.python.parser.PythonStmtParser;

public class PythonCompilePipeline extends TextBasedCompilePipeline {
    public static final PythonCompilePipeline INSTANCE = new PythonCompilePipeline();

    @Override
    public String getFileExtension() {
        return "pscr";
    }

    @Override
    protected Lexer createLexer(String content, ErrorStorage storage) {
        return new PythonLexer(content, storage);
    }

    @Override
    protected HolderParser createHolderParser(ErrorStorage storage, SourceTree tree, CompileSource source) {
        return new PythonHolderParser(storage, tree, source);
    }

    @Override
    protected StmtParser createStmtParser(ErrorStorage storage, SourceTree sourceTree, CompileSource source) {
        return new PythonStmtParser(storage, sourceTree, source);
    }
}
