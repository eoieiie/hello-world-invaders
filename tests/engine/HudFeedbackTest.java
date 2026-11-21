package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the timing and state changes of the HUD responses. */
public class HudFeedbackTest {

	@Test
	void scoreGlowEasesOutAndIgnoresScoreLoss() {
		HudFeedback feedback = new HudFeedback();
		feedback.reset(10, 3);
		feedback.observe(20, 3, 1000);
		assertEquals(1f, feedback.scoreGlow(1000));
		assertEquals(0.125f, feedback.scoreGlow(1150), 0.0001f);
		assertEquals(0f, feedback.scoreGlow(1300));
		feedback.observe(5, 3, 1400);
		assertEquals(0f, feedback.scoreGlow(1400));
	}

	@Test
	void lostLifeOutlineUsesRemovedSlotIncludingLastLife() {
		HudFeedback feedback = new HudFeedback();
		feedback.reset(0, 2);
		feedback.observe(0, 1, 2000);
		assertEquals(1, feedback.lostLifeIndex(2000));
		assertEquals(0.125f, feedback.lostLifeAlpha(2050), 0.0001f);
		feedback.observe(0, 0, 2060);
		assertEquals(0, feedback.lostLifeIndex(2060));
		assertEquals(-1, feedback.lostLifeIndex(2160));
		feedback.observe(0, 1, 2200);
		assertEquals(-1, feedback.lostLifeIndex(2200));
	}

	@Test
	void pauseAndResetDoNotConsumeOrCarryOverEffects() {
		HudFeedback feedback = new HudFeedback();
		feedback.reset(0, 2);
		feedback.observe(5, 1, 1000);
		float scoreBeforePause = feedback.scoreGlow(1050);
		float lifeBeforePause = feedback.lostLifeAlpha(1050);
		feedback.resumeAfterPause(2000);
		assertEquals(scoreBeforePause, feedback.scoreGlow(3050));
		assertEquals(lifeBeforePause, feedback.lostLifeAlpha(3050));
		assertTrue(feedback.scoreGlow(3100) < scoreBeforePause);
		feedback.reset(5, 1);
		assertEquals(0f, feedback.scoreGlow(3100));
		assertEquals(-1, feedback.lostLifeIndex(3100));
	}
}
