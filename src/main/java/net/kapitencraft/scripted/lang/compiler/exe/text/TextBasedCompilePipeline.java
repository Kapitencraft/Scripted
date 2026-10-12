package net.kapitencraft.scripted.lang.compiler.exe.text;

import net.kapitencraft.scripted.lang.compiler.Lexer;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.CompilePipeline;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStage;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStageExecutor;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class TextBasedCompilePipeline implements CompilePipeline<TextBasedCompilePipeline.Source> {

    private static final Map<CompileStage, CompileStageExecutor<Source>> STAGES = new EnumMap<>(Map.of(
            CompileStage.PARSE_SOURCE, Source::parseSource,
            CompileStage.CREATE_SKELETON, Source::applySkeleton,
            CompileStage.VALIDATE, Source::validate,
            CompileStage.SYNTAX_ANALYSIS, Source::analyseSyntax,
            CompileStage.SEMANTIC_ANALYSIS, Source::analyseSemantics,
            CompileStage.FINALIZE_LOAD, Source::finalizeLoad,
            CompileStage.OPTIMIZE, Source::optimize,
            CompileStage.CACHE, Source::cache,
            CompileStage.PUBLISH, Source::publish
    ));

    @Override
    public Source createSource(File source, String name, String pck, SourceTree.DirectoryNode owner) {
        return new Source(name, pck, source, owner);
    }

    public CompileSource createSource(String name, String pck, ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser, SourceTree.DirectoryNode declaring) {
        return new Source(name, pck, holder, storage, parser, declaring);
    }

    @Override
    public CompileStageExecutor<Source> getExecutor(CompileStage stage) {
        return STAGES.get(stage);
    }

    protected abstract Lexer createLexer(String content, ErrorStorage storage);

    protected abstract HolderParser createHolderParser(ErrorStorage storage, SourceTree tree, CompileSource source);

    protected abstract StmtParser createStmtParser(ErrorStorage storage, SourceTree sourceTree, CompileSource source);

    public class Source extends CompileSource {

        private Source(String name, String pck, File file, SourceTree.DirectoryNode declaring) {
            super(name, pck, file, declaring);
        }

        private Source(String name, String pck, ClassConstructor holder, ErrorStorage storage, VarTypeContainer parser, SourceTree.DirectoryNode declaring) {
            super(name, pck, holder, storage, parser, declaring);
        }

        @Override
        public CompilePipeline<?> getPipeline() {
            return TextBasedCompilePipeline.this;
        }

        public void parseSource(SourceTree sourceTree) {
            if (this.holder != null) return; //only parse source if holder wasn't created
            Lexer javaLexer = createLexer(content, storage);
            List<Token> tokens = javaLexer.scanTokens();

            String fileName = file.getName().replace("." + TextBasedCompilePipeline.this.getFileExtension(), "");
            HolderParser parser = createHolderParser(storage, sourceTree, this);
            parser.apply(tokens.toArray(new Token[0]), varTypeContainer);

            ClassConstructor decl = parser.parseFile(fileName, pck);

            if (decl == null) return;

            holder = decl;
        }

        public void analyseSyntax(SourceTree sourceTree) {
            StmtParser stmtParser = TextBasedCompilePipeline.this.createStmtParser(this.storage, sourceTree, this);

            stmtParser.pushFallback(this.holder.target());
            builder = holder.construct(stmtParser, this.varTypeContainer, this.storage);
            stmtParser.popFallback();
        }

        @Override
        public String toString() {
            return "TextBasedCompilePipelineSource for " + TextBasedCompilePipeline.this.getFileExtension();
        }
    }
}
