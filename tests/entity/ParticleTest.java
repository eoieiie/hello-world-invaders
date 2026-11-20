package entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.Random;

import org.junit.jupiter.api.Test;

/** Checks how explosion debris moves. */
class ParticleTest {

	@Test
	void debrisIsThrownUpward() {
		Particle debris = Particle.debris(100, 100, Color.GREEN, new Random(1));
		debris.update();
		assertTrue(debris.getY() < 100, "debris should first move up");
	}

	@Test
	void debrisFallsBelowItsStartLater() {
		for (int seed = 0; seed < 20; seed++) {
			Particle debris = Particle.debris(100, 100, Color.GREEN,
					new Random(seed));
			for (int frame = 0; frame < 60; frame++)
				debris.update();
			assertTrue(debris.getY() > 100,
					"debris should fall back down (seed " + seed + ")");
		}
	}
}
