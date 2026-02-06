package net.kenji.rpg_villager_quests.network.packets.server_side;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.client_side.VillagerGlowPacket;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class QuestMenuOpenPacket {
    private final boolean isMenuOpen;
    private final UUID villager;

    public QuestMenuOpenPacket(boolean isMenuOpen, UUID villager) {
        this.isMenuOpen = isMenuOpen;
        this.villager = villager;
    }

    public static void encode(QuestMenuOpenPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.isMenuOpen);
        buf.writeUUID(packet.villager);
    }

    public static QuestMenuOpenPacket decode(FriendlyByteBuf buf) {
        boolean isOpen = buf.readBoolean();
        UUID villagerUuid = buf.readUUID();
        return new QuestMenuOpenPacket(isOpen, villagerUuid);
    }

    public static void handle(QuestMenuOpenPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            VillagerQuestManager.isQuestMenuOpen.put(player.getUUID(), packet.isMenuOpen);
            Entity entity = player.serverLevel().getEntity(packet.villager);
            if(entity instanceof Villager villager){
                if(packet.isMenuOpen)
                    villager.setTradingPlayer(player);
                else villager.setTradingPlayer(null);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}