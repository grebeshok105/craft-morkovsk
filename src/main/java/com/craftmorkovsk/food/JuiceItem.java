package com.craftmorkovsk.food;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/** A drinkable food item — uses the drink animation/sound instead of eating. */
public class JuiceItem extends FoodItem {

    public JuiceItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }
}
