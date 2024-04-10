package cy.jdkdigital.productivebees.init;

import cy.jdkdigital.productivebees.ProductiveBees;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ProductiveBees.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModPointOfInterestTypes
{
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, ProductiveBees.MODID);

    public static final RegistryObject<PoiType> ADVANCED_HIVES = register("advanced_beehive", () -> {
        List<RegistryObject<Block>> blocks = new ArrayList<>();

        ModBlocks.HIVES.forEach((s, registryObject) -> {
            blocks.add((RegistryObject<Block>) registryObject);
        });

        return blocks;
    }, 1);
    
    public static final RegistryObject<PoiType> SOLITARY_HIVE = register("solitary_hive", ModBlocks.BAMBOO_HIVE, 1);
    public static final RegistryObject<PoiType> SOLITARY_NEST = register("solitary_nest", () -> {
        List<RegistryObject<Block>> blocks = new ArrayList<>();
        blocks.add(ModBlocks.STONE_NEST);
        blocks.add(ModBlocks.SAND_NEST);
        blocks.add(ModBlocks.SNOW_NEST);
        blocks.add(ModBlocks.COARSE_DIRT_NEST);
        blocks.add(ModBlocks.GRAVEL_NEST);
        blocks.add(ModBlocks.SLIMY_NEST);
        blocks.add(ModBlocks.NETHER_BRICK_NEST);
        blocks.add(ModBlocks.NETHER_QUARTZ_NEST);
        blocks.add(ModBlocks.NETHER_GOLD_NEST);
        blocks.add(ModBlocks.GLOWSTONE_NEST);
        blocks.add(ModBlocks.SOUL_SAND_NEST);
        blocks.add(ModBlocks.SUGAR_CANE_NEST);
        blocks.add(ModBlocks.END_NEST);
        blocks.add(ModBlocks.OAK_WOOD_NEST);
        blocks.add(ModBlocks.BIRCH_WOOD_NEST);
        blocks.add(ModBlocks.SPRUCE_WOOD_NEST);
        blocks.add(ModBlocks.DARK_OAK_WOOD_NEST);
        blocks.add(ModBlocks.JUNGLE_WOOD_NEST);
        blocks.add(ModBlocks.ACACIA_WOOD_NEST);
        blocks.add(ModBlocks.CHERRY_WOOD_NEST);
        blocks.add(ModBlocks.MANGROVE_WOOD_NEST);
        return blocks;
    }, 1);

    public static final RegistryObject<PoiType> DRACONIC_NEST = register("draconic_nest", () -> {
        List<RegistryObject<Block>> blocks = new ArrayList<>();
        blocks.add(ModBlocks.DRAGON_EGG_HIVE);
        blocks.add(ModBlocks.OBSIDIAN_PILLAR_NEST);
        return blocks;
    }, 1);

    public static final RegistryObject<PoiType> SUGARBAG_NEST = register("sugarbag_nest", () -> {
        List<RegistryObject<Block>> blocks = new ArrayList<>();
        blocks.add(ModBlocks.SUGARBAG_NEST);
        return blocks;
    }, 1);

    public static final RegistryObject<PoiType> NETHER_NEST = register("nether_nest", () -> {
        List<RegistryObject<Block>> blocks = new ArrayList<>();
        blocks.add(ModBlocks.CRIMSON_BEE_NEST);
        blocks.add(ModBlocks.WARPED_BEE_NEST);
        return blocks;
    }, 1);

    public static final RegistryObject<PoiType> BUMBLE_BEE_NEST = register("bumble_bee_nest", () -> {
        List<RegistryObject<Block>> blocks = new ArrayList<>();
        blocks.add(ModBlocks.BUMBLE_BEE_NEST);
        return blocks;
    }, 1);

    private static RegistryObject<PoiType> register(String name, RegistryObject<Block> block, int maxFreeTickets) {
        List<RegistryObject<Block>> blocks = new ArrayList<>();
        blocks.add(block);
        return register(name, blocks, maxFreeTickets);
    }

    private static RegistryObject<PoiType> register(String name, Supplier<List<RegistryObject<Block>>> supplier, int maxFreeTickets) {
        return register(name, supplier.get(), maxFreeTickets);
    }

    private static RegistryObject<PoiType> register(String name, List<RegistryObject<Block>> blocks, int maxFreeTickets) {
        return POI_TYPES.register(name, () -> {
            Set<BlockState> blockStates = new HashSet<>();
            for (RegistryObject<Block> block : blocks) {
                blockStates.addAll(block.get().getStateDefinition().getPossibleStates());
            }
            return new PoiType(blockStates, maxFreeTickets, 1);
        });
    }
}
