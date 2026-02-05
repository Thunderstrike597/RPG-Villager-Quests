package net.kenji.rpg_villager_quests.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class RpgVillagerQuestsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rpg_villager_quests")
                        .requires(source -> source.hasPermission(2)) // optional
                        .then(SummonQuestVillagerCommand.register())
        );
    }
}
