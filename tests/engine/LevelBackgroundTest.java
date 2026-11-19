package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevelBackgroundTest {

    @Test
    void everyLevelHasItsOwnTheme() {
        Set<LevelBackground.Theme> seen = new HashSet<>();
        for (int level = 1; level <= 10; level++) {
            seen.add(LevelBackground.themeFor(level));
        }
        assertEquals(10, seen.size());
    }

    @ParameterizedTest
    @CsvSource({"1, NEBULA", "5, MID_BOSS", "10, FINAL_BOSS"})
    void firstAndBossLevels(int level, LevelBackground.Theme expected) {
        assertEquals(expected, LevelBackground.themeFor(level));
    }

    @ParameterizedTest
    @CsvSource({"0", "-1", "11"})
    void levelsOutsideTheGameUseTheFirstTheme(int level) {
        assertEquals(LevelBackground.Theme.NEBULA, LevelBackground.themeFor(level));
    }

    @Test
    void backgroundIsNotBlack() {
        BufferedImage image = new BufferedImage(100, 120, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        for (int level = 1; level <= 10; level++) {
            LevelBackground.draw(g, 100, 120, level, 0);
            assertNotEquals(0, image.getRGB(50, 10) & 0xFFFFFF, "level " + level);
        }
        g.dispose();
    }

    @Test
    void starsDriftButTheScreenStaysCovered() {
        BufferedImage a = new BufferedImage(100, 120, BufferedImage.TYPE_INT_RGB);
        BufferedImage b = new BufferedImage(100, 120, BufferedImage.TYPE_INT_RGB);
        LevelBackground.draw(a.createGraphics(), 100, 120, 1, 0);
        LevelBackground.draw(b.createGraphics(), 100, 120, 1, 5000);
        int changed = 0;
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 120; y++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    changed++;
                }
                assertNotEquals(0, b.getRGB(x, y) & 0xFFFFFF);
            }
        }
        assertNotEquals(0, changed);
    }
}
