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

	@Test
	void firingCannotSkipOrReplayUntilEachScreenSeesARelease() {
		KeyReleaseGate skip = new KeyReleaseGate();
		skip.close();
		assertFalse(skip.accept(true), "the last shot is still held");
		assertFalse(skip.accept(false));
		assertTrue(skip.accept(true), "a fresh press skips the animation");

		KeyReleaseGate replay = new KeyReleaseGate();
		replay.close();
		assertFalse(replay.accept(true), "the skip is still held");
		assertFalse(replay.accept(true), "waiting does not turn it into replay");
		assertFalse(replay.accept(false));
		assertTrue(replay.accept(true), "a fresh results press replays");
	}

	@Test
	void naturalGameOverLeavesReplayAvailableOnTheFirstPress() {
		KeyReleaseGate replay = new KeyReleaseGate();
		replay.close();
		assertFalse(replay.accept(false));
		assertTrue(replay.accept(true));
	}
}
