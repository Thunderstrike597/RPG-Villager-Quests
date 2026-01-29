package net.kenji.rpg_villager_quests.network.packets;

import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateQuestProgressPacket {
    private final String questId;
    private final CompoundTag questInstanceData;

    public UpdateQuestProgressPacket(String questId, QuestInstance instance) {
        this.questId = questId;
        this.questInstanceData = instance.serializeNBT();
    }

    public UpdateQuestProgressPacket(FriendlyByteBuf buf) {
        this.questId = buf.readUtf();
        this.questInstanceData = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId);
        buf.writeNbt(questInstanceData);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: update specific quest
            QuestData.updateClientQuest(questId, questInstanceData);
        });
        ctx.get().setPacketHandled(true);
    }
}