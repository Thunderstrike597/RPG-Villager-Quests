package net.kenji.rpg_villager_quests.quest_system;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class QuestEffects {
    public boolean removeItem;
    public boolean keepItem;
    public boolean giveReward;
    public ResourceLocation itemReward;
    public int itemCount;

    public void apply(Player player) {
        player.getInventory().add(new ItemStack((ItemLike) itemReward, itemCount));
    }
}