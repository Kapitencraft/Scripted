package net.kapitencraft.scripted.edit.graphical.selection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record SelectionTab(List<Entry> widgets) {
    public static final Codec<SelectionTab> CODEC = Entry.CODEC.listOf().xmap(SelectionTab::new, SelectionTab::widgets);

    public CodeWidget get(int entry) {
        Preconditions.checkElementIndex(entry, this.widgets.size());
        return this.widgets.get(entry).value();
    }

    public static SelectionTab.Builder builder() {
        return new Builder();
    }

    public int size() {
        return widgets.size();
    }

    public static class Builder {
        private final List<Entry> widgets = new ArrayList<>();

        public Builder withEntry(ExprCodeWidget widget) {
            this.widgets.add(new ExprEntry(widget));
            return this;
        }

        public Builder withEntry(BlockCodeWidget.Builder<?> builder) {
            this.widgets.add(new StmtEntry(builder.build()));
            return this;
        }

        public SelectionTab build() {
            return new SelectionTab(ImmutableList.copyOf(this.widgets));
        }
    }

    private interface Entry {
        Codec<Entry> CODEC = CodeType.CODEC.dispatch(Entry::getType, CodeType::getCodec);

        CodeType getType();

        CodeWidget value();

        enum CodeType implements StringRepresentable {
            EXPR(ExprEntry.CODEC),
            STMT(StmtEntry.CODEC);

            private static final StringRepresentable.EnumCodec<CodeType> CODEC = StringRepresentable.fromEnum(CodeType::values);

            private final Codec<? extends Entry> codec;

            CodeType(Codec<? extends Entry> codec) {
                this.codec = codec;
            }

            public MapCodec<? extends Entry> getCodec() {
                return codec.fieldOf("entry");
            }

            @Override
            public @NotNull String getSerializedName() {
                return name().toLowerCase();
            }
        }
    }

    private record ExprEntry(ExprCodeWidget value) implements Entry {
        private static final Codec<ExprEntry> CODEC = ExprCodeWidget.CODEC.xmap(ExprEntry::new, ExprEntry::value);

        @Override
        public CodeType getType() {
            return CodeType.EXPR;
        }
    }

    private record StmtEntry(BlockCodeWidget value) implements Entry {
        private static final Codec<StmtEntry> CODEC = BlockCodeWidget.CODEC.xmap(StmtEntry::new, StmtEntry::value);

        @Override
        public CodeType getType() {
            return CodeType.STMT;
        }
    }
}