package net.kapitencraft.scripted.code.var.type;

import net.kapitencraft.scripted.code.var.type.abstracts.VarType;
import net.kapitencraft.scripted.code.var.type.collection.MapType;
import net.kapitencraft.scripted.init.VarTypes;
import net.minecraft.world.item.ItemStack;

public class ItemStackType extends VarType<ItemStack> {

    public ItemStackType() {
        super("ItemStack", null, null, null, null, null, null);
        this.addConstructor(context -> context.constructor()
                .withParam("item", VarTypes.ITEM)
                .executes(ItemStack::new)
                .withParam("count", VarTypes.INTEGER)
                .executes(ItemStack::new)
                .withParam("data", VarTypes.DATA_STORAGE)
                .executes(ItemStack::new)
        ); //constructor
        //fields
        this.addField("count", ItemStack::getCount, ItemStack::setCount, VarTypes.INTEGER);

        //methods
        this.addMethod("is", context -> context.returning(VarTypes.BOOL)
                .withParam("item", VarTypes.ITEM)
                .executes(ItemStack::is)
        );
        this.addMethod("getItem", context -> context.returning(VarTypes.ITEM).executes(ItemStack::getItem));
        this.addMethod("split", context -> context.returning(this)
                .withParam("amount", VarTypes.INTEGER)
                .executes(ItemStack::split)
        );
        this.addMethod("damage", context -> context.consumer()
                .withParam("count", VarTypes.INTEGER)
                .executes((stack, integer) -> stack.setDamageValue(stack.getDamageValue() + integer))
        );
        this.addMethod("getEnchantmentLevel", context -> context.returning(VarTypes.INTEGER)
                .withParam("enchantment", VarTypes.ENCHANTMENT)
                .executes(ItemStack::getEnchantmentLevel)
        );
        this.addMethod("getEnchantments", context -> context.returning(MapType.getOrCache(VarTypes.ENCHANTMENT, VarTypes.INTEGER))
                .executes(ItemStack::getAllEnchantments)
        );
    }
}