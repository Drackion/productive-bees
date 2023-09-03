package cy.jdkdigital.productivebees;

import com.mojang.serialization.Codec;
import cy.jdkdigital.productivebees.common.crafting.conditions.BeeExistsCondition;
import cy.jdkdigital.productivebees.common.crafting.conditions.FluidTagEmptyCondition;
import cy.jdkdigital.productivebees.common.entity.bee.ConfigurableBee;
import cy.jdkdigital.productivebees.compat.jei.ingredients.BeeIngredientFactory;
import cy.jdkdigital.productivebees.compat.top.TopPlugin;
import cy.jdkdigital.productivebees.dispenser.CageDispenseBehavior;
import cy.jdkdigital.productivebees.dispenser.ShearsDispenseItemBehavior;
import cy.jdkdigital.productivebees.event.EventHandler;
import cy.jdkdigital.productivebees.event.loot.IngredientModifier;
import cy.jdkdigital.productivebees.event.loot.ItemLootModifier;
import cy.jdkdigital.productivebees.init.*;
import cy.jdkdigital.productivebees.loot.LootItemKilledByUUIDCondition;
import cy.jdkdigital.productivebees.loot.OptionalLootItem;
import cy.jdkdigital.productivebees.loot.condition.OptionalCopyBlockState;
import cy.jdkdigital.productivebees.network.PacketHandler;
import cy.jdkdigital.productivebees.network.packets.Messages;
import cy.jdkdigital.productivebees.setup.BeeReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ProductiveBees.MODID)
@EventBusSubscriber(modid = ProductiveBees.MODID)
public final class ProductiveBees
{
    public static final String MODID = "productivebees";
    public static final RandomSource random = RandomSource.create();
    public static boolean isDevEnv = false;

    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRIES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, MODID);
    public static final DeferredRegister<LootItemFunctionType> LOOT_POOL_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_POOL_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MODID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ProductiveBees.MODID);

    public static final RegistryObject<Codec<ItemLootModifier>> ITEM_MODIFIER = LOOT_SERIALIZERS.register("item_modifier", ItemLootModifier.CODEC);
    public static final RegistryObject<Codec<IngredientModifier>> INGREDIENT_MODIFIER = LOOT_SERIALIZERS.register("ingredient_modifier", IngredientModifier.CODEC);

    public static final RegistryObject<LootPoolEntryType> OPTIONAL_LOOT_ITEM = LOOT_POOL_ENTRIES.register("optional_loot_item", () -> new LootPoolEntryType(new OptionalLootItem.Serializer()));
    public static final RegistryObject<LootItemFunctionType> OPTIONAL_BLOCK_STATE_PROPERTY = LOOT_POOL_FUNCTIONS.register("optional_copy_block_state", () -> new LootItemFunctionType(new OptionalCopyBlockState.Serializer()));
    public static final RegistryObject<LootItemConditionType> KILLED_BY_UUID = LOOT_POOL_CONDITIONS.register("killed_by_uuid", () -> new LootItemConditionType(new LootItemKilledByUUIDCondition.Serializer()));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(MODID, MODID));
    public static RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(MODID, () -> {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(Items.BEE_NEST))
                .title(Component.literal("Productive Bees"))
                .build();
    });

    public ProductiveBees() {
        ModBlocks.registerHives();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onDataSync);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityAttacked);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityHurt);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModPointOfInterestTypes.POI_TYPES.register(modEventBus);
        ModProfessions.PROFESSIONS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.HIVE_BEES.register(modEventBus);
        ModEntities.SOLITARY_BEES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);
        ModContainerTypes.CONTAINER_TYPES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModFeatures.TREE_DECORATORS.register(modEventBus);
        ModFeatures.BIOME_MODIFIERS.register(modEventBus);
        ModRecipeTypes.RECIPE_SERIALIZERS.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);
        LOOT_SERIALIZERS.register(modEventBus);
        LOOT_POOL_ENTRIES.register(modEventBus);
        LOOT_POOL_FUNCTIONS.register(modEventBus);
        LOOT_POOL_CONDITIONS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::onInterModEnqueue);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(EventHandler::onEntityAttributeCreate);
        modEventBus.addListener(EventHandler::tabContents);

        // Config loading
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ProductiveBeesConfig.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ProductiveBeesConfig.CLIENT_CONFIG);

        CraftingHelper.register(FluidTagEmptyCondition.Serializer.INSTANCE);
        CraftingHelper.register(BeeExistsCondition.Serializer.INSTANCE);

        ForgeMod.enableMilkFluid();
    }

    public void onInterModEnqueue(InterModEnqueueEvent event) {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", TopPlugin::new);
    }

    public void onServerStarting(AddReloadListenerEvent event) {
        BeeReloadListener.INSTANCE.context = event.getConditionContext();;
        event.addListener(BeeReloadListener.INSTANCE);
    }

    private void onEntityAttacked(LivingAttackEvent event) {
        if (event.getEntity() instanceof ConfigurableBee bee) {
            if (bee.isIrradiated() && event.getSource().getMsgId().equals("mekanism.radiation")) {
                if (bee.breathCollectionCooldown < 0) {
                    bee.breathCollectionCooldown = 600;
                    bee.internalSetHasNectar(true);
                } else {
                    bee.breathCollectionCooldown-= event.getAmount();
                }
                event.setCanceled(true);
                bee.level().broadcastEntityEvent(bee, (byte) 2);
            }
        }
    }

    private void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ConfigurableBee bee) {
            if (
                    event.getSource().getMsgId().equals("mekanism.radiation") &&
                    bee.getBeeType().equals("productivebees:radioactive") &&
                    ProductiveBeesConfig.BEES.deadBeeConvertChance.get() > event.getEntity().level().random.nextDouble() &&
                    BeeIngredientFactory.getIngredient("productivebees:wasted_radioactive").get() != null
            ) {
                event.setCanceled(true);
                bee.setHealth(bee.getMaxHealth());
                bee.setBeeType("productivebees:wasted_radioactive");
            }
        }
    }

    private void onEntityHurt(LivingHurtEvent event) {
        Entity damageSource = event.getSource().getEntity();
        if (damageSource instanceof LivingEntity attacker && event.getEntity() instanceof Player player) {
            boolean isWearingBeeHelmet = false;
            ItemStack itemstack = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!itemstack.isEmpty() && itemstack.getItem().equals(ModItems.BEE_NEST_DIAMOND_HELMET.get())) {
                isWearingBeeHelmet = true;
            }

            if (isWearingBeeHelmet && player.level().random.nextDouble() < ProductiveBeesConfig.BEES.kamikazBeeChance.get()) {
                Level level = player.level();
                ConfigurableBee bee = ModEntities.CONFIGURABLE_BEE.get().create(level);
                BlockPos pos = player.blockPosition();
                if (bee != null) {
                    bee.setBeeType("productivebees:kamikaz");
                    bee.setDefaultAttributes();
                    bee.setTarget(attacker);
                    bee.moveTo(pos.getX(), pos.getY() + 0.5, pos.getZ(), bee.getYRot(), bee.getXRot());

                    level.addParticle(ParticleTypes.POOF, pos.getX(), pos.getY() + 1, pos.getZ(), 0.2D, 0.1D, 0.2D);
                    level.playSound(player, pos, SoundEvents.BEE_HURT, SoundSource.NEUTRAL, 1.0F, 1.0F);

                    level.addFreshEntity(bee);
                }
            }
        }
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
        ModAdvancements.register();
        ModProfessions.register();

        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(ModItems.BEE_CAGE.get(), new CageDispenseBehavior());
            DispenserBlock.registerBehavior(ModItems.STURDY_BEE_CAGE.get(), new CageDispenseBehavior());
            DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenseItemBehavior());
        });
    }

    private void onDataSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.sendToAllPlayers(new Messages.BeeDataMessage(BeeReloadListener.INSTANCE.getData()));
        } else {
            PacketHandler.sendBeeDataToPlayer(new Messages.BeeDataMessage(BeeReloadListener.INSTANCE.getData()), event.getPlayer());
        }
    }
}
