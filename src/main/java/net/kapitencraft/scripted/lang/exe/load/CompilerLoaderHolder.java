package net.kapitencraft.scripted.lang.exe.load;


import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.lang.compiler.*;
import net.kapitencraft.scripted.lang.compiler.parser.HolderParser;
import net.kapitencraft.scripted.lang.compiler.parser.StmtParser;
import net.kapitencraft.scripted.lang.holder.baked.BakedClass;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.CacheableClass;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class CompilerLoaderHolder extends ClassLoaderHolder<CompilerLoaderHolder> {
    private final String content;
    private final Compiler.ErrorStorage storage;
    private Holder.Class holder;
    private Compiler.ClassBuilder builder;
    private CacheableClass target;
    private final VarTypeParser varTypeParser;

    public CompilerLoaderHolder(File file, String pck) {
        super(file, pck);
        try {
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.storage = new Compiler.ErrorStorage(
                content.split("\n", Integer.MAX_VALUE), //second param required to not skip empty lines
                file.getAbsolutePath().replace(".\\", "") //remove '\.\'
        );
        this.varTypeParser = new VarTypeParser();
    }

    public CompilerLoaderHolder(Holder.Class holder, Compiler.ErrorStorage storage, VarTypeParser parser) {
        super(null, null);
        this.content = null; //not necessary with the holder already present
        this.storage = storage;
        this.holder = holder;
        this.varTypeParser = parser;
    }

    public void parseSource() {
        if (this.holder != null) return; //only parse source if holder wasn't created
        Lexer lexer = new Lexer(content, storage);
        List<Token> tokens = lexer.scanTokens();
        String fileName = file.getName().replace(".scr", "");
        HolderParser parser = new HolderParser(storage);
        parser.apply(tokens.toArray(new Token[0]), varTypeParser);

        Holder.Class decl = parser.parseFile(fileName);

        if (decl == null) return;

        String path = file.getParentFile().getPath().substring(10).replace(".scr", "");
        String pck = path.replace('\\', '.');
        String declPck = decl.pck();
        if (!Objects.equals(declPck, pck)) {
            storage.errorF(
                    tokens.getFirst(),
                    "package path '%s' does not match file path '%s'", declPck, pck);
        }

        holder = decl;
    }

    public void construct() {
        if (!checkHolderCreated()) return;
        StmtParser stmtParser = new StmtParser(this.storage);

        builder = holder.construct(stmtParser, this.varTypeParser, this.storage);
    }

    public void cache(File cacheLoc) {
        try {
            Compiler.cache(
                    cacheLoc,
                    new CacheBuilder(),
                    target.pck().replace(".", "/"),
                    target,
                    target.name()
            );
        } catch (IOException e) {
            Scripted.LOGGER.warn("Error saving class '{}': {}", target.absoluteName(), e.getMessage());
        }
    }

    public boolean checkHolderCreated() {
        return holder != null && !storage.hadError();
    }

    @Override
    public void applySkeleton() {
        if (checkHolderCreated()) this.holder.applySkeleton(storage);
    }

    public void finalizeLoad() {
        if (!checkHolderCreated()) return;

        if (builder.superclass() != null) {
            MethodLookup lookup = MethodLookup.createFromClass(builder.superclass().get(), builder.interfaces());
            lookup.checkAbstract(storage, builder.name(), builder.methods());
            if (builder instanceof BakedClass) {
                lookup.checkFinal(storage, builder.methods());
            }
        }
        target = builder.build();
        this.holder.target().setTarget((ScriptedClass) target);
    }

    public void validate() {
        if (!checkHolderCreated()) return;
        this.varTypeParser.validate(this.storage);
        this.holder.validate(this.storage);
    }

    public void printErrors(ServerPlayer errorSink) {
        this.storage.printAll(errorSink);
    }
}
