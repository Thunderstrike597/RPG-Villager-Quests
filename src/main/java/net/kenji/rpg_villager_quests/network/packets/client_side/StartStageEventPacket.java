package net.kenji.rpg_villager_quests.network.packets.client_side;

import net.kenji.rpg_villager_quests.quest_system.events.QuestStageEvents;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class StartStageEventPacket {
    private final String questId;
    private final String stageId;
    private final UUID villagerUuid;


    public StartStageEventPacket(String questId, String stageId, UUID villagerUuid) {
        this.questId = questId;
        this.stageId = stageId;
        this.villagerUuid = villagerUuid;
    }

    // Encode: Write data to buffer
    public static void encode(StartStageEventPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
        buf.writeUUID(packet.villagerUuid);
    }

    // Decode: Read data from buffer
    public static StartStageEventPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        UUID villagerUuid = buf.readUUID();
        return new StartStageEventPacket(questId, stageId, villagerUuid);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(StartStageEventPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;
                if(player != null) {
                    QuestData questData = QuestData.get(player);
                    QuestInstance questInstance = questData.getQuestInstance(packet.questId, packet.villagerUuid);
                    MinecraftForge.EVENT_BUS.post(new QuestStageEvents.StageStartEvent(questInstance));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}