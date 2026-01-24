package net.kenji.rpg_villager_quests.quest_system;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.objective_types.CollectItemObjective;
import net.kenji.rpg_villager_quests.quest_system.reward_types.ItemReward;
import net.kenji.rpg_villager_quests.quest_system.reward_types.XpReward;
import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestLoader {

    public static Quest load(JsonObject json) throws Exception {
        String id = json.get("id").getAsString();

        String displayName = json.get("quest_display_name").getAsString();
        String type = json.get("type").getAsString();

        List<QuestStage> stages = new ArrayList<>();

        for (JsonElement elem : json.getAsJsonArray("stages")) {
            JsonObject stage = elem.getAsJsonObject();
            String stageType = stage.get("type").getAsString();
            String stageId = stage.get("id").getAsString();

            switch (stageType) {
                case "dialogue" -> stages.add(parseDialogue(stageId, stage, id));
                case "objective" -> stages.add(parseObjective(stageId, stage, id));
            }
        }

        QuestReward questReward = null;
        if (json.has("quest_reward")) {
            JsonObject questRewardObject = json.getAsJsonObject("quest_reward");
            QuestRewardType rewardType;
            try {
                rewardType = QuestRewardType.valueOf(questRewardObject.get("type").getAsString().toUpperCase());
            }catch (Exception e){
                throw new Exception("No Reward Type of: " + questRewardObject.get("type").getAsString());
            }

            switch (rewardType){
                case ITEM_REWARD -> {

                    if(questRewardObject.has("item")) {
                        ResourceLocation item = new ResourceLocation(questRewardObject.get("item").getAsString());
                        int count = 0;
                        if (questRewardObject.has("count")) {
                            count = questRewardObject.get("count").getAsInt();
                            questReward = new ItemReward(item, count);
                        }
                    }
                }
                case XP_REWARD -> {
                    if (questRewardObject.has("amount")) {
                        int amount = questRewardObject.get("amount").getAsInt();
                        questReward = new XpReward(amount);
                    }
                }
           }
        }
        Dialogue.Outcome outcome = null;
        Dialogue.Outcome altOutcome = null;
        if(json.has("completion_dialogue")) {
            JsonObject completionDialogueObj = json.getAsJsonObject("completion_dialogue");
            if (completionDialogueObj.has("outcome")) {
                JsonObject outComeObj = completionDialogueObj.getAsJsonObject("outcome");
                if (outComeObj.has("pages")) {
                    JsonArray pagesArray = outComeObj.getAsJsonArray("pages");
                    List<Page> pages = new ArrayList<>();
                    for(JsonElement pageElem : pagesArray){
                        JsonObject pageObj = pageElem.getAsJsonObject();
                        Page page = new Page();
                        if(pageObj.has("text")){
                            page.text = pageObj.get("text").getAsString();
                        }
                        if(pageObj.has("button_1_text")){
                            page.button1Text = pageObj.get("button_1_text").getAsString();
                        }
                        if(pageObj.has("button_2_text")){
                            page.button2Text = pageObj.get("button_2_text").getAsString();
                        }
                        pages.add(page);
                    }
                    outcome = new Dialogue.Outcome(pages);
                }
            }
            if (completionDialogueObj.has("alt_outcome")) {
                JsonObject outComeObj = completionDialogueObj.getAsJsonObject("alt_outcome");
                if (outComeObj.has("pages")) {
                    JsonArray pagesArray = outComeObj.getAsJsonArray("pages");
                    List<Page> pages = new ArrayList<>();
                    for(JsonElement pageElem : pagesArray){
                        JsonObject pageObj = pageElem.getAsJsonObject();
                        Page page = new Page();
                        if(pageObj.has("text")){
                            page.text = pageObj.get("text").getAsString();
                        }
                        if(pageObj.has("button_1_text")){
                            page.button1Text = pageObj.get("button_1_text").getAsString();
                        }
                        if(pageObj.has("button_2_text")){
                            page.button2Text = pageObj.get("button_2_text").getAsString();
                        }
                        pages.add(page);
                    }
                    altOutcome = new Dialogue.Outcome(pages);
                }
            }
        }


        Dialogue dialogue = new Dialogue(outcome, altOutcome);


        return new Quest(id, displayName, type, stages, dialogue);
    }
    private static DialogueStage parseDialogue(String id, JsonObject stage, String questId) {
        if(!stage.has("dialogue"))
            throw new IllegalStateException("Dialogue stage missing dialogue: " + id);


        JsonObject dialogue = stage.getAsJsonObject("dialogue");


        List<QuestChoice> choices = new ArrayList<>();
        JsonArray pagesArray = dialogue.getAsJsonArray("pages");
        JsonArray choice1PagesArray = dialogue.getAsJsonArray("choice_1_pages");
        JsonArray choice2PagesArray = dialogue.getAsJsonArray("choice_2_pages");

        JsonArray choicesArray = stage.getAsJsonArray("choices");

        List<Page> pages = new ArrayList<>();
        List<Page> choice1Pages = new ArrayList<>();
        List<Page> choice2Pages = new ArrayList<>();

        for (JsonElement pageElem : pagesArray) {
            JsonObject pageObj = pageElem.getAsJsonObject();
            Page newPage = new Page();

            if(pageObj.has("type")){
                newPage.dialogueType = DialogueStage.DialogueType.valueOf(pageObj.get("type").getAsString().toUpperCase());
            }

            newPage.text = pageObj.get("text").getAsString();
            if (pageObj.has("tag")) {
                newPage.tag = pageObj.get("tag").getAsString();
            }
            if(pageObj.has("button_1_text")){
                newPage.button1Text = pageObj.get("button_1_text").getAsString();
            }
            if(pageObj.has("button_2_text")){
                newPage.button2Text = pageObj.get("button_2_text").getAsString();
            }
            if (pageObj.has("effects")) {  // ← Check pageObj, not stage
                QuestEffects completionEffects = new QuestEffects();
                JsonObject completionEffectsObj = pageObj.getAsJsonObject("effects");  // ← Get from pageObj
                if (completionEffectsObj.has("give_reward")) {
                    completionEffects.giveReward = completionEffectsObj.get("give_reward").getAsBoolean();
                }
                if (completionEffectsObj.has("end_quest")) {
                    completionEffects.endQuest = completionEffectsObj.get("end_quest").getAsBoolean();
                }
                if(completionEffectsObj.has("remove_item")) {
                    ItemStack removeItem;
                    JsonObject removeItemObj = pageObj.getAsJsonObject("remove_item");  // ← Get from pageObj

                    ResourceLocation item = new ResourceLocation(removeItemObj.get("item").getAsString());
                    int count = 1;
                    if(removeItemObj.has("count")){
                        count = removeItemObj.get("count").getAsInt();
                    }
                    removeItem = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)), count);
                    completionEffects.removeItem = removeItem;
                }

                newPage.effects = completionEffects;
            }
            pages.add(newPage);
        }
        if(choice1PagesArray != null && choice2PagesArray != null && choicesArray != null) {
            for (JsonElement pageElem : choice1PagesArray) {
                JsonObject pageObj = pageElem.getAsJsonObject();
                Page newPage = new Page();
                newPage.text = pageObj.get("text").getAsString();
                if (pageObj.has("button_1_text")) {
                    newPage.button1Text = pageObj.get("button_1_text").getAsString();
                }
                if (pageObj.has("button_2_text")) {
                    newPage.button2Text = pageObj.get("button_2_text").getAsString();
                }
                if (pageObj.has("effects")) {  // ← Check pageObj, not stage
                    QuestEffects completionEffects = new QuestEffects();
                    JsonObject completionEffectsObj = pageObj.getAsJsonObject("effects");  // ← Get from pageObj
                    if (completionEffectsObj.has("give_reward")) {
                        completionEffects.giveReward = completionEffectsObj.get("give_reward").getAsBoolean();
                    }
                    if (completionEffectsObj.has("end_quest")) {
                        completionEffects.endQuest = completionEffectsObj.get("end_quest").getAsBoolean();
                    }
                    if(completionEffectsObj.has("remove_item")) {
                        ItemStack removeItem;
                        JsonObject removeItemObj = pageObj.getAsJsonObject("remove_item");  // ← Get from pageObj
                        if(removeItemObj != null) {
                            ResourceLocation item = new ResourceLocation(removeItemObj.get("item").getAsString());
                            int count = 1;
                            if (removeItemObj.has("count")) {
                                count = removeItemObj.get("count").getAsInt();
                            }
                            removeItem = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)), count);
                            completionEffects.removeItem = removeItem;
                        }
                    }

                    newPage.effects = completionEffects;
                }
                choice1Pages.add(newPage);
            }
            for (JsonElement pageElem : choice2PagesArray) {
                JsonObject pageObj = pageElem.getAsJsonObject();
                Page newPage = new Page();
                newPage.text = pageObj.get("text").getAsString();
                if (pageObj.has("button_1_text")) {
                    newPage.button1Text = pageObj.get("button_1_text").getAsString();
                }
                if (pageObj.has("button_2_text")) {
                    newPage.button2Text = pageObj.get("button_2_text").getAsString();
                }
                if (pageObj.has("effects")) {  // ← Check pageObj, not stage
                    QuestEffects completionEffects = new QuestEffects();
                    JsonObject completionEffectsObj = pageObj.getAsJsonObject("effects");  // ← Get from pageObj
                    if (completionEffectsObj.has("give_reward")) {
                        completionEffects.giveReward = completionEffectsObj.get("give_reward").getAsBoolean();
                    }
                    if (completionEffectsObj.has("end_quest")) {
                        completionEffects.endQuest = completionEffectsObj.get("end_quest").getAsBoolean();
                    }
                    if(completionEffectsObj.has("remove_item")) {
                        ItemStack removeItem;
                        JsonObject removeItemObj = pageObj.getAsJsonObject("remove_item");  // ← Get from pageObj

                        ResourceLocation item = new ResourceLocation(removeItemObj.get("item").getAsString());
                        int count = 1;
                        if(removeItemObj.has("count")){
                            count = removeItemObj.get("count").getAsInt();
                        }
                        removeItem = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)), count);
                        completionEffects.removeItem = removeItem;
                    }

                    newPage.effects = completionEffects;
                }
                choice2Pages.add(newPage);
            }

            for (JsonElement elem : choicesArray) {
                JsonObject choiceObj = elem.getAsJsonObject();

                String choiceId = choiceObj.get("id").getAsString();
                String text = choiceObj.get("text").getAsString();

                QuestEffects effects = new QuestEffects();
                if (choiceObj.has("effects")) {  // ← Check pageObj, not stage
                    QuestEffects completionEffects = new QuestEffects();
                    JsonObject completionEffectsObj = choiceObj.getAsJsonObject("effects");  // ← Get from pageObj
                    if (completionEffectsObj.has("give_reward")) {
                        completionEffects.giveReward = completionEffectsObj.get("give_reward").getAsBoolean();
                    }
                    if (completionEffectsObj.has("end_quest")) {
                        completionEffects.endQuest = completionEffectsObj.get("end_quest").getAsBoolean();
                    }
                    if(completionEffectsObj.has("remove_item")) {
                        JsonObject removeItemObj = completionEffectsObj.getAsJsonObject("remove_item");

                        ResourceLocation item = new ResourceLocation(removeItemObj.get("item").getAsString());
                        int count = 1;
                        if (removeItemObj.has("count")) {
                            count = removeItemObj.get("count").getAsInt();
                        }
                        ItemStack removeItem = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)), count);
                        completionEffects.removeItem = removeItem;
                    }
                    effects = completionEffects;
                }

                List<QuestReward> rewards = parseRewards(choiceObj);
                choices.add(new QuestChoice(choiceId, text, effects, rewards));
            }
        }
        String nextStage = null;

        if (stage.has("on_accept")) {
           JsonObject onAcceptObject = stage.getAsJsonObject("on_accept");
           if(onAcceptObject.has("goto")) {
               nextStage = onAcceptObject.get("goto").getAsString();
           }
        }else if (stage.has("on_complete")) {
            JsonObject onAcceptObject = stage.getAsJsonObject("on_complete");
            if(onAcceptObject.has("goto")) {
                nextStage = onAcceptObject.get("goto").getAsString();
            }
        }

        return new DialogueStage(id, pages, questId, nextStage, choices, choice1Pages, choice2Pages, parseRewards(stage));
    }
    private static ObjectiveStage parseObjective(String id, JsonObject stage, String questId) {

        JsonObject obj = stage.getAsJsonObject("objective");
        if (obj == null) {
            throw new IllegalStateException("Objective stage missing 'objective': " + id);
        }

        String type = obj.get("objective_type").getAsString();
        String nextStage = stage.getAsJsonObject("on_complete").get("goto").getAsString();


        QuestObjective objective;
        JsonArray inProgressDialogueArray = stage.getAsJsonArray("in_progress_dialogue");

        List<Page> inProgressDialogue = new ArrayList<>();

        for (JsonElement pageElem : inProgressDialogueArray) {
            JsonObject pageObj = pageElem.getAsJsonObject();
            Page newPage = new Page();
            newPage.text = pageObj.get("text").getAsString();
            if(pageObj.has("button_1_text")){
                newPage.button1Text = pageObj.get("button_1_text").getAsString();
            }
            if(pageObj.has("button_2_text")){
                newPage.button2Text = pageObj.get("button_2_text").getAsString();
            }
            inProgressDialogue.add(newPage);
        }


        switch (type) {

            case "collect_item" -> {
                ResourceLocation item = new ResourceLocation(obj.get("item").getAsString());
                int count = obj.get("count").getAsInt();
                boolean consume = obj.get("consume_on_turn_in").getAsBoolean();

                objective = new CollectItemObjective(item, count, consume, questId);
            }

            // future-proofing
            case "kill_entity" -> {
                throw new UnsupportedOperationException("kill_entity not implemented yet");
            }

            default -> throw new IllegalArgumentException(
                    "Unknown objective_type: " + type
            );
        }
        QuestEffects completionEffects = new QuestEffects();

        return new ObjectiveStage(id, objective, inProgressDialogue, questId, nextStage, completionEffects, parseRewards(stage));
    }

    private static QuestEffects parseEffects(JsonObject obj) {
        QuestEffects effects = new QuestEffects();

        if (obj == null) return effects;


        JsonObject completionEffectsObj = obj.getAsJsonObject("effects");  // ← Get from pageObj
            if (completionEffectsObj.has("give_reward")) {
                effects.giveReward = completionEffectsObj.get("give_reward").getAsBoolean();
            }
            if (completionEffectsObj.has("end_quest")) {
                effects.endQuest = completionEffectsObj.get("end_quest").getAsBoolean();
            }
            if(completionEffectsObj.has("remove_item")) {
                ItemStack removeItem;
                JsonObject removeItemObj = obj.getAsJsonObject("remove_item");  // ← Get from pageObj

                ResourceLocation item = new ResourceLocation(removeItemObj.get("item").getAsString());
                int count = 1;
                if (removeItemObj.has("count")) {
                    count = removeItemObj.get("count").getAsInt();
                }
                removeItem = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)), count);
                effects.removeItem = removeItem;
            }
        return effects;
    }
    private static List<QuestReward> parseRewards(JsonObject choiceObj) {
        if (!choiceObj.has("rewards")) return List.of();

        List<QuestReward> rewards = new ArrayList<>();
        JsonArray rewardsArray = choiceObj.getAsJsonArray("rewards");

        for (JsonElement elem : rewardsArray) {
            JsonObject reward = elem.getAsJsonObject();
            String type = reward.get("type").getAsString();

            switch (type) {
                case "item" -> {
                    ResourceLocation item = new ResourceLocation(reward.get("item").getAsString());
                    int count = reward.get("count").getAsInt();
                    rewards.add(new ItemReward(item, count));
                }
                case "xp" -> {
                    int amount = reward.get("amount").getAsInt();
                    rewards.add(new XpReward(amount));
                }
            }
        }

        return rewards;
    }
}