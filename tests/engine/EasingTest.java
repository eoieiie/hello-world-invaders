package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Checks the easing curves named in Screen Rules rule 7. */
class EasingTest {

	/** Tolerance for comparing curve values. */
	private static final double EPSILON = 1e-9;

	@Test
	void everyCurveStartsAtZeroAndEndsAtOne() {
		assertEquals(0, Easing.linear(0), EPSILON);
		assertEquals(1, Easing.linear(1), EPSILON);
		assertEquals(0, Easing.easeOut(0), EPSILON);
		assertEquals(1, Easing.easeOut(1), EPSILON);
		assertEquals(0, Easing.easeInOut(0), EPSILON);
		assertEquals(1, Easing.easeInOut(1), EPSILON);
	}

	@Test
	void inputIsClamped() {
		assertEquals(0, Easing.easeInOut(-1), EPSILON);
		assertEquals(1, Easing.easeInOut(2), EPSILON);
		assertEquals(0, Easing.easeOut(-0.5), EPSILON);
		assertEquals(1, Easing.linear(1.5), EPSILON);
	}

	@Test
	void everyCurveRisesWithoutGoingBackwards() {
		double previousOut = -1;
		double previousInOut = -1;
		for (int step = 0; step <= 100; step++) {
			double t = step / 100.0;
			double out = Easing.easeOut(t);
			double inOut = Easing.easeInOut(t);
			assertTrue(out >= previousOut, "easeOut dipped at " + t);
			assertTrue(inOut >= previousInOut, "easeInOut dipped at " + t);
			assertTrue(out >= 0 && out <= 1, "easeOut left 0-1 at " + t);
			assertTrue(inOut >= 0 && inOut <= 1, "easeInOut left 0-1 at " + t);
			previousOut = out;
			previousInOut = inOut;
		}
	}

	@Test
	void easeOutMatchesTheCurveWarpInAlreadyUses() {
		for (int step = 0; step <= 100; step++) {
			double t = step / 100.0;
			double u = 1 - t;
			assertEquals(1 - u * u * u, Easing.easeOut(t), EPSILON,
					"differs from WarpIn at " + t);
		}
	}

	@Test
	void easeInOutIsSymmetricAboutItsMiddle() {
		assertEquals(0.5, Easing.easeInOut(0.5), EPSILON);
		for (int step = 0; step <= 50; step++) {
			double t = step / 100.0;
			assertEquals(1 - Easing.easeInOut(t), Easing.easeInOut(1 - t),
					EPSILON, "not symmetric at " + t);
		}
	}
}
