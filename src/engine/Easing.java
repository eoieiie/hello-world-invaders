package engine;

/**
 * Easing curves for screen motion, as set out in Screen Rules rule 7. Every
 * method takes progress from 0 to 1 and returns a value from 0 to 1, clamping
 * its input, so callers can pass a raw elapsed/duration ratio.
 */
public final class Easing {

	private Easing() {
	}

	/**
	 * @param t
	 *            Progress, 0 to 1.
	 * @return The same progress, clamped. For continuous motion.
	 */
	public static double linear(final double t) {
		return clamp(t);
	}

	/**
	 * Cubic ease-out: quick to start, settling at the end. This is the same
	 * curve WarpIn already uses privately, so WarpIn can move onto this class
	 * later without looking any different.
	 *
	 * @param t
	 *            Progress, 0 to 1.
	 * @return Eased progress, 0 to 1.
	 */
	public static double easeOut(final double t) {
		double u = 1 - clamp(t);
		return 1 - u * u * u;
	}

	/**
	 * Cubic ease-in-out: gentle at both ends. For motion travelling between two
	 * resting states, such as a screen fade.
	 *
	 * @param t
	 *            Progress, 0 to 1.
	 * @return Eased progress, 0 to 1.
	 */
	public static double easeInOut(final double t) {
		double p = clamp(t);
		if (p < 0.5) {
			return 4 * p * p * p;
		}
		double u = -2 * p + 2;
		return 1 - u * u * u / 2;
	}

	private static double clamp(final double t) {
		return Math.max(0, Math.min(1, t));
	}
}
