package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Quest {
    public final String id;
    public final String displayName;
    public final String type;
    public final List<QuestStage> stages;
    public final Dialogue reconsiderDialogue;
    public final Dialogue completionDialogue;
    public final VillagerProfession questProfession;
    public final boolean isGlobalQuest;

    public Quest(String id, String displayName, String type, List<QuestStage> stages, Dialogue completionDialogue, Dialogue reconsiderDialogue, String villagerProfession, boolean isGlobalQuest) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.stages = stages;
        this.isGlobalQuest = isGlobalQuest;
        if(reconsiderDialogue != null){
            this.reconsiderDialogue = reconsiderDialogue;
        }
        else{
            List<Page> mainPages = new ArrayList<>();
            List<Page> posPages = new ArrayList<>();
            List<Page> negPages = new ArrayList<>();

            Page mainPage = new Page();
            mainPage.text = "You're back? Have you changed your mind?";
            mainPages.add(mainPage);

            Dialogue.Outcome mainOutcome = new Dialogue.Outcome(mainPages);

            this.reconsiderDialogue = new Dialogue(null,null , mainOutcome);
        }
        if(completionDialogue != null)
            this.completionDialogue = completionDialogue;
        else{

            Page defaultPage = new Page();
            defaultPage.text = "That's all for now!";
            defaultPage.button1Text = "NONE";
            defaultPage.button2Text = "Ok";
            List<Page> defaultPageList = new ArrayList<>();
            defaultPageList.add(defaultPage);

            Dialogue.Outcome defaultOutcome = new Dialogue.Outcome(defaultPageList);
            this.completionDialogue = new Dialogue(defaultOutcome, null);
        }
        if(!Objects.equals(villagerProfession, "generic")){
            for (VillagerProfession profession : ForgeRegistries.VILLAGER_PROFESSIONS.getValues()) {
                ResourceLocation key = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession);
                if (key != null && key.getPath().equals(villagerProfession)) {
                    questProfession = profession;
                    return;
                }
            }
        }
        questProfession = null;
    }

    public String getQuestId(){
        return this.id;
    }
    public List<Page> getQuedTemporaryDialogue(QuestInstance questInstance){
            if(questInstance.getQuedTemporaryDialogue() != null) {
                return questInstance.getQuedTemporaryDialogue();
            }
        return null;
    }
    public List<Page> getCompletionDialogue(QuestInstance questInstance) {
        if (completionDialogue.positive != null && completionDialogue.negative != null) {
            if (questInstance.getQuestReputation() == Reputation.GOOD) {
                return completionDialogue.positive.pages;
            } else if (questInstance.getQuestReputation() == Reputation.BAD) {
                return completionDialogue.negative.pages;
            }
        }
        return completionDialogue.main.pages;
    }

    public QuestStage getStageById(String id){
        for(QuestStage stage : stages){
            if(Objects.equals(stage.id, id)){
                return stage;
            }
        }
        return null;
    }


    public void onQuestComplete(QuestInstance questInstance, QuestEffects completionEffects, QuestStage completionStage, Player player){

    }

    public QuestInstance startQuest(ServerPlayer player, UUID villager){
        QuestData questData = QuestData.get(player);
        return questData.startQuest(this, villager, player);
    }

}

