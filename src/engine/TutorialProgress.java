package engine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Remembers whether the player has already been through the tutorial.
 * The save is an empty marker file: present means the tutorial was finished
 * or skipped once, so later runs go straight to Stage 1.
 */
public final class TutorialProgress {

	/** Application logger. */
	private static final Logger LOGGER = Core.getLogger();

	/** Marker file that exists once the tutorial has been seen. */
	private final File markerFile;

	/**
	 * @param markerFile File whose presence marks the tutorial as seen.
	 */
	public TutorialProgress(final File markerFile) {
		this.markerFile = markerFile;
	}

	/** @return True when the tutorial has been finished or skipped before. */
	public boolean isSeen() {
		try {
			return this.markerFile.exists();
		} catch (SecurityException e) {
			LOGGER.warning("Couldn't read tutorial progress: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Records that the tutorial has been seen. A failed save only means the
	 * tutorial shows again next time, so it never interrupts the game.
	 */
	public void markSeen() {
		try {
			if (!this.markerFile.exists() && !this.markerFile.createNewFile())
				LOGGER.warning("Couldn't save tutorial progress.");
		} catch (IOException | SecurityException e) {
			LOGGER.warning("Couldn't save tutorial progress: " + e.getMessage());
		}
	}
}
