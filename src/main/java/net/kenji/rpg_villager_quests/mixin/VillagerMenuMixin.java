package net.kenji.rpg_villager_quests.mixin;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMenuMixin {

    @Inject(method = "startTrading", at = @At("HEAD"))
    private void InterruptTrading(Player pPlayer, CallbackInfo ci){

    }
}
