package net.kenji.rpg_villager_quests.quest_system;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Random;

public class DialogueSet {
    public static class Dialogue {
        public final List<Page> pages;
        public final List<Page> altPages;
        Dialogue(List<Page> pages){
            this.pages = pages;
            this.altPages = null;
        }
        Dialogue(List<Page> pages, List<Page> altPages){
            this.pages = pages;
            this.altPages = altPages;
        }
    }
    public final Dialogue main;
    public final Dialogue positive;
    public final Dialogue negative;

    DialogueSet(Dialogue positive, Dialogue negative){
        this.positive = positive;
        this.negative = negative;
        this.main = null;
    }
    DialogueSet(Dialogue positive, Dialogue negative, Dialogue main){
        this.positive = positive;
        this.negative = negative;
        this.main = main;
    }
    DialogueSet(Dialogue main){
        this.positive = null;
        this.negative = null;
        this.main = main;
    }

    public List<Page> getMainPages(Player player){
        boolean chance = player.getRandom().nextBoolean();
        if(main != null && main.altPages != null) {
            return chance ? main.pages : main.altPages;
        }
        if(main != null){
            return main.pages;
        }
        return null;
    }

}
