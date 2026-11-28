package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DebugGraphics;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.Core;
import engine.DrawManager;
import engine.PurchaseManager;
import entity.Ship.ShipType;
import shop.ShapeSkin;
import shop.ShopCatalog;
import shop.ShopCategory;
import shop.ShopItem;
import shop.Skin;

/**
 * Pins the text and colours {@code ShipSelectScreen.drawInfo()} shows in
 * each state, so the HW-142 refactor can show it decides the same things.
 * Positions are not checked.
 *
 * <p>A locked limited skin still on sale is left out: that text depends on
 * {@code LocalDate.now()}.
 */
class ShipSelectDrawInfoCharacterizationTest {

	private static final ShopItem UPGRADE =
			ShopCatalog.getByCategory(ShopCategory.UPGRADE).get(0);
	private static final ShopItem EFFECT =
			ShopCatalog.getByCategory(ShopCategory.EFFECT).get(0);
	/** The Standard ship has no looks. */
	private static final ShipType LOOK_SHIP = ShipType.FAST_ATTACK;
	private static final ShapeSkin LOOK = ShapeSkin.forShip(LOOK_SHIP).get(0);

	private static final List<String> STANDARD_STATS =
			stats("2", "1.33", "1");

	private ShipSelectScreen screen;

	@BeforeAll
	static void loadDrawManager() {
		Core.getLogger().setUseParentHandlers(false);
		Core.getDrawManager();
	}

	@AfterAll
	static void dropSharedState() throws ReflectiveOperationException {
		field(PurchaseManager.class, "instance").set(null, null);
	}

	@BeforeEach
	void ownNothing() throws ReflectiveOperationException {
		owned().clear();
		switchedOff().clear();
		this.screen = new ShipSelectScreen(448, 520, 60);
		for (String index : new String[] { "shipIndex", "shapeIndex",
				"skinIndex", "upgradeIndex", "effectIndex" })
			set(index, 0);
	}

	// --- Ship row ---

	@Test
	void standardShip() throws Exception {
		row("SHIP");
		assertDrawn(STANDARD_STATS, green("STANDARD IS EQUIPPED"));
	}

	@Test
	void eachOwnedShipShowsItsStats() throws Exception {
		row("SHIP");
		ship(ShipType.FAST_MOVE);
		own(shipEntry(ShipType.FAST_MOVE));
		assertDrawn(stats("4", "1.33", "1"), green("FAST MOVE IS EQUIPPED"));

		ship(ShipType.TWO_WAY);
		own(shipEntry(ShipType.TWO_WAY));
		assertDrawn(stats("1", "2.00", "2"),
				green("TWO-WAY SHOT IS EQUIPPED"));

		ship(ShipType.FAST_ATTACK);
		own(shipEntry(ShipType.FAST_ATTACK));
		assertDrawn(stats("2", "2.67", "1"),
				green("FAST ATTACK IS EQUIPPED"));

		ship(ShipType.BIG_BULLET);
		own(shipEntry(ShipType.BIG_BULLET));
		assertDrawn(stats("2", "1.00", "1"), green("BIG BULLET IS EQUIPPED"));
	}

	@Test
	void lockedShipShowsCoinPrice() throws Exception {
		row("SHIP");
		ship(ShipType.BIG_BULLET);
		assertDrawn(stats("2", "1.00", "1"),
				red("LOCKED - BUY IT IN THE SHOP FOR 300 COINS"));
	}

	// --- Look row ---

	@Test
	void originalLook() throws Exception {
		row("SHAPE");
		ship(LOOK_SHIP);
		assertDrawn(stats("2", "2.67", "1"), green("ORIGINAL IS EQUIPPED"));
	}

	@Test
	void ownedLook() throws Exception {
		row("SHAPE");
		ship(LOOK_SHIP);
		set("shapeIndex", 1);
		own(ShopCatalog.findByRef(ShopCategory.SHAPE, LOOK.name()));
		assertDrawn(stats("2", "2.67", "1"), green("ARROW IS EQUIPPED"));
	}

	@Test
	void lockedLookShowsBothPrices() throws Exception {
		row("SHAPE");
		ship(LOOK_SHIP);
		set("shapeIndex", 1);
		assertDrawn(stats("2", "2.67", "1"),
				red("LOCKED - BUY IT IN THE SHOP FOR 40 COINS OR 20 DIAMONDS"));
	}

	// --- Skin row ---

	@Test
	void classicSkin() throws Exception {
		row("SKIN");
		assertDrawn(STANDARD_STATS, green("CLASSIC IS EQUIPPED"));
	}

	@Test
	void lockedSkin() throws Exception {
		row("SKIN");
		skin(Skin.GILDED);
		assertDrawn(STANDARD_STATS,
				red("LOCKED - BUY IT IN THE SHOP FOR 60 COINS OR 30 DIAMONDS"));
	}

	@Test
	void ownedLimitedSkinIsTagged() throws Exception {
		row("SKIN");
		skin(Skin.RAINBOW_RUSH);
		own(ShopCatalog.findByRef(ShopCategory.SKIN, Skin.RAINBOW_RUSH.name()));
		assertDrawn(STANDARD_STATS, pink("LIMITED EDITION"),
				green("RAINBOW RUSH IS EQUIPPED"));
	}

	@Test
	void endedLimitedSkinIsNoLongerSold() throws Exception {
		row("SKIN");
		skin(Skin.PHANTOM);
		assertDrawn(STANDARD_STATS, pink("LIMITED EDITION"),
				red("LIMITED EDITION - NO LONGER SOLD"));
	}

	// --- Upgrade row ---

	@Test
	void upgradeOn() throws Exception {
		row("UPGRADE");
		own(UPGRADE);
		assertDrawn(STANDARD_STATS,
				green("EXTRA SLOTS IS ON - SPACE TO SWITCH OFF"),
				gray("MORE ITEM SLOTS IN EVERY RUN."));
	}

	@Test
	void upgradeOff() throws Exception {
		row("UPGRADE");
		own(UPGRADE);
		switchOff(UPGRADE);
		assertDrawn(STANDARD_STATS,
				gray("EXTRA SLOTS IS OFF - SPACE TO SWITCH ON"),
				gray("MORE ITEM SLOTS IN EVERY RUN."));
	}

	@Test
	void lockedUpgrade() throws Exception {
		row("UPGRADE");
		assertDrawn(STANDARD_STATS,
				red("LOCKED - BUY IT IN THE SHOP FOR 60 COINS OR 30 DIAMONDS"),
				gray("MORE ITEM SLOTS IN EVERY RUN."));
	}

	@Test
	void noUpgradesInShop() throws Exception {
		row("UPGRADE");
		set("availableUpgrades", Collections.emptyList());
		// Not "No upgrades in the shop": the code never reaches that line.
		assertDrawn(STANDARD_STATS, red("LOCKED - BUY IT IN THE SHOP"));
	}

	// --- Effect row ---

	@Test
	void effectOn() throws Exception {
		row("EFFECT");
		own(EFFECT);
		assertDrawn(STANDARD_STATS,
				green("LIGHTNING CHAIN IS ON - SPACE TO SWITCH OFF"),
				gray("KILLS CHAIN LIGHTNING TO NEARBY ENEMIES. STREAKS TURN"
						+ " IT VIOLET, THEN"),
				gray("GOLD. LOOK ONLY."));
	}

	@Test
	void effectOff() throws Exception {
		row("EFFECT");
		own(EFFECT);
		switchOff(EFFECT);
		assertDrawn(STANDARD_STATS,
				gray("LIGHTNING CHAIN IS OFF - SPACE TO SWITCH ON"),
				gray("KILLS CHAIN LIGHTNING TO NEARBY ENEMIES. STREAKS TURN"
						+ " IT VIOLET, THEN"),
				gray("GOLD. LOOK ONLY."));
	}

	@Test
	void lockedEffect() throws Exception {
		row("EFFECT");
		assertDrawn(STANDARD_STATS,
				red("LOCKED - BUY IT IN THE SHOP FOR 60 COINS OR 30 DIAMONDS"),
				gray("KILLS CHAIN LIGHTNING TO NEARBY ENEMIES. STREAKS TURN"
						+ " IT VIOLET, THEN"),
				gray("GOLD. LOOK ONLY."));
	}

	@Test
	void noEffectsInShop() throws Exception {
		row("EFFECT");
		set("availableEffects", Collections.emptyList());
		assertDrawn(STANDARD_STATS, gray("NO EFFECTS IN THE SHOP YET"));
	}

	// --- Recording ---

	/** Calls drawInfo() and checks every text it drew, in order. */
	private void assertDrawn(final List<String> stats, final String... rest)
			throws Exception {
		List<String> expected = new ArrayList<String>(stats);
		expected.addAll(Arrays.asList(rest));

		List<String> drawn = new ArrayList<String>();
		Graphics canvas = new BufferedImage(448, 520,
				BufferedImage.TYPE_INT_RGB).getGraphics();
		Graphics recorder = new DebugGraphics(canvas) {
			@Override
			public void drawString(final String text, final int x,
					final int y) {
				drawn.add(colourName(getColor()) + " " + text);
			}
		};
		field(DrawManager.class, "backBufferGraphics").set(null, recorder);
		Method drawInfo = ShipSelectScreen.class.getDeclaredMethod("drawInfo");
		drawInfo.setAccessible(true);
		drawInfo.invoke(this.screen);

		assertEquals(expected, drawn);
	}

	private static List<String> stats(final String speed,
			final String shotsPerSecond, final String bullets) {
		return Arrays.asList(gray("SPEED"), white(speed),
				gray("SHOTS PER SEC"), white(shotsPerSecond),
				gray("BULLETS"), white(bullets));
	}

	private static String green(final String text) {
		return "GREEN " + text;
	}

	private static String gray(final String text) {
		return "GRAY " + text;
	}

	private static String white(final String text) {
		return "WHITE " + text;
	}

	private static String red(final String text) {
		return "RED " + text;
	}

	private static String pink(final String text) {
		return "PINK " + text;
	}

	private static String colourName(final Color colour) {
		if (colour.equals(Color.GREEN))
			return "GREEN";
		if (colour.equals(Color.GRAY))
			return "GRAY";
		if (colour.equals(Color.WHITE))
			return "WHITE";
		if (colour.equals(new Color(255, 70, 70)))
			return "RED";
		if (colour.equals(new Color(255, 120, 200)))
			return "PINK";
		return colour.toString();
	}

	// --- Screen and shop state ---

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void row(final String name) throws Exception {
		Class rowType = Class.forName("screen.ShipSelectScreen$Row");
		set("row", Enum.valueOf(rowType, name));
	}

	private void ship(final ShipType type) throws Exception {
		set("shipIndex", type.ordinal());
	}

	private void skin(final Skin skin) throws Exception {
		set("skinIndex", skin.ordinal());
	}

	private void set(final String name, final Object value)
			throws ReflectiveOperationException {
		field(ShipSelectScreen.class, name).set(this.screen, value);
	}

	/** Bought upgrades and effects start switched on. */
	private static void own(final ShopItem item) throws Exception {
		owned().put(item.getId(), 1);
	}

	private static void switchOff(final ShopItem item) throws Exception {
		switchedOff().add(item.getId());
	}

	/** Set directly, so nothing is written to the save file. */
	@SuppressWarnings("unchecked")
	private static Map<String, Integer> owned()
			throws ReflectiveOperationException {
		return (Map<String, Integer>) field(PurchaseManager.class, "owned")
				.get(PurchaseManager.getInstance());
	}

	@SuppressWarnings("unchecked")
	private static Set<String> switchedOff()
			throws ReflectiveOperationException {
		return (Set<String>) field(PurchaseManager.class, "upgradesOff")
				.get(PurchaseManager.getInstance());
	}

	private static ShopItem shipEntry(final ShipType type) {
		return ShopCatalog.findByRef(ShopCategory.SHIP, type.name());
	}

	private static Field field(final Class<?> type, final String name)
			throws NoSuchFieldException {
		Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}
}
