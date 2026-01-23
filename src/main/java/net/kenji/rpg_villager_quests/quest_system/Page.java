package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;

public class Page{
    public DialogueStage.DialogueType dialogueType = DialogueStage.DialogueType.REGULAR;
    public boolean endQuest;
    public String text;
    public String button1Text;
    public String button2Text;

}