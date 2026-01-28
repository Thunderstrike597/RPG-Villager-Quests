package net.kenji.rpg_villager_quests.network;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.network.packets.*;
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
        INSTANCE.messageBuilder(StageCompletionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StageCompletionPacket::decode)
                .encoder(StageCompletionPacket::encode)
                .consumerMainThread(StageCompletionPacket::handle)
                .add();
        INSTANCE.messageBuilder(AddQuestPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(AddQuestPacket::decode)
                .encoder(AddQuestPacket::encode)
                .consumerMainThread(AddQuestPacket::handle)
                .add();
        INSTANCE.messageBuilder(StageStartPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StageStartPacket::decode)
                .encoder(StageStartPacket::encode)
                .consumerMainThread(StageStartPacket::handle)
                .add();
        INSTANCE.messageBuilder(ChoicePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ChoicePacket::decode)
                .encoder(ChoicePacket::encode)
                .consumerMainThread(ChoicePacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncScondaryVillagerPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncScondaryVillagerPacket::decode)
                .encoder(SyncScondaryVillagerPacket::encode)
                .consumerMainThread(SyncScondaryVillagerPacket::handle)
                .add();
    }

    // Helper method to send packet to server
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