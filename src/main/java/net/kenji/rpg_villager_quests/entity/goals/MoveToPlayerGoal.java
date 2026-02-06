package net.kenji.rpg_villager_quests.entity.goals;

import net.kenji.rpg_villager_quests.client.menu.VillagerQuestMenu;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.jline.utils.Log;

import java.util.EnumSet;

public class MoveToPlayerGoal extends Goal {

    private final Villager villager;
    private Player target;
    private final double speed;
    private final float stopDistance;

    public MoveToPlayerGoal(Villager villager, double speed, float stopDistance) {
        this.villager = villager;
        this.speed = speed;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player player = villager.getTradingPlayer();

        if (player != null &&
                VillagerQuestManager.isQuestMenuOpen
                        .getOrDefault(player.getUUID(), false)) {

            this.target = player; // 🔥 REQUIRED
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null
                && isQuestMenuOpen(target)
                && villager.distanceTo(target) > stopDistance;
    }
    @Override
    public void start() {
        Log.info("MoveToPlayerGoal START");
    }
    @Override
    public void tick() {
        villager.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (villager.distanceTo(target) > stopDistance) {
            villager.getNavigation().moveTo(
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    speed
            );
        } else {
            villager.getNavigation().stop();
        }
    }

    private boolean isQuestMenuOpen(Player player) {

        return VillagerQuestManager.isQuestMenuOpen.getOrDefault(player.getUUID(), false);
    }
}
