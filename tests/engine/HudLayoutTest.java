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
	void largeBalancesShortenToAtMostFourCharacters() {
		assertEquals("9999", HudLayout.compactAmount(9_999));
		assertEquals("10K", HudLayout.compactAmount(10_000));
		assertEquals("999K", HudLayout.compactAmount(999_999));
		assertEquals("1M", HudLayout.compactAmount(1_000_000));
		assertEquals("1.2M", HudLayout.compactAmount(1_299_999));
		assertEquals("2.1B", HudLayout.compactAmount(Integer.MAX_VALUE));

	}

	@Test
	void livesScoreCoinsAndGemsDoNotOverlapAtTheirMaxValues() {
		// Measured with the game font (Silkscreen) on the 448px screen:
		// a score digit is 11px wide, the widest short balance ("999M") is
		// 29px in the small font, a life icon is 26px, and "LEVEL 10" spans
		// x=186..261.
		final int screenWidth = 448;
		final int scoreWidth = 11 * HudLayout.MAX_SCORE_DIGITS;
		final int balanceWidth = 8 + HudLayout.ICON_GAP + 29;
		final int maxLives = 4; // life.maxLives 3 + the Max Life item
		final int levelLeft = 186;
		final int levelRight = 261;

		int livesRight = HudLayout.lifeIconX(maxLives - 1) + 26;
		HudLayout.RightGroup max = HudLayout.rightGroup(screenWidth,
				scoreWidth, balanceWidth, balanceWidth);

		assertTrue(livesRight < levelLeft, "lives end at " + livesRight);
		assertTrue(levelRight < max.coinX, "coins start at " + max.coinX);
		assertTrue(max.coinX + balanceWidth < max.diamondX);
		assertTrue(max.diamondX + balanceWidth < max.scoreX);
		assertEquals(screenWidth - HudLayout.RIGHT_MARGIN,
				max.scoreX + scoreWidth);
	}

	@Test
	void itemSlotsOnlyShowWhileTheyHoldAnItem() {
		assertEquals(Collections.emptyList(),
				HudLayout.visibleSlots(Arrays.asList(null, null, null)));
		List<String> slots = Arrays.asList(null, "shield", null, "freeze");
		assertEquals(Arrays.asList(1, 3), HudLayout.visibleSlots(slots));
	}
}
