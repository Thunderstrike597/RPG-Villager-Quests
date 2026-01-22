package net.kenji.rpg_villager_quests.client.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.manager.QuestMenuManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

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

    public static ResourceLocation MENU_BACKGROUND = new ResourceLocation(RpgVillagerQuests.MODID, "textures/gui/villager_quest_menu.png");
    public static ResourceLocation HEAD_DISPLAY = new ResourceLocation(RpgVillagerQuests.MODID, "textures/gui/villager_quest_head_display.png");
    public static ResourceLocation HEAD_DISPLAY_OVERLAY = new ResourceLocation(RpgVillagerQuests.MODID, "textures/gui/villager_quest_head_display_overlay.png");

    public static GuiDisplay backgroundGui;
    public static GuiDisplay headDisplayGui;
    private static final String PAGE_DELIMITER = "/--/";

    public static boolean didAcceptQuest;
    private int currentPage = 0;
    private List<QuestStage.Page> pages;
    private int visibleChars = 0;
    private long lastTypeTime = 0;
    private List<String> words;
    private int currentSentenceStartDelay = 0;
    private static final int TYPE_SPEED_MS = 35;
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

    private boolean pendingInit = false;
    private enum DisplayType{
        BACKGROUND_DISPLAY,
        HEAD_DISPLAY
    }

    private Villager villager;

    private static List<String> getPages(String raw) {
        return Arrays.stream(raw.split(Pattern.quote(PAGE_DELIMITER)))
                .map(String::trim)
                .toList();
    }
    private QuestStage.Page getCurrentPage(){
      return pages.get(currentPage);
    }

    private String getFinalPage(){
        return pages.get(pages.size() - 1).text;
    }

    public VillagerQuestMenu(Component pTitle, Villager questVillager) {
        super(pTitle);
        villager = questVillager;
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
        if(QuestMenuManager.getVillagerQuest(villager) == null)
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

        pages = QuestMenuManager.getVillagerQuest(villager).getCurrentStage().pages;
        words = Arrays.asList(pages.get(currentPage).text.split("(?<=\\s)"));
        currentSentenceStartDelay = 0;

        int pX = bgX + padding;
        int pY = bgY + bgH - buttonHeight - padding;

        if(getCurrentPage().button2Text == null || Objects.equals(getCurrentPage().button2Text, "")) {
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
        }
        else{
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

    public void addPositiveButton(Player player){
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
        int pX = bgX + padding;
        int pY = bgY + bgH - buttonHeight - padding;
        if (currentPage == pages.size() - 1 && !QuestMenuManager.getVillagerQuest(villager).isQuestActive(player)) {
            if(getCurrentPage().button1Text == null || Objects.equals(getCurrentPage().button1Text, "")) {

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
            }
            else{
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
        }
        else{
            if(hasSentenceCompleted) {
                if(getCurrentPage().button1Text == null || Objects.equals(getCurrentPage().button1Text, "")) {
                    posButton = this.addRenderableWidget(
                            Button.builder(
                                    Component.literal("Next"),
                                    btn -> onNextPage(player)
                            ).bounds(
                                    pX + xOffset,
                                    pY + posYOffset,
                                    buttonWidth,
                                    buttonHeight
                            ).build()
                    );
                }
                else{
                    posButton = this.addRenderableWidget(
                            Button.builder(
                                    Component.literal(getCurrentPage().button1Text),
                                    btn -> onNextPage(player)
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

    public void onAcceptQuest(Player player){
        player.playSound(SoundEvents.VILLAGER_YES);
        didAcceptQuest = true;
        hasSentenceCompleted = false;
        Quest quest = QuestMenuManager.getVillagerQuest(villager);
        quest.advanceFromCurrentStage();
        quest.StartQuest(player);
        onClose();
    }
    public void onNextPage(Player player) {
        if (currentPage < pages.size() - 1) {
            currentPage++;
            visibleChars = 0;
            lastTypeTime = 0;
            isPausedAfterPeriod = false;
            pauseStartTime = 0;
            words = Arrays.asList(pages.get(currentPage).text.split("(?<=\\s)"));
            currentSentenceStartDelay = 0;
            hasSentenceCompleted = false;
            player.playSound(SoundEvents.VILLAGER_TRADE);

            this.clearWidgets();
            this.init();
        }
    }

    @Override
    public void onClose() {
        Player player = Minecraft.getInstance().player;
        currentPage = 0;
        visibleChars = 0;
        lastTypeTime = 0;
        isPausedAfterPeriod = false;
        pauseStartTime = 0;
        if(villager != null) {
            villager.setTradingPlayer(null);
            villager = null;
        }
        if(player != null && !didAcceptQuest)
            player.playSound(SoundEvents.VILLAGER_NO);

        didAcceptQuest = false;
        hasSentenceCompleted = false;
        currentSentenceStartDelay = 0;
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
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        if(QuestMenuManager.getVillagerQuest(villager) == null)
            return;
        this.renderBackground(gfx);
        villager.ambientSoundTime = 0;

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

        int bgX = getX(backgroundGui);
        int bgY = getY(backgroundGui);
        int bgW = getBgWidth(backgroundGui);
        int bgH = getBgHeight(backgroundGui);

        int xPadding = 8;
        int yPadding = 15;
        int textX = bgX + xPadding;
        int textY = yPadding;
        int textWidth = (bgW - xPadding * 2) - 30;

        String fullText = pages.get(currentPage).text;
        long now = System.currentTimeMillis();

        if (lastTypeTime == 0) {
            lastTypeTime = now;
        }

        if(currentSentenceStartDelay < SENTENCE_START_DELAY){
            currentSentenceStartDelay++;
        }
        else {
            if (visibleChars < fullText.length()) {
                // Check if we're in a pause after a period
                if (isPausedAfterPeriod) {
                    if (now - pauseStartTime >= PERIOD_PAUSE_MS) {
                        // Pause is over, resume typing
                        isPausedAfterPeriod = false;
                        lastTypeTime = now;
                    }
                } else if (isPausedAfterComma) {
                    if (now - pauseStartTime >= COMMA_PAUSE_MS) {
                        // Pause is over, resume typing
                        isPausedAfterComma = false;
                        lastTypeTime = now;
                    }
                }
                    else if (isPausedAfterExclamationMark) {
                        if (now - pauseStartTime >= EXCLAMATION_PAUSE_MS) {
                            // Pause is over, resume typing
                            isPausedAfterExclamationMark = false;
                            lastTypeTime = now;
                        }
                    }
                    else if (isPausedAfterQuestionMark) {
                    if (now - pauseStartTime >= QUESTION_MARK_PAUSE_MS) {
                        // Pause is over, resume typing
                        isPausedAfterQuestionMark = false;
                        lastTypeTime = now;
                    }
                } else {
                    // Normal typing
                    if (now - lastTypeTime >= TYPE_SPEED_MS) {
                        visibleChars++;
                        lastTypeTime = now;

                        // Check if the character we just revealed is a period
                        if (visibleChars > 0 && visibleChars <= fullText.length()) {
                            char lastChar = fullText.charAt(visibleChars - 1);
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
            }else if (!hasSentenceCompleted) {
                hasSentenceCompleted = true;
                addPositiveButton(getMinecraft().player);
            }


            String visibleText = buildSafeVisibleText(pages.get(currentPage).text, textWidth);
            pose.scale(scaleX / 1.2F, scaleY / 1.2F, 1f);
            gfx.drawWordWrap(
                    this.font,
                    Component.literal(visibleText),
                    textX,
                    textY,
                    textWidth,
                    0xFFFFFF
            );
        }
        pose.popPose();

        if (villager != null) {
            int headWidth  = 48;
            int headHeight = 60;
            int headYOffset = -65;

            // Calculate the actual screen position of the head display
            // It's rendered at headDisplayGui.offsetX/Y, but transformed by the pose stack
            int actualHeadDisplayX = x + (int)(headDisplayGui.offsetX * scaleX);
            int actualHeadDisplayY = y + (int)(headDisplayGui.offsetY * scaleY);
            int actualHeadDisplayWidth = (int)(headDisplayGui.TEXTURE_WIDTH * scaleX);
            int actualHeadDisplayHeight = (int)(headDisplayGui.TEXTURE_HEIGHT * scaleY);

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
                    villager
            );

            gfx.disableScissor();
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
