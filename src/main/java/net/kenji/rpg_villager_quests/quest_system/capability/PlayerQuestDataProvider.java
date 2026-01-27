package net.kenji.rpg_villager_quests.quest_system.capability;

import net.kenji.rpg_villager_quests.quest_system.quest_data.PlayerQuestData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerQuestDataProvider implements ICapabilitySerializable<CompoundTag> {

    private final PlayerQuestData data = new PlayerQuestData();
    private final LazyOptional<PlayerQuestData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == QuestCapabilities.PLAYER_QUESTS ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        try {
            data.deserializeNBT(nbt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
