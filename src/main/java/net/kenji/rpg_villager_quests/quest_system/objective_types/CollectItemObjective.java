package net.kenji.rpg_villager_quests.quest_system.objective_types;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class CollectItemObjective implements QuestObjective {

    private final ResourceLocation item;
    private final int count;
    private final boolean consume;

    public CollectItemObjective(ResourceLocation item, int count, boolean consume) {
        this.item = item;
        this.count = count;
        this.consume = consume;
    }

    @Override
    public boolean isComplete(Player player) {
        return player.getInventory().countItem(
                Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item))
        ) >= count;
    }

    @Override
    public boolean canComplete(Player player) {
       boolean canComplete = false;
        for(ItemStack stack : player.getInventory().items){
           if(stack.getItem() == ForgeRegistries.ITEMS.getValue(item)){
               if(stack.getCount() >= this.count){
                   canComplete = true;
               }
           }
       }
        return canComplete;
    }

    @Override
    public void onTurnIn(Player player) {
        if (!consume) return;

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
}
