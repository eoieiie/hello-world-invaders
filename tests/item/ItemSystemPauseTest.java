package item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entity.Entity;
import item.ItemAPI.EffectKind;
import item.ItemAPI.EffectView;
import item.ItemAPI.GrantRequest;
import item.ItemAPI.GrantSource;
import item.ItemAPI.GrantTiming;
import item.ItemAPI.UseResult;

/** HW-68: a pause must not use up the time left on a Boost. */
class ItemSystemPauseTest {
    private static final long MS = 1_000_000L;
    private static final long PAUSE = 60_000 * MS; // longer than any Boost

    private ItemSystem items;
    private Entity ship;
    private long now;

    @BeforeEach
    void startLevelWithBoost() {
        items = ItemSystem.forLevel(1);
        ship = new Entity(200, 400, 26, 16, Color.GREEN);
        items.beginLevel(1, 448, ship, new ItemSystem.GameHooks() {
            public int getLives() { return 3; }
            public void addLife() { }
            public void addScore(int points) { }
        });
        items.api().tryGrant(new GrantRequest("hw68-test", "boost", GrantSource.REWARD, GrantTiming.NOW));
        int slot = items.api().getView().slots.indexOf(items.api().getItemInfo("boost"));
        assertTrue(slot >= 0, "boost stored in a slot");
        now = 1_000_000 * MS;
        frame(0); // the first frame tells the item system the ship can use items
        assertEquals(UseResult.USED, items.useSlot(slot));
        frame(100);
    }

    private void frame(long millisLater) {
        now += millisLater * MS;
        items.update(ship, true, now);
    }

    private EffectView boost() {
        for (EffectView effect : items.api().getView().effects)
            if (effect.item.effectKind == EffectKind.BOOST) return effect;
        return null;
    }

    @Test
    void boostKeepsItsTimeAcrossAPause() {
        long before = boost().remainingMillis;
        now += PAUSE; // no update() calls while paused
        items.resumeAfterPause();
        frame(0);
        assertNotNull(boost(), "boost survived the pause");
        assertEquals(before, (long) boost().remainingMillis);
        frame(100);
        assertEquals(before - 100, (long) boost().remainingMillis, "boost counts down again");
    }

    @Test
    void boostStillExpiresWhenTheGameIsNotPaused() {
        frame(PAUSE / MS);
        assertNull(boost());
    }
}
