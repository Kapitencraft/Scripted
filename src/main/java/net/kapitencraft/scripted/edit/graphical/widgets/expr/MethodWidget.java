package net.kapitencraft.scripted.edit.graphical.widgets.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
<<<<<<<< HEAD:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodInvokeWidget.java
import net.kapitencraft.scripted.edit.RenderHelper;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.connector.ArgumentExprConnector;
import net.kapitencraft.scripted.edit.graphical.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.fetch.WidgetFetchResult;
========
import net.kapitencraft.kap_lib.core.collection.MapStream;
import net.kapitencraft.scripted.edit.CodeWidgetHelper;
import net.kapitencraft.scripted.edit.graphical.ExprCategory;
import net.kapitencraft.scripted.edit.graphical.MethodContext;
import net.kapitencraft.scripted.edit.graphical.widgets.connector.ArgumentExprConnector;
import net.kapitencraft.scripted.edit.graphical.widgets.connector.Connector;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.ExprWidgetFetchResult;
import net.kapitencraft.scripted.edit.graphical.widgets.fetch.WidgetFetchResult;
>>>>>>>> master:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodWidget.java
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.interaction.CodeInteraction;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

<<<<<<<< HEAD:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodInvokeWidget.java
//TODO removing expression crashes the game
public class MethodInvokeWidget implements ExprCodeWidget {
    public static final MapCodec<MethodInvokeWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprCategory.CODEC.fieldOf("category").forGetter(w -> w.type),
            Codec.STRING.fieldOf("translationKey").forGetter(w -> w.translationKey),
            ExprCodeWidget.CODEC.listOf().fieldOf("args").forGetter(w -> w.args)
    ).apply(i, MethodInvokeWidget::new));

    private final ExprCategory type;
    private final String translationKey;
    private final List<ExprCodeWidget> args = new ArrayList<>();
    private MethodInvokeWidget child;

    public MethodInvokeWidget(ExprCategory type, String translationKey, List<ExprCodeWidget> args) {
========
public class MethodWidget implements ExprCodeWidget {
    public static final MapCodec<MethodWidget> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExprCategory.CODEC.fieldOf("category").forGetter(w -> w.type),
            Codec.STRING.fieldOf("translationKey").forGetter(w -> w.translationKey),
            Codec.unboundedMap(Codec.STRING, ExprCodeWidget.CODEC).fieldOf("args").forGetter(w -> w.args)
    ).apply(i, MethodWidget::new));

    private final ExprCategory type;
    private final String translationKey;
    private final Map<String, ExprCodeWidget> args = new HashMap<>();
    private MethodWidget child;

    public MethodWidget(ExprCategory type, String translationKey, Map<String, ExprCodeWidget> args) {
>>>>>>>> master:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodWidget.java
        this.type = type;
        this.translationKey = translationKey;
        this.args.addAll(args);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public @NotNull Type getType() {
        return Type.EXPR;
    }

    @Override
<<<<<<<< HEAD:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodInvokeWidget.java
    public MethodInvokeWidget copy() {
        MethodInvokeWidget methodInvokeWidget = new MethodInvokeWidget(this.type, this.translationKey, this.args.stream().map(ExprCodeWidget::copy).toList());
        if (this.child != null)
            methodInvokeWidget.setChild(this.child.copy());
        return methodInvokeWidget;
========
    public MethodWidget copy() {
        MethodWidget methodWidget = new MethodWidget(this.type, this.translationKey, MapStream.of(this.args).mapValues(ExprCodeWidget::copy).toMap());
        if (this.child != null)
            methodWidget.setChild(this.child.copy());
        return methodWidget;
>>>>>>>> master:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodWidget.java
    }

    @Override
    public void insertById(int arg, @NotNull ExprCodeWidget obj) {
        if (args.size() > arg) {
            args.set(arg, obj);
        } else {
            throw new IllegalArgumentException("unknown argument in expr '" + this.translationKey + "': " + arg);
        }
    }

    @Override
<<<<<<<< HEAD:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodInvokeWidget.java
    public CodeWidget getByIndex(int argName) {
        return args.get(argName);
========
    public CodeWidget getByName(String arg) {
        return args.get(arg);
>>>>>>>> master:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodWidget.java
    }

    @Override
    public void collectConnectors(int aX, int aY, Font font, Consumer<Connector> collector) {
        ArgumentExprConnector.parse(font, aX + 4, aY, this.translationKey, this.args, this.getHeight(), this, collector);
        //collector.accept(new ExprChainConnector(aX + 4 + this.getWidth(font), aY, this));
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {
        int height = getHeight();
        graphics.blitSprite(type.getSpriteLocation(), renderX, renderY, getWidth(font), height);
        CodeWidgetHelper.renderVisualText(graphics, font, renderX, renderY + 6 + (height - 20) / 2, this.translationKey, this.args);
    }

    @Override
    public int getWidth(Font font) {
        return CodeWidgetHelper.getVisualTextWidth(font, this.translationKey, this.args) + 12;
    }

    @Override
    public int getHeight() {
        return Math.max(18, ExprCodeWidget.getHeightFromArgs(this.args) + 4);
    }

    @Override
    public WidgetFetchResult fetchAndRemoveHovered(int x, int y, Font font) {
        if (x > this.getWidth(font)) {
            if (this.child != null)
                return this.child.fetchAndRemoveHovered(x + this.getWidth(font), y, font);
            return null;
        }
        return ExprWidgetFetchResult.fromExprList(4, x, y, font, this, this.translationKey, this.args);
    }

    @Override
    public void registerInteractions(int xOrigin, int yOrigin, Font font, Consumer<CodeInteraction> sink) {
        CodeWidgetHelper.registerAllInteractions(xOrigin, yOrigin + 5 + (getHeight() - 18) / 2, font, sink, translationKey, args);
    }

<<<<<<<< HEAD:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodInvokeWidget.java
    public void setChild(@Nullable MethodInvokeWidget codeWidget) {
        this.child = codeWidget;
    }

    public @Nullable MethodInvokeWidget getChild() {
        return this.child;
    }

    public Expr parse() {
        return null; //new Expr.StaticCall(this.); //TODO
    }

    public static class Builder implements ExprCodeWidget.Builder<MethodInvokeWidget> {
========
    public void setChild(@Nullable MethodWidget codeWidget) {
        this.child = codeWidget;
    }

    public @Nullable MethodWidget getChild() {
        return this.child;
    }

    public static class Builder implements ExprCodeWidget.Builder<MethodWidget> {
>>>>>>>> master:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodWidget.java
        private ExprCategory type;
        private String translationKey;
        private final List<ExprCodeWidget> args = new ArrayList<>();

        public Builder setTranslationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder setType(ExprCategory type) {
            this.type = type;
            return this;
        }

        public Builder withParam(int idx, ExprCodeWidget entry) {
            this.args.set(idx, entry);
            return this;
        }

        public Builder withParam(int argIdx, ExprCodeWidget.Builder<?> builder) {
            return this.withParam(argIdx, builder.build());
        }

        @Override
<<<<<<<< HEAD:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodInvokeWidget.java
        public MethodInvokeWidget build() {
            return new MethodInvokeWidget(type, translationKey, args);
========
        public MethodWidget build() {
            return new MethodWidget(type, translationKey, args);
>>>>>>>> master:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodWidget.java
        }
    }

    @Override
<<<<<<<< HEAD:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodInvokeWidget.java
    public void update(@Nullable MethodContext context) {
        this.args.forEach(c -> c.update(context));
========
    public void update(@Nullable MethodContext context, Font font) {
        this.args.values().forEach(c -> c.update(context, font));
>>>>>>>> master:src/main/java/net/kapitencraft/scripted/edit/graphical/widgets/expr/MethodWidget.java
    }
}
