package com.craftmorkovsk.machine.framework;

import com.craftmorkovsk.CraftMorkovsk;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

/** One-input/one-output processing recipe shared by all processor machines.
 *  JSON: { "type": "craftmorkovsk:processing", "machine": "grain_mill",
 *          "input": {ingredient}, "output": {item,count}, "time": 200, "energy": 20 } */
public record ProcessingRecipe(ResourceLocation id, String machine, Ingredient input,
                               ItemStack output, int time, int energy) implements Recipe<Container> {

    public static final String TYPE_ID = "processing";

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) { return true; }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output;
    }

    @Override
    public ResourceLocation getId() { return id; }

    @Override
    public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }

    @Override
    public RecipeType<?> getType() { return Type.INSTANCE; }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    public static final class Type implements RecipeType<ProcessingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = TYPE_ID;
        private Type() {}
    }

    public static final class Serializer implements RecipeSerializer<ProcessingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(CraftMorkovsk.MOD_ID, TYPE_ID);
        private Serializer() {}

        @Override
        public ProcessingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String machine = GsonHelper.getAsString(json, "machine");
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            int time = GsonHelper.getAsInt(json, "time", 200);
            int energy = GsonHelper.getAsInt(json, "energy", 20);
            return new ProcessingRecipe(id, machine, input, output, time, energy);
        }

        @Override
        public ProcessingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new ProcessingRecipe(id, buf.readUtf(), Ingredient.fromNetwork(buf),
                    buf.readItem(), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ProcessingRecipe recipe) {
            buf.writeUtf(recipe.machine);
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.output);
            buf.writeVarInt(recipe.time);
            buf.writeVarInt(recipe.energy);
        }
    }
}
