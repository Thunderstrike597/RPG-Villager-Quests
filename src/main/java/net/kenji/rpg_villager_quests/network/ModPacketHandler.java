package net.kenji.rpg_villager_quests.network;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.network.packets.*;
import net.kenji.rpg_villager_quests.network.packets.client_side.VillagerGlowPacket;
import net.kenji.rpg_villager_quests.network.packets.server_side.StartQuestServerPacket;
import net.kenji.rpg_villager_quests.network.packets.server_side.StageCompleteServerPacket;
import net.kenji.rpg_villager_quests.network.packets.server_side.StageStartServerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RpgVillagerQuests.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }


    public static void register() {
        INSTANCE.messageBuilder(StartQuestServerPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StartQuestServerPacket::decode)
                .encoder(StartQuestServerPacket::encode)
                .consumerMainThread(StartQuestServerPacket::handle)
                .add();

        INSTANCE.messageBuilder(StageCompleteServerPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StageCompleteServerPacket::decode)
                .encoder(StageCompleteServerPacket::encode)
                .consumerMainThread(StageCompleteServerPacket::handle)
                .add();
        INSTANCE.messageBuilder(StageStartServerPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StageStartServerPacket::decode)
                .encoder(StageStartServerPacket::encode)
                .consumerMainThread(StageStartServerPacket::handle)
                .add();
        INSTANCE.messageBuilder(ChoicePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ChoicePacket::decode)
                .encoder(ChoicePacket::encode)
                .consumerMainThread(ChoicePacket::handle)
                .add();
        INSTANCE.messageBuilder(VillagerGlowPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(VillagerGlowPacket::decode)
                .encoder(VillagerGlowPacket::encode)
                .consumerMainThread(VillagerGlowPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncQuestDataPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncQuestDataPacket::decode)
                .encoder(SyncQuestDataPacket::encode)
                .consumerMainThread(SyncQuestDataPacket::handle)
                .add();
        INSTANCE.messageBuilder(UpdateQuestProgressPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateQuestProgressPacket::new)
                .encoder(UpdateQuestProgressPacket::encode)
                .consumerMainThread(UpdateQuestProgressPacket::handle)
                .add();
    }


    // Helper method to send packet to server_side
    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }


    // Helper method to send packet to specific player
    public static void sendToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    // Helper method to send packet to all players
    public static void sendToAll(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
}