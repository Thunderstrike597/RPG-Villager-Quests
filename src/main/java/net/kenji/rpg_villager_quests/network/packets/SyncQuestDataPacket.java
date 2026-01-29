package net.kenji.rpg_villager_quests.network.packets;

import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncQuestDataPacket {
    private final CompoundTag questData;

    public SyncQuestDataPacket(CompoundTag questData) {
        this.questData = questData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(questData);
    }
    public static SyncQuestDataPacket decode(FriendlyByteBuf buf) {
        CompoundTag compoundTag = buf.readNbt();
        return new SyncQuestDataPacket(compoundTag);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side handling
            QuestData.loadClientData(questData);
        });
        ctx.get().setPacketHandled(true);
    }
}