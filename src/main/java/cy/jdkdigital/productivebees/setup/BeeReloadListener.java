package cy.jdkdigital.productivebees.setup;

import com.google.gson.*;
import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.util.BeeCreator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class BeeReloadListener extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final BeeReloadListener INSTANCE = new BeeReloadListener();
    public ICondition.IContext context;

    private Map<String, CompoundTag> BEE_DATA = new HashMap<>();
    private Map<String, JsonObject> BEE_CONDITIONS = new HashMap<>();

    public BeeReloadListener() {
        super(GSON, "productivebees");
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> map = super.prepare(manager, profiler);

        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            String simpleId = id.getPath().contains("/") ? id.getNamespace() + ":" + id.getPath().substring(id.getPath().lastIndexOf("/") + 1) : id.toString();
            BEE_CONDITIONS.put(simpleId, entry.getValue().getAsJsonObject());
            BEE_CONDITIONS.put(id.toString(), entry.getValue().getAsJsonObject());
        }

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> dataMap, @Nonnull ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("BeeReloadListener");

        RegistryOps<JsonElement> registryOps = this.makeConditionalOps();

        Map<String, CompoundTag> data = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : dataMap.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                var decoded = Recipe.CONDITIONAL_CODEC.parse(registryOps, entry.getValue()).getOrThrow(JsonParseException::new);

                decoded.ifPresentOrElse(r -> {
                    ResourceLocation simpleId = id.getPath().contains("/") ? new ResourceLocation(id.getNamespace(), id.getPath().substring(id.getPath().lastIndexOf("/") + 1)) : id;
                    CompoundTag nbt = BeeCreator.create(simpleId, entry.getValue().getAsJsonObject());

                    int i = id.getPath().lastIndexOf("/");
                    if (i > 0) {
                        String[] a = {id.getPath().substring(0, i), id.getPath().substring(i)};
                        nbt.putString("group", a[0].substring(0, 1).toUpperCase() + a[0].substring(1));
                    } else {
                        nbt.putString("group", "");
                    }

                    data.remove(simpleId.toString());
                    data.put(simpleId.toString(), nbt);

                    ProductiveBees.LOGGER.debug("Adding to bee data " + simpleId);
                }, () -> {
                    ProductiveBees.LOGGER.debug("Skipping loading recipe {} as its conditions were not met", id);
                });
            } catch (Exception e) {
                ProductiveBees.LOGGER.debug("Skipping loading productive bee {} as its conditions were invalid", id);
                throw e;
            }
        }

        setData(data);

        profiler.popPush("BeeReloadListener");
    }

    public CompoundTag getData(String id) {
        return BEE_DATA.get(id);
    }

    public Map<String, CompoundTag> getData() {
        return BEE_DATA;
    }

    public JsonObject getCondition(String id) {
        return BEE_CONDITIONS.get(id);
    }

    public void setData(Map<String, CompoundTag> data) {
        BEE_DATA = data;
        if (ModList.get().isLoaded("patchouli")) {
//            ProductiveBeesPatchouli.setBeeFlags();
        }
    }
}
