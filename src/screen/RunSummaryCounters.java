package screen;

/** Time-based presentation only; never changes the run's actual statistics. */
final class RunSummaryCounters {
    private final long rowDurationMillis;

    RunSummaryCounters(final long rowDurationMillis) {
        if (rowDurationMillis <= 0)
            throw new IllegalArgumentException("Row duration must be positive");
        this.rowDurationMillis = rowDurationMillis;
    }

    /** Rows count up in order, independent of frame rate. */
    double progress(final int row, final long elapsedMillis) {
        if (row < 0 || row >= 4)
            throw new IllegalArgumentException("Unknown summary row");
        double elapsedInRow = (double) elapsedMillis
                - (double) row * rowDurationMillis;
        return Math.max(0, Math.min(1, elapsedInRow / rowDurationMillis));
    }

    int value(final int target, final int row, final long elapsedMillis) {
        return (int) Math.round(target * progress(row, elapsedMillis));
    }

    /** Keep the existing ratio; a run without shots has no accuracy sample. */
    static float accuracy(final int shipsDestroyed, final int bulletsShot) {
        return bulletsShot == 0 ? 0 : (float) shipsDestroyed / bulletsShot;
    }
}
