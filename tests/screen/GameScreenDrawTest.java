package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.Achievement;
import engine.Core;
import engine.DrawManager;
import engine.DrawManager.SpriteType;
import engine.Frame;
import engine.GameSettings;
import engine.GameState;
import engine.RespawnBlink;
import engine.ScreenTransition;
import entity.Bullet;
import entity.Coin;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;

/**
 * Characterization tests for {@code GameScreen.draw()} (HW-136).
 *
 * <p>
 * Each test puts the screen in one state, draws one frame and looks at the
 * picture: which things are on it and which are not. Frames are animated,
 * so the tests do not compare whole pictures. They count pixels of a known
 * colour in a known place, for example the ship's colour where the ship
 * is. They were written against the method before it was split and must
 * pass unchanged after.
 * </p>
 */
class GameScreenDrawTest {

	private static final int WIDTH = 448;
	private static final int HEIGHT = 520;
	private static final int FPS = 60;
	/** 3 columns, 2 rows. The huge interval keeps enemies from firing. */
	private static final GameSettings QUIET =
			new GameSettings(3, 2, 60, 3_600_000);
	/** Rows of the two stage intro lines; each line is two pixels thick. */
	private static final int INTRO_LINE_TOP = HEIGHT / 2 - HEIGHT / 12;
	private static final int INTRO_LINE_BOTTOM = HEIGHT / 2 + HEIGHT / 12;
	/** Achievement popup box, from DrawManager.drawAchievementUnlocked. */
	private static final int POPUP_LEFT = WIDTH - 218 - 6;
	private static final int POPUP_TOP = 46;
	private static final int POPUP_HEIGHT = 44;

	/** A window that was never opened and draws into an image. */
	private static final class OffscreenWindow extends Frame {
		private static final long serialVersionUID = 1L;
		private static final BufferedImage TARGET =
				new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

		private OffscreenWindow() {
			super(WIDTH, HEIGHT); // never runs, see installOffscreenWindow()
		}

		@Override
		public Graphics getGraphics() {
			return TARGET.getGraphics();
		}

		@Override
		public Insets getInsets() {
			return new Insets(0, 0, 0, 0);
		}
	}

	/**
	 * Opening a real window throws on a machine with no display, so the
	 * window object is created without running its constructor.
	 */
	@BeforeAll
	static void installOffscreenWindow() throws Exception {
		Core.getLogger().setUseParentHandlers(false);
		Field unsafeField =
				Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		Object unsafe = unsafeField.get(null);
		Frame window = (Frame) unsafe.getClass()
				.getMethod("allocateInstance", Class.class)
				.invoke(unsafe, OffscreenWindow.class);
		Core.getDrawManager().setFrame(window);
	}

	/** A screen fade left over from another test would tint every pixel. */
	@BeforeEach
	void noScreenFade() throws Exception {
		Field started = ScreenTransition.class.getDeclaredField("started");
		started.setAccessible(true);
		started.set(null, false);
	}

	// ---- helpers --------------------------------------------------------

	private static Field field(final String name) throws Exception {
		Field field = GameScreen.class.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	@SuppressWarnings("unchecked")
	private static <T> T get(final GameScreen screen, final String name)
			throws Exception {
		return (T) field(name).get(screen);
	}

	private static void set(final GameScreen screen, final String name,
			final Object value) throws Exception {
		field(name).set(screen, value);
	}

	private static int constant(final String name) throws Exception {
		return field(name).getInt(null);
	}

	/** A screen still inside its stage intro. */
	private static GameScreen newScreen(final int level) {
		GameScreen screen = new GameScreen(
				new GameState(level, 0, 3, 0, 0, 0), QUIET, false,
				WIDTH, HEIGHT, FPS);
		screen.initialize();
		return screen;
	}

	/** A screen whose stage intro is already over. */
	private static GameScreen playingScreen(final int level) throws Exception {
		GameScreen screen = newScreen(level);
		set(screen, "gameStartTime", System.currentTimeMillis() - 60_000L);
		return screen;
	}

	private static void pause(final GameScreen screen) throws Exception {
		set(screen, "paused", true);
		set(screen, "pauseStartedAt", System.currentTimeMillis());
	}

	/** Draws one frame and returns the picture. */
	private static BufferedImage draw(final GameScreen screen)
			throws Exception {
		Method draw = GameScreen.class.getDeclaredMethod("draw");
		draw.setAccessible(true);
		draw.invoke(screen);
		Field backBuffer = DrawManager.class.getDeclaredField("backBuffer");
		backBuffer.setAccessible(true);
		return (BufferedImage) backBuffer.get(null);
	}

	/** Pixels of exactly this colour inside the rectangle. */
	private static int count(final BufferedImage image, final Color color,
			final int left, final int top, final int width, final int height) {
		int found = 0;
		for (int x = Math.max(0, left);
				x < Math.min(image.getWidth(), left + width); x++)
			for (int y = Math.max(0, top);
					y < Math.min(image.getHeight(), top + height); y++)
				if ((image.getRGB(x, y) & 0xFFFFFF)
						== (color.getRGB() & 0xFFFFFF))
					found++;
		return found;
	}

	/** Pixels of the entity's own colour inside its own box. */
	private static int pixelsOf(final BufferedImage image, final Entity entity) {
		return count(image, entity.getColor(), entity.getPositionX(),
				entity.getPositionY(), entity.getWidth(), entity.getHeight());
	}

	/** Pixels of this colour on one full row. */
	private static int onRow(final BufferedImage image, final Color color,
			final int row) {
		return count(image, color, 0, row, WIDTH, 1);
	}

	/** Grey pixels in the band where the key hint line is written. */
	private static int keyHintPixels(final BufferedImage image) {
		return count(image, Color.GRAY, 0, HEIGHT - 60, WIDTH, 50);
	}

	/** Green pixels in the middle of the screen, away from ship and HUD. */
	private static int greenInTheMiddle(final BufferedImage image) {
		return count(image, Color.GREEN, 0, HEIGHT / 2 - 30, WIDTH, 60);
	}

	private static EnemyShip firstEnemy(final GameScreen screen)
			throws Exception {
		EnemyShipFormation formation = get(screen, "enemyShipFormation");
		return formation.iterator().next();
	}

	private static Bullet addPlayerBullet(final GameScreen screen)
			throws Exception {
		Set<Bullet> bullets = get(screen, "bullets");
		// Left of the pause menu box, whose text is white like a bullet.
		Bullet bullet = new Bullet(20, 400, -6);
		bullets.add(bullet);
		return bullet;
	}

	// ---- the battlefield ------------------------------------------------

	@Test
	void aPlayingFrameShowsShipEnemiesBulletsAndCoins() throws Exception {
		GameScreen screen = playingScreen(1);
		Ship ship = get(screen, "ship");
		Bullet bullet = addPlayerBullet(screen);
		Coin coin = new Coin(20, 300);
		Set<Coin> coins = get(screen, "coins");
		coins.add(coin);

		BufferedImage frame = draw(screen);

		assertTrue(pixelsOf(frame, ship) > 0, "ship");
		assertTrue(pixelsOf(frame, firstEnemy(screen)) > 0, "enemy");
		assertTrue(pixelsOf(frame, bullet) > 0, "bullet");
		assertTrue(pixelsOf(frame, coin) > 0, "coin");
	}

	@Test
	void aPausedFrameHidesTheBattlefield() throws Exception {
		GameScreen screen = playingScreen(1);
		Ship ship = get(screen, "ship");
		Bullet bullet = addPlayerBullet(screen);
		Coin coin = new Coin(20, 300);
		Set<Coin> coins = get(screen, "coins");
		coins.add(coin);
		pause(screen);

		BufferedImage frame = draw(screen);

		assertEquals(0, pixelsOf(frame, ship), "ship");
		assertEquals(0, pixelsOf(frame, firstEnemy(screen)), "enemy");
		assertEquals(0, pixelsOf(frame, bullet), "bullet");
		assertEquals(0, pixelsOf(frame, coin), "coin");
	}

	@Test
	void theBonusShipIsDrawnOnlyWhileThereIsOne() throws Exception {
		GameScreen screen = playingScreen(1);
		EnemyShip bonus = new EnemyShip();
		bonus.setPositionX(200);

		assertEquals(0, pixelsOf(draw(screen), bonus));

		set(screen, "enemyShipSpecial", bonus);
		assertTrue(pixelsOf(draw(screen), bonus) > 0);
	}

	@Test
	void aBossLevelDrawsTheBossInsteadOfAFormation() throws Exception {
		GameScreen screen = playingScreen(5);
		Entity boss = get(screen, "midBoss");

		assertTrue(pixelsOf(draw(screen), boss) > 0);
	}

	// ---- the ship after a hit -------------------------------------------

	@Test
	void aHitShipWithLivesLeftBlinksInsteadOfShowingTheWreck()
			throws Exception {
		GameScreen screen = playingScreen(1);
		Ship ship = get(screen, "ship");
		ship.destroy();
		long hiddenPhase = RespawnBlink.INTERVAL_MILLIS
				+ RespawnBlink.INTERVAL_MILLIS / 4;
		assertFalse(RespawnBlink.isVisible(hiddenPhase));

		set(screen, "shipHitAt", System.currentTimeMillis() - hiddenPhase);
		assertEquals(0, pixelsOf(draw(screen), ship), "hidden half of a blink");

		set(screen, "shipHitAt", System.currentTimeMillis()
				- RespawnBlink.INTERVAL_MILLIS / 4);
		assertTrue(pixelsOf(draw(screen), ship) > 0, "visible half of a blink");
	}

	@Test
	void theLastHitShowsTheWreckWithoutBlinking() throws Exception {
		GameScreen screen = playingScreen(1);
		Ship ship = get(screen, "ship");
		ship.destroy();
		set(screen, "lives", 0);
		set(screen, "shipHitAt", System.currentTimeMillis()
				- RespawnBlink.INTERVAL_MILLIS
				- RespawnBlink.INTERVAL_MILLIS / 4);

		assertTrue(pixelsOf(draw(screen), ship) > 0);
	}

	// ---- HUD ------------------------------------------------------------

	@Test
	void theHudIsDrawnWhetherPlayingOrPaused() throws Exception {
		int separatorRow = constant("SEPARATION_LINE_HEIGHT") - 1;
		GameScreen screen = playingScreen(1);

		assertEquals(WIDTH, onRow(draw(screen), Color.DARK_GRAY, separatorRow));

		pause(screen);
		assertEquals(WIDTH, onRow(draw(screen), Color.DARK_GRAY, separatorRow));
	}

	@Test
	void screenShakeMovesTheWorldButNotTheHud() throws Exception {
		int separatorRow = constant("SEPARATION_LINE_HEIGHT") - 1;
		GameScreen screen = playingScreen(1);
		((engine.ScreenShake) get(screen, "screenShake")).trigger();

		assertEquals(WIDTH, onRow(draw(screen), Color.DARK_GRAY, separatorRow));
	}

	// ---- pause menu -----------------------------------------------------

	@Test
	void onlyAPausedFrameShowsThePauseMenuAndItsKeyHint() throws Exception {
		GameScreen screen = playingScreen(1);
		BufferedImage playing = draw(screen);
		assertEquals(0, greenInTheMiddle(playing));
		assertEquals(0, keyHintPixels(playing));

		pause(screen);
		BufferedImage paused = draw(screen);
		assertTrue(greenInTheMiddle(paused) > 0, "menu");
		assertTrue(keyHintPixels(paused) > 0, "key hint");
	}

	// ---- stage intro ----------------------------------------------------

	@Test
	void theStageIntroBandIsDrawnDuringTheIntro() throws Exception {
		BufferedImage frame = draw(newScreen(1));

		assertEquals(WIDTH, onRow(frame, Color.GREEN, INTRO_LINE_TOP));
		assertEquals(WIDTH, onRow(frame, Color.GREEN, INTRO_LINE_BOTTOM));
		assertTrue(keyHintPixels(frame) > 0, "\"p pause\" hint");
	}

	@Test
	void theStageIntroBandIsGoneAfterTheIntro() throws Exception {
		BufferedImage frame = draw(playingScreen(1));

		assertEquals(0, onRow(frame, Color.GREEN, INTRO_LINE_TOP));
		assertEquals(0, onRow(frame, Color.GREEN, INTRO_LINE_BOTTOM));
	}

	@Test
	void pausingDuringTheIntroHidesTheBand() throws Exception {
		GameScreen screen = newScreen(1);
		pause(screen);

		BufferedImage frame = draw(screen);

		// The pause menu box is narrower than the screen, so the left
		// edge of the row shows whether the full-width line was drawn.
		assertEquals(0, count(frame, Color.GREEN, 0, INTRO_LINE_TOP, 20, 1));
		assertEquals(0, count(frame, Color.GREEN, 0, INTRO_LINE_BOTTOM, 20, 1));
	}

	// ---- game over ------------------------------------------------------

	@Test
	void shrinkingEnemiesAreDrawnOnceTheGameOverSequenceHasThem()
			throws Exception {
		GameScreen screen = playingScreen(1);
		EnemyShip leftover = new EnemyShip(300, 300, SpriteType.EnemyShipA1, 1);
		assertEquals(0, pixelsOf(draw(screen), leftover));

		Map<EnemyShip, Long> shrinking = new LinkedHashMap<EnemyShip, Long>();
		shrinking.put(leftover, System.currentTimeMillis());
		set(screen, "shrinkingEnemies", shrinking);

		assertTrue(pixelsOf(draw(screen), leftover) > 0);
	}

	@Test
	void theGameOverBannerIsDrawnOnlyWhenItIsDue() throws Exception {
		GameScreen screen = playingScreen(1);
		// Past typing and blinking, before the fade: the full text shows.
		long typed = (long) ((String) field("GAME_OVER_TEXT").get(null)).length()
				* constant("GAME_OVER_TYPE_INTERVAL");
		long blinked = 2L * constant("GAME_OVER_BLINK_COUNT")
				* constant("GAME_OVER_BLINK_INTERVAL");
		set(screen, "gameOverBannerStart",
				System.currentTimeMillis() - typed - blinked - 50);

		assertEquals(0, greenInTheMiddle(draw(screen)));

		set(screen, "showGameOverText", true);
		assertTrue(greenInTheMiddle(draw(screen)) > 0);
	}

	@Test
	void theSkipHintShowsDuringGameOverUnlessPaused() throws Exception {
		GameScreen screen = playingScreen(1);
		set(screen, "gameOverActive", true);

		assertTrue(keyHintPixels(draw(screen)) > 0, "\"space skip\" hint");

		// A paused frame writes its own hint in the same place, so the
		// skip hint is told apart by its different text width.
		int skipHint = keyHintPixels(draw(screen));
		pause(screen);
		set(screen, "gameOverActive", false);
		int pauseHintOnly = keyHintPixels(draw(screen));
		set(screen, "gameOverActive", true);
		assertEquals(pauseHintOnly, keyHintPixels(draw(screen)),
				"no skip hint on top of the pause hint");
		assertTrue(skipHint > 0);
	}

	// ---- achievement popup ----------------------------------------------

	@Test
	void theAchievementPopupIsDrawnOverEverythingWhileOneIsShowing()
			throws Exception {
		GameScreen screen = playingScreen(1);
		int borderRow = POPUP_TOP + POPUP_HEIGHT / 2;
		assertEquals(0, count(draw(screen), Color.GREEN,
				POPUP_LEFT, borderRow, 1, 1));

		set(screen, "unlockedAchievement", new Achievement("test", "Test",
				"For the draw test.", 1, SpriteType.EnemyShipA1, true));
		// Between slide-in and slide-out the box sits at its final place.
		set(screen, "achievementPopupStartedAt",
				System.currentTimeMillis() - 1_000);

		assertEquals(1, count(draw(screen), Color.GREEN,
				POPUP_LEFT, borderRow, 1, 1), "left border of the popup box");
	}

	@Test
	void theAchievementPopupCoversTheBattlefieldUnderIt() throws Exception {
		GameScreen screen = playingScreen(1);
		// The bonus ship flies at the height of the popup box.
		EnemyShip bonus = new EnemyShip();
		bonus.setPositionX(POPUP_LEFT + 100);
		set(screen, "enemyShipSpecial", bonus);
		assertTrue(pixelsOf(draw(screen), bonus) > 0);

		set(screen, "unlockedAchievement", new Achievement("test", "Test",
				"For the draw test.", 1, SpriteType.EnemyShipA1, true));
		set(screen, "achievementPopupStartedAt",
				System.currentTimeMillis() - 1_000);

		assertEquals(0, pixelsOf(draw(screen), bonus));
	}
}
