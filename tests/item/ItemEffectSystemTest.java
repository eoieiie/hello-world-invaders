package item;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import item.ItemAPI.EffectEndReason;
import item.ItemAPI.EffectView;
import item.ItemAPI.GrantFailure;
import item.ItemAPI.ItemInfo;
import org.junit.jupiter.api.Test;

class ItemEffectSystemTest {
    private final ItemEffectSystem effects = new ItemEffectSystem();
    private final ItemDefinitions definitions = new ItemDefinitions();
    private final ItemInfo rapidFire = definitions.find("rapid_fire");

    @Test
    void extraRapidFireAtTheStackLimitLeavesTheActiveEffectIntact() {
        long firstId = effects.apply(rapidFire, null).effect.effectId;
        for (int count = 2; count <= rapidFire.maxStacks; count++) {
            assertNull(effects.apply(rapidFire, null).failure);
        }

        assertEquals(GrantFailure.EFFECT_REJECTED, effects.apply(rapidFire, null).failure);
        List<EffectView> active = effects.snapshot();
        assertEquals(1, active.size());
        assertEquals(firstId, active.get(0).effectId);
        assertEquals(rapidFire.maxStacks, active.get(0).stacks);
    }

    @Test
    void endingALevelClearsBoostAndPreservesRapidFireStacks() {
        effects.apply(rapidFire, null);
        EffectView stacked = effects.apply(rapidFire, null).effect;
        EffectView boost = effects.apply(definitions.find("boost"), null).effect;

        List<ItemEffectSystem.Ended> ended = effects.clear();

        assertEquals(1, ended.size());
        assertEquals(boost.effectId, ended.get(0).effectId);
        assertEquals(EffectEndReason.LEVEL_ENDED, ended.get(0).reason);
        List<EffectView> active = effects.snapshot();
        assertEquals(1, active.size());
        assertEquals(stacked.effectId, active.get(0).effectId);
        assertEquals(2, active.get(0).stacks.intValue());
    }
}
