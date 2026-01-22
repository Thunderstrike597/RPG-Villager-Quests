package net.kenji.rpg_villager_quests.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestLoader;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ChoiceStage;
import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class QuestMenuManager {
    public static final Map<String, String> rawJsonFiles = new HashMap<>();

    public static Map<UUID, Quest> currentQuestMap = new HashMap<>();
    public static Map<UUID, Quest> villagerQuestMap = new HashMap<>();

    public static Quest getVillagerQuest(Villager villager){
        return villagerQuestMap.get(villager.getUUID());
    }

    public static void assignQuestToVillager(Villager villager) {

        List<String> keys = new ArrayList<>(rawJsonFiles.keySet());

        int questCount = keys.size();
        int randomIndex = Mth.nextInt(villager.getRandom(), 0, questCount - 1);
        String questKey = keys.get(randomIndex);

        String jsonContents = rawJsonFiles.get(questKey);

        JsonObject root = JsonParser.parseString(jsonContents).getAsJsonObject();
        Quest quest = QuestLoader.load(root);

        villagerQuestMap.put(villager.getUUID(), quest);
    }


    public static class JsonHelper {

        static Path gameDir = FMLPaths.GAMEDIR.get();
        static Path questDir = gameDir.resolve("rpg_villager_quests");

        public static void init() {
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
                                rawJsonFiles.put(path.getFileName().toString(), Files.readString(path));
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
