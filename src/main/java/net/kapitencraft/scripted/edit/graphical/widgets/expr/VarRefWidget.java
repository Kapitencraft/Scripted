package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.VarNameSelectorWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class VarRefWidget extends ChainableExprWidget {
    public static final MapCodec<VarRefWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("name").forGetter(w -> Optional.ofNullable(w.name))
    ).apply(i, VarRefWidget::fromCodec));

    private static VarRefWidget fromCodec(Optional<String> s) {
        return new VarRefWidget(s.orElse(null));
    }

    private final String name;
    private final VarNameSelectorWidget nameSelector = new VarNameSelectorWidget(ExprCategory.OTHER);
    private final boolean param;
    private ExprCategory exprCategory = ExprCategory.OTHER;

    public VarRefWidget(String name) {
        this(name, false);
    }

    public VarRefWidget(String name, ExprCategory category) {
        this(name, true);
        this.exprCategory = category;
    }

    private VarRefWidget(String name, boolean param) {
        this.name = name;
        this.param = param;
    }

    public ExprCategory getExprCategory() {
        return exprCategory;
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull Type getType() {
        return Type.VAR_REF;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(this.exprCategory.getSpriteLocation(), renderX, renderY, getWidth(font), getHeight());
        graphics.drawString(font, this.name, renderX + 4, renderY + 5, 0, false);
    }

    @Override
    public ExprCodeWidget copy() {
        return new VarRefWidget(this.name, false);
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        throw new IllegalAccessError("can not insert into get var widget");
    }

    @Override
    public CodeWidget getByName(String arg) {
        throw new IllegalAccessError("can not get from get var widget");
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {

    }

    @Override
    public int getWidth(Font font) {
        return 6 + font.width(this.name);
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (x < this.getWidth(font)) {
            if (param) {
                return new ExprWidgetFetchResult(true, x, y, this);
            }
            return ExprWidgetFetchResult.notRemoved(this, x, y);
        }
        return null;
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {

    }

    @Override
    public void update(@Nullable MethodContext context, Font font) {
        if (!this.param) {
            this.nameSelector.update(context, font);
            this.exprCategory = context == null ? ExprCategory.OTHER : context.lvt.getType(this.name);
        }
    }
}
