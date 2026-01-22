package net.kenji.rpg_villager_quests.quest_system;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.objective_types.CollectItemObjective;
import net.kenji.rpg_villager_quests.quest_system.reward_types.ItemReward;
import net.kenji.rpg_villager_quests.quest_system.reward_types.XpReward;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ChoiceStage;
import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.resources.ResourceLocation;
import org.jline.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class QuestLoader {

    public static Quest load(JsonObject json) {
        String id = json.get("quest_display_name").getAsString();
        String type = json.get("type").getAsString();

        List<QuestStage> stages = new ArrayList<>();

        for (JsonElement elem : json.getAsJsonArray("stages")) {
            JsonObject stage = elem.getAsJsonObject();
            String stageType = stage.get("type").getAsString();
            String stageId = stage.get("id").getAsString();

            switch (stageType) {
                case "dialogue" -> stages.add(parseDialogue(stageId, stage));
                case "objective" -> stages.add(parseObjective(stageId, stage));
                case "dialogue_with_choice" -> stages.add(parseChoice(stageId, stage));
            }
        }

        return new Quest(id, type, stages);
    }
    private static DialogueStage parseDialogue(String id, JsonObject stage) {
        JsonArray pagesArray = stage.getAsJsonArray("pages");
        if (pagesArray == null) {
            throw new IllegalStateException("Dialogue stage missing pages: " + id);
        }

        List<String> pages = new ArrayList<>();
        for (JsonElement pageElem : pagesArray) {
            JsonObject pageObj = pageElem.getAsJsonObject();
            pages.add(pageObj.get("text").getAsString());
        }

        String nextStage = null;
        if (stage.has("on_accept")) {
            nextStage = stage.getAsJsonObject("on_accept").get("goto").getAsString();
        }

        return new DialogueStage(id, pages, nextStage);
    }
    private static ChoiceStage parseChoice(String id, JsonObject stage) {
        JsonArray choicesArray = stage.getAsJsonArray("choices");
        if (choicesArray == null) {
            throw new IllegalStateException("Choice stage missing choices: " + id);
        }

        if(stage.has("dialogue")) {
            JsonObject dialogue = stage.get("dialogue").getAsJsonObject();

            List<QuestChoice> choices = new ArrayList<>();

            JsonArray pagesArray = dialogue.getAsJsonArray("pages");
            JsonArray choice1PagesArray = dialogue.getAsJsonArray("choice_1_pages");
            JsonArray choice2PagesArray = dialogue.getAsJsonArray("choice_2_pages");

            List<String> pages = new ArrayList<>();
            List<String> choice1Pages = new ArrayList<>();
            List<String> choice2Pages = new ArrayList<>();


            for(JsonElement pageElem : pagesArray){
                JsonObject pageObj = pageElem.getAsJsonObject();
                pages.add(pageObj.get("text").getAsString());
            }
            for(JsonElement pageElem : choice1PagesArray){
                JsonObject pageObj = pageElem.getAsJsonObject();
                choice1Pages.add(pageObj.get("text").getAsString());
            }
            for(JsonElement pageElem : choice2PagesArray){
                JsonObject pageObj = pageElem.getAsJsonObject();
                choice2Pages.add(pageObj.get("text").getAsString());
            }

            for (JsonElement elem : choicesArray) {
                JsonObject choiceObj = elem.getAsJsonObject();

                String choiceId = choiceObj.get("id").getAsString();
                String text = choiceObj.get("text").getAsString();
                boolean endQuest = choiceObj.get("end_quest").getAsBoolean();

                QuestEffects effects = parseEffects(choiceObj.getAsJsonObject("effects"));
                List<QuestReward> rewards = parseRewards(choiceObj);
                choices.add(new QuestChoice(choiceId, text, endQuest, effects, rewards));
            }
            return new ChoiceStage(id, choices, pages, choice1Pages, choice2Pages);
        }
        else{
            Log.info("DIALOGUE IS NULL");
        }
        return null;
    }
    private static ObjectiveStage parseObjective(String id, JsonObject stage) {

        JsonObject obj = stage.getAsJsonObject("objective");
        if (obj == null) {
            throw new IllegalStateException("Objective stage missing 'objective': " + id);
        }

        String type = obj.get("objective_type").getAsString();
        String nextStage = stage.getAsJsonObject("on_complete").get("goto").getAsString();


        QuestObjective objective;
        JsonArray inProgressDialogueArray = stage.getAsJsonArray("in_progress_dialogue");

        List<String> inProgressDialogue = new ArrayList<>();

        for(JsonElement elem : inProgressDialogueArray){
            JsonObject choiceObj = elem.getAsJsonObject();
            String text = choiceObj.get("text").getAsString();
            inProgressDialogue.add(text);
        }


        switch (type) {

            case "collect_item" -> {
                ResourceLocation item = new ResourceLocation(obj.get("item").getAsString());
                int count = obj.get("count").getAsInt();
                boolean consume = obj.get("consume_on_turn_in").getAsBoolean();

                objective = new CollectItemObjective(item, count, consume);
            }

            // future-proofing
            case "kill_entity" -> {
                throw new UnsupportedOperationException("kill_entity not implemented yet");
            }

            default -> throw new IllegalArgumentException(
                    "Unknown objective_type: " + type
            );
        }

        return new ObjectiveStage(id, objective, nextStage);
    }

    private static QuestEffects parseEffects(JsonObject obj) {
        QuestEffects effects = new QuestEffects();

        if (obj == null) return effects;

        effects.removeItem = obj.has("remove_item") && obj.get("remove_item").getAsBoolean();
        effects.keepItem = obj.has("keep_item") && obj.get("keep_item").getAsBoolean();
        effects.giveReward = obj.has("give_reward") && obj.get("give_reward").getAsBoolean();

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