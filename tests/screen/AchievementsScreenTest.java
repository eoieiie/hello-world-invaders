package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import engine.Achievement;

/** Pure summary checks: no game window or saved player profile is needed. */
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
}
