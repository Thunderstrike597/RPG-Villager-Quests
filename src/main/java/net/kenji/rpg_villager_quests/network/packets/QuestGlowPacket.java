package net.kenji.rpg_villager_quests.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class QuestGlowPacket {

    private final int entityId;
    private final boolean glowing;



    public QuestGlowPacket(int entityId, boolean glowing) {
        this.entityId = entityId;
        this.glowing = glowing;
    }

    /* ---------------- ENCODE / DECODE ---------------- */

    public static void encode(QuestGlowPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.entityId);
        buf.writeBoolean(packet.glowing);
    }

    public static QuestGlowPacket decode(FriendlyByteBuf buf) {
        return new QuestGlowPacket(
                buf.readInt(),
                buf.readBoolean());
    }

    /* ---------------- HANDLE (CLIENT SIDE) ---------------- */

    public static void handle(QuestGlowPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = level.getEntity(msg.entityId);
            if (entity != null) {
                entity.setGlowingTag(msg.glowing);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
