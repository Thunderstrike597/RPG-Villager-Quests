package net.kenji.rpg_villager_quests.quest_system.reward_types;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ItemReward implements QuestReward {
    public final ResourceLocation item;
    public final int count;

    public ItemReward(ResourceLocation item, int count) {
        this.item = item;
        this.count = count;
    }

    @Override
    public void apply(Player player) {
        player.getInventory().add(new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)), count));
    }
}