package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Coloured space backgrounds for the game screen, a different one for every
 * level. Each background is painted once into an image and reused every
 * frame; only the star layer moves.
 */
public final class LevelBackground {

    /** One theme per level, in level order. */
    public enum Theme {
        NEBULA, COBALT, AURORA, TEAL, MID_BOSS, DUSK, SUNSET, ICE, MAGENTA, FINAL_BOSS
    }

    /** Pixels the stars move down per second. */
    private static final double STAR_SPEED = 12;

    private static final Map<Theme, BufferedImage> BASES = new EnumMap<>(Theme.class);
    private static final Map<Theme, BufferedImage> STARS = new EnumMap<>(Theme.class);
    private static int cachedWidth;
    private static int cachedHeight;

    private LevelBackground() {
    }

    /**
     * Theme for a level. Levels outside 1 to 10 use the first theme.
     *
     * @param level Level number.
     * @return Theme to draw.
     */
    public static Theme themeFor(final int level) {
        Theme[] themes = Theme.values();
        if (level < 1 || level > themes.length) {
            return Theme.NEBULA;
        }
        return themes[level - 1];
    }

    /**
     * Draws the background for a level.
     *
     * @param g      Graphics to draw on.
     * @param width  Screen width.
     * @param height Screen height.
     * @param level  Level number.
     * @param millis Current time, for the star drift.
     */
    public static void draw(final Graphics2D g, final int width, final int height,
            final int level, final long millis) {
        if (width != cachedWidth || height != cachedHeight) {
            BASES.clear();
            STARS.clear();
            cachedWidth = width;
            cachedHeight = height;
        }
        Theme theme = themeFor(level);
        BufferedImage base = BASES.computeIfAbsent(theme, t -> paintBase(t, width, height));
        BufferedImage stars = STARS.computeIfAbsent(theme, t -> paintStars(t, width, height));
        g.drawImage(base, 0, 0, null);
        int offset = (int) (millis / 1000.0 * STAR_SPEED) % height;
        g.drawImage(stars, 0, offset - height, null);
        g.drawImage(stars, 0, offset, null);
    }

    private static BufferedImage paintBase(final Theme theme, final int w, final int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        switch (theme) {
            case COBALT:
                bands(g, w, h, 0x060E2E, 0x081338, 0x0A1842, 0x0C1D4C);
                nebula(g, w * 3 / 4, h * 3 / 10, w * 2 / 5, 0x1F6FBF, 0.6);
                nebula(g, w / 5, h * 7 / 10, w * 2 / 5, 0x2F3FAF, 0.5);
                break;
            case AURORA:
                bands(g, w, h, 0x061A1A, 0x08201E, 0x0A2622, 0x0C2C26);
                nebula(g, w / 3, h / 4, w / 2, 0x1F9E6E, 0.5);
                nebula(g, w * 4 / 5, h * 6 / 10, w * 2 / 5, 0x6E3FAF, 0.45);
                break;
            case TEAL:
                bands(g, w, h, 0x04222B, 0x052A33, 0x06323C, 0x073A45);
                planet(g, w * 4 / 5, h * 3 / 10, w / 9, 0x0F5A66, 0x7FE6E0);
                nebula(g, w / 5, h * 6 / 10, w * 2 / 5, 0x0F6A7A, 0.45);
                break;
            case MID_BOSS:
                bands(g, w, h, 0x1A0814, 0x200A18, 0x260C1C, 0x2C0E20);
                nebula(g, w / 6, h * 4 / 10, w * 2 / 5, 0x9E1F3B, 0.6);
                nebula(g, w * 4 / 5, h * 6 / 10, w / 2, 0x6E1F5B, 0.5);
                break;
            case DUSK:
                bands(g, w, h, 0x1A1008, 0x22140A, 0x2A180C, 0x321C0E);
                nebula(g, w * 2 / 3, h / 3, w / 2, 0xB0601F, 0.45);
                nebula(g, w / 5, h * 3 / 4, w / 3, 0x8A3F1F, 0.4);
                break;
            case SUNSET:
                bands(g, w, h, 0x14143C, 0x1D1747, 0x2A1A52, 0x3D1C5A, 0x5A1F5E,
                        0x7D2459, 0x9A2E50, 0xA9394A);
                g.setColor(new Color(0x2B0F2F));
                g.fillOval(-w / 3, h - h / 9, w * 5 / 3, h / 2);
                g.setColor(new Color(255, 154, 90, 150));
                g.drawOval(-w / 3, h - h / 9, w * 5 / 3, h / 2);
                break;
            case ICE:
                bands(g, w, h, 0x0A1424, 0x0D1A2E, 0x102038, 0x132642);
                planet(g, w / 5, h / 4, w / 10, 0x5A7FA6, 0xD8F0FF);
                nebula(g, w * 3 / 4, h * 6 / 10, w * 2 / 5, 0x6FA8D8, 0.4);
                break;
            case MAGENTA:
                bands(g, w, h, 0x1C0624, 0x22082C, 0x280A34, 0x2E0C3C);
                nebula(g, w / 2, h * 4 / 10, w / 2, 0xC21F8A, 0.5);
                nebula(g, w / 6, h * 8 / 10, w / 3, 0x6E1FAF, 0.45);
                break;
            case FINAL_BOSS:
                bands(g, w, h, 0x12040A, 0x18060E, 0x1E0812, 0x240A16);
                nebula(g, w / 2, h * 3 / 10, w * 3 / 5, 0xB01F2A, 0.6);
                nebula(g, w / 6, h * 3 / 4, w * 2 / 5, 0x5B1F9E, 0.5);
                nebula(g, w * 5 / 6, h * 7 / 10, w / 3, 0x9E1F5B, 0.45);
                break;
            case NEBULA:
            default:
                bands(g, w, h, 0x120A2E, 0x170C38, 0x1C0F42, 0x21104A);
                nebula(g, w / 6, h * 4 / 10, w * 2 / 5, 0x5B1F9E, 0.6);
                nebula(g, w * 4 / 5, h * 6 / 10, w * 9 / 20, 0x1F3F9E, 0.6);
                nebula(g, w / 2, h * 9 / 10, w / 3, 0x8A1F7A, 0.45);
                break;
        }
        g.dispose();
        return img;
    }

    private static BufferedImage paintStars(final Theme theme, final int w, final int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        int[] colors;
        switch (theme) {
            case TEAL:
            case AURORA:
            case ICE:
            case COBALT:
                colors = new int[] {0xFFFFFF, 0xA8FFF6, 0x7CC4FF};
                break;
            case DUSK:
                colors = new int[] {0xFFFFFF, 0xFFE1C4, 0xFFD27A};
                break;
            default:
                colors = new int[] {0xFFFFFF, 0xD9C8FF, 0xFFD9F0};
                break;
        }
        Random random = new Random(theme.ordinal() + 7);
        for (int i = 0; i < 70; i++) {
            Color c = new Color(colors[i % colors.length]);
            int alpha = 100 + random.nextInt(156);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            g.fillRect(random.nextInt(w), random.nextInt(h), 2, 2 + random.nextInt(3));
        }
        g.dispose();
        return img;
    }

    private static void bands(final Graphics2D g, final int w, final int h, final int... colors) {
        int bandHeight = (h + colors.length - 1) / colors.length;
        for (int i = 0; i < colors.length; i++) {
            g.setColor(new Color(colors[i]));
            g.fillRect(0, i * bandHeight, w, bandHeight);
        }
    }

    private static void nebula(final Graphics2D g, final int x, final int y, final int radius,
            final int rgb, final double strength) {
        Color c = new Color(rgb);
        for (int k = 0; k < 4; k++) {
            int r = (int) (radius * (1 - k * 0.22));
            int alpha = (int) (255 * strength * 0.35);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            g.fillOval(x - r, y - r, r * 2, r * 2);
        }
    }

    private static void planet(final Graphics2D g, final int x, final int y, final int r,
            final int body, final int ring) {
        Color b = new Color(body);
        Color shade = b.brighter();
        Color rc = new Color(ring);
        g.setColor(b);
        g.fillOval(x - r, y - r, r * 2, r * 2);
        g.setColor(new Color(shade.getRed(), shade.getGreen(), shade.getBlue(), 128));
        g.fillOval(x - r - r / 4, y - r - r / 5, r * 2, r * 2);
        g.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 140));
        g.drawOval(x - r * 5 / 3, y - r / 4, r * 10 / 3, r / 2);
    }
}
