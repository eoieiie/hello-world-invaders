package engine;

import java.awt.Color;

/**
 * Short flash on a ship or boss that was hit but not destroyed, so the
 * player can see the shot landed. The current time is passed in, so the
 * timing can be unit tested.
 */
public final class HitFlash {

	/** How long one flash lasts, in milliseconds. */
	public static final long DURATION_MILLIS = 100;
	/** Colour the sprite is drawn in while it flashes. */
	public static final Color COLOR = Color.WHITE;

	/** How long this flash lasts, in milliseconds. */
	private final long duration;
	/** Time the last flash started, in milliseconds. */
	private long startedAt;
	/** Whether the flash was ever started. */
	private boolean started;

	/** Creates a flash with the default duration. */
	public HitFlash() {
		this(DURATION_MILLIS);
	}

	/**
	 * @param duration
	 *            How long one flash lasts, in milliseconds.
	 */
	public HitFlash(final long duration) {
		if (duration <= 0)
			throw new IllegalArgumentException("Flash duration must be positive");
		this.duration = duration;
	}

	/**
	 * Starts the flash, or restarts it if it is still on.
	 *
	 * @param now
	 *            Current time, in milliseconds.
	 */
	public void trigger(final long now) {
		this.startedAt = now;
		this.started = true;
	}

	/**
	 * @param now
	 *            Current time, in milliseconds.
	 * @return Whether the flash is on at the given time.
	 */
	public boolean isActive(final long now) {
		return this.started && now >= this.startedAt
				&& now - this.startedAt < this.duration;
	}
}
