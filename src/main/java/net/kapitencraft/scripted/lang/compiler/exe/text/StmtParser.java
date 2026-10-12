package net.kapitencraft.scripted.lang.compiler.exe.text;

import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.attribute.OperationHolder;
import net.kapitencraft.scripted.lang.holder.oop.generic.Generics;
import net.kapitencraft.scripted.lang.holder.token.Token;

import java.util.List;

public interface StmtParser {

    void pushFallback(ClassReference classReference);
    void popFallback();

    Expr expression();

    Annotation[] parseAnnotations(AnnotationObj[] annotations, VarTypeContainer parser);

    void apply(Token[] body, VarTypeContainer parser);

    void applyStaticMethod(ClassReference retType, Generics generics);

    void applyMethod(ClassReference retType, Generics generics);

    void applyOperation(OperationHolder holder, VarTypeContainer parser);

    List<Stmt> parse();

    void popMethod(Token token);

    Expr[] args();

    Annotation parseAnnotation(AnnotationObj obj, VarTypeContainer parser);

    Expr literalOrReference();
}
