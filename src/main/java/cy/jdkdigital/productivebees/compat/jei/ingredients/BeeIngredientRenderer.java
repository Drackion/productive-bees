//package cy.jdkdigital.productivebees.compat.jei.ingredients;
//
//import cy.jdkdigital.productivebees.client.render.ingredient.BeeRenderer;
//import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee;
//import cy.jdkdigital.productivebees.setup.BeeReloadListener;
//import mezz.jei.api.ingredients.IIngredientRenderer;
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.item.TooltipFlag;
//
//import javax.annotation.Nonnull;
//import java.util.ArrayList;
//import java.util.List;
//
//public class BeeIngredientRenderer implements IIngredientRenderer<BeeIngredient>
//{
//    @Override
//    public void render(GuiGraphics guiGraphics, BeeIngredient ingredient) {
//        Minecraft minecraft = Minecraft.getInstance();
//        if (minecraft.level != null) {
//            BeeRenderer.render(guiGraphics, ingredient, minecraft);
//        }
//    }
//
//    @Nonnull
//    @Override
//    public List<Component> getTooltip(BeeIngredient beeIngredient, TooltipFlag iTooltipFlag) {
//        List<Component> list = new ArrayList<>();
//        CompoundTag nbt = BeeReloadListener.INSTANCE.getData(beeIngredient.getBeeType().toString());
//        if (nbt != null) {
//            list.add(Component.translatable("entity.productivebees." + ProductiveBee.getBeeName(beeIngredient.getBeeType().toString()) + "_bee"));
//        } else {
//            list.add(beeIngredient.getBeeEntity().getDescription());
//        }
//        list.add(Component.literal(beeIngredient.getBeeType().toString()).withStyle(ChatFormatting.DARK_GRAY));
//        return list;
//    }
//}
