package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** HW-104: the tutorial-seen flag saved in player-profile. */
class TutorialSeenTest {

	/** Writes the profile as the file text, then reads it back. */
	private static PlayerProfile saveAndLoad(final PlayerProfile profile)
			throws IOException {
		StringWriter file = new StringWriter();
		FileManager.toProperties(profile).store(file, null);
		Properties loaded = new Properties();
		loaded.load(new StringReader(file.toString()));
		return FileManager.parsePlayerProfile(loaded);
	}

	@Test
	void firstRunShowsTutorial() throws IOException {
		// A profile saved before HW-104 has no tutorialSeen key.
		Properties oldProfile = new Properties();
		oldProfile.load(new StringReader(
				"totalEnemiesKilled=12\nunlockedAchievements=first_kill\n"));

		assertFalse(FileManager.parsePlayerProfile(oldProfile).isTutorialSeen());
		assertFalse(saveAndLoad(new PlayerProfile()).isTutorialSeen());
	}

	@Test
	void laterRunSkipsTutorialOnceSeen() throws IOException {
		PlayerProfile profile = new PlayerProfile(12, Set.of("first_kill"));
		profile.markTutorialSeen();

		PlayerProfile nextLaunch = saveAndLoad(profile);

		assertTrue(nextLaunch.isTutorialSeen());
		assertEquals(12, nextLaunch.getTotalEnemiesKilled());
		assertEquals(Set.of("first_kill"), nextLaunch.getUnlockedAchievements());
	}
}
