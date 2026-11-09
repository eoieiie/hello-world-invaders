package screen;

import java.awt.event.KeyEvent;

/**
 * A screen reached from the main menu. It does what those screens share, so
 * they follow the screen rules without repeating the code: it draws the title
 * and the key hints, goes back to the menu on ESC, and returns to the menu
 * when it closes. A subclass only draws its own content.
 */
public abstract class MenuScreen extends Screen {

	/** Key hints for a screen where ESC is the only key. */
	private static final String DEFAULT_KEY_HINTS = "esc back";

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	protected MenuScreen(final int width, final int height, final int fps) {
		super(width, height, fps);

		// Back to the main menu when this screen closes.
		this.returnCode = 1;
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code.
	 */
	@Override
	public final int run() {
		super.run();

		return this.returnCode;
	}

	/**
	 * Draws the screen and goes back to the menu on ESC.
	 */
	@Override
	protected final void update() {
		super.update();

		draw();
		if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
				&& this.inputDelay.checkFinished())
			this.isRunning = false;
	}

	/**
	 * Draws the title, the content of the screen and the key hints.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);

		this.drawManager.drawScreenTitle(this, getTitle());
		drawContent();
		this.drawManager.drawKeyHints(this, getKeyHints());

		this.drawManager.completeDrawing(this);
	}

	/**
	 * Getter for the title of the screen.
	 *
	 * @return Title drawn at the top of the screen.
	 */
	protected abstract String getTitle();

	/**
	 * Draws what is between the title and the key hints.
	 */
	protected abstract void drawContent();

	/**
	 * Getter for the keys the player can use on this screen.
	 *
	 * @return Line drawn at the bottom of the screen.
	 */
	protected String getKeyHints() {
		return DEFAULT_KEY_HINTS;
	}
}
