package cy.jdkdigital.productivebees.common.item;

import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee;
import cy.jdkdigital.productivebees.init.ModDataComponents;
import cy.jdkdigital.productivebees.setup.BeeReloadListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class CombBlockItem extends BlockItem
{
    public CombBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static int getColor(ItemStack stack) {
        var tag = stack.get(ModDataComponents.BEE_TYPE);
        if (tag != null) {
            CompoundTag nbt = BeeReloadListener.INSTANCE.getData(tag);
            if (nbt != null) {
                return nbt.getInt("primaryColor");
            }
        }
        return -1;
    }

    @Nonnull
    @Override
    public Component getName(ItemStack stack) {
        var type = stack.get(ModDataComponents.BEE_TYPE);
        if (type != null) {
            CompoundTag nbt = BeeReloadListener.INSTANCE.getData(type);
            if (nbt != null) {
                String name = Component.translatable("entity.productivebees." + ProductiveBee.getBeeName(type) + "_bee").getString();
                return Component.translatable("block.productivebees.comb_configurable", name.replace(" Bee", ""));
            }
        }
        return super.getName(stack);
    }
}
