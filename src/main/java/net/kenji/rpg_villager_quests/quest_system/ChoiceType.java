package net.kenji.rpg_villager_quests.quest_system;

public enum ChoiceType {
    UNCHOSEN(-1),
    OPTION_1(0),
    OPTION_2(1);

    public int choiceIndex;

    private ChoiceType(int choiceIndex){
        this.choiceIndex = choiceIndex;
    }

}
