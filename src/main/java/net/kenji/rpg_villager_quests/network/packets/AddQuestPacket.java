package net.kenji.rpg_villager_quests.network.packets;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class AddQuestPacket {
    private final String questId;
    private final UUID villagerUuid;

    public AddQuestPacket(String questId, UUID villagerUuid) {
        this.questId = questId;
        this.villagerUuid = villagerUuid;
    }

    // Encode: Write data to buffer
    public static void encode(AddQuestPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUUID(packet.villagerUuid);
    }

    // Decode: Read data from buffer
    public static AddQuestPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        UUID villagerUuid = buf.readUUID();
        return new AddQuestPacket(questId, villagerUuid);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(AddQuestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Quest quest = null;
            try {
                quest = VillagerQuestManager.getQuestByName(packet.questId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Entity entity = player.serverLevel().getEntity(packet.villagerUuid);

            if(entity instanceof Villager villager) {

                QuestData questData = QuestData.get(player.getUUID());

                if (questData.getQuestInstance(packet.questId) != null) return;

                questData.putQuest(quest, villager);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}