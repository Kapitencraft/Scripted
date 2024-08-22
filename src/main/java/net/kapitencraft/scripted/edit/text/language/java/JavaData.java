package net.kapitencraft.scripted.edit.text.language.java;

import net.kapitencraft.scripted.edit.text.language.LanguageData;

public class JavaData extends LanguageData<JavaLanguageProvider, JavaIDE, JavaTokenizer, JavaCompiler> {

    public JavaData() {
        super(new JavaLanguageProvider(), JavaIDE::new, new JavaTokenizer(), JavaCompiler::new);
    }
}
