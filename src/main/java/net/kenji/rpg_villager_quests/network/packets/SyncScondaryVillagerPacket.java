package net.kenji.rpg_villager_quests.network.packets;

import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncScondaryVillagerPacket {

    private final String questId;
    private final UUID villagerUuid;
    public SyncScondaryVillagerPacket(String questId, UUID villagerUuid) {
        this.questId = questId;
        this.villagerUuid = villagerUuid;
    }

    // Encode: Write data to buffer
    public static void encode(SyncScondaryVillagerPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUUID(packet.villagerUuid);
    }

    // Decode: Read data from buffer
    public static SyncScondaryVillagerPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        UUID villagerUuid = buf.readUUID();
        return new SyncScondaryVillagerPacket(questId, villagerUuid);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(SyncScondaryVillagerPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection().getReceptionSide().isClient()) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    QuestData questData = QuestData.get(player.getUUID());
                    QuestInstance questInstance = questData.getQuestInstance(packet.questId);

                    if (questInstance != null) {
                        questInstance.currentSecondaryEntity = packet.villagerUuid;
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
