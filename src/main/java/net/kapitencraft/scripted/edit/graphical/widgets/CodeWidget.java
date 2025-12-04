package net.kapitencraft.scripted.edit.graphical.widgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.widgets.block.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface CodeWidget extends Removable {
    Codec<CodeWidget> CODEC = Type.CODEC.dispatch(CodeWidget::getType, Type::getEntryCodec);

    Type getType();

    void render(GuiGraphics graphics, Font font, int renderX, int renderY);

    int getWidth(Font font);

    int getHeight();

    //TODO convert back to code representation before saving
    //lambda necessary to ensure load order doesn't create cycle
    enum Type implements StringRepresentable {
        HEAD(() -> HeadWidget.CODEC),
        TEXT(() -> TextWidget.CODEC),
        WHILE_LOOP(() ->  WhileLoopWidget.CODEC),
        IF(() -> IfWidget.CODEC),
        BODY(() -> VarModWidget.CODEC),
        EXPR(() -> ExprWidget.CODEC),
        METHOD_STMT(() -> MethodStmtWidget.CODEC),
        GET_VAR(() -> GetVarWidget.CODEC),
        SELECTION(() -> SelectionWidget.CODEC);

        public static final EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final Supplier<MapCodec<? extends CodeWidget>> entryCodec;

        Type(Supplier<MapCodec<? extends CodeWidget>> entryCodec) {
            this.entryCodec = entryCodec;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }

        public MapCodec<? extends CodeWidget> getEntryCodec() {
            return entryCodec.get();
        }
    }

    static int getHeightFromArgs(Map<String, CodeWidget> widgets) {
        return widgets.values().stream().mapToInt(CodeWidget::getHeight).max().orElse(0);
    }

    static int getWidthFromList(Font font, List<CodeWidget> widgets) {
        return widgets.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    interface Builder<T extends CodeWidget> {
        T build();
    }
}
