package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import entity.Ship.ShipType;

/** HW-105: the ship moves up and down only inside its bottom band. */
class ShipVerticalBandTest {

	/** Top edge of the band used in these tests. */
	private static final int TOP = 460;
	/** Bottom edge of the band, also the starting row. */
	private static final int BOTTOM = 490;

	private static Ship shipInBand(final ShipType type) {
		Ship ship = new Ship(200, BOTTOM, type);
		ship.setVerticalBand(TOP, BOTTOM);
		return ship;
	}

	@Test
	void movesUpAndDownBySpeedInsideTheBand() {
		Ship ship = shipInBand(ShipType.STANDARD);
		ship.moveUp();
		assertEquals(BOTTOM - ship.getSpeed(), ship.getPositionY());
		ship.moveDown();
		assertEquals(BOTTOM, ship.getPositionY());
	}

	@Test
	void stopsAtTheTopEdgeOfTheBand() {
		Ship ship = shipInBand(ShipType.STANDARD);
		for (int frame = 0; frame < 100; frame++)
			ship.moveUp();
		assertEquals(TOP, ship.getPositionY());
	}

	@Test
	void stopsAtTheBottomEdgeOfTheBand() {
		Ship ship = shipInBand(ShipType.STANDARD);
		ship.moveUp();
		for (int frame = 0; frame < 100; frame++)
			ship.moveDown();
		assertEquals(BOTTOM, ship.getPositionY());
	}

	@Test
	void landsExactlyOnTheEdgeWhenAStepWouldCrossIt() {
		// One pixel away from an edge, a 4 px step would cross it, so the
		// ship stops on the edge instead.
		Ship ship = shipInBand(ShipType.FAST_MOVE);
		ship.setPositionY(TOP + 1);
		ship.moveUp();
		assertEquals(TOP, ship.getPositionY());
		ship.setPositionY(BOTTOM - 1);
		ship.moveDown();
		assertEquals(BOTTOM, ship.getPositionY());
	}

	@Test
	void staysOnItsStartingRowWhenNoBandIsSet() {
		Ship ship = new Ship(200, BOTTOM);
		ship.moveUp();
		assertEquals(BOTTOM, ship.getPositionY());
		ship.moveDown();
		assertEquals(BOTTOM, ship.getPositionY());
	}

	@Test
	void leftAndRightMovementKeepsTheRow() {
		Ship ship = shipInBand(ShipType.STANDARD);
		ship.moveUp();
		int row = ship.getPositionY();
		ship.moveLeft();
		ship.moveRight();
		assertEquals(row, ship.getPositionY());
	}

	@Test
	void rejectsABandWhoseTopIsBelowItsBottom() {
		Ship ship = new Ship(200, BOTTOM);
		assertThrows(IllegalArgumentException.class,
				() -> ship.setVerticalBand(BOTTOM, TOP));
	}
}
