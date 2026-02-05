package net.kenji.rpg_villager_quests.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kenji.rpg_villager_quests.events.QuestVillagerEvents;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestLoader;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jline.utils.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class VillagerQuestManager {
    public static final Map<String, JsonObject> rawJsonFiles = new HashMap<>();

    public static Map<UUID, Quest> currentQuestMap = new HashMap<>();
    public static Map<UUID, Quest> villagerQuestMap = new HashMap<>();

    public static Quest getVillagerQuest(UUID villager){
        return villagerQuestMap.get(villager);
    }

    public static Quest getQuestByName(String questName) {

        // Strip .json extension if present (backwards compatibility with old villager data)
        if (questName != null && questName.endsWith(".json")) {
            questName = questName.substring(0, questName.length() - 5);
            Log.info("Stripped .json, now using: " + questName);
        }
        JsonObject root = rawJsonFiles.get(questName);

        if (root != null) {
            return QuestLoader.load(root);
        }
        return null;
    }

    public static Quest getRandomQuest(Villager villager){
        List<String> keys = new ArrayList<>(rawJsonFiles.keySet());

        int questCount = keys.size();
        int randomIndex = Mth.nextInt(villager.getRandom(), 0, questCount - 1);
        String questName = keys.get(randomIndex);
        return getQuestByName(questName);
    }

    public static void assignQuestToVillager(Quest quest, Villager villager) {
        villagerQuestMap.put(villager.getUUID(), quest);
        villager.getPersistentData().putString(QuestVillagerEvents.QUEST_VILLAGER_TAG, quest.getQuestId());
    }

    public static class JsonHelper {

        static Path gameDir = FMLPaths.GAMEDIR.get();
        static Path questDir = gameDir.resolve("rpg_villager_quests");

        public static void init() {
            Log.info("Logging Init()");

            try {
                Files.createDirectories(questDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (Stream<Path> files = Files.list(questDir)) {
                files.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                String rawFileText = Files.readString(path);
                                JsonObject root = JsonParser.parseString(rawFileText).getAsJsonObject();
                                String questId;
                                if(root.has("id")) {
                                    questId = root.get("id").getAsString();
                               }
                               else{
                                   throw new RuntimeException("No \"id\" type in quest file");
                               }
                                Log.info("Registering: " + questId);
                                rawJsonFiles.put(questId, root);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
