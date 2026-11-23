package screen;

import java.awt.event.KeyEvent;
import engine.Core;
import engine.Cooldown;
import engine.soundEngine.Sound;
import engine.soundEngine.SoundManager;

/**
 * Lets the player choose normal play or a boss challenge after selecting Play.
 */
public final class ModeSelectScreen extends Screen {

    private int bossLevel;

    private static final String[] OPTIONS = {
        "Normal Play",
        "Boss Challenge : stage 5",
        "Boss Challenge : stage 10"
    };

    public boolean isPracticeMode() {
        return bossLevel != 0;
    }

    public int getBossLevel() {
        return bossLevel;
    }

    /** Selected option: 0 = normal play, 1 = stage 5 boss, 2 = stage 10 boss. */
    private int selectedIndex;

    private final Cooldown selectionCooldown = Core.getCooldown(200);

    public ModeSelectScreen(
            final int width,
            final int height,
            final int fps) {
        super(width, height, fps);
    }

    @Override
    public void initialize() {
        super.initialize();
        SoundManager.getInstance().playBgm(Sound.BGM_TITLE);

        bossLevel = 0;
        selectedIndex = 0;
        returnCode = 1;

        inputDelay.reset();
        selectionCooldown.reset();
    }

    @Override
    public int run() {
        super.run();
        return returnCode;
    }

    @Override
    protected void update() {
        super.update();

        if (inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
                finish(1);

            } else if (selectionCooldown.checkFinished()) {
                if (isUpDown()) {
                    selectedIndex = Math.floorMod(
                            selectedIndex - 1, OPTIONS.length);
                    selectionCooldown.reset();

                } else if (isDownDown()) {
                    selectedIndex = (selectedIndex + 1) % OPTIONS.length;
                    selectionCooldown.reset();

                } else if (isConfirmDown()) {
                    bossLevel = selectedIndex == 1 ? 5
                            : selectedIndex == 2 ? 10 : 0;

                    finish(2);
                }
            }
        }

        draw();
    }

    private boolean isUpDown() {
        return inputManager.isKeyDown(KeyEvent.VK_UP)
                || inputManager.isKeyDown(KeyEvent.VK_W);
    }

    private boolean isDownDown() {
        return inputManager.isKeyDown(KeyEvent.VK_DOWN)
                || inputManager.isKeyDown(KeyEvent.VK_S);
    }

    private boolean isConfirmDown() {
        return inputManager.isKeyDown(KeyEvent.VK_ENTER)
                || inputManager.isKeyDown(KeyEvent.VK_SPACE);
    }

    /**
     * Closes the screen and sets the return code passed to Core.
     */
    private void finish(final int nextScreenCode) {
        returnCode = nextScreenCode;
        isRunning = false;
    }

    private void draw() {
        drawManager.initDrawing(this);

        drawManager.drawScreenTitle(this, "Select mode");

        int firstRowY = height / 2;
        int rowSpacing = 50;

        for (int i = 0; i < OPTIONS.length; i++) {
            drawManager.drawMenuRow(
                    this,
                    OPTIONS[i],
                    firstRowY + i * rowSpacing,
                    selectedIndex == i);
        }

        drawManager.drawKeyHints(
                this,
                "w+s/arrows move, enter/space select, esc back");

        drawManager.completeDrawing(this);
    }
}
