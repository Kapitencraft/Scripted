package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.TextRenderHelper;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class GetVarWidget implements ExprCodeWidget {
    public static final MapCodec<GetVarWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("name").forGetter(w -> Optional.ofNullable(w.name))
    ).apply(i, GetVarWidget::fromCodec));

    private static GetVarWidget fromCodec(Optional<String> s) {
        return new GetVarWidget(s.orElse(null));
    }

    private final @Nullable String name;
    private final VarNameSelectorWidget nameSelector = new VarNameSelectorWidget();
    private ExprCategory exprCategory = ExprCategory.OTHER;

    public GetVarWidget(@Nullable String name) {
        this.name = name;
    }

    @Override
    public @NotNull Type getType() {
        return Type.GET_VAR;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        graphics.blitSprite(this.exprCategory.getSpriteLocation(), renderX, renderY, getWidth(font), getHeight());
        TextRenderHelper.renderVisualText(graphics, font, renderX, renderY + 5, "§get", Map.of("var", this.nameSelector));
    }

    @Override
    public ExprCodeWidget copy() {
        return new GetVarWidget(this.name);
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
        return 6 + TextRenderHelper.getVisualTextWidth(font, "§get", Map.of("var", this.nameSelector));
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return x < this.getWidth(font) ? ExprWidgetFetchResult.notRemoved(this, x, y) : null;
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {

    }

    @Override
    public void update(@Nullable MethodContext context, Font font) {
        this.nameSelector.update(context, font);
        this.exprCategory = context == null ? ExprCategory.OTHER : context.lvt.getType(this.name);
    }
}
