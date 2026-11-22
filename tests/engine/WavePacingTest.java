package engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WavePacingTest {
    @Test
    void earlyFormationsUseShorterMovementIntervals() {
        assertEquals(45, Core.settingsForLevel(1).getBaseSpeed());
        assertEquals(40, Core.settingsForLevel(2).getBaseSpeed());
    }

    @Test
    void lateFormationsHaveSixColumnsAndKeepTheirRowProgression() {
        GameSettings eighth = Core.settingsForLevel(8);
        GameSettings ninth = Core.settingsForLevel(9);
        assertEquals(6, eighth.getFormationWidth());
        assertEquals(4, eighth.getFormationHeight());
        assertEquals(6, ninth.getFormationWidth());
        assertEquals(5, ninth.getFormationHeight());
        assertEquals(6, ninth.getTwoHitEnemyCount());
    }
}
