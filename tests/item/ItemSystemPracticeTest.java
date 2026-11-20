package item;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ItemSystemPracticeTest {

    @Test
    void practiceRunDoesNotReplaceCurrentRun() {
        ItemSystem normalRun = ItemSystem.forLevel(1);

        ItemSystem practiceRun = ItemSystem.newPracticeRun();

        assertSame(normalRun, ItemSystem.current(),
                "Practice must not replace the normal run.");
        assertNotSame(normalRun, practiceRun,
                "Practice must use a separate item system.");
    }
}
