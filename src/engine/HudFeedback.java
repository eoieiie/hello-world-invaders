package engine;

/**
 * Tracks short HUD responses to score gains and lost lives. The displayed
 * score and life count always come from the game state, not from this timer.
 */
public final class HudFeedback {

	/** Duration of a score counter response, in milliseconds. */
	private static final int SCORE_DURATION = 300;
	/** Duration of a lost-life impact response, in milliseconds. */
	private static final int LIFE_DURATION = 100;
	/** Score observed on the preceding frame. */
	private int previousScore;
	/** Life count observed on the preceding frame. */
	private int previousLives;
	/** Start of the current score response, or -1 when inactive. */
	private long scoreStartedAt = -1;
	/** Start of the current lost-life response, or -1 when inactive. */
	private long lifeStartedAt = -1;
	/** Index of the first icon removed by the latest life loss. */
	private int lostLifeIndex = -1;

	/**
	 * Starts a level with its actual values and clears any earlier response.
	 *
	 * @param score Score at the start of the level.
	 * @param lives Lives at the start of the level.
	 */
	public void reset(final int score, final int lives) {
		this.previousScore = score;
		this.previousLives = lives;
		this.scoreStartedAt = -1;
		this.lifeStartedAt = -1;
		this.lostLifeIndex = -1;
	}

	/**
	 * Observes every score and life change made during this game frame.
	 * Score losses and life gains cancel the corresponding old response.
	 *
	 * @param score Current score.
	 * @param lives Current life count.
	 * @param now Current game clock time, in milliseconds.
	 */
	public void observe(final int score, final int lives, final long now) {
		if (score > this.previousScore)
			this.scoreStartedAt = now;
		else if (score < this.previousScore)
			this.scoreStartedAt = -1;
		if (lives < this.previousLives) {
			this.lifeStartedAt = now;
			this.lostLifeIndex = Math.max(0, lives);
		} else if (lives > this.previousLives) {
			this.lifeStartedAt = -1;
			this.lostLifeIndex = -1;
		}
		this.previousScore = score;
		this.previousLives = lives;
	}

	/**
	 * Shifts response start times so a pause consumes none of their duration.
	 *
	 * @param pausedMillis Time spent paused, in milliseconds.
	 */
	public void resumeAfterPause(final long pausedMillis) {
		if (this.scoreStartedAt >= 0)
			this.scoreStartedAt += pausedMillis;
		if (this.lifeStartedAt >= 0)
			this.lifeStartedAt += pausedMillis;
	}

	/**
	 * @param now Current game clock time, in milliseconds.
	 * @return Score glow strength from 1 to 0.
	 */
	public float scoreGlow(final long now) {
		return remaining(now, this.scoreStartedAt, SCORE_DURATION);
	}

	/**
	 * @param now Current game clock time, in milliseconds.
	 * @return Lost-life outline opacity from 1 to 0.
	 */
	public float lostLifeAlpha(final long now) {
		return remaining(now, this.lifeStartedAt, LIFE_DURATION);
	}

	/**
	 * @param now Current game clock time, in milliseconds.
	 * @return Removed icon index while its outline is visible, otherwise -1.
	 */
	public int lostLifeIndex(final long now) {
		return lostLifeAlpha(now) > 0 ? this.lostLifeIndex : -1;
	}

	/**
	 * @param now Current game clock time, in milliseconds.
	 * @param start Response start, or -1 when inactive.
	 * @param duration Response duration, in milliseconds.
	 * @return Remaining eased opacity from 1 to 0.
	 */
	private static float remaining(final long now, final long start,
			final int duration) {
		if (start < 0)
			return 0;
		double progress = (double) (now - start) / duration;
		return (float) (1 - Easing.easeOut(progress));
	}
}
