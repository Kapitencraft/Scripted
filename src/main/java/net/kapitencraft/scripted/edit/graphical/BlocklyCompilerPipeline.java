package net.kapitencraft.scripted.edit.graphical;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.kapitencraft.kap_lib.core.helpers.IOHelper;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.StmtCodeWidget;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.CompilePipeline;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStage;
import net.kapitencraft.scripted.lang.compiler.exe.CompileStageExecutor;
import net.kapitencraft.scripted.lang.compiler.exe.source.CompileSource;
import net.kapitencraft.scripted.lang.compiler.exe.source.SourceTree;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.attribute.MethodHolder;
import net.kapitencraft.scripted.lang.holder.oop.attribute.OperationHolder;
import net.kapitencraft.scripted.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.scripted.lang.holder.oop.clazz.ClassHolder;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;
import org.w3c.dom.css.CSS2Properties;

import java.io.File;
import java.util.List;
import java.util.Map;

public class BlocklyCompilerPipeline implements CompilePipeline<BlocklyCompilerPipeline.Source> {
    public static final Codec<List<StmtCodeWidget>> SOURCE_CODEC = StmtCodeWidget.CODEC.listOf();

    public static final BlocklyCompilerPipeline INSTANCE = new BlocklyCompilerPipeline();

    private static final Map<CompileStage, CompileStageExecutor<Source>> STAGES = Map.of(
            CompileStage.PARSE_SOURCE, Source::parseSource,
            CompileStage.CREATE_SKELETON, Source::applySkeleton,
            CompileStage.VALIDATE, Source::validate,
            CompileStage.SYNTAX_ANALYSIS, Source::convert,
            CompileStage.SEMANTIC_ANALYSIS, Source::analyseSemantics,
            CompileStage.FINALIZE_LOAD, Source::finalizeLoad,
            CompileStage.OPTIMIZE, Source::optimize,
            CompileStage.CACHE, Source::cache,
            CompileStage.PUBLISH, Source::publish
    );

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
        private List<StmtCodeWidget> methods;

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

        public void parseSource(SourceTree tree) {
            List<StmtCodeWidget> methods = IOHelper.loadFile(this.file, SOURCE_CODEC, List::of);

            ClassConstructor constructor = new ClassHolder(
                    this.reference,
                    0,
                    new AnnotationObj[0],
                    Generics.EMPTY,
                    this.pck,
                    Token.createNative(this.name),
                    SourceReference.createNative(VarTypeManager.OBJECT),
                    new SourceReference[]{SourceReference.from(Token.createNative("CommandScript"), VarTypeManager.COMMAND_SCRIPT)},
                    new OperationHolder[0],
                    new OperationHolder[]{
                            new MethodHolder(
                                    0, new AnnotationObj[0],
                                    Generics.EMPTY,
                                    SourceReference.createNative(VarTypeManager.VOID.reference()),
                                    Token.createNative("execute"),
                                    Token.createNative(")"),
                                    List.of(
                                            Pair.of(SourceReference.from(Token.createNative("data"), VarTypeManager.COMMAND_DATA), "data")
                                    ),

                            )
                    }
            );

            this.holder = constructor;
        }
    }
}
