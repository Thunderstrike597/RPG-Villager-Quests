package net.kenji.rpg_villager_quests.events;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.quest_system.capability.PlayerQuestDataProvider;
import net.kenji.rpg_villager_quests.quest_system.capability.QuestCapabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class QuestCapabilityEvents {

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    new ResourceLocation(RpgVillagerQuests.MODID, "player_quests"),
                    new PlayerQuestDataProvider()
            );
        }
    }
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(QuestCapabilities.PLAYER_QUESTS).ifPresent(oldCap -> {
            event.getEntity().getCapability(QuestCapabilities.PLAYER_QUESTS).ifPresent(newCap -> {
                try {
                    newCap.copyFrom(oldCap);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}