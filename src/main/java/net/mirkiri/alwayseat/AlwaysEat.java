package net.mirkiri.alwayseat;

import java.util.Objects;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

@Mod(AlwaysEat.MOD_ID)
public class AlwaysEat {

    public static final String MOD_ID = "alwayseat";

    static HashMap<Item, Boolean> defaultValue = new HashMap<>(39);

    public AlwaysEat() {
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedInEvent);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEvents::new);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG);
    }

    public void onPlayerLoggedInEvent(ServerStartedEvent event) {
        updateFoodItems();
    }

    public static void updateFoodItems() {
        for (Item item : ForgeRegistries.ITEMS) {
            FoodProperties food = item.getFoodProperties();
            if (food != null) {
                if (!defaultValue.containsKey(item)) {
                    defaultValue.put(item, food.canAlwaysEat);
                }

                String registryName = Objects.requireNonNull(item.getRegistryName()).toString();

                // In blacklist mode all items except the ones in the list will be set to true
                if (Config.MODE.get() == Config.Mode.BLACKLIST) {
                    if (!Config.ITEM_LIST.get().contains(registryName)) {
                        food.canAlwaysEat = true;
                    } else {
                        food.canAlwaysEat = defaultValue.get(item);
                    }
                } else {
                    // In whitelist mode only items in the list will be set to true
                    if (Config.ITEM_LIST.get().contains(registryName)) {
                        food.canAlwaysEat = true;
                    } else {
                        food.canAlwaysEat = defaultValue.get(item);
                    }
                }

                // If an item is in the uneatable items list always set it to false
                if (Config.UNEATABLE_ITEMS.get().contains(registryName)) {
                    food.canAlwaysEat = false;
                }
            }
        }
    }

}
