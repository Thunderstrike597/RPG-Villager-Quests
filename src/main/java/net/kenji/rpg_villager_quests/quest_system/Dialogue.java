package net.kenji.rpg_villager_quests.quest_system;

import java.util.List;

public class Dialogue {
    public static class Outcome{
        public final List<Page> pages;
        Outcome(List<Page> pages){
            this.pages = pages;
        }
    }

    public final Outcome outcome;
    public final Outcome altOutcome;

    Dialogue(Outcome outcome, Outcome altOutcome){
        this.outcome = outcome;
        this.altOutcome = altOutcome;
    }
}
