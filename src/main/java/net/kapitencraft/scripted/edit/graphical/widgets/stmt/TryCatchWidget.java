package net.kapitencraft.scripted.edit.graphical.widgets.stmt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TODO
public class TryCatchWidget extends StmtCodeWidget {
    public static final MapCodec<TryCatchWidget> CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i)
            .and(
                    StmtCodeWidget.CODEC.optionalFieldOf("body").forGetter(w -> Optional.ofNullable(w.body))
            ).and(
                    CatchBranch.CODEC.listOf().fieldOf("branches").forGetter(w -> w.branches)
            )
            .apply(i, TryCatchWidget::new)
    );

    private StmtCodeWidget body;
    private final List<CatchBranch> branches = new ArrayList<>();

    private TryCatchWidget(Optional<StmtCodeWidget> child, Optional<StmtCodeWidget> body, List<CatchBranch> branches) {
        child.ifPresent(this::setChild);
        this.body = body.orElse(null);
        this.branches.addAll(branches);
    }

    private TryCatchWidget(StmtCodeWidget child, StmtCodeWidget body, List<CatchBranch> branches) {
        this.setChild(child);
        this.body = body;
        this.branches.addAll(branches);
    }

    @Override
    protected @NotNull Type getType() {
        return Type.TRY_CATCH_STMT;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public StmtCodeWidget copy() {
        return new TryCatchWidget(
                this.getChild().copy(),
                this.body.copy(),
                this.branches
        );
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int renderX, int renderY) {

        super.render(graphics, font, renderX, renderY);
    }

    @Override
    public void insertByName(@NotNull String arg, @NotNull ExprCodeWidget obj) {

    }

    @Override
    public CodeWidget getByName(String arg) {
        return null;
    }

    public static class CatchBranch {
        private static final Codec<CatchBranch> CODEC = RecordCodecBuilder.create(i -> i.group(
                StmtCodeWidget.CODEC.optionalFieldOf("body").forGetter(b -> Optional.ofNullable(b.body))
        ).apply(i, CatchBranch::new));
        private StmtCodeWidget body;

        private CatchBranch(Optional<StmtCodeWidget> body) {
            this.body = body.orElse(null);
        }

    }
}
