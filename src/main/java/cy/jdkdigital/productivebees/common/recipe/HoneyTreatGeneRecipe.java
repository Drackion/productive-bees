package cy.jdkdigital.productivebees.common.recipe;

import com.google.gson.JsonObject;
import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.common.item.Gene;
import cy.jdkdigital.productivebees.common.item.HoneyTreat;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import cy.jdkdigital.productivebees.setup.BeeReloadListener;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoneyTreatGeneRecipe implements CraftingRecipe
{
    public final ResourceLocation id;
    public final ItemStack honeyTreat;

    public HoneyTreatGeneRecipe(ResourceLocation id, ItemStack honeyTreat) {
        this.id = id;
        this.honeyTreat = honeyTreat;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        // Valid if inv contains 1 honey treat and any number of genes
        // genes must not be mutually exclusive (2 levels of the same attribute are not allowed)
        Map<String, Integer> addedGenes = new HashMap<>();
        ItemStack honeyTreatStack = null;
        boolean hasAddedGenes = false;
        for (int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem().equals(ModItems.HONEY_TREAT.get()) && honeyTreatStack == null) {
                    honeyTreatStack = itemstack;
                    // Read existing attributes from treat
                    ListTag genes = HoneyTreat.getGenes(honeyTreatStack);
                    for (Tag inbt : genes) {
                        ItemStack insertedGene = ItemStack.of((CompoundTag) inbt);
                        String attribute = Gene.getAttributeName(insertedGene);
                        if (addedGenes.containsKey(attribute) && !addedGenes.get(attribute).equals(Gene.getValue(insertedGene))) {
                            return false;
                        }
                        addedGenes.put(attribute, Gene.getValue(insertedGene));
                    }
                } else if (itemstack.getItem().equals(ModItems.GENE.get())) {
                    String attribute = Gene.getAttributeName(itemstack);
                    boolean isTypeGene = BeeReloadListener.INSTANCE.getData(attribute) != null;

                    // Treats can have either 1 type gene or a mix of other genes
                    if (isTypeGene) {
                        if (addedGenes.size() > 0 && !addedGenes.containsKey(attribute)) {
                            return false;
                        }
                        addedGenes.put(attribute, Gene.getValue(itemstack));
                        hasAddedGenes = true;
                    } else {
                        if (addedGenes.containsKey(attribute) && !addedGenes.get(attribute).equals(Gene.getValue(itemstack))) {
                            // Disallow adding genes of the same type with different strengths
                            return false;
                        } else {
                            addedGenes.put(attribute, Gene.getValue(itemstack));
                            hasAddedGenes = true;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        if (honeyTreatStack == null) {
            return false;
        }
        return hasAddedGenes;
    }

    @Nonnull
    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        // Combine genes with honey treat
        ItemStack treat = null;
        List<ItemStack> genes = new ArrayList<>();

        for (int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem().equals(ModItems.HONEY_TREAT.get())) {
                    treat = itemstack;
                } else if (itemstack.getItem().equals(ModItems.GENE.get())) {
                    genes.add(itemstack);
                }
            }
        }

        if (treat != null) {
            final ItemStack honeyTreat = treat.copy();
            genes.forEach(gene -> {
                HoneyTreat.addGene(honeyTreat, gene);
            });
            honeyTreat.setCount(1);

            return honeyTreat;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return this.honeyTreat;
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        list.add(Ingredient.of(honeyTreat.copy()));
        list.add(Ingredient.of(new ItemStack(ModItems.GENE.get())));

        return list;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.GENE_TREAT.get();
    }

    public static class Serializer<T extends HoneyTreatGeneRecipe> implements RecipeSerializer<T>
    {
        final HoneyTreatGeneRecipe.Serializer.IRecipeFactory<T> factory;

        public Serializer(HoneyTreatGeneRecipe.Serializer.IRecipeFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T fromJson(ResourceLocation id, JsonObject json) {
            return this.factory.create(id, new ItemStack(ModItems.HONEY_TREAT.get()));
        }

        public T fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buffer) {
            try {
                return this.factory.create(id, buffer.readItem());
            } catch (Exception e) {
                ProductiveBees.LOGGER.error("Error reading honey treat gene recipe from packet. " + id, e);
                throw e;
            }
        }

        public void toNetwork(@Nonnull FriendlyByteBuf buffer, T recipe) {
            try {
                buffer.writeItem(recipe.honeyTreat);
            } catch (Exception e) {
                ProductiveBees.LOGGER.error("Error writing honey treat gene recipe to packet. " + recipe.getId(), e);
                throw e;
            }
        }

        public interface IRecipeFactory<T extends HoneyTreatGeneRecipe>
        {
            T create(ResourceLocation id, ItemStack honeyTreat);
        }
    }
}