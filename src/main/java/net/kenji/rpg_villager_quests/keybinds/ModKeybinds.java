package net.kenji.rpg_villager_quests.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

    public static final String CATEGORY = "key.categories.rpg_villager_quests";

    public static final KeyMapping NEXT_PAGE_KEY = new KeyMapping(
            "key.rpg_villager_quests.next",
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_ENTER,
            CATEGORY
    );
}

