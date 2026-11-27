package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Values carried between levels by GameState. */
class GameStateTest {

	@Test
	void fullConstructorKeepsEveryValue() {
		GameState state = new GameState(3, 1200, 2, 40, 25, 5, 2, 17);
		assertEquals(3, state.getLevel());
		assertEquals(1200, state.getScore());
		assertEquals(2, state.getLivesRemaining());
		assertEquals(40, state.getBulletsShot());
		assertEquals(25, state.getShipsDestroyed());
		assertEquals(5, state.getPendingDiamonds());
		assertEquals(2, state.getLevelsCompletedRun());
		assertEquals(17, state.getCoinsCollected());
	}

	@Test
	void shortConstructorStartsWithNoDiamondsAndNoClearedLevels() {
		GameState state = new GameState(1, 0, 3, 0, 0, 9);
		assertEquals(0, state.getPendingDiamonds());
		assertEquals(0, state.getLevelsCompletedRun());
		assertEquals(9, state.getCoinsCollected());
	}

	@Test
	void diamondConstructorKeepsDiamondsAndCoinsApart() {
		// pendingDiamonds and coinsCollected are both ints next to each
		// other, so check they do not end up in each other's place.
		GameState state = new GameState(2, 500, 3, 10, 8, 4, 11);
		assertEquals(4, state.getPendingDiamonds());
		assertEquals(11, state.getCoinsCollected());
		assertEquals(0, state.getLevelsCompletedRun());
	}

	@Test
	void negativeDiamondsAndClearedLevelsBecomeZero() {
		GameState state = new GameState(1, 0, 3, 0, 0, -5, -2, 0);
		assertEquals(0, state.getPendingDiamonds());
		assertEquals(0, state.getLevelsCompletedRun());
	}
}
