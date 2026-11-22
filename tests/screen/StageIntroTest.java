package screen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StageIntroTest {
    @Test
    void stageLabelRemainsAfterWarpAndGameplayStartsAt1200Millis() {
        assertTrue(GameScreen.stageIntroActive(0));
        assertTrue(GameScreen.stageIntroActive(600));
        assertTrue(GameScreen.stageIntroActive(1199));
        assertFalse(GameScreen.stageIntroActive(1200));
    }
}
