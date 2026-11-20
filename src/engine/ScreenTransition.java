package engine;

/**
 * Timing for the fade a screen plays as it opens. Rule 7's Normal step, eased
 * in and out, changing alpha only.
 */
public final class ScreenTransition {

	/** How long the fade lasts, in milliseconds. Rule 7's Normal step. */
	public static final long DURATION_MILLIS = 300;

	/** When the current fade started, in milliseconds. */
	private static long startedAt;
	/** Whether a fade was ever started. */
	private static boolean started;

	private ScreenTransition() {
	}

	/**
	 * Starts a fade. Called when a screen opens.
	 *
	 * @param now
	 *            Current time, in milliseconds.
	 */
	public static void start(final long now) {
		startedAt = now;
		started = true;
	}

	/**
	 * How opaque the black overlay should be: 1 when the fade starts, 0 once it
	 * has finished. The elapsed time is kept in a long and only divided as a
	 * double, so a real wall-clock timestamp cannot overflow it.
	 *
	 * @param now
	 *            Current time, in milliseconds.
	 * @return Overlay alpha, 0 to 1.
	 */
	public static double overlayAlpha(final long now) {
		return started ? alphaFor(now - startedAt) : 0;
	}

	/**
	 * The fade curve on its own, with no clock behind it, so it can be
	 * checked directly.
	 *
	 * @param elapsed
	 *            Milliseconds since the fade started.
	 * @return Overlay alpha, 0 to 1.
	 */
	static double alphaFor(final long elapsed) {
		if (elapsed < 0 || elapsed >= DURATION_MILLIS) {
			return 0;
		}
		return 1 - Easing.easeInOut((double) elapsed / DURATION_MILLIS);
	}
}
