package entity;

import engine.Core;
import engine.DrawManager.SpriteType;
import engine.GameSettings;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MidBossSpreadShotTest {
    private static final GameSettings SETTINGS = new GameSettings(5, 4, 5, 1000);

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = MidBoss.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static MidBoss ready(double roll) throws Exception {
        MidBoss boss = new MidBoss(100, 60, SpriteType.MidBoss1, new Random() {
            @Override public double nextDouble() { return roll; }
        });
        set(boss, "shootCooldown", Core.getCooldown(-1));
        set(boss, "shootingInterval", 1000);
        return boss;
    }

    @Test
    void spreadsBelowThirtyPercentWithSlowerSideBullets() throws Exception {
        Set<Bullet> bullets = new HashSet<>();
        assertTrue(ready(0.299999).shoot(bullets, SETTINGS));
        assertEquals(3, bullets.size());
        Set<Integer> directions = new HashSet<>();
        for (Bullet bullet : bullets) {
            directions.add(bullet.getSpeedX());
            assertEquals(116 - bullet.getWidth() / 2, bullet.getPositionX());
            assertEquals(80, bullet.getPositionY());
            assertEquals(bullet.getSpeedX() == 0 ? 5 : 4, bullet.getSpeed());
            assertTrue(Math.hypot(bullet.getSpeedX(), bullet.getSpeed()) <= 5);
        }
        assertEquals(Set.of(-2, 0, 2), directions);
    }

    @Test
    void keepsSingleShotsAtAndAboveThirtyPercent() throws Exception {
        for (double roll : new double[] {0.30, 0.99}) {
            Set<Bullet> bullets = new HashSet<>();
            ready(roll).shoot(bullets, SETTINGS);
            assertEquals(1, bullets.size());
            assertEquals(0, bullets.iterator().next().getSpeedX());
            assertEquals(5, bullets.iterator().next().getSpeed());
        }
    }

    @Test
    void retainsCooldownAccelerationAndMinimumInterval() throws Exception {
        MidBoss boss = ready(0);
        Set<Bullet> bullets = new HashSet<>();
        boss.shoot(bullets, SETTINGS);
        assertFalse(boss.shoot(bullets, SETTINGS));
        assertEquals(3, bullets.size());
        Field interval = MidBoss.class.getDeclaredField("shootingInterval");
        interval.setAccessible(true);
        assertEquals(970, interval.getInt(boss));
        set(boss, "shootingInterval", 510);
        set(boss, "shootCooldown", Core.getCooldown(-1));
        boss.shoot(bullets, SETTINGS);
        assertEquals(500, interval.getInt(boss));
        set(boss, "shootCooldown", Core.getCooldown(-1));
        boss.shoot(bullets, SETTINGS);
        assertEquals(500, interval.getInt(boss));
    }

    @Test
    void destroyedBossDoesNotShoot() throws Exception {
        MidBoss boss = ready(0);
        boss.destroy();
        Set<Bullet> bullets = new HashSet<>();
        assertFalse(boss.shoot(bullets, SETTINGS));
        assertTrue(bullets.isEmpty());
    }
}
