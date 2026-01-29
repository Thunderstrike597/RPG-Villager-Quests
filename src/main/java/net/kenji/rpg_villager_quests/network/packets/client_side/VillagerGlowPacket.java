package net.kenji.rpg_villager_quests.network.packets.client_side;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class VillagerGlowPacket {
    private final int villagerId;
    private final boolean shouldGlow;


    public VillagerGlowPacket(int villagerId, boolean shouldGlow) {
        this.villagerId = villagerId;
        this.shouldGlow = shouldGlow;
    }

    // Encode: Write data to buffer
    public static void encode(VillagerGlowPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.villagerId);
        buf.writeBoolean(packet.shouldGlow);
    }

    // Decode: Read data from buffer
    public static VillagerGlowPacket decode(FriendlyByteBuf buf) {
        int villagerId = buf.readInt();
        boolean shouldGlow = buf.readBoolean();
        return new VillagerGlowPacket(villagerId, shouldGlow);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(VillagerGlowPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;

                if(mc.level != null) {
                    Entity entity = mc.level.getEntity(packet.villagerId);

                    if (entity != null) {
                        entity.setGlowingTag(packet.shouldGlow);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}