package net.kapitencraft.scripted.edit;

import net.kapitencraft.scripted.edit.client.IRenderable;
import net.kapitencraft.scripted.edit.client.RenderMap;
import net.kapitencraft.scripted.edit.client.block_element.BlockElement;
import net.kapitencraft.scripted.edit.client.block_element.TextElement;
import net.minecraft.locale.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface RenderHelper {
    Pattern VAR_TEXT_REGEX = Pattern.compile("%\\{([a-zA-Z0-9_]+)}%"); //oooh pattern :pog:

    static void renderFunction(int x, int y) {

    }

    static List<BlockElement> decompileVisualText(IRenderable renderable) {
        RenderMap map = renderable.getParamData();
        List<BlockElement> list = new ArrayList<>();
        String inst = Language.getInstance().getOrDefault(renderable.translationKey());
        Matcher matcher = VAR_TEXT_REGEX.matcher(inst);
        int j, l;
        for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String subElement = inst.substring(j, k);
            list.add(new TextElement(subElement));
            String name = matcher.group(1);
            list.addAll(decompileVisualText(map.getParam(name)));
        }
        return list;
    }
}
