package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import engine.DrawManager.SpriteType;
import engine.GameSettings;

/** Layout and ship removal of the enemy formation. Shooting is in EnemySpreadShotTest. */
class EnemyShipFormationTest {

	/** 3 columns, 2 rows, no two-hit enemies. */
	private static final GameSettings SETTINGS = new GameSettings(3, 2, 60, 1500);

	private static List<EnemyShip> ships(final EnemyShipFormation formation) {
		List<EnemyShip> ships = new ArrayList<EnemyShip>();
		for (EnemyShip ship : formation)
			ships.add(ship);
		return ships;
	}

	@Test
	void startsWithOneShipPerGridCell() {
		EnemyShipFormation formation = new EnemyShipFormation(SETTINGS);
		Set<String> cells = new HashSet<String>();
		for (EnemyShip ship : formation)
			cells.add(ship.getPositionX() + "," + ship.getPositionY());

		// Columns start at x = 20 and rows at y = 100, 40 px apart.
		assertEquals(Set.of("20,100", "60,100", "100,100",
				"20,140", "60,140", "100,140"), cells);
		assertFalse(formation.isEmpty());
	}

	@Test
	void nextShooterIsTheLowestShipStillAlive() {
		EnemyShip top = new EnemyShip(0, 100, SpriteType.EnemyShipA1, 1);
		EnemyShip bottom = new EnemyShip(0, 140, SpriteType.EnemyShipA1, 1);
		List<EnemyShip> column = List.of(top, bottom);
		EnemyShipFormation formation = new EnemyShipFormation(SETTINGS);

		assertSame(bottom, formation.getNextShooter(column));
		bottom.destroy();
		assertSame(top, formation.getNextShooter(column));
		top.destroy();
		assertNull(formation.getNextShooter(column));
	}

	@Test
	void destroyingEveryShipEmptiesTheFormation() {
		EnemyShipFormation formation = new EnemyShipFormation(SETTINGS);
		List<EnemyShip> ships = ships(formation);
		for (EnemyShip ship : ships) {
			assertFalse(formation.isEmpty());
			formation.destroy(ship);
		}
		assertTrue(formation.isEmpty());
		for (EnemyShip ship : ships)
			assertTrue(ship.isDestroyed());
	}

	@Test
	void removeDestroyedTakesOnlyDestroyedShipsOut() {
		EnemyShipFormation formation = new EnemyShipFormation(SETTINGS);
		EnemyShip hit = ships(formation).get(0);
		formation.destroy(hit);

		formation.removeDestroyed();

		List<EnemyShip> left = ships(formation);
		assertEquals(5, left.size());
		assertFalse(left.contains(hit));
	}

	@Test
	void removeBottomRowReturnsTheLowestLivingRow() {
		EnemyShipFormation formation = new EnemyShipFormation(SETTINGS);

		List<EnemyShip> bottomRow = formation.removeBottomRow();
		assertEquals(3, bottomRow.size());
		for (EnemyShip ship : bottomRow)
			assertEquals(140, ship.getPositionY());

		List<EnemyShip> left = ships(formation);
		assertEquals(3, left.size());
		for (EnemyShip ship : left)
			assertEquals(100, ship.getPositionY());
	}

	@Test
	void removeBottomRowIgnoresDestroyedShips() {
		EnemyShipFormation formation = new EnemyShipFormation(SETTINGS);
		for (EnemyShip ship : ships(formation))
			if (ship.getPositionY() == 140)
				formation.destroy(ship);

		// The destroyed bottom row is skipped, so the top row is taken.
		List<EnemyShip> row = formation.removeBottomRow();
		assertEquals(3, row.size());
		for (EnemyShip ship : row)
			assertEquals(100, ship.getPositionY());
	}

	@Test
	void removeBottomRowIsEmptyWhenNoShipIsAlive() {
		EnemyShipFormation formation = new EnemyShipFormation(SETTINGS);
		for (EnemyShip ship : ships(formation))
			formation.destroy(ship);
		assertTrue(formation.removeBottomRow().isEmpty());
	}
}
