package net.kapitencraft.scripted.edit.graphical.widgets.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.CodeWidgetSprites;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.fetch.BlockWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.tool.StringReader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

//the difference to ExprCodeWidget?
//idk
public class MethodStmtWidget extends StmtCodeWidget {
    public static final MapCodec<MethodStmtWidget> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i)
                    .and(Codec.STRING.fieldOf("signature").forGetter(w -> w.signature))
                    .and(ExprCodeWidget.CODEC.listOf().fieldOf("args").forGetter(w -> w.args))
                    .apply(i, MethodStmtWidget::new)
    );

    private final String signature;
    private final List<ExprCodeWidget> args = new ArrayList<>();

    public MethodStmtWidget(Optional<StmtCodeWidget> child, String signature, List<ExprCodeWidget> args) {
        this.signature = signature;
        this.args.addAll(args);
        child.ifPresent(this::setChild);
    }

    public MethodStmtWidget(StmtCodeWidget child, String sig, List<ExprCodeWidget> args) {
        this.setChild(child);
        this.signature = sig;
        this.args.addAll(args);
    }

    @Override
    public StmtCodeWidget copy() {
        return new MethodStmtWidget(
                this.getChildCopy(),
                this.signature,
                this.args
        );
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {
        if (args.containsKey(arg)) { //TODO
            args.put(arg, obj);
        } else {
            throw new IllegalArgumentException("unknown argument in expr '" + this.signature + "': " + arg);
        }
    }

    @Override
    public CodeWidget getByName(String argName) {
        return args.get(argName);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    @NotNull
    protected Type getType() {
        return Type.METHOD_STMT;
    }

    @Override
    public Stmt parse() {
        StringReader reader = new StringReader(signature);
        ClassReference reference = VarTypeManager.parseType(reader);
        String mName = reader.readUntil('(');

        return new Stmt.Expression(
                new Expr.StaticCall(
                        reference,
                        Token.createNative(mName),
                )
        );
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {

        int height = getHeight();
        graphics.blitSprite(CodeWidgetSprites.SIMPLE_BLOCK, renderX, renderY, 6 + getWidth(font), 3 + height);
        RenderHelper.renderVisualText(graphics, font, renderX + 4, renderY + 7, signature, args);
    }

    @Override
    public int getWidth(Font font) {
        return RenderHelper.getVisualTextWidth(font, this.signature, this.args) + 12;
    }

    @Override
    public int getHeight() {
        return Math.max(19, ExprCodeWidget.getHeightFromArgs(this.args) + 4);
    }

    @Override
    public @Nullable WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        return x < this.getWidth(font) ? BlockWidgetFetchResult.notRemoved(this, x, y) : super.fetchAndRemoveHovered(x, y, font);
    }

    @Override
    public void update(@Nullable MethodContext context) {
        this.args.values().forEach(c -> c.update(context));
        super.update(context);
    }

    public static class Builder implements StmtCodeWidget.Builder<MethodStmtWidget> {
        private StmtCodeWidget child = null;
        private String signature = null;
        private final List<ExprCodeWidget> arguments = new ArrayList<>();

        public Builder setChild(StmtCodeWidget child) {
            this.child = child;
            return this;
        }

        public Builder setChild(StmtCodeWidget.Builder<?> child) {
            return this.setChild(child.build());
        }

        public Builder setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder withArgument(String name, ExprCodeWidget child) {
            this.arguments.put(name, child);
            return this;
        }

        @Override
        public MethodStmtWidget build() {
            return new MethodStmtWidget(child, Objects.requireNonNull(signature, "missing signature"), arguments);
        }
    }
}
