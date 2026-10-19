package net.kapitencraft.scripted.edit.graphical.selection;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.core.GraphicalEditor;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.io.CreateMethodWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.StmtCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record SelectionTab(List<Entry> entries) {
    public static final Codec<SelectionTab> CODEC = Entry.CODEC.listOf().xmap(SelectionTab::new, SelectionTab::entries);

    public static SelectionTab.Builder builder() {
        return new Builder();
    }

    public int size() {
        return entries.size();
    }

    public int getHeight() {
        int h = 0;
        for (Entry widget : entries) {
            h += widget.getHeight();
            h += 10;
        }
        return h;
    }

    public void render(GuiGraphics pGuiGraphics, Font font, int yO) {
        for (Entry widget : entries) {
            widget.render(pGuiGraphics, font, yO);
            yO += widget.getHeight();
            yO += 10;
        }
    }

    public void interact(int uX, int uY, GraphicalEditor.InteractionAccess access) {
        for (Entry widget : entries) {
            widget.interact(uX, uY, access);
            uY -= widget.getHeight();
            uY -= 10;
        }
    }

    public static class Builder {
        private final List<Entry> widgets = new ArrayList<>();

        public Builder withEntry(ExprCodeWidget widget) {
            this.widgets.add(new ExprEntry(widget));
            return this;
        }

        public Builder withAction() {
            this.widgets.add(new DoActionEntry());
            return this;
        }

        public Builder withEntry(StmtCodeWidget.Builder<?> builder) {
            return withEntry(builder.build());
        }

        public Builder withEntry(StmtCodeWidget widget) {
            this.widgets.add(new StmtEntry(widget));
            return this;
        }

        public SelectionTab build() {
            return new SelectionTab(ImmutableList.copyOf(this.widgets));
        }
    }

    public void update(Font font) {
        for (Entry widget : this.entries) {
            widget.update(font);
        }
    }

    private interface Entry {
        Codec<Entry> CODEC = CodeType.CODEC.dispatch(Entry::getType, CodeType::getCodec);

        default void update(Font font) {
        }

        int getHeight();

        CodeType getType();

        void render(GuiGraphics guiGraphics, Font font, int y);

        void interact(int uX, int uY, GraphicalEditor.InteractionAccess access);

        enum CodeType implements StringRepresentable {
            EXPR(ExprEntry.CODEC),
            STMT(StmtEntry.CODEC),
            ACTION(DoActionEntry.CODEC);

            private static final Codec<CodeType> CODEC = StringRepresentable.fromEnum(CodeType::values);

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
        public void update(Font font) {
            value.update(null, font);
        }

        @Override
        public int getHeight() {
            return value.getHeight();
        }

        @Override
        public CodeType getType() {
            return CodeType.EXPR;
        }

        @Override
        public void render(GuiGraphics guiGraphics, Font font, int y) {
            this.value.render(guiGraphics, font, 0, y);
        }

        @Override
        public void interact(int uX, int uY, GraphicalEditor.InteractionAccess access) {
            if (uY > 0 && uY < getHeight()) {
                access.setDragged(this.value, uX, uY);
            }
        }
    }

    private record StmtEntry(StmtCodeWidget value) implements Entry {
        private static final Codec<StmtEntry> CODEC = StmtCodeWidget.CODEC.xmap(StmtEntry::new, StmtEntry::value);

        @Override
        public void update(Font font) {
            value.update(null, font);
        }

        @Override
        public int getHeight() {
            return value.getHeight();
        }

        @Override
        public CodeType getType() {
            return CodeType.STMT;
        }

        @Override
        public void render(GuiGraphics guiGraphics, Font font, int y) {
            this.value.render(guiGraphics, font, 0, y);
        }

        @Override
        public void interact(int uX, int uY, GraphicalEditor.InteractionAccess access) {
            if (uY > 0 && uY < getHeight()) {
                access.setDragged(this.value, uX, uY);
            }
        }
    }

    private record DoActionEntry() implements Entry {
        private static final Component TITLE = Component.translatable("scripted.widget.create_method");

        private static final DoActionEntry INSTANCE = new DoActionEntry();
        private static final Codec<DoActionEntry> CODEC = Codec.unit(INSTANCE);

        @Override
        public int getHeight() {
            return 18;
        }

        @Override
        public CodeType getType() {
            return CodeType.ACTION;
        }

        @Override
        public void render(GuiGraphics guiGraphics, Font font, int y) {
            guiGraphics.blitSprite(CodeWidgetSprites.GENERIC_EXPR, 0, y, font.width(TITLE), 18);
            guiGraphics.drawString(font, TITLE, 4, y + 6, 0, false);
        }

        @Override
        public void interact(int uX, int uY, GraphicalEditor.InteractionAccess access) {
            access.openWidget(new CreateMethodWidget(100, 50, 100, access.font(), access::addWidgetAndClose));
        }
    }
}