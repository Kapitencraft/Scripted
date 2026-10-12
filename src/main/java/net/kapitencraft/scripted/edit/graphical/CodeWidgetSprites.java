package net.kapitencraft.scripted.edit.graphical;

import net.kapitencraft.scripted.Scripted;
import net.minecraft.resources.ResourceLocation;

public interface CodeWidgetSprites {
    ResourceLocation BOOL_EXPR = Scripted.res("code/bool_expr");
    ResourceLocation BRANCH_END = Scripted.res("code/branch_end");
    ResourceLocation SCOPE_BOTH_SIDE = Scripted.res("code/scope_both_side");
    ResourceLocation SCOPE_BOTH_SIDE_NO_IN = Scripted.res("code/scope_both_side_no_in");
    ResourceLocation GENERIC_EXPR = Scripted.res("code/generic_expr");
    ResourceLocation SCOPE_HEAD = Scripted.res("code/loop_head");
    ResourceLocation METHOD_HEAD = Scripted.res("code/method_head");
    ResourceLocation NUMBER_EXPR = Scripted.res("code/number_expr");
    ResourceLocation SCOPE_ENCLOSURE = Scripted.res("code/scope_enclosure");
    ResourceLocation SCOPE_END = Scripted.res("code/scope_end");
    ResourceLocation SCOPE_END_NO_IN = Scripted.res("code/scope_end_no_in");
    ResourceLocation SCOPE_END_WITH_CODE = Scripted.res("code/scope_end_with_code");
    ResourceLocation SIMPLE_BLOCK = Scripted.res("code/simple_block");
    ResourceLocation MODIFY_IF = Scripted.res("code/modify_if");
}
