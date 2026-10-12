package net.kapitencraft.scripted.edit.graphical.widgets.stmt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class SimpleScopeEndWidget extends ScopeEndWidget {
    public static final SimpleScopeEndWidget RETURN = new SimpleScopeEndWidget(EndType.RETURN);
    public static final SimpleScopeEndWidget CONTINUE = new SimpleScopeEndWidget(EndType.CONTINUE);
    public static final SimpleScopeEndWidget BREAK = new SimpleScopeEndWidget(EndType.BREAK);

    protected static final MapCodec<SimpleScopeEndWidget> CODEC = EndType.CODEC.xmap(SimpleScopeEndWidget::new, w -> w.endType).fieldOf("end_type");

    private SimpleScopeEndWidget(EndType endType) {
        this.endType = endType;
    }

    private enum EndType implements StringRepresentable {
        RETURN,
        CONTINUE,
        BREAK;

        private static final Codec<EndType> CODEC = StringRepresentable.fromEnum(EndType::values);

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

    private final EndType endType;

    @Override
    protected @NotNull Type getType() {
        return Type.SIMPLE_SCOPE_END;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(Component.translatable("§" + this.endType.getSerializedName())) + 6;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        super.render(graphics, font, renderX, renderY);
        graphics.drawString(font, Component.translatable("§" + this.endType.getSerializedName()), renderX + 4, renderY + 7, 0, false);
    }

    @Override
    public StmtCodeWidget copy() {
        return this; //singletons; no need to create a new one
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        throw new IllegalAccessError("can not insert into scope end widget");
    }

    @Override
    public CodeWidget getByName(String arg) {
        throw new IllegalAccessError("can not insert into scope end widget");
    }
}
