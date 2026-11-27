package item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import item.ItemAPI.DropRule;
import item.ItemAPI.DropSource;
import item.ItemAPI.EventType;
import item.ItemAPI.ItemEvent;
import item.ItemAPI.LevelRules;
import item.ItemAPI.LifePort;

/** HW-130: drops still on the field when a level is cleared are collected. */
class LevelClearCollectTest {

    private static final LifePort LIVES = new LifePort() {
        public boolean canAddLife() { return true; }
        public boolean tryAddLife() { return true; }
    };

    /** A manager with the given inventory size whose enemies always drop the given item. */
    private static ItemManager managerDropping(String itemId, int capacity) {
        ItemManager manager = new ItemManager(capacity, ItemBalance.defaults(), new Random(1));
        Map<String, Double> weights = Collections.singletonMap(itemId, 1.0);
        Map<DropSource, DropRule> drops = new EnumMap<DropSource, DropRule>(DropSource.class);
        drops.put(DropSource.REGULAR_ENEMY, new DropRule(1.0, weights));
        drops.put(DropSource.SPECIAL_ENEMY, new DropRule(1.0, weights));
        manager.beginLevel(new LevelRules("test", 0, 448, 0, 500, 0.1, 20, 20, 10_000, drops), LIVES);
        return manager;
    }

    private static int count(ItemManager manager, EventType type) {
        int count = 0;
        for (ItemEvent event : manager.drainEvents())
            if (event.type == type) count++;
        return count;
    }

    @Test
    void fallingItemIsCollectedIntoAFreeSlot() {
        ItemManager manager = managerDropping("shield", 2);
        manager.onEnemyDefeated(DropSource.REGULAR_ENEMY, 100, 50);
        manager.drainEvents();

        manager.collectRemainingDrops();

        assertTrue(manager.getView().drops.isEmpty(), "no drop left on the field");
        assertEquals("shield", manager.getView().slots.get(0).itemId);
        assertEquals(1, count(manager, EventType.ITEM_COLLECTED));
    }

    @Test
    void itemThatDoesNotFitAFullInventoryIsNotCollectedAndIsClearedAtLevelEnd() {
        ItemManager manager = managerDropping("shield", 1);
        manager.onEnemyDefeated(DropSource.REGULAR_ENEMY, 100, 50);
        manager.onEnemyDefeated(DropSource.REGULAR_ENEMY, 200, 50);
        manager.drainEvents();

        manager.collectRemainingDrops();

        assertEquals(1, manager.getView().drops.size(), "the second shield had no free slot");
        assertEquals(1, count(manager, EventType.ITEM_COLLECTED));

        manager.endLevel();
        assertTrue(manager.getView().drops.isEmpty(), "endLevel removes what was left");
    }

    @Test
    void runStackAtItsLimitBecomesScore() {
        ItemManager manager = managerDropping("rapid_fire", 2);
        int maxStacks = manager.getItemInfo("rapid_fire").maxStacks;
        // Reach one below the limit, then leave two drops on the field.
        for (int i = 0; i < maxStacks - 1; i++) {
            manager.onEnemyDefeated(DropSource.REGULAR_ENEMY, 100, 50);
            manager.collectRemainingDrops();
        }
        manager.onEnemyDefeated(DropSource.REGULAR_ENEMY, 100, 50);
        manager.onEnemyDefeated(DropSource.REGULAR_ENEMY, 200, 50);
        manager.drainEvents();

        manager.collectRemainingDrops();

        assertTrue(manager.getView().drops.isEmpty(), "both drops are gone");
        Map<EventType, Integer> counts = new EnumMap<EventType, Integer>(EventType.class);
        for (ItemEvent event : manager.drainEvents())
            counts.merge(event.type, 1, Integer::sum);
        assertEquals(Integer.valueOf(1), counts.get(EventType.ITEM_COLLECTED), "the first one reaches the limit");
        assertEquals(Integer.valueOf(1), counts.get(EventType.ITEM_CONVERTED), "the second one becomes score");
    }
}
