package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests the one-row top bar layout from HW-118. */
public class HudLayoutTest {

	@Test
	void scoreStaysRightAlignedWithCurrencyPackedToItsLeft() {
		HudLayout.RightGroup small = HudLayout.rightGroup(448, 50, 25, 30);
		assertEquals(448 - HudLayout.RIGHT_MARGIN - 50, small.scoreX);
		assertEquals(small.scoreX - HudLayout.GROUP_GAP - 25, small.diamondX);
		assertEquals(small.diamondX - HudLayout.GROUP_GAP - 30, small.coinX);

		// A longer score keeps its right edge and pushes the currency left.
		HudLayout.RightGroup wide = HudLayout.rightGroup(448, 80, 25, 30);
		assertEquals(448 - HudLayout.RIGHT_MARGIN, wide.scoreX + 80);
		assertEquals(small.diamondX - 30, wide.diamondX);
		assertEquals(small.coinX - 30, wide.coinX);
		assertTrue(wide.coinX + 30 < wide.diamondX);
		assertTrue(wide.diamondX + 25 < wide.scoreX);
	}

	@Test
	void largeBalancesShortenSoTheWorstCaseClearsTheLevelText() {
		assertEquals("9999", HudLayout.compactAmount(9_999));
		assertEquals("10K", HudLayout.compactAmount(10_000));
		assertEquals("999K", HudLayout.compactAmount(999_999));
		assertEquals("1M", HudLayout.compactAmount(1_000_000));
		assertEquals("1.2M", HudLayout.compactAmount(1_299_999));
		assertEquals("2.1B", HudLayout.compactAmount(Integer.MAX_VALUE));

		// Silkscreen widths: 11px per score digit, 7px per small character,
		// "LEVEL 10" ends at x=261 on a 448px screen. A six-digit score with
		// both balances at the int maximum still leaves a gap after it.
		int icon = 8 + HudLayout.ICON_GAP;
		int balance = icon + 7 * HudLayout
				.compactAmount(Integer.MAX_VALUE).length();
		HudLayout.RightGroup worst = HudLayout.rightGroup(448, 11 * 6,
				balance, balance);
		assertTrue(worst.coinX > 261, "coins start at " + worst.coinX);
	}

	@Test
	void itemSlotsOnlyShowWhileTheyHoldAnItem() {
		assertEquals(Collections.emptyList(),
				HudLayout.visibleSlots(Arrays.asList(null, null, null)));
		List<String> slots = Arrays.asList(null, "shield", null, "freeze");
		assertEquals(Arrays.asList(1, 3), HudLayout.visibleSlots(slots));
	}
}
