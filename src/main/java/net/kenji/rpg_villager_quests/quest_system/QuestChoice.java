package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.jline.utils.Log;

import java.util.List;

public class QuestChoice {
    public final String id;
    public final String text;
    public final QuestEffects effects;
    public final List<QuestReward> rewards;

    public QuestChoice(
            String id,
            String text,
            QuestEffects effects,
            List<QuestReward> rewards
    ) {
        this.id = id;
        this.text = text;

        this.rewards = rewards;
        if(effects == null){
            this.effects = new QuestEffects();
        }
        else this.effects = effects;

    }

    public void applyRewards(Player player){
        if(rewards != null){
            for (QuestReward reward : rewards){
                reward.apply(player);
            }
        }
        if(effects.removeItem != null && !effects.removeItem.isEmpty()){

            var inv = player.getInventory();
            var itemObj = effects.removeItem.getItem();
            Log.info("IS LOGGING ITEM COUNT: " + effects.removeItem.getCount());
            int remaining = effects.removeItem.getCount();
            for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
                var stack = inv.getItem(i);
                if (stack.is(itemObj)) {
                    int removed = Math.min(stack.getCount(), remaining);
                    stack.shrink(removed);
                    remaining -= removed;
                }
            }
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }
}