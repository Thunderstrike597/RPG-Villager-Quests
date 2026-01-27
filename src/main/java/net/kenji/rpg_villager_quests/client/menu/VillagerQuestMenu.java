package net.kenji.rpg_villager_quests.client.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.keybinds.ModKeybinds;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.network.packets.ChoicePacket;
import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.StageCompletionPacket;
import net.kenji.rpg_villager_quests.quest_system.Page;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestChoice;
import net.kenji.rpg_villager_quests.quest_system.Reputation;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.jline.utils.Log;

import java.util.*;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class VillagerQuestMenu extends Screen {

    public class GuiDisplay{
        public ResourceLocation texture;
        private int texWidth;
        private int texHeight;
        private int texUvX;
        private int texUvY;
        private int offsetWidth;
        private int offsetHeight;
        private int offsetX;
        private int offsetY;
        public int TEXTURE_WIDTH;
        public int TEXTURE_HEIGHT;

        GuiDisplay(ResourceLocation texture,int texWidth, int textHeight, int texUvX, int texUvY, int offsetWidth, int offsetHeight, int offsetX, int offsetY, int texture_width ,int texture_height){
            this.texture = texture;
            this.texWidth = texWidth;
            this.texHeight = textHeight;
            this.texUvX = texUvX;
            this.texUvY = texUvY;
            this.offsetWidth = offsetWidth;
            this.offsetHeight = offsetHeight;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.TEXTURE_WIDTH = texture_width;
            this.TEXTURE_HEIGHT = texture_height;
        }

        private float texRatioWidth = texWidth * 0.25F;
        private float textRatioHeight = texHeight * 0.25F;

        public float DISPLAY_WIDTH = GetTextureSize().x + offsetWidth;
        public int DISPLAY_HEIGHT = GetTextureSize().y + offsetHeight;

        private float scaleX = (float) DISPLAY_WIDTH / TEXTURE_WIDTH;
        private float scaleY = (float) DISPLAY_HEIGHT / TEXTURE_HEIGHT;

        public ResourceLocation getTexture(){
            return texture;
        }
        public IntPair GetTextureSize() {
            Minecraft minecraft = Minecraft.getInstance();
            IntPair uvCoords = new IntPair();
            uvCoords.x = (int) texRatioWidth;
            uvCoords.y = (int) textRatioHeight;
            return uvCoords;
        }
        public IntPair GetUVCoords(){
            Minecraft minecraft = Minecraft.getInstance();
            IntPair uvCoords = new IntPair();
            uvCoords.x = texUvX;
            uvCoords.y = texUvY;

            return uvCoords;
        }

        public float getScaleX(){
            return scaleX;
        }
        public float getScaleY(){
            return scaleY;
        }
    }

    public static ResourceLocation MENU_BACKGROUND = ResourceLocation.fromNamespaceAndPath(RpgVillagerQuests.MODID, "textures/gui/villager_quest_menu.png");
    public static ResourceLocation HEAD_DISPLAY = ResourceLocation.fromNamespaceAndPath(RpgVillagerQuests.MODID, "textures/gui/villager_quest_head_display.png");
    public static ResourceLocation HEAD_DISPLAY_OVERLAY = ResourceLocation.fromNamespaceAndPath(RpgVillagerQuests.MODID, "textures/gui/villager_quest_head_display_overlay.png");

    public static GuiDisplay backgroundGui;
    public static GuiDisplay headDisplayGui;
    private static final String PAGE_DELIMITER = "/--/";

    public static boolean didPositiveInteraction;
    private int currentPageIndex = 0;
    private List<Page> pages;
    private int visibleChars = 0;
    private long lastTypeTime = 0;
    private List<String> words;
    private int currentSentenceStartDelay = 0;
    private static final int TYPE_SPEED_MS = 35;
    private static final int TYPE_SPEED_SKIP_MS = 10;
    private static final int PERIOD_PAUSE_MS = 600; // Pause after periods
    private static final int COMMA_PAUSE_MS = 400; // Pause after periods
    private static final int EXCLAMATION_PAUSE_MS = 900; // Pause after periods
    private static final int QUESTION_MARK_PAUSE_MS = 800; // Pause after periods

    private static final int SENTENCE_START_DELAY = 10; // Pause after periods

    private boolean isPausedAfterPeriod = false;
    private boolean isPausedAfterComma = false;
    private boolean isPausedAfterQuestionMark = false;
    private boolean isPausedAfterExclamationMark = false;
    private long pauseStartTime = 0;
    private static boolean hasSentenceCompleted;
    public Button posButton;
    public Quest villagerQuest;
    private boolean skipDialogue;


    private boolean pendingInit = false;
    private enum DisplayType{
        BACKGROUND_DISPLAY,
        HEAD_DISPLAY
    }
    private enum ButtonType{
        POSITIVE,
        NEGATIVE
    }


    private UUID questVillager;
    private UUID interactingVillager;

    private static List<String> getPages(String raw) {
        return Arrays.stream(raw.split(Pattern.quote(PAGE_DELIMITER)))
                .map(String::trim)
                .toList();
    }
    private String getButtonText(QuestInstance questInstance, ButtonType buttonType){
        if(getCurrentPage().dialogueType != DialogueStage.DialogueType.CHOICE){
            if(buttonType == ButtonType.POSITIVE)
                return getCurrentPage().button1Text;
            if(buttonType == ButtonType.NEGATIVE)
                return getCurrentPage().button2Text;
        }
        else{
           if(questInstance.getCurrentStage() instanceof DialogueStage dialogueStage){
               if(dialogueStage.choices != null){
                   if(buttonType == ButtonType.POSITIVE)
                       return dialogueStage.choices.get(0).text;
                   if(buttonType == ButtonType.NEGATIVE)
                       return dialogueStage.choices.get(1).text;
               }
           }
        }
        return "No Text";
    }

    private Page getCurrentPage(){
      return pages.get(currentPageIndex);
    }

    private String getFinalPage(){
        return pages.get(pages.size() - 1).text;
    }

    public VillagerQuestMenu(Component pTitle, UUID questVillager, UUID secondaryVillager) {
        super(pTitle);
        this.questVillager = questVillager;
        this.interactingVillager = secondaryVillager;

        villagerQuest = VillagerQuestManager.getVillagerQuest(this.questVillager);
    }
    private int getBgWidth(GuiDisplay ui) {
        return ui.TEXTURE_WIDTH;
    }

    private int getBgHeight(GuiDisplay ui) {
        return ui.TEXTURE_HEIGHT;
    }

    private int getX(GuiDisplay ui) {
        return (this.width - getBgWidth(ui)) / 2;
    }

    private int getY(GuiDisplay ui) {
        return (this.height - getBgHeight(ui)) / 2;
    }


    @Override
    protected void init() {
       Player player = Minecraft.getInstance().player;
        if(VillagerQuestManager.getVillagerQuest(questVillager) == null)
            return;
        backgroundGui = new GuiDisplay(
                MENU_BACKGROUND,
                228,
                96,
                15,
                135,
                30,
                20,
                130,
                0,
                228,
                96);
        headDisplayGui = new GuiDisplay(
                HEAD_DISPLAY,
                90,
                90,
                0, 0,
                0,
                0,
                120,
                -35,
                50,
                50);
        if(player == null)
            return;

        player.playSound(SoundEvents.VILLAGER_TRADE);

        int bgX = getX(backgroundGui);
        int bgY = getY(backgroundGui);
        int bgW = getBgWidth(backgroundGui);
        int bgH = getBgHeight(backgroundGui);

        int buttonWidth = 90;
        int buttonHeight = 20;
        int padding = 8;

        int xOffset = -100;
        int posYOffset = -10;
        int negYOffset = 15;


        QuestData questData = QuestData.get(player.getUUID());
        QuestInstance questInstance = questData.getQuestInstance(villagerQuest.id);

        pages = getQuestDialoge(questData, questInstance);
        words = Arrays.asList(pages.get(currentPageIndex).text.split("(?<=\\s)"));
        currentSentenceStartDelay = 0;

        int pX = bgX + padding;
        int pY = bgY + bgH - buttonHeight - padding;
    }

    public void addButtons(Player player, QuestData questData) {
        // Recalculate the same values as in render()
        int DISPLAY_WIDTH = GetTextureSize(DisplayType.BACKGROUND_DISPLAY).x + 20;
        int DISPLAY_HEIGHT = GetTextureSize(DisplayType.BACKGROUND_DISPLAY).y + 10;

        int x = GetUVCoords().x - 15;
        int y = GetUVCoords().y;

        float scaleX = (float) DISPLAY_WIDTH / TEXTURE_WIDTH;
        float scaleY = (float) DISPLAY_HEIGHT / TEXTURE_HEIGHT;

        // Calculate where the background ACTUALLY is on screen
        int actualBgX = x + (int)(backgroundGui.offsetX * scaleX);
        int actualBgY = y + (int)(backgroundGui.offsetY * scaleY);
        int actualBgW = (int)(backgroundGui.TEXTURE_WIDTH * scaleX);
        int actualBgH = (int)(backgroundGui.TEXTURE_HEIGHT * scaleY);

        // Scale all button dimensions and offsets
        int buttonWidth = (int)(45 * scaleX);
        int buttonHeight = (int)(15 * scaleY);
        int padding = (int)(8 * scaleX);

        int xOffset = (int)(-55 * scaleX);
        int posYOffset = (int)(-60 * scaleY);
        int negYOffset = (int)(-35 * scaleY);

        // Use ACTUAL background position
        int pX = actualBgX + padding;
        int pY = actualBgY + actualBgH - buttonHeight - padding;

        if (hasSentenceCompleted) {
            if (currentPageIndex == pages.size() - 1 && questData.getQuestInstance(villagerQuest.getQuestId()) == null) {
                if (getCurrentPage().button1Text == null || Objects.equals(getCurrentPage().button1Text, "")) {
                    posButton = this.addRenderableWidget(
                            Button.builder(
                                    Component.literal("Accept Quest"),
                                    btn -> onAcceptQuest(player)
                            ).bounds(
                                    pX + xOffset,
                                    pY + posYOffset,
                                    buttonWidth,
                                    buttonHeight
                            ).build()
                    );
                } else {
                    posButton = this.addRenderableWidget(
                            Button.builder(
                                    Component.literal(getCurrentPage().button1Text),
                                    btn -> onAcceptQuest(player)
                            ).bounds(
                                    pX + xOffset,
                                    pY + posYOffset,
                                    buttonWidth,
                                    buttonHeight
                            ).build()
                    );
                }
            } else {
                QuestInstance questInstance = questData.getQuestInstance(villagerQuest.getQuestId());
                if ((questInstance != null && questInstance.getCurrentStage().canCompleteStage(player)) || currentPageIndex < pages.size() - 1) {
                    if (!Objects.equals(getCurrentPage().button1Text, "NONE")) {
                        if ((getButtonText(questInstance, ButtonType.POSITIVE) == null || getButtonText(questInstance, ButtonType.POSITIVE).isEmpty()) && getCurrentPage().dialogueType != DialogueStage.DialogueType.CHOICE) {

                            posButton = this.addRenderableWidget(
                                    Button.builder(
                                            Component.literal("Next"),
                                            btn -> {
                                                if (questInstance != null) {

                                                    onPositivePress(player, questInstance);
                                                } else {
                                                    onNextPage(player);
                                                }
                                            }
                                    ).bounds(
                                            pX + xOffset,
                                            pY + posYOffset,
                                            buttonWidth,
                                            buttonHeight
                                    ).build()
                            );
                        } else {

                            posButton = this.addRenderableWidget(
                                    Button.builder(
                                            Component.literal(getButtonText(questInstance, ButtonType.POSITIVE)),
                                            btn -> {
                                                if (questInstance != null) {

                                                    onPositivePress(player, questInstance);
                                                } else {
                                                    onNextPage(player);
                                                }
                                            }
                                    ).bounds(
                                            pX + xOffset,
                                            pY + posYOffset,
                                            buttonWidth,
                                            buttonHeight
                                    ).build()
                            );
                        }
                    }
                }
            }
            QuestInstance questInstance = questData.getQuestInstance(villagerQuest.getQuestId());
            if (!Objects.equals(getCurrentPage().button2Text, "NONE")) {
                if (questInstance != null) {
                    if (getButtonText(questInstance, ButtonType.NEGATIVE) == null || getButtonText(questInstance, ButtonType.NEGATIVE).isEmpty()) {
                        this.addRenderableWidget(
                                Button.builder(
                                        Component.literal("Close"),
                                        btn -> onNegativePress(player, questInstance)
                                ).bounds(
                                        pX + xOffset,
                                        pY + negYOffset,
                                        buttonWidth,
                                        buttonHeight
                                ).build()
                        );
                    } else {
                        this.addRenderableWidget(
                                Button.builder(
                                        Component.literal(getButtonText(questInstance, ButtonType.NEGATIVE)),
                                        btn -> onNegativePress(player, questInstance)
                                ).bounds(
                                        pX + xOffset,
                                        pY + negYOffset,
                                        buttonWidth,
                                        buttonHeight
                                ).build()
                        );
                    }
                } else {
                    if (getCurrentPage().button2Text == null || Objects.equals(getCurrentPage().button2Text, "")) {
                        this.addRenderableWidget(
                                Button.builder(
                                        Component.literal("Close"),
                                        btn -> this.onClose()
                                ).bounds(
                                        pX + xOffset,
                                        pY + negYOffset,
                                        buttonWidth,
                                        buttonHeight
                                ).build()
                        );
                    } else {
                        this.addRenderableWidget(
                                Button.builder(
                                        Component.literal(getCurrentPage().button2Text),
                                        btn -> this.onClose()
                                ).bounds(
                                        pX + xOffset,
                                        pY + negYOffset,
                                        buttonWidth,
                                        buttonHeight
                                ).build()
                        );
                    }
                }
            }
        }
    }

    private List<Page> getQuestDialoge(QuestData questData, QuestInstance questInstance){
        if(questVillager == interactingVillager && questData.getActiveQuests() == null || !questData.getActiveQuests().contains(questInstance))
            return villagerQuest.stages.get(0).pages;
        else {
            return questInstance.getCurrentStage().getDialogue(questInstance, interactingVillager);
        }
    }

    public void onAcceptQuest(Player player){
        player.playSound(SoundEvents.VILLAGER_YES);
        didPositiveInteraction = true;
        hasSentenceCompleted = false;
        Quest quest = VillagerQuestManager.getVillagerQuest(questVillager);
        quest.StartQuestClient(player, questVillager);
        onClose();
    }
    public void onNextPage(Player player) {
        if (currentPageIndex < pages.size()) {
            if(currentPageIndex < pages.size() - 1)
                currentPageIndex++;
            visibleChars = 0;
            lastTypeTime = 0;
            isPausedAfterPeriod = false;
            pauseStartTime = 0;
            words = Arrays.asList(pages.get(currentPageIndex).text.split("(?<=\\s)"));
            currentSentenceStartDelay = 0;
            hasSentenceCompleted = false;
            player.playSound(SoundEvents.VILLAGER_TRADE);

            this.clearWidgets();
            if(posButton != null)
                posButton.active = false;
            this.init();
        }
    }
    public void onPositivePress(Player player, QuestInstance questInstance){
        if(questInstance != null && !questInstance.isComplete()) {
            if (pages.get(currentPageIndex).dialogueType != DialogueStage.DialogueType.CHOICE) {
                if (currentPageIndex < pages.size() - 1) {
                    onNextPage(player);
                } else {
                    if (questInstance.getCurrentStage().canCompleteStage(player)) {
                        if (pages.get(currentPageIndex).effects == null || !pages.get(currentPageIndex).effects.endQuest) {
                            onCompleteStage(player, questInstance, pages.get(currentPageIndex));
                        } else if (pages.get(currentPageIndex).effects != null && pages.get(currentPageIndex).effects.endQuest) {
                            onCompleteQuest(player, questInstance, pages.get(currentPageIndex));
                        }

                        if (!(questInstance.getCurrentStage() instanceof DialogueStage))
                            onClose();
                    } else {
                        if (!(questInstance.getCurrentStage().getNextStage(player, questInstance) instanceof DialogueStage))
                            onClose();
                    }
                }
            } else {
                if (questInstance.getCurrentStage() instanceof DialogueStage dialogueStage) {
                    dialogueStage.setChosenDialogue(DialogueStage.ChoiceType.OPTION_1);
                    Page storedPage = pages.get(currentPageIndex);

                    currentPageIndex = 0;  // Reset to first page of new dialogue
                    visibleChars = 0;
                    lastTypeTime = 0;
                    words = Arrays.asList(pages.get(currentPageIndex).text.split("(?<=\\s)"));
                    currentSentenceStartDelay = 0;
                    hasSentenceCompleted = false;
                    questInstance.setQuestReputation(Reputation.GOOD);

                    if (dialogueStage.choices.get(0) != null) {
                        QuestChoice choice = dialogueStage.choices.get(1);
                        if (choice.effects != null) {
                            if (dialogueStage.choices.get(0).effects.endQuest) {
                                onCompleteQuest(player, questInstance, storedPage);
                                return;
                            }
                            choice.effects.apply(player);
                            choice.applyRewards(player);
                            ModPacketHandler.sendToServer(new ChoicePacket(questInstance.getQuest().getQuestId(), questInstance.getCurrentStage().id, 0));
                        }
                    }
                    if (pages.get(currentPageIndex).effects != null) {
                        if (pages.get(currentPageIndex).effects.endQuest) {
                            onCompleteQuest(player, questInstance, storedPage);
                        }
                    }

                    this.clearWidgets();
                    this.init();
                }
            }
        }
        else{
            if(currentPageIndex < pages.size() - 1){
                onNextPage(player);
            }
            else{
                onAcceptQuest(player);
            }
        }
    }
    public void onNegativePress(Player player, QuestInstance questInstance){
        if(questInstance != null && !questInstance.isComplete()) {

            if (pages.get(currentPageIndex).dialogueType != DialogueStage.DialogueType.CHOICE) {
                if (questInstance.getCurrentStage().canCompleteStage(player)) {
                    int currentStageIndex = questInstance.getCurrentStageIndex();
                    boolean isLastStage = currentStageIndex == questInstance.getQuest().stages.size() - 1;

                    if (isLastStage) {
                        if (pages.get(currentPageIndex).effects == null || !pages.get(currentPageIndex).effects.endQuest) {
                            onCompleteStage(player, questInstance, pages.get(currentPageIndex));
                            this.onClose();
                        } else if (pages.get(currentPageIndex).effects != null && pages.get(currentPageIndex).effects.endQuest) {
                            onCompleteQuest(player, questInstance, pages.get(currentPageIndex));
                        }
                    } else {
                        this.onClose();
                    }
                }
                else{
                    this.onClose();
                }
            } else {
                if (questInstance.getCurrentStage() instanceof DialogueStage dialogueStage) {
                    dialogueStage.setChosenDialogue(DialogueStage.ChoiceType.OPTION_2);
                    Page storedPage = pages.get(currentPageIndex);
                    currentPageIndex = 0;  // Reset to first page of new dialogue
                    visibleChars = 0;
                    lastTypeTime = 0;
                    words = Arrays.asList(pages.get(currentPageIndex).text.split("(?<=\\s)"));
                    currentSentenceStartDelay = 0;
                    hasSentenceCompleted = false;
                    questInstance.setQuestReputation(Reputation.BAD);

                    if (dialogueStage.choices.get(1) != null) {
                        QuestChoice choice = dialogueStage.choices.get(1);
                        if (choice.effects != null) {
                            if (dialogueStage.choices.get(1).effects.endQuest) {
                                onCompleteQuest(player, questInstance, storedPage);
                                return;
                            }
                            choice.effects.apply(player);
                            ModPacketHandler.sendToServer(new ChoicePacket(questInstance.getQuest().getQuestId(), questInstance.getCurrentStage().id, 1));
                        }
                    }


                    this.clearWidgets();
                    this.init();
                }
            }
        } else{
           onClose();
        }
    }

    public void onCompleteQuest(Player player, QuestInstance questInstance, Page currentPage) {
        visibleChars = 0;
        lastTypeTime = 0;
        isPausedAfterPeriod = false;
        pauseStartTime = 0;
        words = Arrays.asList(pages.get(currentPageIndex).text.split("(?<=\\s)"));
        currentSentenceStartDelay = 0;
        hasSentenceCompleted = false;
        ModPacketHandler.sendToServer(new StageCompletionPacket(questInstance.getQuest().getQuestId(), questInstance.getCurrentStage().id, currentPage.effects));
        questInstance.getCurrentStage().onComplete(currentPage.effects, player,questInstance);

        Entity villagerEntity = null;
        for(Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
            if(entity.getUUID() == questVillager) {
                villagerEntity = entity;
                break;
            }
        }
        if(villagerEntity != null)
            villagerEntity.setGlowingTag(false);
        player.playSound(SoundEvents.VILLAGER_YES);
        didPositiveInteraction = true;
        this.onClose();
    }
    public void onCompleteStage(Player player, QuestInstance questInstance, Page currentPage) {
        if (currentPageIndex < pages.size()) {
            visibleChars = 0;
            lastTypeTime = 0;
            isPausedAfterPeriod = false;
            pauseStartTime = 0;
            words = Arrays.asList(pages.get(currentPageIndex).text.split("(?<=\\s)"));
            currentSentenceStartDelay = 0;
            hasSentenceCompleted = false;

            ModPacketHandler.sendToServer(new StageCompletionPacket(questInstance.getQuest().getQuestId(), questInstance.getCurrentStage().id, currentPage.effects));
            questInstance.getCurrentStage().onComplete(currentPage.effects, player,questInstance);

            player.playSound(SoundEvents.VILLAGER_TRADE);

            this.clearWidgets();
            posButton.active = false;
            this.init();
        }
    }

    @Override
    public void onClose() {
        Player player = Minecraft.getInstance().player;
        currentPageIndex = 0;
        visibleChars = 0;
        lastTypeTime = 0;
        isPausedAfterPeriod = false;
        pauseStartTime = 0;
        Villager villagerEntity = null;
        for(Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
            if(entity.getUUID() == questVillager) {
                if(entity instanceof Villager villagerEntity1) {
                    villagerEntity = villagerEntity1;
                    break;
                }
            }
        }
        if(villagerEntity != null) {
            villagerEntity.setTradingPlayer(null);
            villagerEntity = null;
        }
        if(player != null && !didPositiveInteraction)
            player.playSound(SoundEvents.VILLAGER_NO);

        didPositiveInteraction = false;
        hasSentenceCompleted = false;
        currentSentenceStartDelay = 0;
        if(posButton != null)
            posButton.active = false;
        super.onClose();
    }




    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final int TEXTURE_WIDTH = 228;
    private static final int TEXTURE_HEIGHT = 96;
    public static float prevStamina = -1;
    private static float redBarStamina = -1;
    private static class IntPair {
        public int x;
        public int y;
    }
    private static IntPair GetUVCoords(){
        Minecraft minecraft = Minecraft.getInstance();
        IntPair uvCoords = new IntPair();
            uvCoords.x = 15;
            uvCoords.y = 135;

        return uvCoords;
    }
    private static IntPair GetTextureSize(DisplayType displayType){
        Minecraft minecraft = Minecraft.getInstance();
        IntPair uvCoords = new IntPair();

        switch (displayType) {
            case BACKGROUND_DISPLAY -> {
                uvCoords.x = 239;
                uvCoords.y = 103;
            }
            case HEAD_DISPLAY -> {
                uvCoords.x = 90;
                uvCoords.y = 90;
            }
        }

        return uvCoords;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Check if our custom keybind was pressed
        if (ModKeybinds.NEXT_PAGE_KEY.matches(keyCode, scanCode)) {
            Player player = getMinecraft().player;
            if(player != null) {

                if (!hasSentenceCompleted) {
                    skipDialogue = true;

                    // Force-end any active pause
                    isPausedAfterPeriod = false;
                    isPausedAfterComma = false;
                    isPausedAfterExclamationMark = false;
                    isPausedAfterQuestionMark = false;

                    lastTypeTime = System.currentTimeMillis();
                    return true; // Consume the event
                } else {
                    QuestInstance questInstance = QuestData.get(player.getUUID()).getQuestInstance(villagerQuest.getQuestId());
                    if (posButton != null)
                        onPositivePress(player, questInstance);
                    else onNegativePress(player, questInstance);
                    return true;
                }
            }
        }
        // Let parent handle other keys (like ESC to close)
        return super.keyPressed(keyCode, scanCode, modifiers);
    }



    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        if(villagerQuest == null)
            return;
        this.renderBackground(gfx);
        Villager villagerEntity = null;
        for(Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
            if(entity.getUUID() == questVillager || entity.getUUID() == interactingVillager) {
                if(entity instanceof Villager villagerEntity1) {
                    villagerEntity = villagerEntity1;
                    break;
                }
            }
        }
        if(villagerEntity == null)
            return;

        villagerEntity.ambientSoundTime = 0;
        Player player = Minecraft.getInstance().player;
        if(player != null) {
            QuestData questData = QuestData.get(player.getUUID());

            int DISPLAY_WIDTH = GetTextureSize(DisplayType.BACKGROUND_DISPLAY).x + 20;
            int DISPLAY_HEIGHT = GetTextureSize(DisplayType.BACKGROUND_DISPLAY).y + 10;

            int x = GetUVCoords().x - 15;
            int y = GetUVCoords().y;

            float scaleX = (float) DISPLAY_WIDTH / TEXTURE_WIDTH;
            float scaleY = (float) DISPLAY_HEIGHT / TEXTURE_HEIGHT;

            PoseStack pose = gfx.pose();
            pose.pushPose();
            pose.translate(x, y, 0);
            pose.scale(scaleX, scaleY, 1f);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.8f);

            gfx.blit(
                    backgroundGui.getTexture(),
                    backgroundGui.offsetX, backgroundGui.offsetY,
                    0, 0,
                    backgroundGui.TEXTURE_WIDTH, backgroundGui.TEXTURE_HEIGHT,
                    backgroundGui.TEXTURE_WIDTH, backgroundGui.TEXTURE_HEIGHT
            );
            gfx.blit(
                    headDisplayGui.getTexture(),
                    headDisplayGui.offsetX, headDisplayGui.offsetY,
                    0, 0,
                    headDisplayGui.TEXTURE_WIDTH, headDisplayGui.TEXTURE_HEIGHT,
                    headDisplayGui.TEXTURE_WIDTH, headDisplayGui.TEXTURE_HEIGHT
            );
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            pose.popPose();

// Calculate where the background ACTUALLY is on screen
            int actualBgX = x + (int) (backgroundGui.offsetX * scaleX);
            int actualBgY = y + (int) (backgroundGui.offsetY * scaleY);
            int actualBgW = (int) (backgroundGui.TEXTURE_WIDTH * scaleX);
            int actualBgH = (int) (backgroundGui.TEXTURE_HEIGHT * scaleY);

            int xPadding = (int) (15 * scaleX);
            int yPadding = (int) (15 * scaleY);

// Text position based on ACTUAL background position
            int textX = actualBgX + xPadding;
            int textY = actualBgY + yPadding;
            int textWidth = actualBgW - (xPadding * 2) - (int) (30 * scaleX);

            String fullText = pages.get(currentPageIndex).text;
            long now = System.currentTimeMillis();

            if (lastTypeTime == 0) {
                lastTypeTime = now;
            }

            if (currentSentenceStartDelay < SENTENCE_START_DELAY) {
                currentSentenceStartDelay++;
            } else {
                if (visibleChars < fullText.length()) {
                    if (isPausedAfterPeriod && !skipDialogue) {
                        if (now - pauseStartTime >= PERIOD_PAUSE_MS) {
                            // Pause is over, resume typing
                            isPausedAfterPeriod = false;
                            lastTypeTime = now;
                        }
                    } else if (isPausedAfterComma && !skipDialogue) {
                        if (now - pauseStartTime >= COMMA_PAUSE_MS) {
                            // Pause is over, resume typing
                            isPausedAfterComma = false;
                            lastTypeTime = now;
                        }
                    } else if (isPausedAfterExclamationMark && !skipDialogue) {
                        if (now - pauseStartTime >= EXCLAMATION_PAUSE_MS) {
                            // Pause is over, resume typing
                            isPausedAfterExclamationMark = false;
                            lastTypeTime = now;
                        }
                    } else if (isPausedAfterQuestionMark && !skipDialogue) {
                        if (now - pauseStartTime >= QUESTION_MARK_PAUSE_MS) {
                            // Pause is over, resume typing
                            isPausedAfterQuestionMark = false;
                            lastTypeTime = now;
                        }
                    } else {
                        int typeSpeed = skipDialogue ? TYPE_SPEED_SKIP_MS : TYPE_SPEED_MS;

                        if (now - lastTypeTime >= typeSpeed) {
                            visibleChars++;
                            lastTypeTime = now;

                            // Check if the character we just revealed is a period
                            if (visibleChars > 0 && visibleChars <= fullText.length()) {
                                char lastChar = fullText.charAt(visibleChars - 1);
                                if (!skipDialogue) {
                                    if (lastChar == '.') {
                                        isPausedAfterPeriod = true;
                                        pauseStartTime = now;
                                    }
                                    if (lastChar == ',') {
                                        isPausedAfterComma = true;
                                        pauseStartTime = now;
                                    }
                                    if (lastChar == '!') {
                                        isPausedAfterExclamationMark = true;
                                        pauseStartTime = now;
                                    }
                                    if (lastChar == '?') {
                                        isPausedAfterQuestionMark = true;
                                        pauseStartTime = now;
                                    }
                                }
                            }
                        }
                    }
                } else if (!hasSentenceCompleted) {
                    hasSentenceCompleted = true;
                    skipDialogue = false;
                    addButtons(getMinecraft().player, questData);
                }
                String visibleText = buildSafeVisibleText(pages.get(currentPageIndex).text, textWidth);

                // Create a new pose stack for text scaling
                pose.pushPose();

                // Move to text position
                pose.translate(textX, textY, 0);

                // Scale the text 1.2x
                pose.scale(1.2F, 1.2F, 1.0F);

                // Draw at origin (0, 0) since we already translated
                gfx.drawWordWrap(
                        this.font,
                        Component.literal(visibleText),
                        0,  // Draw at 0,0 because we translated the pose
                        0,
                        (int) (textWidth / 1.2F),  // Adjust width for the scale
                        0xFFFFFF
                );

                pose.popPose();
            }


            if (questVillager != null) {
                int headWidth = 48;
                int headHeight = 60;
                int headYOffset = -65;

                // Calculate the actual screen position of the head display
                // It's rendered at headDisplayGui.offsetX/Y, but transformed by the pose stack
                int actualHeadDisplayX = x + (int) (headDisplayGui.offsetX * scaleX);
                int actualHeadDisplayY = y + (int) (headDisplayGui.offsetY * scaleY);
                int actualHeadDisplayWidth = (int) (headDisplayGui.TEXTURE_WIDTH * scaleX);
                int actualHeadDisplayHeight = (int) (headDisplayGui.TEXTURE_HEIGHT * scaleY);

                int entityX = actualHeadDisplayX + actualHeadDisplayWidth / 2;
                int entityY = actualHeadDisplayY + actualHeadDisplayHeight + 60;
                int scale = 55;

                int clipX = entityX - headWidth / 2;
                int clipY = entityY - headHeight + headYOffset;

                gfx.enableScissor(
                        clipX,
                        clipY,
                        clipX + headWidth,
                        clipY + headHeight
                );

                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        gfx,
                        entityX,
                        entityY,
                        scale,
                        entityX - mouseX,
                        entityY - (mouseY + 40),
                        villagerEntity
                );

                gfx.disableScissor();
            }
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }


    private String buildSafeVisibleText(String fullText, int maxWidth) {
        if (words == null || words.isEmpty()) {
            return fullText.substring(0, Math.min(visibleChars, fullText.length()));
        }

        StringBuilder builder = new StringBuilder();
        int charCount = 0;

        for (String word : words) {
            if (charCount >= visibleChars) {
                break;
            }

            // Calculate how much of this word should be visible
            int wordCharsToShow = Math.min(word.length(), visibleChars - charCount);

            // If we can't show the full word yet, check if showing partial would overflow
            if (wordCharsToShow < word.length()) {

                // First word: always allow partial rendering
                if (builder.length() == 0) {
                    builder.append(word, 0, wordCharsToShow);
                    break;
                }

                String testString = builder + word.substring(0, wordCharsToShow);

                int heightBefore = this.font.wordWrapHeight(Component.literal(builder.toString()), maxWidth);
                int heightAfter = this.font.wordWrapHeight(Component.literal(testString), maxWidth);

                if (heightAfter > heightBefore) {
                    break; // wait until full word fits
                }

                builder.append(word, 0, wordCharsToShow);
                break;
            }

            // Full word is visible, add it
            builder.append(word);
            charCount += word.length();
        }

        return builder.toString();
    }
}
