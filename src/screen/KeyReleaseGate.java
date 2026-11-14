package screen;

/**
 * Ignores a key that is still held from before the gate was closed, so a
 * key held for one purpose (firing) is not read as a new press somewhere
 * else (confirming a menu option). The key counts again once released.
 */
final class KeyReleaseGate {
	/** True until the key has been seen released after close(). */
	private boolean waitingForRelease;

	/** Ignores the key until it has been released at least once. */
	void close() {
		this.waitingForRelease = true;
	}

	/**
	 * Call once per frame with the key state.
	 *
	 * @param keyDown
	 *            Whether the key is down this frame.
	 * @return True when the key is down and was pressed after close().
	 */
	boolean accept(final boolean keyDown) {
		if (!keyDown) {
			this.waitingForRelease = false;
			return false;
		}
		return !this.waitingForRelease;
	}
}
