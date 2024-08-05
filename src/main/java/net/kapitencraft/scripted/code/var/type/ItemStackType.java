package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.param.ParamSet;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.analysis.IVarAnalyser;
import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStackType extends VarType<ItemStack> {

    public ItemStackType() {
        super("ItemStack", null, null, null, null, null, null);
        this.setConstructor(new InstConstructor()); //constructor
        //fields
        this.addField("count", ItemStack::getCount, ItemStack::setCount, VarTypes.INTEGER);

        //methods
        this.addMethod(GetItem::new);
        this.addMethod(Split::new);
        this.addMethod(Damage::new);
    }


    private class InstConstructor extends SimpleConstructor {

        protected InstConstructor() {
            super(set -> set.addEntry(entry ->
                            entry.addParam("item", VarTypes.ITEM)
                                    .addOptionalParam("count", VarTypes.INTEGER)
                                    .addOptionalParam("data", VarTypes.DATA_STORAGE)
                    )
            );
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

    private class GetItem extends SimpleInstanceMethod<Item> {

        protected GetItem() {
            super(ParamSet.empty(), "getItem");
        }


        @Override
        public Item call(VarMap map, ItemStack inst) {
            return inst.getItem();
        }

        @Override
        public VarType<Item> getType(IVarAnalyser analyser) {
            return VarTypes.ITEM.get();
        }
    }
    private class Split extends SimpleInstanceMethod<ItemStack> {

        protected Split() {
            super(set ->
                    set.addEntry(entry ->
                            entry.addParam("amount", VarTypes.INTEGER)),
                    "split");
        }

        @Override
        public ItemStack call(VarMap map, ItemStack inst) {
            return inst.split(map.getVarValue("amount", VarTypes.INTEGER));
        }

        @Override
        public VarType<ItemStack> getType(IVarAnalyser analyser) {
            return VarTypes.ITEM_STACK.get();
        }
    }

    private class Damage extends SimpleInstanceFunction {

        protected Damage() {
            super("damage", set -> set.addEntry(entry -> entry.addParam("count", VarTypes.INTEGER)));
        }

        @Override
        public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<ItemStack> instance) {
            ItemStack stack = instance.getValue();
            stack.setDamageValue(stack.getDamageValue() + map.getVarValue("count", VarTypes.INTEGER));
        }
    }
}