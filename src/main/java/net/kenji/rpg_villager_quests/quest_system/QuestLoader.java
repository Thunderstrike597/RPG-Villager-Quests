package net.kenji.rpg_villager_quests.quest_system;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.objective_types.CollectItemObjective;
import net.kenji.rpg_villager_quests.quest_system.objective_types.KillEntityObjective;
import net.kenji.rpg_villager_quests.quest_system.objective_types.PackageDeliverObjective;
import net.kenji.rpg_villager_quests.quest_system.reward_types.ItemReward;
import net.kenji.rpg_villager_quests.quest_system.reward_types.XpReward;
import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestLoader {

    public static Quest load(JsonObject json){
        String id = json.get("id").getAsString();
        String displayName = id;

        if(json.has("display_name")) {
            displayName = json.get("display_name").getAsString();
        }
        String type = json.get("type").getAsString();

        List<QuestStage> stages = new ArrayList<>();

        for (JsonElement elem : json.getAsJsonArray("stages")) {
            JsonObject stage = elem.getAsJsonObject();
            String stageType = stage.get("type").getAsString();
            String stageId = stage.get("id").getAsString();

            switch (stageType) {
                case "dialogue" -> stages.add(parseDialogue(stageId, stage, id));
                case "objective" -> stages.add(parseObjective(stageId, stage, id, stageId));
            }
        }

        QuestReward questReward = null;
        if (json.has("quest_reward")) {
            JsonObject questRewardObject = json.getAsJsonObject("quest_reward");
            QuestRewardType rewardType;

            rewardType = QuestRewardType.valueOf(questRewardObject.get("type").getAsString().toUpperCase());


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
        String villagerProfession = "generic";
        if (json.has("profession")) {
            villagerProfession = json.get("profession").getAsString();
        }
        boolean isGlobalQuest = false;
        if (json.has("is_global")) {
            isGlobalQuest = json.get("is_global").getAsBoolean();
        }

        return new Quest(id, displayName, type, stages, dialogue, villagerProfession, isGlobalQuest);
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
        String stageTag = null;
        if(stage.has("tag")) {
            stageTag = stage.get("tag").getAsString();
        }
        String displayName = "Dialogue";

        if(stage.has("display_name")){
            displayName = stage.get("display_name").getAsString();
        }

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

        return new DialogueStage(id, displayName, pages, questId, nextStage, choices, choice1Pages, choice2Pages, parseRewards(stage), stageTag);
    }
    private static ObjectiveStage parseObjective(String id, JsonObject stage, String questId, String stageId) {

        JsonObject obj = stage.getAsJsonObject("objective");
        if (obj == null) {
            throw new IllegalStateException("Objective stage missing 'objective': " + id);
        }

        String type = obj.get("objective_type").getAsString();
        String nextStage = stage.getAsJsonObject("on_complete").get("goto").getAsString();
        String displayName = null;

        QuestObjective objective;
        JsonArray mainInProgressDialogueArray = null;
        JsonArray secondaryInProgressDialogueArray = null;

        if(stage.has("display_name")){
           displayName = stage.get("display_name").getAsString();
        }

        if(stage.has("in_progress_dialogue")) {
            JsonObject inProgressDialogue = stage.get("in_progress_dialogue").getAsJsonObject();
            if(inProgressDialogue.has("main")) {
                mainInProgressDialogueArray = inProgressDialogue.getAsJsonArray("main");
            }
            else{
                throw new RuntimeException("Missing \"main\" In Progress Dialogue Type");
            }
            if(inProgressDialogue.has("secondary")) {
                secondaryInProgressDialogueArray = inProgressDialogue.getAsJsonArray("secondary");
            }
        }


        List<Page> mainInProgressDialogue = new ArrayList<>();
        List<Page> secondaryInProgressDialogue = new ArrayList<>();

        String stageTag = "Objective";
        if(stage.has("tag")) {
            stageTag = stage.get("tag").getAsString();
        }

        if(mainInProgressDialogueArray != null) {
            for (JsonElement pageElem : mainInProgressDialogueArray) {
                JsonObject pageObj = pageElem.getAsJsonObject();
                Page newPage = new Page();
                newPage.text = pageObj.get("text").getAsString();
                if (pageObj.has("button_1_text")) {
                    newPage.button1Text = pageObj.get("button_1_text").getAsString();
                }
                if (pageObj.has("button_2_text")) {
                    newPage.button2Text = pageObj.get("button_2_text").getAsString();
                }
                mainInProgressDialogue.add(newPage);
            }
        }
        if(secondaryInProgressDialogueArray != null) {
            for (JsonElement pageElem : secondaryInProgressDialogueArray) {
                JsonObject pageObj = pageElem.getAsJsonObject();
                Page newPage = new Page();
                newPage.text = pageObj.get("text").getAsString();
                if (pageObj.has("button_1_text")) {
                    newPage.button1Text = pageObj.get("button_1_text").getAsString();
                }
                if (pageObj.has("button_2_text")) {
                    newPage.button2Text = pageObj.get("button_2_text").getAsString();
                }
                secondaryInProgressDialogue.add(newPage);
            }
        }


        switch (type) {

            case "collect_item" -> {
                ResourceLocation item = null;
                if(obj.has("item")){
                    item = new ResourceLocation(obj.get("item").getAsString());
                }else{
                    throw new RuntimeException("Missing Item Type in Objective: " + type);
                }

                int count = 1;
                if(stage.has("count")) {
                    count = obj.get("count").getAsInt();
                }
                boolean consume = true;
                if(obj.has("consume_on_turn_in")) {
                    consume = obj.get("consume_on_turn_in").getAsBoolean();
                }

                objective = new CollectItemObjective(item, count, consume, questId);
            }

            // future-proofing
            case "kill_entity" -> {
                ResourceLocation entityType = null;
                if(obj.has("entity_type")) {
                    entityType = new ResourceLocation(obj.get("entity_type").getAsString());
                }
                else{
                    throw new RuntimeException("Missing \"entity_type\" Type in Objective: " + type);
                }
                int count = obj.get("count").getAsInt();

                objective = new KillEntityObjective(entityType, count, questId);
            }
            case "deliver_package" -> {
                ResourceLocation entityType = new ResourceLocation("minecraft","villager");
                if(stage.has("entity_type")) {
                    entityType = new ResourceLocation(obj.get("entity_type").getAsString());
                }
                ResourceLocation item = null;
                if(obj.has("item")){
                    item = new ResourceLocation(obj.get("item").getAsString());
                }else{
                    throw new RuntimeException("Missing \"item\" Type in Objective: " + type);
                }
                int count = 1;
                if(stage.has("count")) {
                    count = obj.get("count").getAsInt();
                }
                boolean consume = true;
                if(obj.has("consume_on_deliver")) {
                    consume = obj.get("consume_on_deliver").getAsBoolean();
                }
                int minDist = 1;
                if(obj.has("minDist")) {
                    minDist = obj.get("minDist").getAsInt();
                }
                int maxDist = 0;
                if(obj.has("maxDist")) {
                    minDist = obj.get("maxDist").getAsInt();
                }
                else{
                    throw new RuntimeException("Missing \"maxDist\" in Objective: " + type);
                }
                String structure = null;
                if(obj.has("structure")){
                    structure = obj.get("structure").getAsString();
                }
                if(secondaryInProgressDialogue.isEmpty()){
                    throw new RuntimeException("\"secondary\" Dialogue is Empty Or Missing!!");
                }
                List<Page> deliveryDialogue = new ArrayList<>();
                JsonArray deliveryDialogueArray;
                if(obj.has("delivered_dialogue")){
                    deliveryDialogueArray = obj.get("delivered_dialogue").getAsJsonArray();
                    for (JsonElement pageElem : deliveryDialogueArray) {
                        JsonObject pageObj = pageElem.getAsJsonObject();
                        Page newPage = new Page();
                        newPage.text = pageObj.get("text").getAsString();
                        if (pageObj.has("button_1_text")) {
                            newPage.button1Text = pageObj.get("button_1_text").getAsString();
                        }
                        if (pageObj.has("button_2_text")) {
                            newPage.button2Text = pageObj.get("button_2_text").getAsString();
                        }
                        deliveryDialogue.add(newPage);
                    }
                }
                else{
                    throw new RuntimeException("Missing \"delivered_dialogue\" type in: " + type);
                }

                objective = new PackageDeliverObjective(item, entityType, consume, questId, stageId, minDist, maxDist, structure, secondaryInProgressDialogue, deliveryDialogue);
            }
            default -> throw new IllegalArgumentException(
                    "Unknown objective_type: " + type
            );
        }
        QuestEffects completionEffects = new QuestEffects();

        return new ObjectiveStage(id, displayName, objective, mainInProgressDialogue, questId, nextStage, completionEffects, parseRewards(stage), stageTag);
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