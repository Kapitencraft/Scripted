package net.kapitencraft.scripted.code.script.type;

import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SimpleScript extends ScriptType<CommandSourceStack, Void> {

    public SimpleScript() {
        super("sct", "scripts", null);
        this.addPossibleParam("pos", ModVarTypes.VEC3);
        this.addPossibleParam("permissionLevel", ModVarTypes.INTEGER);
    }

    @Override
    public VarMap instantiate(CommandSourceStack inst) {
        VarMap map = new VarMap();
        map.addVar("pos", inst.getPosition());
        //map.addVar("permissionLevel", );
        return map;
    }
}
