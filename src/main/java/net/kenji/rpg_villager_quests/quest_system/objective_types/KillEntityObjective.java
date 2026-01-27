package net.kenji.rpg_villager_quests.quest_system.objective_types;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KillEntityObjective implements QuestObjective {

    private final EntityType<?> entityType;
    private final int count;
    private final String belongingQuestId;

    public int entitiesKilled = 0;

    public KillEntityObjective(ResourceLocation entityType, int count, String belongingQuestId) {
        this.entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityType);
        this.count = count;
        this.belongingQuestId = belongingQuestId;
    }

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            QuestData questData = QuestData.get(player.getUUID());
            if (questData.getActiveQuests() != null) {
                for (QuestInstance questInstance : questData.getActiveQuests()) {
                    if (!questInstance.isComplete()) {
                        if(questInstance.getCurrentStage() instanceof ObjectiveStage objectiveStage){
                            if(objectiveStage.getObjective() instanceof KillEntityObjective killEntityObjective) {
                                if (event.getEntity().getType() == killEntityObjective.entityType){
                                    killEntityObjective.entitiesKilled++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {

        QuestData questData = QuestData.get(event.player.getUUID());
        if (questData.getActiveQuests() != null) {
            for (QuestInstance questInstance : questData.getActiveQuests()) {
                if (!questInstance.isComplete()) {
                    if (questInstance.getCurrentStage() instanceof ObjectiveStage objectiveStage) {
                        if (objectiveStage.getObjective() instanceof KillEntityObjective killEntityObjective) {
                           if(killEntityObjective.canComplete(event.player)){
                               if(objectiveStage.tag != null && objectiveStage.tag.equals("complete_on_kill")){
                                   objectiveStage.onComplete(objectiveStage.getStageEffects(), event.player, questInstance);
                               }
                           }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStartObjective(Player player, QuestInstance questInstance) {

    }

    @Override
    public void onRestartObjective(Player player) {

    }

    @Override
    public boolean canComplete(Player player) {
        if(this.entitiesKilled >= this.count) {
            return true;
        }
        return false;
    }

    @Override
    public void onComplete(QuestEffects effects, Player player) {

    }

    @Override
    public boolean shouldRestartObjective(Player player) {
        return false;
    }
}
