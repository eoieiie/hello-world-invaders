package screen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import engine.Achievement;
import engine.AchievementManager;
import engine.Cooldown;
import engine.Core;
import engine.DrawManager;

/**
 * Implements the achievements screen, split into pages.
 * Page 1 shows normal achievements, page 2 shows tier achievements.
 */
public class AchievementsScreen extends Screen {
	/**
	 * Colour of the row the player has selected.
	 */
	private static final Color SELECTED = Color.GREEN;
	/**
	 * Colour of rows that are not selected.
	 */
	private static final Color UNSELECTED = Color.WHITE;
	/**
	 * Colour of secondary text and locked items.
	 */
	private static final Color MUTED = Color.GRAY;
	/**
	 * Colour of an unlocked achievement's icon.
	 */
	private static final Color UNLOCKED = Color.YELLOW;
	/**
	 * Colour of a locked achievement's icon.
	 */
	private static final Color LOCKED = Color.DARK_GRAY;
	/**
	 * Vertical position of the page label.
	 */
	private static final int PAGE_LABEL_Y = 95;
	/**
	 * Vertical position of the first row.
	 */
	private static final int FIRST_ROW_Y = 110;
	/**
	 * Vertical distance between rows.
	 */
	private static final int ROW_SPACING = 52;
	/**
	 * Horizontal position of the trophy icon.
	 */
	private static final int TROPHY_X = 30;
	/**
	 * Horizontal position of the name and description.
	 */
	private static final int TEXT_X = 70;
	/**
	 * Horizontal position of the status area.
	 */
	private static final int STATUS_X = 320;
	/** Summary and scroll cue share the row above the key hints. */
	private static final int SUMMARY_Y = 430;
	/**
	 * Time between selection moves, in milliseconds.
	 */
	private static final int SELECTION_INTERVAL = 200;
	/**
	 * Name of each page, in page order.
	 */
	private static final String[] PAGE_NAMES = {"Normal", "Tier"};

	/**
	 * Scroll function
	 */
	private int scrollOffset;
	/**
	 * Achievements on each page, in page order.
	 */
	private List<List<Achievement>> pages;
	/** Filtered pages retain the catalogue's order and page boundaries. */
	private List<List<Achievement>> visiblePages;
	/** Starts in All mode every time the screen is opened. */
	private boolean lockedOnly;
	/** Requires a new F press instead of repeating while F is held. */
	private boolean filterKeyWasDown;
	/** Total progress across both pages, refreshed when the screen opens. */
	private String achievementSummary;
	/**
	 * Index of the page currently shown.
	 */
	private int currentPage;
	/**
	 * Enemies defeated across all games.
	 */
	private int totalKills;
	/**
	 * Index of the selected row on the current page.
	 */
	private int selected;
	/**
	 * Stops the selection moving on every frame.
	 */
	private Cooldown selectionCooldown;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width  Screen width.
	 * @param height Screen height.
	 * @param fps    Frames per second.
	 */
	public AchievementsScreen(final int width, final int height,
							  final int fps) {
		super(width, height, fps);
		// Return to the main menu when this screen closes.
		this.returnCode = 1;
		AchievementManager manager = Core.getAchievementManager();
		this.pages = new ArrayList<List<Achievement>>();
		this.pages.add(manager.getNormalAchievements());
		this.pages.add(manager.getTierAchievements());
		this.visiblePages = this.pages;
		this.achievementSummary = summarizeAchievements(this.pages);
		this.totalKills = manager.getTotalEnemiesKilled();
		this.currentPage = 0;
		this.selected = 0;
		this.scrollOffset = 0;
		this.selectionCooldown = Core.getCooldown(SELECTION_INTERVAL);
		this.selectionCooldown.reset();
	}

	/**
	 * Starts the screen.
	 *
	 * @return Next screen code.
	 */
	@Override
	public final int run() {
		super.run();
		return this.returnCode;
	}

	/**
	 * Draws the screen and checks for keyboard input.
	 */
	@Override
	protected final void update() {
		super.update();
		boolean filterKeyDown = this.inputManager.isKeyDown(KeyEvent.VK_F);
		if (this.inputDelay.checkFinished()
				&& filterKeyDown && !this.filterKeyWasDown) {
			this.lockedOnly = !this.lockedOnly;
			this.visiblePages = filterAchievements(this.pages, this.lockedOnly);
			this.selected = 0;
			this.scrollOffset = 0;
			this.selectionCooldown.reset();
		}
		this.filterKeyWasDown = filterKeyDown;
		draw();
		if (this.inputDelay.checkFinished()
				&& this.selectionCooldown.checkFinished()) {
			if (this.inputManager.isKeyDown(KeyEvent.VK_UP)
					|| this.inputManager.isKeyDown(KeyEvent.VK_W)) {
				if (this.selected > 0) {
					this.selected--;
					this.selectionCooldown.reset();
				}
			}
			if (this.inputManager.isKeyDown(KeyEvent.VK_DOWN)
					|| this.inputManager.isKeyDown(KeyEvent.VK_S)) {
				if (this.selected
						< this.visiblePages.get(this.currentPage).size() - 1) {
					this.selected++;
					this.selectionCooldown.reset();
				}
			}
			if (this.inputManager.isKeyDown(KeyEvent.VK_LEFT)
					|| this.inputManager.isKeyDown(KeyEvent.VK_A)) {
				if (this.currentPage > 0) {
					this.currentPage--;
					this.selected = 0;
					this.scrollOffset = 0;
					this.selectionCooldown.reset();
				}
			}
			if (this.inputManager.isKeyDown(KeyEvent.VK_RIGHT)
					|| this.inputManager.isKeyDown(KeyEvent.VK_D)) {
				if (this.currentPage < this.pages.size() - 1) {
					this.currentPage++;
					this.selected = 0;
					this.scrollOffset = 0;
					this.selectionCooldown.reset();
				}
			}
			int visibleRows = AchievementManager.ACHIEVEMENTS_PER_PAGE;

			if (this.selected < this.scrollOffset) {
				this.scrollOffset = this.selected;
			} else if (this.selected >= this.scrollOffset + visibleRows) {
				this.scrollOffset = this.selected - visibleRows + 1;
			}
		}
		if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
				&& this.inputDelay.checkFinished()) {
			this.isRunning = false;
		}
	}

	/**
	 * @return Number of rows shown on the current page, at most
	 * {@link AchievementManager#ACHIEVEMENTS_PER_PAGE}.
	 */
	private int getRowCount() {
		int remaining = this.visiblePages.get(this.currentPage).size()
				- this.scrollOffset;

		return Math.min(
				remaining,
				AchievementManager.ACHIEVEMENTS_PER_PAGE);
	}

	/** Filters without changing the catalogue, its pages or achievement objects. */
	static List<List<Achievement>> filterAchievements(
			final List<List<Achievement>> pages, final boolean lockedOnly) {
		List<List<Achievement>> filteredPages = new ArrayList<>();
		for (List<Achievement> page : pages) {
			List<Achievement> filteredPage = new ArrayList<>();
			for (Achievement achievement : page) {
				if (!lockedOnly || !achievement.isUnlocked())
					filteredPage.add(achievement);
			}
			filteredPages.add(filteredPage);
		}
		return filteredPages;
	}

	/** Counts all achievements, including rows outside the visible page. */
	static String summarizeAchievements(final List<List<Achievement>> pages) {
		int unlocked = 0;
		int total = 0;
		for (List<Achievement> page : pages) {
			for (Achievement achievement : page) {
				total++;
				if (achievement.isUnlocked())
					unlocked++;
			}
		}
		return "Unlocked: " + unlocked + "/" + total;
	}

	/**
	 * Draws the title, the page label, the current page's rows, and the
	 * key hints.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);
		this.drawManager.drawScreenTitle(
				this, MenuItem.ACHIEVEMENTS.getTitle());

		String pageLabel = "< " + PAGE_NAMES[this.currentPage] + "  "
				+ (this.currentPage + 1) + "/" + this.pages.size() + " >  "
				+ (this.lockedOnly ? "Locked only" : "All");
		this.drawManager.drawMenuRow(this, pageLabel, PAGE_LABEL_Y, false);

		List<Achievement> page = this.visiblePages.get(this.currentPage);
		if (page.isEmpty()) {
			this.drawManager.drawMenuRow(this, this.lockedOnly
					? "All achievements on this page unlocked."
					: "No achievements yet.",
					FIRST_ROW_Y + 24, false);
		} else {
			for (int i = 0; i < getRowCount(); i++) {
				int achievementIndex = this.scrollOffset + i;

				drawAchievement(
						page.get(achievementIndex),
						FIRST_ROW_Y + i * ROW_SPACING,
						achievementIndex == this.selected);
			}

		}
		String summary = this.achievementSummary;
		if (this.scrollOffset + getRowCount()
				< page.size()) {
			summary += "  (more below)";
		}
		this.drawManager.drawMenuRow(this,
				summary, SUMMARY_Y, false);
		this.drawManager.drawKeyHints(this,
				"a/d page, w/s row, f all/locked, esc back");
		this.drawManager.completeDrawing(this);
	}

	/**
	 * Draws a single achievement row.
	 *
	 * @param achievement Achievement to draw.
	 * @param positionY   Vertical position of the row's top edge.
	 * @param isSelected  Whether this row is currently highlighted.
	 */
	private void drawAchievement(final Achievement achievement,
								 final int positionY, final boolean isSelected) {

		Color trophyColor = achievement.isUnlocked()
				? achievement.getIconColor() : LOCKED;

		if (achievement.getSpriteType() == DrawManager.SpriteType.FleetMaster) {
			trophyColor = achievement.isUnlocked()
					? Color.WHITE : Color.GRAY;
		}

		Color nameColor = isSelected ? SELECTED : UNSELECTED;

		DrawManager.SpriteType icon = achievement.getSpriteType();

		if (icon == null)
			icon = DrawManager.SpriteType.FirstFlight;

		AchievementManager manager = Core.getAchievementManager();

		if (icon == DrawManager.SpriteType.FleetMaster) {
			this.drawManager.drawSmallSprite(
					icon, TROPHY_X, positionY, trophyColor);
		} else {
			this.drawManager.drawSprite(
					icon, TROPHY_X, positionY, trophyColor);
		}

		this.drawManager.drawRegularString(
				achievement.getName(),
				TEXT_X, positionY + 8, nameColor);

		this.drawManager.drawRegularString(
				manager.getRequirementText(achievement),
				TEXT_X, positionY + 24, MUTED);

		this.drawManager.drawRegularString(
				manager.getProgressText(achievement),
				STATUS_X, positionY + 8,
				achievement.isUnlocked() ? SELECTED : MUTED);
	}
}
