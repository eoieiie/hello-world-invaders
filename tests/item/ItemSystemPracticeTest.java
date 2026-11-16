package item;

import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

class ItemSystemPracticeTest {

    @Test
    void eachPracticeAttemptCreatesANewItemSystem() {
        ItemSystem firstAttempt = ItemSystem.newPracticeRun();
        ItemSystem secondAttempt = ItemSystem.newPracticeRun();

        assertNotSame(firstAttempt, secondAttempt);
    }
}