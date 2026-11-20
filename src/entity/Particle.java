package entity;

import java.awt.Color;
import java.util.Random;

/** A tiny sparkle that flies outward and fades away. */
public class Particle {

    private float x, y;              // position
    private float speedX, speedY;    // pixels per frame
    private int size;                // square size in pixels
    private Color color;
    private long born;               // creation time
    private int lifetime;            // milliseconds
    private float gravity;           // added to speedY each frame (debris)

    /** Pull that makes debris fall, in pixels per frame per frame. */
    static final float DEBRIS_GRAVITY = 0.15f;

    public Particle(final float x, final float y, final Color color) {
        double angle = Math.random() * Math.PI * 2;      // random direction
        float speed = 0.5f + (float) Math.random() * 2f; // random speed
        this.x = x;
        this.y = y;
        this.speedX = (float) Math.cos(angle) * speed;
        this.speedY = (float) Math.sin(angle) * speed;
        this.size = Math.random() < 0.5 ? 2 : 3;
        this.color = color;
        this.born = System.currentTimeMillis();
        this.lifetime = 400 + (int) (Math.random() * 300);
    }

    /**
     * A larger piece of debris that is thrown up and then falls, for ship
     * and boss explosions.
     *
     * @param x      Start position.
     * @param y      Start position.
     * @param color  Colour of the exploding ship.
     * @param random Source of the random direction, size and lifetime.
     * @return New debris piece.
     */
    public static Particle debris(final float x, final float y,
            final Color color, final Random random) {
        Particle debris = new Particle(x, y, color);
        debris.speedX = (random.nextFloat() - 0.5f) * 4f;  // -2 to 2
        debris.speedY = -(1f + random.nextFloat() * 2f);   // 1 to 3 upward
        debris.size = 3 + random.nextInt(2);               // 3 or 4 px
        debris.gravity = DEBRIS_GRAVITY;
        debris.lifetime = 700 + random.nextInt(300);
        return debris;
    }

    /** Moves the particle; it slows down a little each frame. */
    public final void update() {
        this.x += this.speedX;
        this.y += this.speedY;
        this.speedX *= 0.96f;
        this.speedY = this.speedY * 0.96f + this.gravity;
    }

    /** 1 when new, 0 when fully faded. */
    public final float getAlpha() {
        float age = (System.currentTimeMillis() - this.born)
                / (float) this.lifetime;
        return Math.max(0f, 1f - age);
    }

    public final boolean isDead() { return getAlpha() <= 0f; }

        /** True while any part of the particle is inside the given area. */
    public final boolean isInside(final int minX, final int minY,
            final int maxX, final int maxY) {
        return this.x + this.size > minX && this.x < maxX
                && this.y + this.size > minY && this.y < maxY;
    }
    
    public final int getX() { return Math.round(this.x); }
    public final int getY() { return Math.round(this.y); }
    public final int getSize() { return this.size; }
    public final Color getColor() { return this.color; }
}