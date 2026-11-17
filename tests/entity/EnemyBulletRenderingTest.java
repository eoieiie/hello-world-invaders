package entity;

import engine.Core;
import engine.DrawManager;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyBulletRenderingTest {
    @Test
    void enemySpritesFollowVelocityAndRestoreTheDrawingTransform() throws Exception {
        DrawManager manager = Core.getDrawManager();
        Field graphicsField = DrawManager.class.getDeclaredField("backBufferGraphics");
        graphicsField.setAccessible(true);
        Field spritesField = DrawManager.class.getDeclaredField("spriteMap");
        spritesField.setAccessible(true);
        Object oldGraphics = graphicsField.get(null);
        @SuppressWarnings("unchecked")
        Map<DrawManager.SpriteType, boolean[][]> sprites =
                (Map<DrawManager.SpriteType, boolean[][]>) spritesField.get(null);
        boolean[][] oldSprite = sprites.get(DrawManager.SpriteType.EnemyBullet);
        // A narrow vertical sprite makes its orientation measurable in pixels.
        boolean[][] sprite = new boolean[3][5];
        for (int y = 0; y < 5; y++) sprite[1][y] = true;
        sprites.put(DrawManager.SpriteType.EnemyBullet, sprite);
        try {
            for (int speedX : new int[] {-2, 0, 2}) {
                BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    graphics.translate(3, 4);
                    AffineTransform before = graphics.getTransform();
                    graphicsField.set(null, graphics);
                    Bullet bullet = new Bullet(25, 25, 3);
                    bullet.setSpeedX(speedX);
                    manager.drawEntity(bullet, 25, 25);
                    assertEquals(before, graphics.getTransform());
                    double sumX = 0, sumY = 0, sumXY = 0;
                    int count = 0;
                    for (int x = 0; x < 64; x++) {
                        for (int y = 0; y < 64; y++) {
                            if ((image.getRGB(x, y) & 0xffffff) != 0) {
                                count++;
                                sumX += x;
                                sumY += y;
                                sumXY += x * y;
                            }
                        }
                    }
                    assertTrue(count > 0);
                    double covariance = sumXY / count - sumX * sumY / (count * count);
                    if (speedX == 0) assertEquals(0, covariance, 0.001);
                    else assertTrue(covariance * speedX > 0,
                            "Sprite should lean in its travel direction");
                } finally {
                    graphics.dispose();
                }
            }
        } finally {
            graphicsField.set(null, oldGraphics);
            sprites.put(DrawManager.SpriteType.EnemyBullet, oldSprite);
        }
    }
}
