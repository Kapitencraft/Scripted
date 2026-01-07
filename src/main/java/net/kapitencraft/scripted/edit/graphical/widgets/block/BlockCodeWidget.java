package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockRemovable;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.inserter.block.BlockGhostInserter;
import net.kapitencraft.scripted.edit.graphical.inserter.block.ChildBlockGhostInserter;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class BlockCodeWidget implements BlockRemovable, CodeWidget {
    public static final Codec<BlockCodeWidget> CODEC = Type.CODEC.dispatch(BlockCodeWidget::getType, Type::getEntryCodec);

    protected static <T extends BlockCodeWidget> Products.P1<RecordCodecBuilder.Mu<T>, Optional<BlockCodeWidget>> commonFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                CODEC.optionalFieldOf("child")
                        .forGetter(w  -> Optional.ofNullable(w.getChild())));
    }

    private BlockCodeWidget child;

    public void setChild(BlockCodeWidget child) {
        this.child = child;
    }

    public void setBottomChild(BlockCodeWidget ghostTarget) {
        BlockCodeWidget parent = this;
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

    //TODO convert back to code representation before saving
    //lambda necessary to ensure load order doesn't create cycle
    protected enum Type implements StringRepresentable {
        HEAD(() -> HeadWidget.CODEC),
        WHILE_LOOP(() ->  WhileLoopWidget.CODEC),
        IF(() -> IfWidget.CODEC),
        BODY(() -> VarModWidget.CODEC),
        METHOD_STMT(() -> MethodStmtWidget.CODEC);

        public static final EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final Supplier<MapCodec<? extends BlockCodeWidget>> entryCodec;

        Type(Supplier<MapCodec<? extends BlockCodeWidget>> entryCodec) {
            this.entryCodec = entryCodec;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }

        public MapCodec<? extends BlockCodeWidget> getEntryCodec() {
            return entryCodec.get();
        }
    }

    public abstract int getWidth(Font font);

    public abstract int getHeight();

    public @Nullable BlockCodeWidget getChild() {
        return child;
    }

    protected @Nullable BlockCodeWidget getChildCopy() {
        return this.getChild() != null ? this.getChild() : null;
    }

    public abstract BlockCodeWidget copy();

    protected BlockWidgetFetchResult fetchChildRemoveHovered(int x, int y, Font font) {
        BlockWidgetFetchResult result = this.child.fetchAndRemoveHovered(x, y, font);
        if (result == null) return null;
        if (!result.removed())
            this.setChild(null);
        return result.setRemoved();
    }

    public int getHeightWithChildren() {
        int height = this.getHeight();
        BlockCodeWidget widget = this.getChild();
        while (widget != null) {
            height += widget.getHeight();
            widget = widget.getChild();
        }
        return height;
    }

    public @Nullable BlockGhostInserter getGhostBlockWidgetTarget(int x, int y) {
        if (y < 0)
            return null;
        if (y < this.getHeight() + 10 && x > -10 && x < 30) {
            return new ChildBlockGhostInserter(this);
        }
        if (this.child != null)
            return this.child.getGhostBlockWidgetTarget(x, y - getHeight());
        return null;
    }

    public void insertChildMiddle(BlockCodeWidget widget) {
        widget.setChild(this.child);
        this.setChild(widget);
    }

    public interface Builder<T extends BlockCodeWidget> {
        T build();
    }
}
