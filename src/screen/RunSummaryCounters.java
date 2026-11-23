package screen;

import engine.Easing;
import engine.ScreenTransition;

/** Time-based presentation only; never changes the run's actual statistics. */
final class RunSummaryCounters {
    private final long rowDurationMillis;
    private final KeyReleaseGate spaceGate = new KeyReleaseGate();
    private long startNanos;
    private boolean skipped;

    RunSummaryCounters(final long rowDurationMillis) {
        if (rowDurationMillis <= 0)
            throw new IllegalArgumentException("Row duration must be positive");
        this.rowDurationMillis = rowDurationMillis;
    }

    void start(final long nowNanos) {
        startNanos = nowNanos + ScreenTransition.DURATION_MILLIS * 1_000_000L;
        skipped = false;
        spaceGate.close();
    }

    long elapsedMillis(final long nowNanos) {
        return (nowNanos - startNanos) / 1_000_000L;
    }

    boolean isCounting(final long elapsedMillis) {
        return progress(3, elapsedMillis) < 1;
    }

    /** The first press reveals unfinished rows. A later, fresh press can replay. */
    boolean requestsReplay(final boolean spaceDown, final long elapsedMillis) {
        if (!spaceGate.accept(spaceDown))
            return false;
        spaceGate.close();
        if (isCounting(elapsedMillis)) {
            skipped = true;
            return false;
        }
        return true;
    }

    /** Rows count up in order, independent of frame rate. */
    double progress(final int row, final long elapsedMillis) {
        if (row < 0 || row >= 4)
            throw new IllegalArgumentException("Unknown summary row");
        double elapsedInRow = (double) elapsedMillis
                - (double) row * rowDurationMillis;
        return skipped ? 1 : Easing.linear(elapsedInRow / rowDurationMillis);
    }

    int value(final int target, final int row, final long elapsedMillis) {
        return (int) Math.round(target * progress(row, elapsedMillis));
    }

    /** Keep the existing ratio; a run without shots has no accuracy sample. */
    static float accuracy(final int shipsDestroyed, final int bulletsShot) {
        return bulletsShot == 0 ? 0 : (float) shipsDestroyed / bulletsShot;
    }
}
