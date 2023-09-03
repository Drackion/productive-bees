package cy.jdkdigital.productivebees.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import cy.jdkdigital.productivebees.common.block.entity.AmberBlockEntity;
import cy.jdkdigital.productivebees.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class AmberItemRenderer extends BlockEntityWithoutLevelRenderer {
    AmberBlockEntity blockEntity = null;

    public AmberItemRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext itemDisplayContext, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLightIn, int packedOverlay) {
        if (blockEntity == null) {
            blockEntity = new AmberBlockEntity(BlockPos.ZERO, ModBlocks.AMBER.get().defaultBlockState());
        }

        blockEntity.loadPacketNBT(stack.getTag());

        poseStack.pushPose();

        Minecraft.getInstance().getBlockEntityRenderDispatcher().render(blockEntity, 0, poseStack, buffer);

//        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockEntity.getBlockState(), poseStack, buffer, packedLightIn, packedOverlay, ModelData.EMPTY, null);
//        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(blockEntity, poseStack, buffer, packedLightIn, packedOverlay);

        poseStack.popPose();
    }
}
