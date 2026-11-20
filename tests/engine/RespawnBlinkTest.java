package engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Checks the on/off rhythm of the player ship after a hit. */
class RespawnBlinkTest {

	@Test
	void isShownRightAfterTheHit() {
		assertTrue(RespawnBlink.isVisible(0));
		assertTrue(RespawnBlink.isVisible(RespawnBlink.INTERVAL_MILLIS - 1));
	}

	@Test
	void isHiddenDuringTheSecondInterval() {
		assertFalse(RespawnBlink.isVisible(RespawnBlink.INTERVAL_MILLIS));
		assertFalse(RespawnBlink.isVisible(2 * RespawnBlink.INTERVAL_MILLIS - 1));
	}

	@Test
	void isShownAgainDuringTheThirdInterval() {
		assertTrue(RespawnBlink.isVisible(2 * RespawnBlink.INTERVAL_MILLIS));
	}
}
