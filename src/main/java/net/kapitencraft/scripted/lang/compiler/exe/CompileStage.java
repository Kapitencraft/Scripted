package net.kapitencraft.scripted.lang.compiler.exe;

public enum CompileStage {
    PARSE_SOURCE,
    CREATE_SKELETON,
    VALIDATE,
    SYNTAX_ANALYSIS,
    SEMANTIC_ANALYSIS,
    FINALIZE_LOAD,
    OPTIMIZE,
    CACHE,
    PUBLISH
}