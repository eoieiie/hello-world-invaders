package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Set;

import org.junit.jupiter.api.Test;

/** Recycling rules of the shared bullet pool. */
class BulletPoolTest {

	/*
	 * The pool is static and shared with other tests, so the recycling tests
	 * use sizes no other code asks for. That way the pool can only hand back
	 * the bullet recycled in the same test.
	 */
	private static final int ODD_WIDTH = 7;
	private static final int ODD_HEIGHT = 13;

	@Test
	void newBulletIsCenteredOnTheRequestedX() {
		Bullet bullet = BulletPool.getBullet(100, 200, 4);
		assertEquals(Bullet.DEFAULT_WIDTH, bullet.getWidth());
		assertEquals(Bullet.DEFAULT_HEIGHT, bullet.getHeight());
		assertEquals(100 - Bullet.DEFAULT_WIDTH / 2, bullet.getPositionX());
		assertEquals(200, bullet.getPositionY());
		assertEquals(4, bullet.getSpeed());
		assertEquals(0, bullet.getSpeedX());
	}

	@Test
	void recycledBulletIsReusedForTheSameSize() {
		Bullet first = BulletPool.getCustomBullet(50, 60, 4, ODD_WIDTH, ODD_HEIGHT);
		BulletPool.recycle(Set.of(first));

		Bullet second = BulletPool.getCustomBullet(80, 90, 4, ODD_WIDTH, ODD_HEIGHT);
		assertSame(first, second);
	}

	@Test
	void recycledBulletGetsFreshPositionSpeedAndSprite() {
		// An enemy bullet that was flying diagonally to the left...
		Bullet bullet = BulletPool.getCustomBullet(50, 60, 3, ODD_WIDTH, ODD_HEIGHT);
		bullet.setSpeedX(-2);
		bullet.update();
		BulletPool.recycle(Set.of(bullet));

		// ...comes back as a straight player bullet at the new position.
		Bullet reused = BulletPool.getCustomBullet(120, 300, -6, ODD_WIDTH, ODD_HEIGHT);
		assertSame(bullet, reused);
		assertEquals(120 - ODD_WIDTH / 2, reused.getPositionX());
		assertEquals(300, reused.getPositionY());
		assertEquals(-6, reused.getSpeed());
		assertEquals(0, reused.getSpeedX());
		assertEquals(engine.DrawManager.SpriteType.Bullet, reused.getSpriteType());
	}

	@Test
	void recycledBulletOfAnotherSizeIsNotReused() {
		Bullet odd = BulletPool.getCustomBullet(50, 60, 4, ODD_WIDTH, ODD_HEIGHT);
		BulletPool.recycle(Set.of(odd));

		Bullet other = BulletPool.getCustomBullet(50, 60, 4, ODD_WIDTH + 2, ODD_HEIGHT + 2);
		assertNotSame(odd, other);
		assertEquals(ODD_WIDTH + 2, other.getWidth());
		assertEquals(ODD_HEIGHT + 2, other.getHeight());

		// The odd-sized bullet is still waiting in the pool.
		assertSame(odd, BulletPool.getCustomBullet(0, 0, 4, ODD_WIDTH, ODD_HEIGHT));
	}
}
