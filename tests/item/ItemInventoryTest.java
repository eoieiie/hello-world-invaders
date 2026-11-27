package item;

import static org.junit.jupiter.api.Assertions.*;

import item.ItemAPI.ItemInfo;
import org.junit.jupiter.api.Test;

class ItemInventoryTest {
    private final ItemInventory inventory = new ItemInventory(2);
    private final ItemDefinitions definitions = new ItemDefinitions();
    private final ItemInfo boost = definitions.find("boost");
    private final ItemInfo shield = definitions.find("shield");

    @Test
    void consumingAnItemLeavesTheOtherSlotInPlace() {
        inventory.store(0, boost);
        inventory.store(1, shield);
        inventory.consume(0);

        assertNull(inventory.at(0));
        assertSame(shield, inventory.at(1));
        assertEquals(0, inventory.firstEmptySlot());
    }

    @Test
    void fillingTheLastSlotLeavesNoEmptySlot() {
        inventory.store(0, boost);
        assertEquals(1, inventory.firstEmptySlot());
        inventory.store(1, shield);
        assertEquals(-1, inventory.firstEmptySlot());
    }

    @Test
    void occupiedSlotRejectsReplacementAndKeepsOriginalItem() {
        inventory.store(0, boost);
        assertThrows(IllegalStateException.class, () -> inventory.store(0, shield));
        assertSame(boost, inventory.at(0));
        assertNull(inventory.at(1));
    }

    @Test
    void rejectsSlotIndexesOutsideBothEnds() {
        for (int index : new int[] {-1, inventory.capacity()}) {
            assertThrows(IndexOutOfBoundsException.class, () -> inventory.store(index, boost));
            assertThrows(IndexOutOfBoundsException.class, () -> inventory.at(index));
            assertThrows(IndexOutOfBoundsException.class, () -> inventory.consume(index));
        }
        assertEquals(0, inventory.firstEmptySlot());
    }
}
