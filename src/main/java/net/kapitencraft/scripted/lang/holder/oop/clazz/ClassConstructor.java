package net.kapitencraft.scripted.lang.holder.oop.clazz;

import com.mojang.datafixers.util.Pair;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.VarTypeContainer;
import net.kapitencraft.scripted.lang.compiler.error.ErrorStorage;
import net.kapitencraft.scripted.lang.compiler.exe.text.StmtParser;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.oop.AnnotationObj;
import net.kapitencraft.scripted.lang.holder.oop.Validatable;
import net.kapitencraft.scripted.lang.holder.oop.attribute.FieldHolder;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.method.CompileCallable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface ClassConstructor extends Validatable {

    AnnotationObj[] annotations();

    ClassReference target();

    Compiler.ClassBuilder construct(StmtParser javaStmtParser, VarTypeContainer parser, ErrorStorage logger);

    ScriptedClass createSkeleton(ErrorStorage logger);

    Token name();

    String pck();

    default @NotNull Expr getFieldBody(StmtParser stmtParser, VarTypeContainer parser, FieldHolder fieldHolder, List<Stmt> statics) {
        stmtParser.apply(fieldHolder.body(), parser);
        Expr initializer = stmtParser.expression();
        if (Modifiers.isStatic(fieldHolder.modifiers())) {
            Stmt.Expression stmt1 = new Stmt.Expression();
            {
                Expr.StaticSet staticSet = new Expr.StaticSet();
                staticSet.target = target();
                staticSet.name = fieldHolder.name();
                staticSet.value = initializer;
                staticSet.assignType = fieldHolder.assign();
                staticSet.executor = target();
                stmt1.expression = staticSet;
            }
            statics.add(stmt1);
        }
        return initializer;
    }

    default void checkFinalsPopulated(List<Stmt> body, List<String> finalFields) {

    }

    default Annotation[] parseAnnotations(StmtParser stmtParser, VarTypeContainer parser) {
        List<Annotation> annotations = new ArrayList<>();
        for (AnnotationObj obj : this.annotations()) {
            annotations.add(stmtParser.parseAnnotation(obj, parser));
        }
        return annotations.toArray(Annotation[]::new);
    }

    static void addClinit(List<Stmt> statics, List<Pair<Token, CompileCallable>> methods) {
        Stmt.Return aReturn = new Stmt.Return();
        aReturn.keyword = Token.createNative("return");
        statics.add(aReturn);
        methods.add(Pair.of( //add <clinit> method
                Token.createNative("<clinit>"),
                new CompileCallable(
                        VarTypeManager.VOID.reference(),
                        List.of(),
                        new ClassReference[0],
                        statics,
                        Modifiers.pack(true, true, false),
                        new Annotation[0]
                )
        ));
    }
}
