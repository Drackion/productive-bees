package cy.jdkdigital.productivebees.compat.hwyla;

import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.common.block.entity.AdvancedBeehiveBlockEntityAbstract;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee;
import cy.jdkdigital.productivebees.util.BeeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public class ProductiveBeeProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor>
{
    public static final ResourceLocation UID = new ResourceLocation(ProductiveBees.MODID, "productive_bee");
    public static final ProductiveBeeProvider INSTANCE = new ProductiveBeeProvider();

    public ProductiveBeeProvider() {
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (config.get(ProductiveBeesWailaPlugin.BEE_ATTRIBUTES) && accessor.getServerData().getBoolean("isProductiveBee")) {
            List<Component> list =  new ArrayList<>();
            BeeHelper.populateBeeInfoFromTag(accessor.getServerData(), list, true);
            for (Component component: list) {
                tooltip.add(component);
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, EntityAccessor entityAccessor) {
        if (entityAccessor.getEntity() instanceof ProductiveBee bee) {
            bee.saveWithoutId(compoundTag);
            compoundTag.putBoolean("isProductiveBee", true);
            AdvancedBeehiveBlockEntityAbstract.removeIgnoredTags(compoundTag);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
