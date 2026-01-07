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

public interface ExprCodeWidget extends Removable {
    Codec<ExprCodeWidget> CODEC = Type.CODEC.dispatch(ExprCodeWidget::getType, Type::getEntryCodec);

    @NotNull Type getType();

    void render(GuiGraphics graphics, Font font, int renderX, int renderY);

    int getWidth(Font font);

    int getHeight();

    ExprCodeWidget copy();

    //TODO convert back to code representation before saving
    //lambda necessary to ensure load order doesn't create cycle
    enum Type implements StringRepresentable {
        PARAM(() -> ParamWidget.CODEC),
        EXPR(() -> ExprWidget.CODEC),
        GET_VAR(() -> GetVarWidget.CODEC),
        LIST_SELECTION(() -> ListSelectionWidget.CODEC),
        SELECT_BLOCK(() -> BlockSelectWidget.CODEC);

        public static final EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final Supplier<MapCodec<? extends ExprCodeWidget>> entryCodec;

        Type(Supplier<MapCodec<? extends ExprCodeWidget>> entryCodec) {
            this.entryCodec = entryCodec;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }

        public MapCodec<? extends ExprCodeWidget> getEntryCodec() {
            return entryCodec.get();
        }
    }

    static int getHeightFromArgs(Map<String, ExprCodeWidget> widgets) {
        return widgets.values().stream().mapToInt(ExprCodeWidget::getHeight).max().orElse(0);
    }

    static int getWidthFromList(Font font, List<ExprCodeWidget> widgets) {
        return widgets.stream().mapToInt(w -> w.getWidth(font)).sum();
    }

    interface Builder<T extends ExprCodeWidget> {
        T build();
    }
}
