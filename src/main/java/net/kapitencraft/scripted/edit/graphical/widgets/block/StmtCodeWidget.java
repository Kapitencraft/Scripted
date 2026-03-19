package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.ChildBlockConnector;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class StmtCodeWidget implements CodeWidget {
    public static final Codec<StmtCodeWidget> CODEC = Type.CODEC.dispatch(StmtCodeWidget::getType, Type::getEntryCodec);

    protected static <T extends StmtCodeWidget> Products.P1<RecordCodecBuilder.Mu<T>, Optional<StmtCodeWidget>> commonFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                CODEC.optionalFieldOf("child")
                        .forGetter(w -> Optional.ofNullable(w.getChild())));
    }

    private StmtCodeWidget child;

    public void setChild(StmtCodeWidget child) {
        this.child = child;
    }

    public void setBottomChild(StmtCodeWidget ghostTarget) {
        StmtCodeWidget parent = this;
        while (parent.child != null) {
            parent = parent.child;
        }
        parent.setChild(ghostTarget);
    }

    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        if (this.child != null)
            this.child.render(graphics, font, renderX, renderY + getHeight());
    }

    protected abstract @NotNull Type getType();

    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        collector.accept(new ChildBlockConnector(aX, aY, this));
        if (this.child != null) {
            this.child.collectConnectors(aX, aY + this.getHeight(), font, collector);
        }
    }

    //TODO convert back to code representation before saving
    //lambda necessary to ensure load order doesn't create cycle
    protected enum Type implements StringRepresentable {
        HEAD(() -> HeadWidget.CODEC),
        WHILE_LOOP(() -> WhileLoopWidget.CODEC),
        IF(() -> IfWidget.CODEC),
        BODY(() -> VarModWidget.CODEC),
        METHOD_STMT(() -> MethodStmtWidget.CODEC);

        public static final EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final Supplier<MapCodec<? extends StmtCodeWidget>> entryCodec;

        Type(Supplier<MapCodec<? extends StmtCodeWidget>> entryCodec) {
            this.entryCodec = entryCodec;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }

        public MapCodec<? extends StmtCodeWidget> getEntryCodec() {
            return entryCodec.get();
        }
    }

    public abstract int getWidth(Font font);

    public abstract int getHeight();

    public @Nullable StmtCodeWidget getChild() {
        return child;
    }

    protected @Nullable StmtCodeWidget getChildCopy() {
        return this.getChild() != null ? this.getChild() : null;
    }

    public abstract StmtCodeWidget copy();

    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        WidgetFetchResult result = this.child.fetchAndRemoveHovered(x, y, font);
        if (result == null) return null;
        if (!result.removed()) {
            StmtCodeWidget target = null;
            if (Screen.hasControlDown()) {
                StmtCodeWidget bcW = (StmtCodeWidget) result.widget();
                target = bcW.child;
                bcW.setChild(null);
            }
            this.setChild(target);
        }
        return result.setRemoved();
    }

    public int getHeightWithChildren() {
        int height = this.getHeight();
        StmtCodeWidget widget = this.getChild();
        while (widget != null) {
            height += widget.getHeight();
            widget = widget.getChild();
        }
        return height;
    }

    public void insertChildMiddle(StmtCodeWidget widget) {
        widget.setChild(this.child);
        this.setChild(widget);
    }

    @Override
    public void update(@Nullable MethodContext context) {
        if (this.child != null)
            this.child.update(context);
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        if (this.child != null) {
            this.child.registerInteractions(xOrigin, yOrigin + this.getHeight(), font, sink);
        }
    }

    public interface Builder<T extends StmtCodeWidget> {
        T build();
    }
}
