package engine;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.zip.CRC32;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the pixels {@link WarpIn#drawWarp} produces, so that splitting the
 * method into its stages can be shown to change nothing a player sees.
 *
 * <p>The checksums below were recorded against {@code 3e5b4fa}, tagged
 * v0.3.0, with nothing changed in the method but the visibility of
 * {@code drawWarp}. A refactor that keeps behaviour identical leaves every one
 * of them unchanged; any difference, even one pixel in one frame, fails and
 * names the frame.
 *
 * <p>Two sets of frames are checked. The first walks the effect in even steps
 * and covers the body of each stage. The second sits on the stage constants
 * themselves and just below them, because an even walk steps over most of
 * them: at 0.05 apart, only {@code LOCK_END} and {@code RAIN_END} land on a
 * sampled frame, so moving {@code RAIN_START} or {@code IMPACT} by 0.01 would
 * otherwise go unnoticed.
 *
 * <p><b>If these fail after a JDK upgrade rather than a code change.</b>
 * Frames from {@code IMPACT} onwards reach {@code drawImpact}, which draws
 * anti-aliased ovals through a {@link BasicStroke}, and how those pixels come
 * out is not guaranteed to be identical across JDK versions. These numbers
 * were produced on JDK 17 and have also been confirmed on JDK 25. If a later
 * JDK disagrees while the source is untouched, re-record them at v0.3.0 on
 * that JDK rather than reading it as a regression.
 */
class WarpInCharacterizationTest {

	/** Canvas size, the same 448 x 520 the game window uses. */
	private static final int WIDTH = 448;
	private static final int HEIGHT = 520;

	/** Fixed arguments, so only the progress value changes between frames. */
	private static final int SHIP_X = 200;
	private static final int SHIP_Y = 400;
	private static final int TOP_Y = 100;
	private static final long SEED = 12345L;
	private static final Color COLOUR = Color.GREEN;

	/** Number of steps taken across the effect. One more frame than this is
	 * drawn, because both ends are included. */
	private static final int STEPS = 20;

	/** CRC32 of the ARGB pixels of each evenly spaced frame. */
	private static final long[] STEP_CHECKSUMS = {
			4195230127L,   // p = 0.00
			 765896844L,   // p = 0.05
			 322707761L,   // p = 0.10
			1646565526L,   // p = 0.15
			3022459503L,   // p = 0.20
			1613120375L,   // p = 0.25
			2874645532L,   // p = 0.30
			1844596534L,   // p = 0.35
			3908401733L,   // p = 0.40
			1147355342L,   // p = 0.45
			3397106831L,   // p = 0.50
			2501532183L,   // p = 0.55
			1657742135L,   // p = 0.60
			 551970294L,   // p = 0.65
			 873204678L,   // p = 0.70
			3081957810L,   // p = 0.75
			 626069350L,   // p = 0.80
			3109882237L,   // p = 0.85
			3773108431L,   // p = 0.90
			1419125826L,   // p = 0.95
			3789987647L,   // p = 1.00
	};

	/** Progress values sitting on each stage constant, and just below it. */
	private static final double[] BOUNDARY_FRAMES = {
			0.175, 0.18,
			0.195, 0.20,
			0.255, 0.26,
			0.275, 0.28,
			0.595, 0.60,
			0.675, 0.68,
	};

	/** CRC32 of the ARGB pixels of each frame in {@link #BOUNDARY_FRAMES}. */
	private static final long[] BOUNDARY_CHECKSUMS = {
			 387480940L,   // p = 0.175 just below STRIKE_START
			2567885434L,   // p = 0.18  STRIKE_START
			 726414467L,   // p = 0.195 just below LOCK_END
			3022459503L,   // p = 0.20  LOCK_END
			2609435732L,   // p = 0.255 just below RAIN_START
			3931103088L,   // p = 0.26  RAIN_START
			2906730274L,   // p = 0.275 just below STRIKE_END
			1457471185L,   // p = 0.28  STRIKE_END
			 681333739L,   // p = 0.595 just below RAIN_END and SCAN_START
			1657742135L,   // p = 0.60  RAIN_END and SCAN_START
			2257865584L,   // p = 0.675 just below IMPACT
			3082404820L,   // p = 0.68  IMPACT
	};

	@BeforeAll
	static void headless() {
		System.setProperty("java.awt.headless", "true");
	}

	/** A fixed sprite, built here rather than loaded, so the test needs no
	 * resource files and cannot drift when the artwork changes. */
	private static boolean[][] sprite() {
		final boolean[][] sprite = new boolean[13][8];
		for (int column = 0; column < sprite.length; column++) {
			for (int row = 0; row < sprite[0].length; row++) {
				sprite[column][row] = ((column * 7 + row * 5) % 3) != 0;
			}
		}
		return sprite;
	}

	/** Draws one frame and returns a checksum of every pixel in it. */
	private static long checksumAt(final double progress) {
		final BufferedImage canvas =
				new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = canvas.createGraphics();
		try {
			WarpIn.drawWarp(graphics, sprite(), SHIP_X, SHIP_Y, COLOUR, TOP_Y,
					progress, SEED);
		} finally {
			graphics.dispose();
		}
		final CRC32 checksum = new CRC32();
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				final int argb = canvas.getRGB(x, y);
				checksum.update(argb >>> 24);
				checksum.update((argb >> 16) & 0xFF);
				checksum.update((argb >> 8) & 0xFF);
				checksum.update(argb & 0xFF);
			}
		}
		return checksum.getValue();
	}

	@Test
	@DisplayName("every frame of the warp draws the pixels it drew at v0.3.0")
	void everyFrameIsUnchanged() {
		for (int step = 0; step <= STEPS; step++) {
			final double progress = step / (double) STEPS;
			assertEquals(STEP_CHECKSUMS[step], checksumAt(progress),
					String.format("frame at p = %.2f", progress));
		}
	}

	@Test
	@DisplayName("the frames on and just below each stage boundary are unchanged")
	void stageBoundariesAreUnchanged() {
		for (int frame = 0; frame < BOUNDARY_FRAMES.length; frame++) {
			final double progress = BOUNDARY_FRAMES[frame];
			assertEquals(BOUNDARY_CHECKSUMS[frame], checksumAt(progress),
					String.format("boundary frame at p = %.3f", progress));
		}
	}

	@Test
	@DisplayName("the graphics state it was handed comes back as it was")
	void restoresTheGraphicsStateItWasGiven() {
		final BufferedImage canvas =
				new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = canvas.createGraphics();
		final Composite composite =
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
		final Stroke stroke = new BasicStroke(7f);
		final Paint paint = Color.MAGENTA;
		try {
			graphics.setComposite(composite);
			graphics.setStroke(stroke);
			graphics.setPaint(paint);
			WarpIn.drawWarp(graphics, sprite(), SHIP_X, SHIP_Y, COLOUR, TOP_Y,
					0.40, SEED);
			assertAll(
					() -> assertSame(composite, graphics.getComposite(),
							"composite"),
					() -> assertSame(stroke, graphics.getStroke(), "stroke"),
					() -> assertSame(paint, graphics.getPaint(), "paint"));
		} finally {
			graphics.dispose();
		}
	}

	@Test
	@DisplayName("the same frame drawn twice gives the same pixels")
	void drawingIsDeterministic() {
		final double progress = 0.40;
		assertEquals(checksumAt(progress), checksumAt(progress),
				"drawWarp must be reproducible for the checksums above to mean"
						+ " anything");
	}

	@Test
	@DisplayName("the frames are not all identical, so the checksums test something")
	void framesDiffer() {
		final long first = STEP_CHECKSUMS[0];
		int same = 0;
		for (final long checksum : STEP_CHECKSUMS) {
			if (checksum == first) {
				same++;
			}
		}
		assertEquals(1, same,
				"if every frame had the same checksum the test would pass on a"
						+ " method that drew nothing");
	}
}
