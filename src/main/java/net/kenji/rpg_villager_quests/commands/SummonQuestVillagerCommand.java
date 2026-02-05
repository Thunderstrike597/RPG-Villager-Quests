package net.kenji.rpg_villager_quests.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kenji.rpg_villager_quests.entity.villager.VillagerQuestTypes;
import net.kenji.rpg_villager_quests.events.QuestVillagerEvents;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;


public class SummonQuestVillagerCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("summon_quest_villager")
                .then(
                        Commands.argument("quest_id", StringArgumentType.string())
                                .suggests((ctx, builder) -> {

                                    VillagerQuestManager.rawJsonFiles.keySet()
                                            .forEach(builder::suggest);

                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {

                                    String questId = StringArgumentType.getString(ctx, "quest_id");

                                    executeSummonQuestVillager(ctx, questId);
                                    return 1;
                                })
                );
    }
    private static void executeSummonQuestVillager(CommandContext<CommandSourceStack> ctx, String questId){
        Quest quest = VillagerQuestManager.getQuestByName(questId);

        if(quest != null && ctx.getSource().getEntity() != null){
            LivingEntity livingEntity = EntityType.VILLAGER.spawn(ctx.getSource().getLevel(), ctx.getSource().getEntity().blockPosition(), MobSpawnType.TRIGGERED);
            if(livingEntity instanceof Villager villager) {
                VillagerQuestManager.assignQuestToVillager(quest, villager);
                villager.getPersistentData().putBoolean(QuestVillagerEvents.IS_QUEST_VILLAGER_TAG, true);
                villager.getPersistentData().putString(QuestVillagerEvents.QUEST_VILLAGER_TAG, questId);
                villager.setVillagerData(
                        villager.getVillagerData().setType(VillagerQuestTypes.QUEST_VILLAGER)
                );
                if (quest.questProfession != null) {
                    villager.setVillagerData(
                            villager.getVillagerData().setProfession(quest.questProfession)
                                    .setLevel(2)
                    );
                }
                ctx.getSource().sendSuccess(
                        () -> Component.literal("Summoning quest villager for quest: " + questId),
                        false
                );
            }
            else{
                ctx.getSource().sendFailure(Component.literal("Summoning quest villager for quest: " + questId));
            }
        }
    }
}