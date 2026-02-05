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
import net.kenji.rpg_villager_quests.quest_system.stage_types.IntroStage;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.resources.ResourceLocation;
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


        List<QuestStage> finalStages = new ArrayList<>();
        for (JsonElement elem : json.getAsJsonArray("stages")) {
            JsonObject stage = elem.getAsJsonObject();
            String stageType = stage.get("type").getAsString();
            String stageId = stage.get("id").getAsString();

            switch (stageType) {
                case "dialogue" -> finalStages.add(parseDialogue(stageId, stage, id));
                case "objective" -> finalStages.add(parseObjective(stageId, stage, id, stageId));
            }
        }
        QuestStage questStage = new IntroStage("Intro", getDialogueMainInclusive(id, json, "intro_dialogue"), id, finalStages.get(0).id);

        stages.add(questStage);
        stages.addAll(finalStages);


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

        Dialogue completionDialogue = getDialogue(id, json, "completion_dialogue");
        Dialogue reconsiderDialogue = getDialogueMainExclusive(id, json, "reconsider_dialogue");

        String villagerProfession = "generic";
        if (json.has("profession")) {
            villagerProfession = json.get("profession").getAsString();
        }
        boolean isGlobalQuest = false;
        if (json.has("is_global")) {
            isGlobalQuest = json.get("is_global").getAsBoolean();
        }

        return new Quest(id, displayName, type, stages, completionDialogue, reconsiderDialogue, villagerProfession, isGlobalQuest);
    }
    private static DialogueStage parseDialogue(String id, JsonObject stage, String questId) {
        if (!stage.has("dialogue"))
            throw new IllegalStateException("Dialogue stage missing dialogue: " + id);


        JsonObject dialogue = stage.getAsJsonObject("dialogue");


        List<QuestChoice> choices = new ArrayList<>();
        JsonArray choicesArray = stage.getAsJsonArray("choices");


        Dialogue pages = getJsonDialoguePages(dialogue, "pages");
        List<Page> choice1Pages = getJsonPages(dialogue, "choice_1_pages");
        List<Page> choice2Pages = getJsonPages(dialogue, "choice_2_pages");

        String stageTag = null;
        if (stage.has("tag")) {
            stageTag = stage.get("tag").getAsString();
        }
        String displayName = "Dialogue";

        if (stage.has("display_name")) {
            displayName = stage.get("display_name").getAsString();
        }

        if(choicesArray != null) {
            for (JsonElement elem : choicesArray) {
                JsonObject choiceObj = elem.getAsJsonObject();

                String choiceId = choiceObj.get("id").getAsString();
                String text = choiceObj.get("text").getAsString();

                QuestEffects effects = getQuestEffects(choiceObj);

                List<QuestReward> rewards = parseRewards(choiceObj);
                choices.add(new QuestChoice(choiceId, text, effects, rewards));
            }
        }
        String nextStage = null;

        if (stage.has("on_accept")) {
            JsonObject onAcceptObject = stage.getAsJsonObject("on_accept");
            if (onAcceptObject.has("goto")) {
                nextStage = onAcceptObject.get("goto").getAsString();
            }
        } else if (stage.has("on_complete")) {
            JsonObject onAcceptObject = stage.getAsJsonObject("on_complete");
            if (onAcceptObject.has("goto")) {
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
        Dialogue mainInProgressDialogue = null;
        List<Page> secondaryInProgressDialogue = new ArrayList<>();

        if(stage.has("display_name")){
           displayName = stage.get("display_name").getAsString();
        }

        if(stage.has("in_progress_dialogue")) {
            JsonObject inProgressDialogue = stage.get("in_progress_dialogue").getAsJsonObject();
            if(inProgressDialogue.has("main")) {
                mainInProgressDialogue = getJsonDialoguePages(inProgressDialogue, "main");
            }
            else{
                throw new RuntimeException("Missing \"main\" In Progress Dialogue Type");
            }
            if(inProgressDialogue.has("secondary")) {
                secondaryInProgressDialogue = getJsonPages(inProgressDialogue, "secondary");
            }
        }
        else{
            List<Page> replaceInProgressPage = new ArrayList<>();

            Page replacePage = new Page();
            replacePage.text = "Well? What are you waiting for?";
            replacePage.button1Text = "NONE";
            replacePage.button2Text = "Leave";
            replaceInProgressPage.add(replacePage);

            Dialogue.Outcome mainOutcome = new Dialogue.Outcome(replaceInProgressPage);
            mainInProgressDialogue = new Dialogue(null, null, mainOutcome);
        }

        String stageTag = "Objective";
        if(stage.has("tag")) {
            stageTag = stage.get("tag").getAsString();
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
                String itemName = null;
                if(obj.has("item_name")) {
                    itemName = obj.get("item_name").getAsString();
                }

                objective = new CollectItemObjective(item, count, consume, questId, itemName);
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
    private static Dialogue getDialogueMainExclusive(String questId, JsonObject json, String dialogueName){
        Dialogue.Outcome posOutcome = null;
        Dialogue.Outcome negOutcome = null;
        Dialogue.Outcome mainOutcome = null;

        if(json.has(dialogueName)) {
            JsonObject completionDialogueObj = json.getAsJsonObject(dialogueName);
            if (completionDialogueObj.has("positive")) {
                List<Page> pages = getJsonPages(completionDialogueObj, "positive");
                posOutcome = new Dialogue.Outcome(pages);
            }
            else{
                throw new RuntimeException("Missing 'positive' dialogue type '" + dialogueName + "' in quest -> " + questId);
            }
            if (completionDialogueObj.has("negative")) {
                List<Page> pages = getJsonPages(completionDialogueObj, "negative");
                negOutcome = new Dialogue.Outcome(pages);
            }
            else{
                throw new RuntimeException("Missing 'negative' dialogue type '" + dialogueName + "' in quest -> " + questId);
            }
            if (completionDialogueObj.has("main")) {
                List<Page> pages = getJsonPages(completionDialogueObj, "main");
                mainOutcome = new Dialogue.Outcome(pages);
            }
            return new Dialogue(posOutcome, negOutcome, mainOutcome);
        }
        return null;
    }
    private static Dialogue getDialogueMainInclusive(String questId, JsonObject json, String dialogueName){
        Dialogue.Outcome posOutcome = null;
        Dialogue.Outcome negOutcome = null;
        Dialogue.Outcome mainOutcome = null;

        if(json.has(dialogueName)) {
            JsonObject dialogueObj = json.getAsJsonObject(dialogueName);
            if (dialogueObj.has("positive")) {
                List<Page> pages = getJsonPages(dialogueObj, "positive");

                posOutcome = new Dialogue.Outcome(pages);
            }
            if (dialogueObj.has("negative")) {
                List<Page> pages = getJsonPages(dialogueObj, "negative");
                negOutcome = new Dialogue.Outcome(pages);
            }
            if (dialogueObj.has("main")) {
                List<Page> pages = getJsonPages(dialogueObj, "main");
                mainOutcome = new Dialogue.Outcome(pages);
            }
            else{
                throw new RuntimeException("Missing 'main' dialogue type '" + dialogueName + "' in quest -> " + questId);
            }
            return new Dialogue(posOutcome, negOutcome, mainOutcome);
        }
        return null;
    }
    private static Dialogue getDialogue(String questId, JsonObject json, String dialogueName){
        Dialogue.Outcome posOutcome = null;
        Dialogue.Outcome negOutcome = null;
        Dialogue.Outcome mainOutcome = null;

        if(json.has(dialogueName)) {
            JsonObject dialogueObj = json.getAsJsonObject(dialogueName);
            if (dialogueObj.has("positive")) {
                List<Page> pages = getJsonPages(dialogueObj, "positive");

                posOutcome = new Dialogue.Outcome(pages);
            }
            if (dialogueObj.has("negative")) {
                List<Page> pages = getJsonPages(dialogueObj, "negative");
                negOutcome = new Dialogue.Outcome(pages);
            }
            if (dialogueObj.has("main")) {
                List<Page> pages = getJsonPages(dialogueObj, "main");
                mainOutcome = new Dialogue.Outcome(pages);
            }
            return new Dialogue(posOutcome, negOutcome, mainOutcome);
        }
        return null;
    }

    private static List<Page> getJsonPages(JsonObject json, String objectName){
        List<Page> pages = new ArrayList<>();

        if(json.has(objectName)){
            JsonArray dialogueArray = json.getAsJsonArray(objectName);
            for (JsonElement pageElem : dialogueArray) {
                JsonObject pageObj = pageElem.getAsJsonObject();
                Page newPage = new Page();

                newPage.text = pageObj.get("text").getAsString();
                if (pageObj.has("button_1_text")) {
                    newPage.button1Text = pageObj.get("button_1_text").getAsString();
                }
                if (pageObj.has("button_2_text")) {
                    newPage.button2Text = pageObj.get("button_2_text").getAsString();
                }
                if(pageObj.has("type")){
                    newPage.dialogueType = DialogueStage.DialogueType.valueOf(pageObj.get("type").getAsString().toUpperCase());
                }
                newPage.effects = getQuestEffects(pageObj);
                pages.add(newPage);
            }
        }
        return pages;
    }
    private static Dialogue getJsonDialoguePages(JsonObject json, String objectName){
        List<Page> pages = new ArrayList<>();

        if(json.has(objectName)){
            JsonArray dialogueArray = json.getAsJsonArray(objectName);
            for (JsonElement pageElem : dialogueArray) {
                JsonObject pageObj = pageElem.getAsJsonObject();
                Page newPage = new Page();
                newPage.text = pageObj.get("text").getAsString();
                if (pageObj.has("button_1_text")) {
                    newPage.button1Text = pageObj.get("button_1_text").getAsString();
                }
                if (pageObj.has("button_2_text")) {
                    newPage.button2Text = pageObj.get("button_2_text").getAsString();
                }
                if(pageObj.has("type")){
                    newPage.dialogueType = DialogueStage.DialogueType.valueOf(pageObj.get("type").getAsString().toUpperCase());
                }
                newPage.effects = getQuestEffects(pageObj);
                pages.add(newPage);
            }
        }
        Dialogue.Outcome mainOutcome = new Dialogue.Outcome(pages);
        return new Dialogue(null, null, mainOutcome);
    }
    private static QuestEffects getQuestEffects(JsonObject json){
        QuestEffects completionEffects = new QuestEffects();
        if (json.has("effects")) {  // ← Check pageObj, not stage
            JsonObject completionEffectsObj = json.getAsJsonObject("effects");  // ← Get from pageObj
            if (completionEffectsObj.has("give_reward")) {
                completionEffects.giveReward = completionEffectsObj.get("give_reward").getAsBoolean();
            }
            if (completionEffectsObj.has("end_quest")) {
                completionEffects.endQuest = completionEffectsObj.get("end_quest").getAsBoolean();
            }
            if(completionEffectsObj.has("remove_item")) {
                ItemStack removeItem;
                JsonObject removeItemObj = completionEffectsObj.getAsJsonObject("remove_item");  // ← Get from pageObj

                ResourceLocation item = new ResourceLocation(removeItemObj.get("item").getAsString());
                int count = 1;
                if(removeItemObj.has("count")){
                    count = removeItemObj.get("count").getAsInt();
                }
                removeItem = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)), count);
                completionEffects.removeItem = removeItem;
            }
        }
        return completionEffects;
    }

}