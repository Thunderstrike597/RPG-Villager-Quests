package net.kenji.rpg_villager_quests.quest_system.objective_types;

import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.SyncScondaryVillagerPacket;
import net.kenji.rpg_villager_quests.quest_system.Page;
import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class PackageDeliverObjective extends SecondaryVillagerQuestObjective {

    private final Item item;
    private final int maxDistance;
    private final int minDistance;
    private final TagKey<Structure> structure;
    private final boolean consume;
    private final String belongingQuestId;
    private final String belongingStageId;

    public static String objectiveEntityTag = "OBJECTIVE_ENTITY";

    public boolean hasDelivered;
    public PackageDeliverObjective(ResourceLocation item, ResourceLocation deliverEntity, boolean consume, String belongingQuestId, String belongingStageId,int maxDistance, int minDistance, String structure, List<Page> secondaryDialogue, List<Page> deliveredDialogue) {
       super(secondaryDialogue, deliveredDialogue);
        this.item = ForgeRegistries.ITEMS.getValue(item);
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
        this.consume = consume;
        this.belongingQuestId = belongingQuestId;
        this.belongingStageId = belongingStageId;
        if(structure != null)
        this.structure = TagKey.create(
                Registries.STRUCTURE,
                new ResourceLocation(structure));
        else this.structure = null;
    }
    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {

        QuestData questData = QuestData.get(event.player.getUUID());
        if (questData.getActiveQuests() != null) {
            for (QuestInstance questInstance : questData.getActiveQuests()) {
                if (!questInstance.isComplete()) {
                    if (questInstance.getCurrentStage() instanceof ObjectiveStage objectiveStage) {
                        if (objectiveStage.getObjective() instanceof PackageDeliverObjective deliverPackageObjective) {
                            if(deliverPackageObjective.canComplete(event.player)){
                                if(objectiveStage.tag != null){
                                    objectiveStage.onComplete(objectiveStage.getStageEffects(), event.player, questInstance);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStartObjective(Player player, QuestInstance questInstance) {
        Level level = player.level();

        if(level instanceof ServerLevel serverLevel) {
            if (player instanceof ServerPlayer serverPlayer) {
                BlockPos pos;
                BlockPos origin = player.blockPosition();
                int randomDist = (int) Mth.randomBetween(player.getRandom(), minDistance, maxDistance);

                if (structure != null) {
                    BlockPos structurePos = serverLevel.findNearestMapStructure(
                            structure,
                            origin,
                            randomDist / 16,
                            false
                    );

                    if (structurePos != null) {
                        int x = structurePos.getX();
                        int z = structurePos.getZ();

                        BlockPos chunkPos = new BlockPos(x, 0, z);
                        serverLevel.getChunk(chunkPos);  // This forces chunk load

                        BlockPos surface = serverLevel.getHeightmapPos(
                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                chunkPos
                        );

                        // Safety check: ensure Y is reasonable
                        if (surface.getY() < serverLevel.getMinBuildHeight()) {
                            surface = new BlockPos(x, serverLevel.getSeaLevel(), z);
                        }

                        pos = surface.above();
                    } else {
                        pos = null;
                    }
                } else {
                    RandomSource random = level.random;

                    double angle = random.nextDouble() * Math.PI * 2.0;
                    double distance = Mth.randomBetween(random, minDistance, maxDistance);

                    int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
                    int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);

                    // Force load the chunk before querying heightmap
                    BlockPos chunkPos = new BlockPos(x, 0, z);
                    serverLevel.getChunk(chunkPos);  // This forces chunk load

                    BlockPos surface = serverLevel.getHeightmapPos(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            chunkPos
                    );

                    // Safety check: ensure Y is reasonable
                    if (surface.getY() < serverLevel.getMinBuildHeight()) {
                        surface = new BlockPos(x, serverLevel.getSeaLevel(), z);
                    }

                    pos = surface.above();
                }

                if (pos != null) {
                    Villager newVillager = EntityType.VILLAGER.spawn(serverLevel, ItemStack.EMPTY, player, pos, MobSpawnType.TRIGGERED, false, false);
                    if (newVillager != null) {
                        questInstance.currentSecondaryEntity = newVillager.getUUID();
                        ModPacketHandler.sendToPlayer(new SyncScondaryVillagerPacket(belongingQuestId, newVillager.getUUID()), serverPlayer);
                        Entity entity = serverLevel.getEntity(questInstance.currentSecondaryEntity);
                        if (entity instanceof Villager villagerEntity) {
                            villagerEntity.getPersistentData().putUUID(objectiveEntityTag, questInstance.getQuestVillager());
                        }
                    }
                    Component coords = Component.literal(
                            "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(
                                    ClickEvent.Action.SUGGEST_COMMAND,
                                    "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                            ))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Click to teleport")
                            ))
                    );

                    player.sendSystemMessage(
                            Component.literal("Delivery target located at ").append(coords)
                    );
                }

                ItemStack stack = new ItemStack(item);
                stack.getOrCreateTag().putString("QuestID", belongingQuestId); // unique tag
                stack.getOrCreateTag().putBoolean("QuestDeliveryItem", true); // optional extra
                player.addItem(stack);
            }
        }
    }

    @Override
    public void onRestartObjective(Player player) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString("QuestID", belongingQuestId); // unique tag
        stack.getOrCreateTag().putBoolean("QuestDeliveryItem", true); // optional extra
        player.addItem(stack);
    }

    @Override
    public boolean canComplete(Player player) {
        var inv = player.getInventory();
        Slot slot = null;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.is(item)){
                slot = player.inventoryMenu.getSlot(i);
                break;
            }
        }
        return slot != null;
    }

    @Override
    public void onComplete(QuestEffects effects, Player player) {
        if(!consume) return;

        var inv = player.getInventory();


        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.is(item)) {
                inv.removeItem(stack);
            }
        }
    }

    @Override
    public boolean shouldRestartObjective(Player player) {
       boolean restart = true;
        for(int i = 0; i < player.getInventory().getContainerSize(); i++){
           ItemStack stack = player.getSlot(i).get();
            if (stack.is(item) && stack.getTag() != null && stack.hasTag() &&
                    stack.getTag().getString("QuestID").equals(belongingQuestId)) {
                restart = false;
                break;
            }
       }
        return restart;
    }
}
