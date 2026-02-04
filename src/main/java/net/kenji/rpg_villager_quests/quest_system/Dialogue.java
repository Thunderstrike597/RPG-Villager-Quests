package net.kenji.rpg_villager_quests.quest_system;

import java.util.List;

public class Dialogue {
    public static class Outcome{
        public final List<Page> pages;
        Outcome(List<Page> pages){
            this.pages = pages;
        }
    }
    public final Outcome main;
    public final Outcome positive;
    public final Outcome negative;
    public final boolean isDefaultText;

    Dialogue(Outcome positive, Outcome negative){
        this.positive = positive;
        this.negative = negative;
        this.main = null;
        this.isDefaultText = false;
    }
    Dialogue(Outcome positive, Outcome negative, Outcome main){
        this.positive = positive;
        this.negative = negative;
        this.main = main;
        this.isDefaultText = (positive == null | negative == null) && main != null;
    }
}
