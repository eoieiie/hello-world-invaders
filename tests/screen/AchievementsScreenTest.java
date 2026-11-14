package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import engine.Achievement;

/** Pure summary/filter checks: no game window or saved profile is needed. */
class AchievementsScreenTest {
    private static Achievement achievement(final String id,
            final boolean unlocked) {
        return new Achievement(id, id, "Test achievement", 1, null, unlocked);
    }

    @Test
    void zeroUnlockedIncludesBothPagesInTotal() {
        assertEquals("Unlocked: 0/3", AchievementsScreen.summarizeAchievements(
                List.of(List.of(achievement("normal", false)),
                        List.of(achievement("tier1", false),
                                achievement("tier2", false)))));
    }

    @Test
    void partialCompletionCountsUnlockedOnBothPagesAndOffscreenRows() {
        List<Achievement> tiers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            tiers.add(achievement("tier" + i, i == 7));
        }
        assertEquals("Unlocked: 2/10", AchievementsScreen.summarizeAchievements(
                List.of(List.of(achievement("normal1", true),
                        achievement("normal2", false)), tiers)));
    }

    @Test
    void fullCompletionCountsEveryAchievement() {
        assertEquals("Unlocked: 3/3", AchievementsScreen.summarizeAchievements(
                List.of(List.of(achievement("normal", true)),
                        List.of(achievement("tier1", true),
                                achievement("tier2", true)))));
    }

    @Test
    void emptyCatalogueShowsZeroOutOfZero() {
        assertEquals("Unlocked: 0/0", AchievementsScreen.summarizeAchievements(
                List.of(List.of(), List.of())));
    }

    @Test
    void reopeningSummaryUsesCurrentUnlockState() {
        Achievement normal = achievement("normal", false);
        Achievement tier = achievement("tier", false);
        List<List<Achievement>> pages = List.of(List.of(normal), List.of(tier));
        assertEquals("Unlocked: 0/2", AchievementsScreen.summarizeAchievements(pages));
        tier.unlock();
        assertEquals("Unlocked: 1/2", AchievementsScreen.summarizeAchievements(pages));
    }

    @Test
    void allModeIncludesLockedAndUnlockedInOriginalOrder() {
        List<List<Achievement>> pages = List.of(
                List.of(achievement("done", true), achievement("next", false)),
                List.of(achievement("tier", true)));
        assertEquals(pages, AchievementsScreen.filterAchievements(pages, false));
    }

    @Test
    void lockedOnlyPreservesPagesOrderAndRequirementObjects() {
        Achievement first = achievement("first", false);
        Achievement second = achievement("second", false);
        Achievement tier = achievement("tier", false);
        List<List<Achievement>> pages = List.of(
                List.of(achievement("done", true), first, second),
                List.of(tier, achievement("doneTier", true)));
        assertEquals(List.of(List.of(first, second), List.of(tier)),
                AchievementsScreen.filterAchievements(pages, true));
        assertEquals(3, pages.get(0).size());
        assertEquals("Unlocked: 2/5", AchievementsScreen.summarizeAchievements(pages));
        assertEquals(pages, AchievementsScreen.filterAchievements(pages, false));
    }

    @Test
    void completedPageStaysEmptyWithoutBorrowingRowsFromOtherPage() {
        Achievement tier = achievement("tier", false);
        assertEquals(List.of(List.of(), List.of(tier)),
                AchievementsScreen.filterAchievements(List.of(
                        List.of(achievement("done", true)), List.of(tier)), true));
    }

    @Test
    void noUnlocksKeepsAllRowsIncludingThoseBeyondTheViewport() {
        List<Achievement> tiers = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            tiers.add(achievement("tier" + i, false));
        List<List<Achievement>> pages = List.of(List.of(), tiers);
        assertEquals(pages, AchievementsScreen.filterAchievements(pages, true));
    }

    @Test
    void allUnlockedAndEmptyCatalogueKeepBothPages() {
        List<List<Achievement>> emptyPages = List.of(List.of(), List.of());
        assertEquals(emptyPages, AchievementsScreen.filterAchievements(List.of(
                List.of(achievement("done", true)),
                List.of(achievement("doneTier", true))), true));
        assertEquals(emptyPages, AchievementsScreen.filterAchievements(emptyPages, true));
    }

    @Test
    void filteringAgainReadsCurrentUnlockState() {
        Achievement next = achievement("next", false);
        List<List<Achievement>> pages = List.of(List.of(next), List.of());
        assertEquals(pages, AchievementsScreen.filterAchievements(pages, true));
        next.unlock();
        assertEquals(List.of(List.of(), List.of()),
                AchievementsScreen.filterAchievements(pages, true));
        assertEquals(pages, AchievementsScreen.filterAchievements(pages, false));
    }
}
