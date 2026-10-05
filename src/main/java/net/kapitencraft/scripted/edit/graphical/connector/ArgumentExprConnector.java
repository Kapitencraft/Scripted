package net.kapitencraft.scripted.edit.graphical.connector;

import net.kapitencraft.scripted.edit.CodeWidgetHelper;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ExprCodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.expr.ParamWidget;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public class ArgumentExprConnector extends ExprConnector {
    private final CodeWidget owner;
    private final String argName;

    public ArgumentExprConnector(int x, int y, CodeWidget owner, String argName) {
        super(x, y);
        this.owner = owner;
        this.argName = argName;
    }

    public static void parse(Font font, int aX, int aY, String key, Map<String, ExprCodeWidget> args, int height, CodeWidget owner, Consumer<Connector> collector) {
        int connectorOffset = aY + (height - 20) / 2 ;
        CodeWidgetHelper.forPartialWidth(font, key, args, (s, integer) -> {
            int finalOffset = connectorOffset - (args.get(s).getHeight() - 8) / 2;
            collector.accept(new ArgumentExprConnector(aX + integer, aY + finalOffset, owner, s));
            args.get(s).collectConnectors(font, collector);
        });
    }

    @Override
    public void insert(@Nullable CodeWidget widget) {
        if (widget == null) {
            widget = ParamWidget.OBJ.get();
        }
        owner.insertByName(argName, (ExprCodeWidget) widget);
    }

    @Override
    public CodeWidget get() {
        return owner.getByName(argName);
    }
}
