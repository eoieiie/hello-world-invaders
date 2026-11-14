package screen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** HW-66: Space held while firing must not confirm the pause menu. */
class KeyReleaseGateTest {
	@Test
	void keyHeldWhenClosedIsIgnoredUntilReleased() {
		KeyReleaseGate gate = new KeyReleaseGate();
		gate.close();
		for (int frame = 0; frame < 100; frame++)
			assertFalse(gate.accept(true), "held key counted on frame " + frame);
		assertFalse(gate.accept(false));
		assertTrue(gate.accept(true), "new press after release");
	}

	@Test
	void keyNotHeldWhenClosedCountsOnNextPress() {
		KeyReleaseGate gate = new KeyReleaseGate();
		gate.close();
		assertFalse(gate.accept(false));
		assertTrue(gate.accept(true));
	}

	@Test
	void openGateCountsEveryFrameTheKeyIsDown() {
		KeyReleaseGate gate = new KeyReleaseGate();
		assertTrue(gate.accept(true));
		assertTrue(gate.accept(true));
		assertFalse(gate.accept(false));
	}

	@Test
	void closingAgainIgnoresTheHeldKeyAgain() {
		KeyReleaseGate gate = new KeyReleaseGate();
		gate.close();
		gate.accept(false);
		assertTrue(gate.accept(true));
		gate.close();
		assertFalse(gate.accept(true));
	}
}
