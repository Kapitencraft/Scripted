package net.kapitencraft.scripted.edit.graphical.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodRenderer extends IRenderer {
    private final Pattern METHOD_MATCHER = Pattern.compile("@(\\w+)@");

    private final String methodId;


    private final Map<String, MethodRenderer> parameters = new HashMap<>();
    private @Nullable MethodRenderer child;

    public MethodRenderer(String methodId) {
        this.methodId = methodId;
    }


    public void render(GuiGraphics graphics) {
        String translation = I18n.get(methodId);
        int i = 0;
        Matcher matcher = METHOD_MATCHER.matcher(translation);
        for (; i < translation.length(); i++) {
            if (matcher.find(i)) {

            }
        }
    }

    private int width() {
        return 0;
    }

    public void setChild(@Nullable MethodRenderer child) {
        this.child = child;
    }
}
