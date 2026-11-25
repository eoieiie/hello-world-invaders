package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.zip.CRC32;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the pixels {@link WarpIn#drawWarp} produces, so that splitting the
 * method into its five stages can be shown to change nothing a player sees.
 *
 * <p>The checksums below were recorded against {@code 3e5b4fa}, the commit
 * tagged v0.3.0, before any of the method was moved. A refactor that keeps
 * behaviour identical leaves every one of them unchanged; any difference, even
 * one pixel in one frame, fails the test and names the frame.
 *
 * <p>The effect draws from a {@link java.util.Random} seeded with the caller's
 * seed and the progress value, so a given frame is reproducible. Five renders
 * of the same frame were checked against each other before these numbers were
 * recorded.
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

	/** Number of frames sampled across the effect, inclusive of both ends. */
	private static final int FRAMES = 20;

	/** CRC32 of the ARGB pixels of each frame, recorded at 3e5b4fa. */
	private static final long[] FRAME_CHECKSUMS = {
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
		for (int frame = 0; frame <= FRAMES; frame++) {
			final double progress = frame / (double) FRAMES;
			assertEquals(FRAME_CHECKSUMS[frame], checksumAt(progress),
					String.format("frame at p = %.2f", progress));
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
		final long first = FRAME_CHECKSUMS[0];
		int same = 0;
		for (final long checksum : FRAME_CHECKSUMS) {
			if (checksum == first) {
				same++;
			}
		}
		assertEquals(1, same,
				"if every frame had the same checksum the test would pass on a"
						+ " method that drew nothing");
	}
}
