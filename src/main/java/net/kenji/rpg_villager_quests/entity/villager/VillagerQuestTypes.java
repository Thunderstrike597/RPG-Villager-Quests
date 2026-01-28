package net.kenji.rpg_villager_quests.entity.villager;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerType;

public class VillagerQuestTypes {
    public static VillagerType QUEST_VILLAGER;

    public static VillagerType registerVillageType(String pKey) {
        return Registry.register(BuiltInRegistries.VILLAGER_TYPE, new ResourceLocation(pKey), new VillagerType(pKey));
    }
}
