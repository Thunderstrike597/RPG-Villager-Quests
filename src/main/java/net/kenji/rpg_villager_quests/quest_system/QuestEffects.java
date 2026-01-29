package net.kenji.rpg_villager_quests.quest_system;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class QuestEffects {
    public boolean giveReward = true;
    public boolean endQuest = false;
    public int itemCount = 0;
    public ItemStack removeItem = ItemStack.EMPTY;

    public void apply(ServerPlayer player){
        if(removeItem != null && !removeItem.isEmpty()){
            player.getInventory().removeItem(removeItem);
        }
    }
}