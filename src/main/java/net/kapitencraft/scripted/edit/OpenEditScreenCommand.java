package net.kapitencraft.scripted.edit;

import com.mojang.brigadier.CommandDispatcher;
import net.kapitencraft.kap_lib.core.helpers.CommandHelper;
import net.kapitencraft.scripted.edit.graphical.BlocklyEditScreen;
import net.kapitencraft.scripted.edit.text.TextEditScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OpenEditScreenCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("code")
                .executes(CommandHelper.createScreenCommand(BlocklyEditScreen::new))
                .then(Commands.literal("text")
                        .executes(CommandHelper.createScreenCommand(TextEditScreen::new))
                )
        );
    }
}
