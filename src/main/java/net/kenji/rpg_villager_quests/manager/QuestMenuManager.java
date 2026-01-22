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
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class QuestMenuManager {
    public static final Map<String, String> rawJsonFiles = new HashMap<>();

    public static List<String> getPages(String questName) {
        String jsonFile = rawJsonFiles.get(questName);

        JsonObject root = JsonParser.parseString(jsonFile).getAsJsonObject();

        Quest quest = QuestLoader.load(root);

        List<String> pages;

        pages = quest.getCurrentStage().getDialogue();

        return pages;
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
