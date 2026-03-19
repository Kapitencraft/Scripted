package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.code.ExprCodeVisitor;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ExprCodeWidget extends CodeWidget {
    Codec<ExprCodeWidget> CODEC = Type.CODEC.dispatch(ExprCodeWidget::getType, Type::getEntryCodec);

    @NotNull Type getType();

    void render(GuiGraphics graphics, Font font, int renderX, int renderY);

    int getWidth(Font font);

    int getHeight();

    ExprCodeWidget copy();

    //TODO convert back to code representation before saving
    //lambda necessary to ensure load order doesn't create cycle
    enum Type implements StringRepresentable, ExprCodeVisitor {
        PARAM(() -> ParamWidget.CODEC, ParamWidget::parse),
        EXPR(() -> MethodInvokeWidget.CODEC, MethodInvokeWidget::parse),
        GET_VAR(() -> GetVarWidget.CODEC, GetVarWidget::parse),
        BINARY(() -> BinaryOperationWidget.CODEC, BinaryOperationWidget::parse),
        SELECT_BLOCK(() -> BlockSelectWidget.CODEC, BlockSelectWidget::parse);

        public static final EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final Supplier<MapCodec<? extends ExprCodeWidget>> entryCodec;
        private final Function<ExprCodeWidget, Expr> parser;

        Type(Supplier<MapCodec<? extends ExprCodeWidget>> entryCodec, Function<ExprCodeWidget, Expr> parser, Function<Expr, ExprCodeVisitor> decoder) {
            this.entryCodec = entryCodec;
            this.parser = parser;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }

        public MapCodec<? extends ExprCodeWidget> getEntryCodec() {
            return entryCodec.get();
        }

        @Override
        public Expr parse(ExprCodeWidget widget) {
            return null;
        }

        @Override
        public ExprCodeWidget decode(Expr expr) {
            return null;
        }
    }

    static int getHeightFromArgs(Map<String, ExprCodeWidget> widgets) {
        return getHeightFromEntries(widgets.values());
    }

    static int getHeightFromEntries(Collection<ExprCodeWidget> widgets) {
        return widgets.stream().mapToInt(ExprCodeWidget::getHeight).max().orElse(0);
    }

    static int getWidthFromList(Font font, List<ExprCodeWidget> widgets) {
        return widgets.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    interface Builder<T extends ExprCodeWidget> {
        T build();
    }
}
