package net.kapitencraft.scripted.edit.graphical.selection;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.kapitencraft.scripted.edit.graphical.widgets.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.block.BlockCodeWidget;

import java.util.ArrayList;
import java.util.List;

public record SelectionTab(List<ExprCodeWidget> widgets) {
    public static final Codec<SelectionTab> CODEC = ExprCodeWidget.CODEC.listOf().xmap(SelectionTab::new, SelectionTab::widgets);

    public static SelectionTab.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<ExprCodeWidget> widgets = new ArrayList<>();

        public Builder withEntry(ExprCodeWidget widget) {
            this.widgets.add(widget);
            return this;
        }

        public Builder withEntry(BlockCodeWidget.Builder<?> builder) {
            this.widgets.add(builder.build());
            return this;
        }

        public SelectionTab build() {
            return new SelectionTab(ImmutableList.copyOf(this.widgets));
        }
    }
}