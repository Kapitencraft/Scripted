package net.kapitencraft.scripted.edit.graphical;

import net.kapitencraft.scripted.Scripted;
import net.minecraft.resources.ResourceLocation;

public interface CodeWidgetSprites {
    ResourceLocation BOOL_EXPR = Scripted.res("code/bool_expr");
    ResourceLocation ELSE_CONDITION_HEAD = Scripted.res("code/else_condition_head");
    ResourceLocation GENERIC_EXPR = Scripted.res("code/generic_expr");
    ResourceLocation LOOP_HEAD = Scripted.res("code/loop_head");
    ResourceLocation METHOD_HEAD = Scripted.res("code/method_head");
    ResourceLocation NUMBER_EXPR = Scripted.res("code/number_expr");
    ResourceLocation SCOPE_ENCLOSURE = Scripted.res("code/scope_enclosure");
    ResourceLocation SCOPE_END = Scripted.res("code/scope_end");
    ResourceLocation SCOPE_END_WITH_CODE = Scripted.res("code/scope_end_with_code");
    ResourceLocation SIMPLE_BLOCK = Scripted.res("code/simple_block");
    ResourceLocation MODIFY_IF = Scripted.res("code/modify_if");
}
