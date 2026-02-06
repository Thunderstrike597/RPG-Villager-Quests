package net.kenji.rpg_villager_quests.sound;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS;

    public static final RegistryObject<SoundEvent> VILLAGER_TALK;

    private static RegistryObject<SoundEvent> registerSound(String name) {
        ResourceLocation res = new ResourceLocation(RpgVillagerQuests.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(res));
    }

    static {
        SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RpgVillagerQuests.MODID);
        VILLAGER_TALK = registerSound("entity.villager.villager_talk");
    }
}

