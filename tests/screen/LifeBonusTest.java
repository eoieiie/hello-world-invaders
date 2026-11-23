package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Checks the life bonus added when a game screen ends. */
class LifeBonusTest {

	@Test
	void givesNoBonusAndNoPenaltyOnGameOver() {
		assertEquals(0, GameScreen.lifeBonus(0));
	}

	@Test
	void givesNoBonusWithOneLifeLeft() {
		assertEquals(0, GameScreen.lifeBonus(1));
	}

	@Test
	void givesOneHundredForEachExtraLife() {
		assertEquals(100, GameScreen.lifeBonus(2));
		assertEquals(200, GameScreen.lifeBonus(3));
	}
}
