package net.kenji.rpg_villager_quests.quest_system.objective_types;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class CollectItemObjective implements QuestObjective {

    private final ResourceLocation item;
    private final int count;
    private final boolean consume;
    private final String belongingQuestId;
    public CollectItemObjective(ResourceLocation item, int count, boolean consume, String belongingQuestId) {
        this.item = item;
        this.count = count;
        this.consume = consume;
        this.belongingQuestId = belongingQuestId;
    }

    @Override
    public void onStartObjective(Player player, QuestInstance questInstance) {

    }

    @Override
    public void onRestartObjective(Player player) {

    }

    @Override
    public boolean canComplete(Player player) {
        Item requiredItem = ForgeRegistries.ITEMS.getValue(item);
        if (requiredItem == null) return false;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;

            if (stack.is(requiredItem) && stack.getCount() >= this.count) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onComplete(QuestEffects effects, Player player) {
        if(!consume) return;

        var inv = player.getInventory();
        var itemObj = ForgeRegistries.ITEMS.getValue(item);

        int remaining = count;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            var stack = inv.getItem(i);
            if (stack.is(itemObj)) {
                int removed = Math.min(stack.getCount(), remaining);
                stack.shrink(removed);
                remaining -= removed;
            }
        }
    }

    @Override
    public boolean shouldRestartObjective(Player player) {
        return false;
    }
}
