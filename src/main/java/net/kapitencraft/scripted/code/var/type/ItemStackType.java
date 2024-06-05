package net.kapitencraft.scripted.code.var.type;

import com.google.gson.JsonObject;
import net.kapitencraft.scripted.code.exe.methods.Method;
import net.kapitencraft.scripted.code.exe.MethodPipeline;
import net.kapitencraft.scripted.code.exe.functions.abstracts.InstanceFunction;
import net.kapitencraft.scripted.code.exe.methods.param.ParamSet;
import net.kapitencraft.scripted.code.exe.methods.param.ParamData;
import net.kapitencraft.scripted.code.oop.Constructor;
import net.kapitencraft.scripted.code.oop.Field;
import net.kapitencraft.scripted.code.oop.InstanceMethod;
import net.kapitencraft.scripted.code.var.Var;
import net.kapitencraft.scripted.code.var.analysis.VarAnalyser;
import net.kapitencraft.scripted.code.var.VarMap;
import net.kapitencraft.scripted.code.var.VarType;
import net.kapitencraft.scripted.init.ModVarTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStackType extends VarType<ItemStack> {

    public ItemStackType() {
        super(null, null, null, null);
        this.setConstructor(new InstConstructor()); //constructor
        //fields
        this.addField("count", new Field<>(ItemStack::getCount, ItemStack::setCount, ModVarTypes.INTEGER));
        //methods
        this.addMethod("getItem", new GetItem());
        this.addMethod("split", new Split());
        //functions
        this.addFunction("damage", new Damage());
    }


    private static class InstConstructor extends Constructor<ItemStack> {

        protected InstConstructor() {
            super(ParamSet.single(ParamSet.builder().addParam("item", ModVarTypes.ITEM).addOptionalParam("amount", ModVarTypes.INTEGER).addOptionalParam("data", ModVarTypes.DATA_STORAGE)), "newItemStack");
        }

        @Override
        public Method<ItemStack>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return new Instance(data);
        }

        @Override
        public Method<ItemStack>.Instance construct(ParamData data) {
            return new Instance(data);
        }

        public class Instance extends Method<ItemStack>.Instance {
            protected Instance(ParamData data) {
                super(data);
            }

            @Override
            public VarType<ItemStack> getType(VarAnalyser analyser){
                return ModVarTypes.ITEM_STACK.get();
            }

            @Override
            public Var<ItemStack> call(VarMap params){
                ItemStack stack = new ItemStack(params.getVarValue("item", ModVarTypes.ITEM));
                params.getOptionalVarValue("count", ModVarTypes.INTEGER).ifPresent(stack::setCount);
                params.getOptionalVarValue("data", ModVarTypes.DATA_STORAGE).ifPresent(stack::setTag);
                return new Var<>(stack);
            }

            @Override
            public void analyse(VarAnalyser analyser){
                analyser.assertVarExistence("item", ModVarTypes.ITEM);
                analyser.assertOptionVarExistence("count", ModVarTypes.INTEGER);
                analyser.assertOptionVarExistence("data", ModVarTypes.DATA_STORAGE);
            }
        }
    }

    private static class GetItem extends InstanceMethod<ItemStack, Item> {

        protected GetItem() {
            super(ParamSet.empty(), "getItem");
        }

        @Override
        public InstanceMethod<ItemStack, Item>.Instance load(ParamData set, Method<ItemStack>.Instance inst, JsonObject object) {
            return new Instance(set, inst);
        }

        @Override
        public Method<Item>.Instance load(JsonObject object, VarAnalyser analyser, ParamData data) {
            return null;
        }

        public class Instance extends InstanceMethod<ItemStack, Item>.Instance {

            protected Instance(ParamData paramData, Method<ItemStack>.Instance parent) {
                super(paramData, parent);
            }

            @Override
            public Var<Item> call(VarMap map, Var<ItemStack> inst) {
                return new Var<>(inst.getValue().getItem());
            }

            @Override
            public VarType<Item> getType(VarAnalyser analyser) {
                return ModVarTypes.ITEM.get();
            }
        }
    }
    private static class Split extends InstanceMethod<ItemStack, ItemStack> {

        protected Split() {
            super(ParamSet.single(ParamSet.builder().addParam("amount", ModVarTypes.INTEGER)), "split");
        }

        @Override
        public InstanceMethod<ItemStack, ItemStack>.Instance load(ParamData set, Method<ItemStack>.Instance inst, JsonObject object) {
            return new Instance(set, inst);
        }

        public class Instance extends InstanceMethod<ItemStack, ItemStack>.Instance {

            protected Instance(ParamData paramData, Method<ItemStack>.Instance parent) {
                super(paramData, parent);
            }

            @Override
            public Var<ItemStack> call(VarMap map, Var<ItemStack> inst) {
                return new Var<>(inst.getValue().split(map.getVarValue("amount", ModVarTypes.INTEGER)));
            }

            @Override
            public VarType<ItemStack> getType(VarAnalyser analyser) {
                return ModVarTypes.ITEM_STACK.get();
            }

            @Override
            public void analyse(VarAnalyser analyser) {
                analyser.assertVarExistence("amount", ModVarTypes.INTEGER);
            }
        }
    }

    private static class Damage extends InstanceFunction<ItemStack> {

        @Override
        public InstanceFunction<ItemStack>.Instance loadInstance(JsonObject object, VarAnalyser analyser, Method<ItemStack>.Instance inst) {
            Method<Integer>.Instance amount = Method.loadFromSubObject(object, "amount", analyser);
            return new Instance(inst, amount);
        }

        public class Instance extends InstanceFunction<ItemStack>.Instance {
            private final Method<Integer>.Instance amount;

            protected Instance(Method<ItemStack>.Instance supplier, Method<Integer>.Instance amount) {
                super(supplier);
                this.amount = amount;
            }

            @Override
            public void executeInstanced(VarMap map, MethodPipeline<?> source, Var<ItemStack> instance) {
                ItemStack stack = instance.getValue();
                stack.setDamageValue(stack.getDamageValue() + this.amount.callInit(map).getValue());
            }

            @Override
            public void analyse(VarAnalyser analyser) {
                this.amount.analyse(analyser);
                super.analyse(analyser);
            }

            @Override
            public void save(JsonObject object) {
                super.save(object);
                object.add("amount", this.amount.toJson());
            }
        }
    }
}