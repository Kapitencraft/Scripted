package net.kapitencraft.scripted.mixin.classes;

import net.kapitencraft.scripted.edit.graphical.core.GraphicalEditor;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Shadow protected abstract void debugFeedbackTranslated(String message, Object... args);

    @SuppressWarnings("AssignmentUsedAsCondition")
    @Inject(method = "handleDebugKeys",at = @At("HEAD"))
    private void handleToggleDebug(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_J) {
            this.debugFeedbackTranslated((GraphicalEditor.renderDebug = !GraphicalEditor.renderDebug) ? "debug.editor.on" : "debug.editor.off");
        }
    }
}
