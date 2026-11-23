package screen;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import engine.ScreenTransition;

class RunSummaryCountersTest {
    private final RunSummaryCounters counters = new RunSummaryCounters(300);

    @Test
    void nonzeroScoreStartsCountingAfterTheScreenFade() {
        long opening = 8_000_000_000L;
        long fadeEnd = opening + ScreenTransition.DURATION_MILLIS * 1_000_000L;
        counters.start(opening);
        assertEquals(0, counters.value(-80, 0, counters.elapsedMillis(opening)));
        assertEquals(0, counters.value(-80, 0, counters.elapsedMillis(fadeEnd)));
        assertEquals(-40, counters.value(-80, 0, counters.elapsedMillis(fadeEnd + 150_000_000L)));
        assertEquals(-80, counters.value(-80, 0, counters.elapsedMillis(fadeEnd + 300_000_000L)));
    }

    @Test
    void freshSpaceRevealsEveryRowAndAHeldSkipCannotStartReplay() {
        counters.start(0);
        assertFalse(counters.requestsReplay(true, 100), "Space held over from gameplay");
        assertTrue(counters.isCounting(100));
        assertFalse(counters.requestsReplay(false, 110));
        assertFalse(counters.requestsReplay(true, 120), "skip stays on the summary");
        assertFalse(counters.isCounting(120));
        for (int row = 0; row < 4; row++)
            assertEquals(1.0, counters.progress(row, 120));
        assertFalse(counters.requestsReplay(true, 1500), "held skip after counters finish");
        assertFalse(counters.requestsReplay(false, 1510));
        assertTrue(counters.requestsReplay(true, 1520), "fresh press can replay");
    }

    @Test
    void freshSpaceReplaysAfterNaturalCompletionWithoutAnExtraSkip() {
        counters.start(0);
        counters.requestsReplay(false, 0);
        assertTrue(counters.isCounting(1199));
        assertFalse(counters.isCounting(1200));
        assertTrue(counters.requestsReplay(true, 1200));
    }

    @Test
    void rowsStartOnlyAfterThePreviousRowFinishes() {
        assertEquals(50, counters.value(100, 0, 150));
        assertEquals(0, counters.value(100, 1, 150));
        assertEquals(100, counters.value(100, 0, 300));
        assertEquals(0, counters.value(100, 1, 300));
        assertEquals(50, counters.value(100, 1, 450));
        assertEquals(0, counters.value(100, 2, 450));
        assertEquals(0.5, counters.progress(3, 1050));
    }

    @Test
    void exactEndAndLateFramesShowExactFinalValues() {
        for (int row = 0; row < 4; row++) {
            assertEquals(1.0, counters.progress(row, (row + 1) * 300L));
            assertEquals(12345, counters.value(12345, row, 9000));
            assertEquals(Integer.MAX_VALUE,
                    counters.value(Integer.MAX_VALUE, row, Long.MAX_VALUE));
        }
    }

    @Test
    void zeroAndNegativeElapsedTimeStartAtZero() {
        for (int row = 0; row < 4; row++) {
            assertEquals(0, counters.value(100, row, -100));
            assertEquals(0, counters.value(100, row, 0));
            assertEquals(0, counters.value(0, row, 600));
        }
    }

    @Test
    void droppedFramesCannotOvershootOrReverseTheCounter() {
        int previous = 0;
        for (long elapsed : new long[] { 0, 17, 81, 180, 299, 310, 5000 }) {
            int value = counters.value(987654321, 0, elapsed);
            assertTrue(value >= previous && value <= 987654321);
            previous = value;
        }
        assertEquals(987654321, previous);
    }

    @Test
    void durationCanFollowTheSharedMotionRulesWithoutChangingTargets() {
        RunSummaryCounters slower = new RunSummaryCounters(500);
        assertEquals(50, slower.value(100, 2, 1250));
        assertEquals(100, slower.value(100, 2, 1500));
    }

    @Test
    void negativeScoresArePreservedRatherThanClampedOrSavedAsZero() {
        assertEquals(-50, counters.value(-100, 0, 150));
        assertEquals(-100, counters.value(-100, 0, 1200));
    }

    @Test
    void accuracyIsFiniteWithoutShotsAndKeepsTheExistingRatioOtherwise() {
        assertEquals(0f, RunSummaryCounters.accuracy(0, 0));
        assertEquals(0.25f, RunSummaryCounters.accuracy(5, 20));
        // The existing multi-projectile counting issue is outside this UI story.
        assertEquals(1.5f, RunSummaryCounters.accuracy(3, 2));
    }

    @Test
    void rejectsInvalidDurationAndRow() {
        assertThrows(IllegalArgumentException.class,
                () -> new RunSummaryCounters(0));
        assertThrows(IllegalArgumentException.class,
                () -> new RunSummaryCounters(-1));
        assertThrows(IllegalArgumentException.class,
                () -> counters.progress(-1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> counters.progress(4, 0));
    }
}
