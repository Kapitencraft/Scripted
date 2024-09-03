package net.kapitencraft.scripted.edit;

import com.mojang.brigadier.CommandDispatcher;
import net.kapitencraft.kap_lib.helpers.ClientHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OpenEditScreenCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("code").executes(context -> {
            ClientHelper.postCommandScreen = new EditScreen();
            return 1;
        }));
    }
}
