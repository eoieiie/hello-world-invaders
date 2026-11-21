package engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** HW-104: the tutorial shows on the first run only. */
class TutorialProgressTest {

	@Test
	void firstRunShowsTutorial(@TempDir final Path dir) {
		TutorialProgress progress = new TutorialProgress(
				dir.resolve("tutorial-seen").toFile());

		assertFalse(progress.isSeen());
	}

	@Test
	void laterRunSkipsTutorialOnceSeen(@TempDir final Path dir) {
		File marker = dir.resolve("tutorial-seen").toFile();
		new TutorialProgress(marker).markSeen();

		// A new instance stands for the next launch reading the same save.
		assertTrue(new TutorialProgress(marker).isSeen());
	}

	@Test
	void failedSaveKeepsTutorialForNextRun(@TempDir final Path dir) {
		// The parent folder doesn't exist, so the marker can't be written.
		TutorialProgress progress = new TutorialProgress(
				dir.resolve("missing").resolve("tutorial-seen").toFile());

		progress.markSeen();

		assertFalse(progress.isSeen());
	}
}
