package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Checks the fade a screen plays as it opens. */
class ScreenTransitionTest {

	/** Tolerance for comparing alpha values. */
	private static final double EPSILON = 1e-9;

	/** A realistic wall-clock timestamp, the kind the game actually passes. */
	private static final long NOW = 1_795_000_000_000L;

	@Test
	void startsFullyBlackAndEndsClear() {
		assertEquals(1, ScreenTransition.alphaFor(0), EPSILON);
		assertEquals(0,
				ScreenTransition.alphaFor(ScreenTransition.DURATION_MILLIS),
				EPSILON);
	}

	@Test
	void fadesWithoutEverGoingBackwards() {
		double previous = 2;
		for (long elapsed = 0; elapsed <= ScreenTransition.DURATION_MILLIS;
				elapsed += 10) {
			double alpha = ScreenTransition.alphaFor(elapsed);
			assertTrue(alpha <= previous, "alpha rose at " + elapsed);
			assertTrue(alpha >= 0 && alpha <= 1, "alpha left 0-1 at " + elapsed);
			previous = alpha;
		}
	}

	@Test
	void staysClearAfterTheFadeAndBeforeItStarts() {
		assertEquals(0, ScreenTransition.alphaFor(-1), EPSILON);
		assertEquals(0, ScreenTransition.alphaFor(10_000), EPSILON);
	}

	@Test
	void worksFromARealTimestamp() {
		ScreenTransition.start(NOW);
		assertEquals(1, ScreenTransition.overlayAlpha(NOW), EPSILON);
		assertTrue(ScreenTransition.overlayAlpha(NOW + 150) > 0,
				"mid-fade should still be dark");
		assertTrue(ScreenTransition.overlayAlpha(NOW + 150) < 1,
				"mid-fade should not be fully black");
		assertEquals(0, ScreenTransition.overlayAlpha(
				NOW + ScreenTransition.DURATION_MILLIS), EPSILON);
	}

	@Test
	void aNewScreenRestartsTheFade() {
		ScreenTransition.start(NOW);
		assertEquals(0, ScreenTransition.overlayAlpha(NOW + 500), EPSILON);
		ScreenTransition.start(NOW + 500);
		assertEquals(1, ScreenTransition.overlayAlpha(NOW + 500), EPSILON);
	}
}
