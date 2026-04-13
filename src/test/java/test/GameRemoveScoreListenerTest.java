package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import game.PrisonersDilemma;
import game.listener.ScoreListener;
import robot.AlwaysCooperateBot;
import robot.Robot;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GameRemoveScoreListenerTest {

    private String scoreFile = "test_remove_scores.log";

    @AfterEach
    public void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(scoreFile));
        } catch (IOException e) { }
    }

    @Test
    public void testRemoveScoreListenerPreventsLogging() {
        PrisonersDilemma game = new PrisonersDilemma(3);
        ScoreListener listener = new ScoreListener(scoreFile);
        game.addScoreListener(listener);
        game.removeScoreListener(listener);

        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysCooperateBot("R2");
        game.run(r1, r2);

        listener.close();

        // File should be empty since listener was removed before the game ran
        File file = new File(scoreFile);
        if (file.exists()) {
            assertEquals(0, file.length());
        }
    }
}
