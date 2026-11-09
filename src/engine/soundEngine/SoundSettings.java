package engine.soundEngine;

/** Validated, immutable volumes persisted independently of player progress. */
public final class SoundSettings {

	/** Default background music volume. */
	public static final float DEFAULT_BGM_VOLUME = 0.5f;
	/** Default sound effect volume. */
	public static final float DEFAULT_SFX_VOLUME = 0.7f;
	private final float bgmVolume;
	private final float sfxVolume;

	/**
	 * Creates settings, recovering each invalid saved value independently.
	 *
	 * @param bgm Background music volume, between 0 and 1.
	 * @param sfx Sound effect volume, between 0 and 1.
	 */
	public SoundSettings(final float bgm, final float sfx) {
		bgmVolume = validated(bgm, DEFAULT_BGM_VOLUME);
		sfxVolume = validated(sfx, DEFAULT_SFX_VOLUME);
	}

	private static float validated(final float volume, final float fallback) {
		return Float.isNaN(volume) || volume < 0.0f || volume > 1.0f
				? fallback : volume;
	}

	/** @return Background music volume between 0 and 1. */
	public float getBgmVolume() {
		return bgmVolume;
	}

	/** @return Sound effect volume between 0 and 1. */
	public float getSfxVolume() {
		return sfxVolume;
	}
}
