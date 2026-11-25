package item;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import item.ItemAPI.ActivationMode;
import item.ItemAPI.DurationKind;
import item.ItemAPI.EffectKind;
import item.ItemAPI.GrantTiming;
import item.ItemAPI.ItemInfo;

/** Checks the built-in item catalog and the rules each definition must follow. */
class ItemDefinitionsTest {

    @Test
    void defaultCatalogPassesValidation() {
        assertDoesNotThrow(() -> new ItemDefinitions(ItemBalance.defaults()));
    }

    @Test
    void everyEffectKindHasExactlyOneDefinition() {
        ItemDefinitions definitions = new ItemDefinitions(ItemBalance.defaults());

        Set<EffectKind> kinds = EnumSet.noneOf(EffectKind.class);
        for (ItemInfo item : definitions.all())
            kinds.add(item.effectKind);

        assertEquals(EffectKind.values().length, definitions.all().size());
        assertEquals(EnumSet.allOf(EffectKind.class), kinds);
    }

    @Test
    void findReturnsTheSameDefinitionAsTheList() {
        ItemDefinitions definitions = new ItemDefinitions(ItemBalance.defaults());

        for (ItemInfo item : definitions.all())
            assertSame(item, definitions.find(item.itemId));
    }

    // ---------- rules for each effect kind ----------

    private static final Set<GrantTiming> NOW = EnumSet.of(GrantTiming.NOW);

    private static ItemInfo item(ActivationMode mode, EffectKind kind, DurationKind duration,
            Long millis, Integer charges, Double magnitude, Integer maxStacks,
            Set<GrantTiming> timings) {
        return new ItemInfo("test", "Test", "Test item", "test", mode, kind, duration,
            millis, charges, magnitude, maxStacks, timings);
    }

    @Test
    void everyDefaultDefinitionFollowsItsRule() {
        for (ItemInfo item : new ItemDefinitions(ItemBalance.defaults()).all())
            assertTrue(ItemDefinitions.followsRules(item), item.itemId);
    }

    @Test
    void lifeWithADurationIsRejected() {
        ItemInfo life = item(ActivationMode.ON_PICKUP, EffectKind.LIFE, DurationKind.INSTANT,
            5000L, null, null, null, NOW);
        assertFalse(ItemDefinitions.followsRules(life));
    }

    @Test
    void shieldWithoutChargesIsRejected() {
        ItemInfo shield = item(ActivationMode.MANUAL, EffectKind.SHIELD, DurationKind.TIMED,
            5000L, null, null, null, NOW);
        assertFalse(ItemDefinitions.followsRules(shield));
    }

    @Test
    void freezeWithChargesIsRejected() {
        ItemInfo freeze = item(ActivationMode.MANUAL, EffectKind.FREEZE, DurationKind.TIMED,
            5000L, 2, null, null, NOW);
        assertFalse(ItemDefinitions.followsRules(freeze));
    }

    @Test
    void boostNeedsAMultiplierAboveOne() {
        ItemInfo atOne = item(ActivationMode.MANUAL, EffectKind.BOOST, DurationKind.TIMED,
            5000L, null, 1.0, null, NOW);
        ItemInfo above = item(ActivationMode.MANUAL, EffectKind.BOOST, DurationKind.TIMED,
            5000L, null, 1.5, null, NOW);
        assertFalse(ItemDefinitions.followsRules(atOne));
        assertTrue(ItemDefinitions.followsRules(above));
    }

    @Test
    void runStackWithoutNextLevelTimingIsRejected() {
        ItemInfo rapidFire = item(ActivationMode.ON_PICKUP, EffectKind.RAPID_FIRE,
            DurationKind.UNTIL_RUN_END, null, null, 1.2, 3, NOW);
        assertFalse(ItemDefinitions.followsRules(rapidFire));
    }

    @Test
    void equippedItemThatStacksIsRejected() {
        ItemInfo magnet = item(ActivationMode.EQUIPPED, EffectKind.MAGNET,
            DurationKind.WHILE_EQUIPPED, null, null, 2.0, 3, NOW);
        assertFalse(ItemDefinitions.followsRules(magnet));
    }
}
