package engine;

/**
 * Blinking of the player ship while it recovers from a hit that did not
 * take its last life. The ship is shown and hidden in turns, so it is clear
 * the ship is out of action for a moment but will come back.
 */
public final class RespawnBlink {

	/** How long the ship stays shown, then hidden, in milliseconds. */
	public static final long INTERVAL_MILLIS = 100;

	/** Only static helpers. */
	private RespawnBlink() {
	}

	/**
	 * @param elapsed
	 *            Time since the hit, in milliseconds.
	 * @return Whether the ship is drawn at this moment.
	 */
	public static boolean isVisible(final long elapsed) {
		if (elapsed < 0)
			return true;
		return (elapsed / INTERVAL_MILLIS) % 2 == 0;
	}
}
