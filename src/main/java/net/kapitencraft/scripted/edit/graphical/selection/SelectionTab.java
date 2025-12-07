package net.kapitencraft.scripted.edit.graphical.selection;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockWidget;

import java.util.ArrayList;
import java.util.List;

public record SelectionTab(List<CodeWidget> widgets) {
    public static final Codec<SelectionTab> CODEC = CodeWidget.CODEC.listOf().xmap(SelectionTab::new, SelectionTab::widgets);

    public static SelectionTab.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<CodeWidget> widgets = new ArrayList<>();

        public Builder withEntry(CodeWidget widget) {
            this.widgets.add(widget);
            return this;
        }

        public Builder withEntry(BlockWidget.Builder<?> builder) {
            this.widgets.add(builder.build());
            return this;
        }

        public SelectionTab build() {
            return new SelectionTab(ImmutableList.copyOf(this.widgets));
        }
    }
}