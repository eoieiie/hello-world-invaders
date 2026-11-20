package engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Checks when a hit flash is on and off. */
class HitFlashTest {

	@Test
	void isOffBeforeAnyHit() {
		HitFlash flash = new HitFlash(100);
		assertFalse(flash.isActive(0));
	}

	@Test
	void isOnDuringItsDuration() {
		HitFlash flash = new HitFlash(100);
		flash.trigger(1000);
		assertTrue(flash.isActive(1000));
		assertTrue(flash.isActive(1099));
	}

	@Test
	void isOffOnceItsDurationHasPassed() {
		HitFlash flash = new HitFlash(100);
		flash.trigger(1000);
		assertFalse(flash.isActive(1100));
	}

	@Test
	void restartsWhenHitAgain() {
		HitFlash flash = new HitFlash(100);
		flash.trigger(1000);
		flash.trigger(1080);
		assertTrue(flash.isActive(1150));
		assertFalse(flash.isActive(1180));
	}

	@Test
	void rejectsNonPositiveDuration() {
		assertThrows(IllegalArgumentException.class, () -> new HitFlash(0));
	}
}
