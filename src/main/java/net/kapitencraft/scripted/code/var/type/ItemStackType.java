package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.item.ItemStack;

public class ItemStackType extends VarType<ItemStack> {

    public ItemStackType() {
        super("ItemStack", null, null, null, null, null, null);
        this.setConstructor(context -> context.constructor()); //constructor
        //fields
        this.addField("count", ItemStack::getCount, ItemStack::setCount, VarTypes.INTEGER);

        //methods TODO make (working) lambda builder
        this.addMethod("getItem", context -> context.returning(VarTypes.ITEM).executes(ItemStack::getItem));
        this.addMethod("split", context -> context.returning(this)
                .withParam("amount", VarTypes.INTEGER)
                .executes(ItemStack::split)
        );
        this.addMethod("damage", context -> context.consumer()
                .withParam("count", VarTypes.INTEGER)
                .executes((stack, integer) -> stack.setDamageValue(stack.getDamageValue() + integer))
        );
    }


    private class InstConstructor extends SimpleConstructor {

        protected InstConstructor() {
        }

        @Override
        public ItemStack call(VarMap params){
            ItemStack stack = new ItemStack(params.getVarValue("item", VarTypes.ITEM));
            params.getOptionalVarValue("count", VarTypes.INTEGER).ifPresent(stack::setCount);
            params.getOptionalVarValue("data", VarTypes.DATA_STORAGE).ifPresent(stack::setTag);
            return stack;
        }

        @Override
        public VarType<ItemStack> getType(IVarAnalyser analyser){
                return VarTypes.ITEM_STACK.get();
            }
    }

}