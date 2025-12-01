package net.kapitencraft.scripted.edit;

import net.kapitencraft.scripted.edit.graphical.RenderMap;
import net.kapitencraft.scripted.edit.graphical.code.Code;
import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import net.kapitencraft.scripted.edit.graphical.widgets.TextWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface RenderHelper {

    Pattern VAR_TEXT_REGEX = Pattern.compile("%\\{([a-zA-Z0-9_]+)}%"); //oooh pattern :pog:

    static List<CodeWidget> decompileVisualText(Code renderable) {
        RenderMap map = renderable.getParamData();
        List<CodeWidget> list = new ArrayList<>();
        String inst = Language.getInstance().getOrDefault(renderable.translationKey());
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            list.add(new TextWidget(subElement));
            String name = matcher.group(1);
            list.addAll(decompileVisualText(map.getParam(name)));
        }
        return list;
    }

    static void renderExprList(GuiGraphics graphics, Font font, int renderX, int renderY, List<CodeWidget> children) {
        int i = 0;
        for (CodeWidget child : children) {
            child.render(graphics, font, renderX + i, renderY);
            i += child.getWidth(font);
        }
    }
}
