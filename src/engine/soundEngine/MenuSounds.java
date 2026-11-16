package engine.soundEngine;

/**
 * Sounds played by the menus. Screens call this instead of talking to
 * the SoundManager, so the sound can be changed in one place.
 */
public final class MenuSounds {

    /** Minimum time between two move sounds, in milliseconds. */
    private static final long MIN_INTERVAL_MS = 60;
    /** Minimum time between two volume previews, in milliseconds. */
    private static final long PREVIEW_INTERVAL_MS = 120;
    /** When the move sound last played. */
    private static long lastPlayed;
    /** When the volume preview last played. */
    private static long lastPreview;

    private MenuSounds() {
    }

    /** Plays the short sound heard when the menu cursor moves. */
    public static void playMove() {
        final long now = System.currentTimeMillis();
        if (now - lastPlayed < MIN_INTERVAL_MS)
            return;
        lastPlayed = now;
        SoundManager.getInstance().playSfx(Sound.MENU_MOVE);
    }

    /**
     * Plays a short sample at the current effect volume, so the player
     * hears the new level while adjusting it. Rate limited, because a
     * slider drag changes the volume on almost every frame.
     *
     * @return True if the sample played, false if it was too soon.
     */
    public static boolean playVolumePreview() {
        final long now = System.currentTimeMillis();
        if (now - lastPreview < PREVIEW_INTERVAL_MS)
            return false;
        lastPreview = now;
        SoundManager.getInstance().playSfx(Sound.MENU_MOVE);
        return true;
    }
}
