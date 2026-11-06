import engine.Score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class ScoreTest {

    @Test
    void sortsScoresInDescendingOrder() {
        List<Score> scores = new ArrayList<>();
        scores.add(new Score("AAA", 100));
        scores.add(new Score("BBB", 500));
        scores.add(new Score("CCC", 300));

        Collections.sort(scores);

        assertEquals(500, scores.get(0).getScore());
        assertEquals(300, scores.get(1).getScore());
        assertEquals(100, scores.get(2).getScore());
    }
}