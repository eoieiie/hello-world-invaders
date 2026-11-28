package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.Core;
import engine.DrawManager;
import engine.Frame;
import engine.GameSettings;
import engine.GameState;
import entity.Boss;
import entity.Bullet;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Ship;

/**
 * Characterization tests for {@link GameScreen#update()} (HW-135).
 *
 * <p>
 * They drive the real method one frame at a time with scripted keys and
 * check what a frame does to the game: movement, firing, pause, game over,
 * level clear and the end of the screen. They were written against the
 * method before it was split and must pass unchanged after, which is how
 * the split is shown to keep the behaviour.
 * </p>
 *
 * <p>
 * {@code update()} ends by drawing, and drawing needs a window.
 * {@link #installOffscreenWindow()} gives the draw manager a window that
 * paints into memory, so the tests also run where there is no display.
 * </p>
 */
class GameScreenUpdateTest {

	private static final int WIDTH = 448;
	private static final int HEIGHT = 520;
	private static final int FPS = 60;
	/** 3 columns, 2 rows. The huge interval keeps enemies from firing. */
	private static final GameSettings QUIET =
			new GameSettings(3, 2, 60, 3_600_000);
	/** Longer than the 200 ms pause-key debounce. */
	private static final long PAUSE_DEBOUNCE_WAIT_MS = 260;

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

	@BeforeEach
	@AfterEach
	void releaseAllKeys() {
		for (int code : new int[] {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
				KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_A,
				KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S,
				KeyEvent.VK_SPACE, KeyEvent.VK_P})
			release(code);
	}

	// ---- helpers --------------------------------------------------------

	private static KeyEvent keyEvent(final int id, final int code) {
		return new KeyEvent(new java.awt.Component() {
			private static final long serialVersionUID = 1L;
		}, id, 0L, 0, code, KeyEvent.CHAR_UNDEFINED);
	}

	private static void press(final int code) {
		Core.getInputManager().keyPressed(keyEvent(KeyEvent.KEY_PRESSED, code));
	}

	private static void release(final int code) {
		Core.getInputManager()
				.keyReleased(keyEvent(KeyEvent.KEY_RELEASED, code));
	}

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

	/** A level-1 screen with three lives, still inside the stage intro. */
	private static GameScreen newScreen(final int level,
			final boolean practiceMode) {
		GameScreen screen = new GameScreen(
				new GameState(level, 0, 3, 0, 0, 0), QUIET, false,
				WIDTH, HEIGHT, FPS, practiceMode);
		screen.initialize();
		screen.isRunning = true; // run() sets this; the tests call update()
		return screen;
	}

	/** A screen whose stage intro is already over, so input is read. */
	private static GameScreen playableScreen() throws Exception {
		return playableScreen(1, false);
	}

	private static GameScreen playableScreen(final int level,
			final boolean practiceMode) throws Exception {
		GameScreen screen = newScreen(level, practiceMode);
		set(screen, "gameStartTime", System.currentTimeMillis() - 60_000L);
		return screen;
	}

	private static Ship ship(final GameScreen screen) throws Exception {
		return get(screen, "ship");
	}

	private static void destroyEveryEnemy(final GameScreen screen)
			throws Exception {
		EnemyShipFormation formation = get(screen, "enemyShipFormation");
		List<EnemyShip> ships = new ArrayList<EnemyShip>();
		for (EnemyShip enemy : formation)
			ships.add(enemy);
		for (EnemyShip enemy : ships)
			formation.destroy(enemy);
	}

	// ---- stage intro ----------------------------------------------------

	@Test
	void inputIsIgnoredDuringTheStageIntro() throws Exception {
		GameScreen screen = newScreen(1, false);
		int startX = ship(screen).getPositionX();

		press(KeyEvent.VK_RIGHT);
		press(KeyEvent.VK_SPACE);
		screen.update();

		assertEquals(startX, ship(screen).getPositionX());
		assertEquals(0, screen.getGameState().getBulletsShot());
	}

	// ---- movement -------------------------------------------------------

	@Test
	void rightAndLeftMoveTheShipByItsSpeed() throws Exception {
		GameScreen screen = playableScreen();
		Ship ship = ship(screen);
		int startX = ship.getPositionX();

		press(KeyEvent.VK_RIGHT);
		screen.update();
		assertEquals(startX + ship.getSpeed(), ship.getPositionX());

		release(KeyEvent.VK_RIGHT);
		press(KeyEvent.VK_A);
		screen.update();
		assertEquals(startX, ship.getPositionX());
	}

	@Test
	void theShipStopsAtBothSideBorders() throws Exception {
		GameScreen screen = playableScreen();
		Ship ship = ship(screen);

		int rightEdge = WIDTH - 1 - ship.getWidth();
		ship.setPositionX(rightEdge);
		press(KeyEvent.VK_D);
		screen.update();
		assertEquals(rightEdge, ship.getPositionX());

		release(KeyEvent.VK_D);
		ship.setPositionX(1);
		press(KeyEvent.VK_LEFT);
		screen.update();
		assertEquals(1, ship.getPositionX());
	}

	@Test
	void upMovesInsideTheBandAndUpPlusDownCancelOut() throws Exception {
		GameScreen screen = playableScreen();
		Ship ship = ship(screen);
		int startY = ship.getPositionY();

		press(KeyEvent.VK_UP);
		screen.update();
		int raisedY = ship.getPositionY();
		assertTrue(raisedY < startY, "up moves the ship up");

		press(KeyEvent.VK_S);
		screen.update();
		assertEquals(raisedY, ship.getPositionY());

		release(KeyEvent.VK_UP);
		screen.update();
		assertTrue(ship.getPositionY() > raisedY, "down moves the ship down");
	}

	// ---- firing ---------------------------------------------------------

	@Test
	void spaceFiresAndCountsEveryBullet() throws Exception {
		GameScreen screen = playableScreen();
		Set<Bullet> bullets = get(screen, "bullets");

		press(KeyEvent.VK_SPACE);
		screen.update();

		assertFalse(bullets.isEmpty(), "a shot adds bullets");
		assertEquals(bullets.size(), screen.getGameState().getBulletsShot());
	}

	// ---- pause ----------------------------------------------------------

	@Test
	void pTogglesPauseAndAPausedFrameMovesNothing() throws Exception {
		GameScreen screen = playableScreen();
		Ship ship = ship(screen);
		int startX = ship.getPositionX();

		press(KeyEvent.VK_P);
		press(KeyEvent.VK_RIGHT);
		screen.update();
		assertTrue((Boolean) get(screen, "paused"));
		assertEquals(startX, ship.getPositionX());

		release(KeyEvent.VK_P);
		screen.update();
		assertTrue((Boolean) get(screen, "paused"), "still paused");
		assertEquals(startX, ship.getPositionX());

		Thread.sleep(PAUSE_DEBOUNCE_WAIT_MS);
		press(KeyEvent.VK_P);
		screen.update();
		assertFalse((Boolean) get(screen, "paused"));
		assertEquals(startX + ship.getSpeed(), ship.getPositionX(),
				"the frame that resumes plays normally");
	}

	@Test
	void holdingPDoesNotToggleEveryFrame() throws Exception {
		GameScreen screen = playableScreen();

		press(KeyEvent.VK_P);
		screen.update();
		screen.update();

		assertTrue((Boolean) get(screen, "paused"));
	}

	@Test
	void spaceHeldWhilePausingDoesNotConfirmResume() throws Exception {
		GameScreen screen = playableScreen();

		press(KeyEvent.VK_SPACE);
		press(KeyEvent.VK_P);
		screen.update();
		Thread.sleep(PAUSE_DEBOUNCE_WAIT_MS);
		release(KeyEvent.VK_P);
		screen.update();

		assertTrue((Boolean) get(screen, "paused"));
	}

	@Test
	void theMenuCursorWaitsOutItsDebounceRightAfterPausing() throws Exception {
		GameScreen screen = playableScreen();

		press(KeyEvent.VK_P);
		press(KeyEvent.VK_DOWN);
		screen.update();
		screen.update();

		assertEquals("RESUME",
				((Enum<?>) get(screen, "pauseSelection")).name());
	}

	// ---- game over ------------------------------------------------------

	@Test
	void runningOutOfLivesStartsTheGameOverSequence() throws Exception {
		GameScreen screen = playableScreen();
		Set<Bullet> bullets = get(screen, "bullets");
		press(KeyEvent.VK_SPACE);
		screen.update();
		release(KeyEvent.VK_SPACE);
		assertFalse(bullets.isEmpty());

		set(screen, "lives", 0);
		screen.update();

		assertTrue((Boolean) get(screen, "gameOverActive"));
		assertTrue((Boolean) get(screen, "levelFinished"));
		assertTrue(bullets.isEmpty(), "bullets are cleared");
		assertTrue(screen.isRunning, "the sequence plays before the screen ends");
		assertEquals(0, screen.getGameState().getPendingDiamonds());
	}

	@Test
	void aNewSpacePressSkipsTheGameOverSequence() throws Exception {
		GameScreen screen = playableScreen();
		set(screen, "lives", 0);
		screen.update();
		assertTrue(screen.isRunning);

		press(KeyEvent.VK_SPACE);
		screen.update();

		assertFalse(screen.isRunning);
	}

	@Test
	void spaceHeldFromBeforeGameOverDoesNotSkipIt() throws Exception {
		GameScreen screen = playableScreen();
		press(KeyEvent.VK_SPACE);
		set(screen, "lives", 0);
		screen.update();
		screen.update();

		assertTrue(screen.isRunning);
	}

	// ---- level clear ----------------------------------------------------

	@Test
	void clearingALevelAliveFinishesItAndPaysItsLevelInDiamonds()
			throws Exception {
		GameScreen screen = playableScreen(3, false);
		destroyEveryEnemy(screen);

		screen.update();

		assertTrue((Boolean) get(screen, "levelFinished"));
		assertFalse((Boolean) get(screen, "gameOverActive"));
		assertEquals(3, screen.getGameState().getPendingDiamonds());
		assertEquals(1, screen.getGameState().getLevelsCompletedRun());
		assertTrue(screen.isRunning, "the screen waits before it closes");
	}

	@Test
	void aClearedLevelIsRewardedOnlyOnce() throws Exception {
		GameScreen screen = playableScreen(3, false);
		destroyEveryEnemy(screen);

		screen.update();
		screen.update();

		assertEquals(3, screen.getGameState().getPendingDiamonds());
		assertEquals(1, screen.getGameState().getLevelsCompletedRun());
	}

	@Test
	void bossLevelsPayTheirLevelInNormalPlay() throws Exception {
		GameScreen screen = playableScreen(5, false);
		((Boss) get(screen, "midBoss")).destroy();

		screen.update();

		assertEquals(5, screen.getGameState().getPendingDiamonds());
	}

	@Test
	void bossPracticePaysThreeDiamondsAtLevelFiveAndFiveAtLevelTen()
			throws Exception {
		GameScreen five = playableScreen(5, true);
		((Boss) get(five, "midBoss")).destroy();
		five.update();
		assertEquals(3, five.getGameState().getPendingDiamonds());

		GameScreen ten = playableScreen(10, true);
		((Boss) get(ten, "midBoss")).destroy();
		ten.update();
		assertEquals(5, ten.getGameState().getPendingDiamonds());
	}

	// ---- end of the screen ----------------------------------------------

	@Test
	void theScreenClosesOnceTheFinishDelayIsOver() throws Exception {
		GameScreen screen = playableScreen();
		set(screen, "levelFinished", true);
		// A cooldown that was never started counts as finished.
		set(screen, "screenFinishedCooldown", Core.getCooldown(1500));

		screen.update();

		assertFalse(screen.isRunning);
	}

	@Test
	void theScreenStaysOpenDuringTheFinishDelay() throws Exception {
		GameScreen screen = playableScreen();
		set(screen, "levelFinished", true);
		engine.Cooldown delay = Core.getCooldown(60_000);
		delay.reset();
		set(screen, "screenFinishedCooldown", delay);

		screen.update();

		assertTrue(screen.isRunning);
	}

	// ---- bonus ship -----------------------------------------------------

	@Test
	void theBonusShipAppearsWhenItsCooldownEndsAndLeavesPastTheRightEdge()
			throws Exception {
		GameScreen screen = playableScreen();
		assertNull(get(screen, "enemyShipSpecial"));
		// A cooldown that was never started counts as finished.
		set(screen, "enemyShipSpecialCooldown", Core.getCooldown(60_000));

		screen.update();
		EnemyShip bonus = get(screen, "enemyShipSpecial");
		assertNotNull(bonus);

		int beforeMove = bonus.getPositionX();
		screen.update();
		assertEquals(beforeMove + 2, bonus.getPositionX());

		bonus.setPositionX(WIDTH + 1);
		screen.update();
		assertNull(get(screen, "enemyShipSpecial"));
	}

	// ---- drawing --------------------------------------------------------

	@Test
	void everyFrameIsDrawnIntoTheBackBuffer() throws Exception {
		GameScreen screen = playableScreen();
		Field backBuffer = DrawManager.class.getDeclaredField("backBuffer");
		backBuffer.setAccessible(true);
		backBuffer.set(null, null);

		screen.update();
		assertNotNull(backBuffer.get(null), "a playing frame is drawn");

		backBuffer.set(null, null);
		press(KeyEvent.VK_P);
		screen.update();
		assertNotNull(backBuffer.get(null), "a paused frame is drawn");
	}
}
