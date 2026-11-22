package engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Positions for the in-game top bar (HW-118): one row with the life icons on
 * the left, the level in the middle, and the coins, diamonds and score on the
 * right. Pure arithmetic, so the layout can be tested without drawing.
 */
public final class HudLayout {

	/** Baseline shared by every piece of text in the top bar. */
	public static final int TEXT_BASELINE = 25;
	/** Top edge of the life icons, centred on the text row. */
	public static final int LIFE_ICON_Y = 12;
	/** Left edge of the first life icon. */
	public static final int LIFE_ICON_LEFT = 10;
	/** Horizontal distance from one life icon to the next. */
	public static final int LIFE_ICON_SPACING = 35;
	/** Gap between the score digits and the right edge of the screen. */
	public static final int RIGHT_MARGIN = 8;
	/** Gap between the score, the diamonds and the coins. */
	public static final int GROUP_GAP = 10;
	/** Gap between a currency icon and its amount. */
	public static final int ICON_GAP = 3;

	private HudLayout() {
	}

	/**
	 * @param index Zero-based life icon index.
	 * @return Left edge of that life icon.
	 */
	public static int lifeIconX(final int index) {
		return LIFE_ICON_LEFT + LIFE_ICON_SPACING * index;
	}

	/**
	 * Lays out the right side of the top bar, from the right edge inwards:
	 * score digits, then diamonds, then coins. The score keeps its right
	 * edge fixed as it grows, and the currencies move left to make room.
	 *
	 * @param screenWidth Width of the screen.
	 * @param scoreWidth Width of the score digits.
	 * @param diamondWidth Width of the diamond icon, gap and amount.
	 * @param coinWidth Width of the coin icon, gap and amount.
	 * @return Left edges of the score, diamonds and coins.
	 */
	public static RightGroup rightGroup(final int screenWidth,
			final int scoreWidth, final int diamondWidth,
			final int coinWidth) {
		int scoreX = screenWidth - RIGHT_MARGIN - scoreWidth;
		int diamondX = scoreX - GROUP_GAP - diamondWidth;
		int coinX = diamondX - GROUP_GAP - coinWidth;
		return new RightGroup(scoreX, diamondX, coinX);
	}

	/**
	 * Longest score the top bar is laid out for. Eight digits would push the
	 * coins into "LEVEL 10"; seven already takes hours of farming bonus
	 * ships in one run.
	 */
	public static final int MAX_SCORE_DIGITS = 7;

	/** Amounts from here up are shortened with K, M or B. */
	public static final int COMPACT_FROM = 10_000;

	/**
	 * Shortens a coin or diamond balance so it never takes more than four
	 * characters: 9999, 12K, 999K, 1.2M, 2.1B. Balances have no cap, so
	 * without this a large one runs into the level text. Rounds down, so a
	 * balance is never shown as more than the player has.
	 *
	 * @param amount Balance to show.
	 * @return The balance as written in the top bar.
	 */
	public static String compactAmount(final int amount) {
		if (amount < COMPACT_FROM)
			return Integer.toString(amount);
		long[] units = { 1_000_000_000L, 1_000_000L, 1_000L };
		String[] suffixes = { "B", "M", "K" };
		for (int i = 0; i < units.length; i++) {
			if (amount < units[i])
				continue;
			long whole = amount / units[i];
			long tenth = amount % units[i] * 10 / units[i];
			if (whole >= 10 || tenth == 0)
				return whole + suffixes[i];
			return whole + "." + tenth + suffixes[i];
		}
		return Integer.toString(amount);
	}

	/**
	 * Item slots are only drawn while they hold an item.
	 *
	 * @param slots Inventory slots, null where a slot is empty.
	 * @return Indexes of the slots to draw, in key order.
	 */
	public static List<Integer> visibleSlots(final List<?> slots) {
		List<Integer> visible = new ArrayList<Integer>();
		for (int i = 0; i < slots.size(); i++)
			if (slots.get(i) != null)
				visible.add(i);
		return visible;
	}

	/** Left edges of the pieces on the right side of the top bar. */
	public static final class RightGroup {
		/** Left edge of the score digits. */
		public final int scoreX;
		/** Left edge of the diamond icon. */
		public final int diamondX;
		/** Left edge of the coin icon. */
		public final int coinX;

		RightGroup(final int scoreX, final int diamondX, final int coinX) {
			this.scoreX = scoreX;
			this.diamondX = diamondX;
			this.coinX = coinX;
		}
	}
}
