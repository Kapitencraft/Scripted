package net.kapitencraft.scripted.code.script.type;

import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.kapitencraft.scripted.io.IOHelper;
import net.minecraft.commands.CommandSourceStack;

import java.io.File;

public class SimpleScript extends ScriptType<CommandSourceStack, Void> {

    public SimpleScript() {
        super("sct", new File(IOHelper.SCRIPTED_DIRECTORY, "scripts"), null);
        this.addPossibleParam("pos", ModVarTypes.VEC3);
        this.addPossibleParam("permissionLevel", ModVarTypes.INTEGER);
    }

    @Override
    public VarMap instantiate(CommandSourceStack inst) {
        VarMap map = new VarMap();
        map.setVar("pos", inst.getPosition());
        map.setVar("permissionLevel", inst.permissionLevel);
        return map;
    }
}
