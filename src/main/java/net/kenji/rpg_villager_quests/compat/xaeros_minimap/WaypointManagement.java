package net.kenji.rpg_villager_quests.compat.xaeros_minimap;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.events.QuestStageEvents;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.waypoints.WaypointInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jline.utils.Log;
import xaero.common.core.XaeroMinimapCore;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointWorld;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.minimap.XaeroMinimap;

import java.util.*;


public class WaypointManagement {

    public static void init(){
        MinecraftForge.EVENT_BUS.register(new WaypointEventHandler());
    }

    public static class WaypointEventHandler {
        // Map quest instances to their waypoints for easier tracking

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (!event.player.level().isClientSide()) return;

            // Only run every 20 ticks (once per second) to reduce overhead
            if (event.player.tickCount % 20 != 0) return;

            QuestData questData = QuestData.get(event.player);
            var instances = questData.getActiveQuests();

            if (instances != null && !instances.isEmpty()) {
                for (QuestInstance questInstance : instances) {
                    WaypointInstance waypointInstance = questInstance.currentQuestWaypoint;

                    if (waypointInstance != null && !waypointInstance.hasCreatedCompatWaypoint) {
                        createXaerosWaypoint(waypointInstance, questInstance);
                        waypointInstance.hasCreatedCompatWaypoint = true;
                    }
                    if (waypointInstance != null) {
                        updateWaypoint(event.player, questInstance);
                    }

                    if (waypointInstance == null) {
                        deleteAllWaypoints(questInstance.getQuest());
                    }
                }
                return;
            }
            Collection<Quest> quests = VillagerQuestManager.villagerQuestMap.values();
            for (Quest quest : quests) {
                deleteAllWaypoints(quest);
            }
        }

        @SubscribeEvent
        public static void onStageStart(QuestStageEvents.StageStartEvent event) {
            if (event.getQuestInstance().currentQuestWaypoint != null) {
                createXaerosWaypoint(event.getQuestInstance().currentQuestWaypoint, event.getQuestInstance());
                event.getQuestInstance().currentQuestWaypoint.hasCreatedCompatWaypoint = true;
            }
        }

        @SubscribeEvent
        public void onStageCompleted(QuestStageEvents.StageCompleteEvent event) {
            QuestStage completedStage = event.getCompletedStage(); // Get the completing stage

            deleteWaypointByName(completedStage.displayName); // Delete by stage name
        }

        private static void updateWaypoint(Player player, QuestInstance questInstance) {
            WaypointsManager manager = XaeroMinimapCore.currentSession.getWaypointsManager();
            if (manager != null) {
                WaypointWorld waypointWorld = manager.getCurrentWorld();
                WaypointInstance waypointInstance = questInstance.currentQuestWaypoint;
                if(waypointInstance.waypointEntityUuid == null) return;
                for(Waypoint waypoint : waypointWorld.getCurrentWaypointSet().getWaypoints()){
                    if(waypoint.getNameSafe("NONE").equals(waypointInstance.waypointName)){
                        Entity waypointEntity = null;
                        if(player.level().isClientSide()){
                           Minecraft mc = Minecraft.getInstance();
                            for(Entity entity : mc.level.entitiesForRendering()){
                                if(entity.getUUID().equals(waypointInstance.waypointEntityUuid)){
                                    waypointEntity = entity;
                                    break;
                                }
                            }
                        }
                        if(waypointEntity != null){
                            waypoint.setX(waypointEntity.getBlockX());
                            waypoint.setY(waypointEntity.getBlockY());
                            waypoint.setZ(waypointEntity.getBlockZ());
                        }
                    }
                }
            }
        }


        private static void deleteWaypointByName(String waypointName) {
            try {
                WaypointsManager manager = XaeroMinimapCore.currentSession.getWaypointsManager();
                if(manager != null) {
                    WaypointWorld waypointWorld = manager.getCurrentWorld();
                    if(waypointWorld != null) {
                        Waypoint waypointToDelete = null;
                        for (Waypoint waypoint : waypointWorld.getCurrentWaypointSet().getWaypoints()) {
                            if (waypoint.getNameSafe("NONE").equals(waypointName)) {
                                waypointToDelete = waypoint;
                                break;
                            }
                        }
                        if(waypointToDelete != null){
                            waypointWorld.getCurrentWaypointSet().remove(waypointToDelete);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

            private static void deleteAllWaypoints(Quest quest){
            try {
                XaeroMinimap minimap = XaeroMinimap.instance;
                if (minimap == null) return;

                WaypointsManager manager = XaeroMinimapCore.currentSession.getWaypointsManager();
                if(manager != null) {
                    WaypointWorld waypointWorld = manager.getCurrentWorld();
                    if(waypointWorld != null) {
                        List<Waypoint> waypoints = new ArrayList<>();
                        for(Waypoint waypoint : waypointWorld.getCurrentWaypointSet().getWaypoints()){
                            for(QuestStage stage : quest.stages){
                                if(waypoint.getNameSafe("NONE").equals(stage.displayName))
                                    waypoints.add(waypoint);
                                }
                            }

                        if(!waypoints.isEmpty()) {
                            for (Waypoint waypoint : waypoints) {
                                waypointWorld.getCurrentSet().remove(waypoint);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void createXaerosWaypoint(WaypointInstance waypointInstance, QuestInstance questInstance) {
            try {
                XaeroMinimap minimap = XaeroMinimap.instance;
                if (minimap == null) return;

                WaypointsManager manager = XaeroMinimapCore.currentSession.getWaypointsManager();
                if(manager != null){
                    WaypointWorld waypointWorld = manager.getCurrentWorld();

                    if (waypointWorld != null) {
                        Waypoint waypoint = new Waypoint(
                                waypointInstance.x,
                                waypointInstance.y,
                                waypointInstance.z,
                                waypointInstance.waypointName,
                                "O",
                                questInstance.getCurrentStage().waypointColorIndex
                        );

                        waypointWorld.getCurrentSet().add(waypoint);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}