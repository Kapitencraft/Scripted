package net.kapitencraft.scripted.edit.graphical.holder;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.StmtCodeWidget;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.class_ref.SourceReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;

import java.util.List;

public interface BlocklyOperationHolder {
    ClassReference retType();

    Generics generics();

    StmtCodeWidget body();

    boolean isStatic();

    short modifiers();

    AnnotationObj[] annotations();

    default List<Pair<ClassReference, String>> extractParams() {
        return this.params().stream().map(p -> p.mapFirst(SourceReference::getReference)).toList();
    }

    default ClassReference[] extractThrown() {
        return thrown().stream().map(SourceReference::getReference).toArray(ClassReference[]::new);
    }

    String name();

    List<Pair<SourceReference, String>> params();

    List<SourceReference> thrown();
}
