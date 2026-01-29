package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Quest {
    public final String id;
    public final String displayName;
    public final String type;
    public final List<QuestStage> stages;

    public final Dialogue completionDialogue;


    public Quest(String id, String displayName, String type, List<QuestStage> stages, Dialogue completionDialogue) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.stages = stages;
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
    }

    public String getQuestId(){
        return this.id;
    }

    public List<Page> getCompletionDialogue(QuestInstance questInstance){
        if(questInstance.getQuestReputation() == Reputation.GOOD){
            return completionDialogue.outcome.pages;
        }
        else if(questInstance.getQuestReputation() == Reputation.BAD){
            if(completionDialogue.altOutcome == null)
                return completionDialogue.outcome.pages;
            return completionDialogue.altOutcome.pages;
        }
        return completionDialogue.outcome.pages;
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

