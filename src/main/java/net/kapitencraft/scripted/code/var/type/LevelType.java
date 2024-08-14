package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.level.Level;

public class LevelType extends VarType<Level> {

    public LevelType() {
        super("Level", null, null, null, null, null, null);

        addMethod("spawn", this.context.consumer().withParam("entity", VarTypes.ENTITY).executes());
        addMethod(SetBlock::new);
    }

    private class SpawnEntity extends SimpleInstanceFunction {

        protected SpawnEntity() {
            super("spawn", set -> set.addEntry(entry -> entry.addParam("entity", VarTypes.ENTITY)));
        }

        @Override
        public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<Level> instance) {
            instance.getValue().addFreshEntity(map.getVarValue("entity", VarTypes.ENTITY));
        }
    }
    private class SetBlock extends SimpleInstanceFunction {

        protected SetBlock() {
            super("setBlock", set -> set.addEntry(entry -> entry
                    .addParam("state", VarTypes.BLOCK_STATE)
                    .addParam("pos", VarTypes.BLOCK_POS)
            ));
        }

        @Override
        public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<Level> instance) {
            instance.getValue().setBlock(map.getVarValue("pos", VarTypes.BLOCK_POS), map.getVarValue("state", VarTypes.BLOCK_STATE), 3);
        }
    }
}
