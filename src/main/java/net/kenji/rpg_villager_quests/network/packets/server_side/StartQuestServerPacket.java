package net.kenji.rpg_villager_quests.network.packets.server_side;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.client_side.VillagerGlowPacket;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.jline.utils.Log;

import java.util.UUID;
import java.util.function.Supplier;

public class StartQuestServerPacket {
    private final String questId;
    private final UUID villagerUuid;

    public StartQuestServerPacket(String questId, UUID villagerUuid) {
        this.questId = questId;
        this.villagerUuid = villagerUuid;
    }

    public static void encode(StartQuestServerPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUUID(packet.villagerUuid);
    }

    public static StartQuestServerPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        UUID villagerUuid = buf.readUUID();
        return new StartQuestServerPacket(questId, villagerUuid);
    }

    public static void handle(StartQuestServerPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            Quest quest = VillagerQuestManager.getQuestByName(packet.questId);
            Entity entity = player.serverLevel().getEntity(packet.villagerUuid);

            if (entity != null) {
                quest.startQuest(player, entity.getUUID());
                ModPacketHandler.sendToPlayer(new VillagerGlowPacket(entity.getId(), true), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}