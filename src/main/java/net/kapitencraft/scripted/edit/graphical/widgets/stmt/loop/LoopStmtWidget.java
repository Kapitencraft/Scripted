package net.kapitencraft.scripted.edit.graphical.widgets.stmt.loop;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.CommonBranchBlockConnector;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.StmtCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class LoopStmtWidget extends StmtCodeWidget {
    protected static <T extends LoopStmtWidget> Products.P2<RecordCodecBuilder.Mu<T>, Optional<StmtCodeWidget>, Optional<StmtCodeWidget>> loopCommonFields(RecordCodecBuilder.Instance<T> instance) {
        return commonFields(instance).and(StmtCodeWidget.CODEC.optionalFieldOf("body").forGetter(l -> Optional.ofNullable(l.body)));
    }

    protected @Nullable StmtCodeWidget body;

    protected abstract int getHeadWidth(Font font);

    protected abstract int getHeadHeight();

    protected int getBodyHeight() {
        return this.body != null ? this.body.getHeightWithChildren() : 10;
    }

    @Override
    public int getHeight() {
        return getHeadHeight() +
                getBodyHeight() + 13;
    }

    @Override
    public int getWidth(Font font) {
        int width = getHeadWidth(font);
        if (this.body != null) {
            int i = this.body.getWidth(font) + 6;
            if (i > width)
                width = i;
        }
        return width;
    }

    @Override
    public final void render(GuiGraphics graphics, Font font) {
        int renderX = this.x;
        int renderY = this.y;
        int loopWidth = getHeadWidth(font);
        int headHeight = getHeadHeight();
        graphics.blitSprite(CodeWidgetSprites.SCOPE_HEAD, renderX, renderY, loopWidth, headHeight + 3);
        this.renderHead(graphics, font, renderX, renderY);
        int bodyHeight = getBodyHeight();
        if (this.body != null)
            this.body.render(graphics, font);
        graphics.blitSprite(CodeWidgetSprites.SCOPE_ENCLOSURE,
                renderX, renderY + headHeight + 3, 6, bodyHeight - 3
        );
        graphics.blitSprite(this.body != null && this.body.ends() ? CodeWidgetSprites.SCOPE_END_NO_IN : CodeWidgetSprites.SCOPE_END,
                renderX, renderY + headHeight + bodyHeight, loopWidth, 16
        );
        super.render(graphics, font);
    }

    abstract void renderHead(GuiGraphics graphics, Font font, int renderX, int renderY);

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        collector.accept(new CommonBranchBlockConnector(
                aX + 6,
                aY + this.getHeadHeight(),
                this::setBody,
                () -> this.body,
                font,
                collector
        ));
        super.collectConnectors(aX, aY, font, collector);
    }

    public void setBody(@Nullable StmtCodeWidget body) {
        this.body = body;
    }

    @Override
    public void registerInteractions(Font font, Consumer<CodeInteraction> sink) {
        if (this.body != null)
            this.body.registerInteractions(font, sink);
        super.registerInteractions(font, sink);
    }

    public void insertBodyMiddle(StmtCodeWidget widget) {
        widget.setChild(this.body);
        this.body = widget;
    }

    @Override
    public void update(@Nullable MethodContext context, Font font, int x, int y) {
        if (this.body != null) {
            if (context != null) {
                context.lvt.push();
            }
            this.body.update(context, font, x + 6, y + getHeadHeight());
            if (context != null) {
                context.lvt.pop();
            }
        }
        super.update(context, font, x, y);
    }
}
