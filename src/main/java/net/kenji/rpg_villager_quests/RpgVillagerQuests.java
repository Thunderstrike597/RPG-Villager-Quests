package net.kenji.rpg_villager_quests;

import com.mojang.logging.LogUtils;
import net.kenji.rpg_villager_quests.compat.xaeros_minimap.WaypointManagement;
import net.kenji.rpg_villager_quests.entity.villager.VillagerQuestTypes;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import xaero.minimap.XaeroMinimap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RpgVillagerQuests.MODID)
public class RpgVillagerQuests {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "rpg_villager_quests";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean isXaerosMinimapLoaded = false;
    public RpgVillagerQuests() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);


        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        isXaerosMinimapLoaded = ModList.get().isLoaded("xaerominimap");


        VillagerQuestManager.JsonHelper.init();
        event.enqueueWork(ModPacketHandler::register);
        VillagerQuestTypes.QUEST_VILLAGER = VillagerQuestTypes.registerVillageType("quest_villager");
        if(isXaerosMinimapLoaded){
            WaypointManagement.init();
        }
    }



    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server_side starts
        LOGGER.info("HELLO from server_side starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
