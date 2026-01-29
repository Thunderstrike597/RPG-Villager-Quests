package net.kenji.rpg_villager_quests.quest_system.quest_data.saved_data;

import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestSavedData extends SavedData {
    private static final String DATA_NAME = "rpg_villager_quests";

    // Store all player quest data
    private final Map<UUID, QuestData> questDataMap = new HashMap<>();

    public QuestSavedData() {
        super();
    }

    public static QuestSavedData load(CompoundTag tag) {
        QuestSavedData data = new QuestSavedData();

        CompoundTag playersTag = tag.getCompound("Players");
        for (String uuidString : playersTag.getAllKeys()) {
            UUID playerId = UUID.fromString(uuidString);
            CompoundTag playerTag = playersTag.getCompound(uuidString);

            QuestData questData = QuestData.fromNBT(playerTag);
            data.questDataMap.put(playerId, questData);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag playersTag = new CompoundTag();

        for (Map.Entry<UUID, QuestData> entry : questDataMap.entrySet()) {
            playersTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }

        tag.put("Players", playersTag);
        return tag;
    }

    public static QuestSavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(QuestSavedData::load, QuestSavedData::new, DATA_NAME);
    }

    public QuestData getQuestData(UUID playerId) {
        return questDataMap.computeIfAbsent(playerId, id -> new QuestData());
    }

    public void markDirty() {
        this.setDirty();
    }
}