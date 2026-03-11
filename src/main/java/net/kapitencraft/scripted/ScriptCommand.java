package net.kapitencraft.scripted;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kapitencraft.scripted.lang.bytecode.exe.VirtualMachine;
import net.kapitencraft.scripted.lang.compiler.Compiler;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.load.ClassLoader;
import net.kapitencraft.scripted.lang.exe.natives.NativeClassInstance;
import net.kapitencraft.scripted.lang.exe.natives.impl.NativeClassImpl;
import net.kapitencraft.scripted.lang.exe.natives.scripted.exe.CommandData;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.inst.DynamicClassInstance;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;

public class ScriptCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("script")
                .then(Commands.literal("compile")
                        .executes(ScriptCommand::compile)
                ).then(Commands.literal("run")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(ScriptCommand::run)
                        )
                ).then(Commands.literal("list")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ScriptCommand::list)
                        )
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, "id");
        ClassReference c = VarTypeManager.getClassForName(id);
        if (c == null || !c.exists()) {
            context.getSource().sendFailure(Component.literal("no script for class '" + id + "'"));
            return 1;
        }

        if (!c.get().isChildOf(VarTypeManager.COMMAND_SCRIPT.get())) {
            context.getSource().sendFailure(Component.literal("class not a command script"));
            return 1;
        }

        CommandData data = new CommandData(context.getSource().getPlayerOrException());

        VirtualMachine.executeMethod("Lscripted/exe/CommandScript;execute(Lscripted/exe/CommandData;)",
                new DynamicClassInstance(c.get()),
                new NativeClassInstance(((NativeClassImpl) VarTypeManager.COMMAND_DATA.get()), data)
        );

        return 1;
    }

    private static int compile(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        File src = new File(
                context.getSource().getServer().getWorldPath(LevelResource.ROOT).toFile(),
                "scripted"
        );
        Compiler.compileAll(src, context.getSource());
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        ClassLoader.list(StringArgumentType.getString(context, "name"));
        return 1;
    }
}
