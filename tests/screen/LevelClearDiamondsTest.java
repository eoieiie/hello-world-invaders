package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Diamonds paid for clearing a level alive (HW-135). */
class LevelClearDiamondsTest {

	@Test
	void normalPlayPaysTheLevelNumber() {
		assertEquals(1, GameScreen.levelClearDiamonds(1, false));
		assertEquals(5, GameScreen.levelClearDiamonds(5, false));
		assertEquals(10, GameScreen.levelClearDiamonds(10, false));
	}

	@Test
	void bossPracticePaysAFixedAmountForTheTwoBossLevels() {
		assertEquals(3, GameScreen.levelClearDiamonds(5, true));
		assertEquals(5, GameScreen.levelClearDiamonds(10, true));
	}

	@Test
	void bossPracticePaysNothingForAnyOtherLevel() {
		assertEquals(0, GameScreen.levelClearDiamonds(1, true));
		assertEquals(0, GameScreen.levelClearDiamonds(7, true));
	}
}
