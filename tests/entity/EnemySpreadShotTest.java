package entity;

import engine.Core;
import engine.GameSettings;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemySpreadShotTest {
    private static final GameSettings SETTINGS = new GameSettings(2, 2, 60, 1500);

    private static EnemyShipFormation ready(int level, double roll) throws Exception {
        EnemyShipFormation formation = new EnemyShipFormation(SETTINGS, level, new Random() {
            @Override public int nextInt(int bound) { return 0; }
            @Override public double nextDouble() { return roll; }
        });
        Field cooldown = EnemyShipFormation.class.getDeclaredField("shootingCooldown");
        cooldown.setAccessible(true);
        cooldown.set(formation, Core.getCooldown(-1));
        return formation;
    }

    @Test
    void earlyStagesAlwaysFireOneVerticalBullet() throws Exception {
        for (int level = 1; level <= 3; level++) {
            Set<Bullet> bullets = new HashSet<>();
            ready(level, 0).shoot(bullets);
            assertEquals(1, bullets.size());
            Bullet bullet = bullets.iterator().next();
            assertEquals(0, bullet.getSpeedX());
            assertEquals(4, bullet.getSpeed());
        }
    }

    @Test
    void laterOrdinaryStagesCanFireSpreadShots() throws Exception {
        for (int level : new int[] {4, 6, 7, 8, 9}) {
            Set<Bullet> bullets = new HashSet<>();
            ready(level, 0.199999).shoot(bullets);
            assertEquals(3, bullets.size());
            Set<Integer> directions = new HashSet<>();
            Set<Integer> originsX = new HashSet<>();
            Set<Integer> originsY = new HashSet<>();
            for (Bullet bullet : bullets) {
                directions.add(bullet.getSpeedX());
                originsX.add(bullet.getPositionX());
                originsY.add(bullet.getPositionY());
                assertTrue(Math.hypot(bullet.getSpeedX(), bullet.getSpeed()) <= 4);
                int x = bullet.getPositionX();
                int y = bullet.getPositionY();
                bullet.update();
                assertEquals(x + bullet.getSpeedX(), bullet.getPositionX());
                assertTrue(bullet.getPositionY() > y);
            }
            assertEquals(Set.of(-2, 0, 2), directions);
            assertEquals(1, originsX.size());
            assertEquals(1, originsY.size());
        }
    }

    @Test
    void spreadChanceMatchesEachOrdinaryStage() throws Exception {
        int[] levels = {4, 6, 7, 8, 9};
        double[] chances = {0.20, 0.40, 0.30, 0.30, 0.25};
        for (int i = 0; i < levels.length; i++) {
            int level = levels[i];
            double chance = chances[i];
            Set<Bullet> below = new HashSet<>();
            ready(level, chance - 0.000001).shoot(below);
            assertEquals(3, below.size(), "Spread below stage threshold: " + level);
            Set<Bullet> boundary = new HashSet<>();
            ready(level, chance).shoot(boundary);
            assertEquals(1, boundary.size(), "Single shot at stage threshold: " + level);
            Set<Bullet> above = new HashSet<>();
            ready(level, 0.99).shoot(above);
            assertEquals(1, above.size());
        }
    }

    @Test
    void cooldownPreventsAnotherVolley() throws Exception {
        EnemyShipFormation formation = ready(4, 0);
        Field cooldown = EnemyShipFormation.class.getDeclaredField("shootingCooldown");
        cooldown.setAccessible(true);
        cooldown.set(formation, Core.getCooldown(60_000));
        Set<Bullet> bullets = new HashSet<>();
        formation.shoot(bullets);
        assertEquals(3, bullets.size());
        formation.shoot(bullets);
        assertEquals(3, bullets.size());
    }

    @Test
    void emptyFormationDoesNotFire() throws Exception {
        EnemyShipFormation formation = ready(4, 0);
        Field shooters = EnemyShipFormation.class.getDeclaredField("shooters");
        shooters.setAccessible(true);
        ((List<?>) shooters.get(formation)).clear();
        Set<Bullet> bullets = new HashSet<>();
        assertDoesNotThrow(() -> formation.shoot(bullets));
        assertTrue(bullets.isEmpty());
    }

    @Test
    void recycledSpreadBulletsDoNotMakeOrdinaryBulletsDrift() throws Exception {
        Set<Bullet> spread = new HashSet<>();
        ready(4, 0).shoot(spread);
        BulletPool.recycle(spread);
        for (int i = 0; i < 3; i++) {
            Bullet bullet = BulletPool.getBullet(100, 100, -4);
            assertEquals(0, bullet.getSpeedX());
            assertEquals(-4, bullet.getSpeed());
            int x = bullet.getPositionX();
            bullet.update();
            assertEquals(x, bullet.getPositionX());
        }
    }
}
