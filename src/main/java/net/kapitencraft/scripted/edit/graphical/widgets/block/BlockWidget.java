package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.Removable;
import net.kapitencraft.scripted.edit.graphical.widgets.WidgetFetchResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public abstract class BlockWidget implements CodeWidget, Removable {
    public static final Codec<BlockWidget> CODEC = CodeWidget.CODEC.comapFlatMap(c ->
            c instanceof BlockWidget bw ? DataResult.success(bw) :
                    DataResult.error(() -> "code widget not block: " + c.getType().getSerializedName()),
            Function.identity()
    );

    protected static <T extends BlockWidget> Products.P1<RecordCodecBuilder.Mu<T>, Optional<BlockWidget>> commonFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                CODEC.optionalFieldOf("child")
                        .forGetter(w  -> Optional.ofNullable(w.getChild())));
    }

    private BlockWidget child;

    public void setChild(BlockWidget child) {
        this.child = child;
    }

    public void setBottomChild(BlockWidget ghostTarget) {
        BlockWidget parent = this;
        while (parent.child != null) {
            parent = parent.child;
        }
        parent.setChild(ghostTarget);
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        if (this.child != null)
            this.child.render(graphics, font, renderX, renderY + getHeight());
    }

    public BlockWidget getChild() {
        return child;
    }

    protected @Nullable BlockWidget getChildCopy() {
        return this.getChild() != null ? this.getChild() : null;
    }

    @Override
    public abstract BlockWidget copy();

    protected WidgetFetchResult fetchChildRemoveHovered(int x, int y, Font font) {
        WidgetFetchResult result = this.child.fetchAndRemoveHovered(x, y, font);
        if (result == null) return null;
        if (!result.removed())
            this.setChild(null);
        return result.setRemoved();
    }

    public int getHeightWithChildren() {
        int height = this.getHeight();
        BlockWidget widget = this.getChild();
        while (widget != null) {
            height += widget.getHeight();
            widget = widget.getChild();
        }
        return height;
    }

    public BlockWidget getGhostBlockWidgetTarget(int x, int y) {
        if (y < this.getHeight()) {
            return this;
        }
        if (this.child != null)
            return this.child.getGhostBlockWidgetTarget(x, y - getHeight());
        return null;
    }

    public void insertChildMiddle(BlockWidget widget) {
        widget.setChild(this.child);
        this.setChild(widget);
    }

    public interface Builder<T extends BlockWidget> {
        T build();
    }
}
