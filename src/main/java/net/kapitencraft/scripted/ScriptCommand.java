package net.kapitencraft.scripted;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;

public class ScriptCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("script")
                .then(Commands.literal("compile")
                        .executes(ScriptCommand::compile)
                )
        );
    }

    private static int compile(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        File src = new File(
                context.getSource().getServer().getWorldPath(LevelResource.ROOT).toFile(),
                "scripted"
        );
        Compiler.compileAll(src, context.getSource().getPlayerOrException());
        return 1;
    }
}
