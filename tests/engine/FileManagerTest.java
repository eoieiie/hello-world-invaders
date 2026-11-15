package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Checks how high scores are read from the scores file text. */
class FileManagerTest {

	/** Parses the given scores file text. */
	private static List<Score> parse(final String text) throws IOException {
		return FileManager.parseHighScores(
				new BufferedReader(new StringReader(text)));
	}

	/** Turns scores into "NAME=score" strings so a whole list can be compared. */
	private static List<String> entries(final List<Score> scores) {
		List<String> entries = new ArrayList<String>();
		for (Score score : scores)
			entries.add(score.getName() + "=" + score.getScore());
		return entries;
	}

	@Test
	void readsNameAndScorePairs() throws IOException {
		assertEquals(List.of("AAA=100", "BBB=200"),
				entries(parse("AAA\n100\nBBB\n200\n")));
	}

	@Test
	void ignoresBlankLines() throws IOException {
		assertEquals(List.of("AAA=100", "BBB=200"),
				entries(parse("AAA\n\n100\n\nBBB\n200\n\n")));
	}

	@Test
	void skipsEntryWithNonNumericScore() throws IOException {
		assertEquals(List.of("BBB=200"),
				entries(parse("AAA\nabc\nBBB\n200\n")));
	}

	@Test
	void recoversWhenNameLineIsMissing() throws IOException {
		// BBB's name line is gone, so its score 200 sits where a name should be.
		assertEquals(List.of("AAA=100", "CCC=300"),
				entries(parse("AAA\n100\n200\nCCC\n300\n")));
	}

	@Test
	void returnsEmptyListForEmptyFile() throws IOException {
		assertEquals(List.of(), entries(parse("")));
	}

	@Test
	void dropsNameWithoutScoreAtTheEnd() throws IOException {
		assertEquals(List.of("AAA=100"),
				entries(parse("AAA\n100\nBBB\n")));
	}
}
