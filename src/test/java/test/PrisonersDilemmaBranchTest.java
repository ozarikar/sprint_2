package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import game.PrisonersDilemma;
import robot.Robot;

public class PrisonersDilemmaBranchTest {

    // Custom robot that returns whatever action we configure
    static class ConfigurableBot extends Robot {
        private String action;
        private int score = 0;

        public ConfigurableBot(String name, String action) {
            super(name);
            this.action = action;
        }

        @Override public String getAction() { return action; }
        @Override public int getScore() { return score; }

        @Override
        public void giveOutcome(String opponentName, String myAction,
                                String opponentAction, int myScore) {
            super.giveOutcome(opponentName, myAction, opponentAction, myScore);
            this.score += myScore;
        }

        public void setAction(String action) { this.action = action; }
    }

    @Test
    public void testInvalidActionCombinationScoresZero() {
        PrisonersDilemma game = new PrisonersDilemma(1);
        ConfigurableBot bot1 = new ConfigurableBot("B1", "INVALID");
        ConfigurableBot bot2 = new ConfigurableBot("B2", "INVALID");
        int[] scores = game.run(bot1, bot2);
        // No branch matches, both scores stay 0
        assertEquals(0, scores[0]);
        assertEquals(0, scores[1]);
    }

    @Test
    public void testDefectVsCooperateScoring() {
        // Explicitly covers the DEFECT-vs-COOPERATE branch
        PrisonersDilemma game = new PrisonersDilemma(1);
        ConfigurableBot bot1 = new ConfigurableBot("B1", "DEFECT");
        ConfigurableBot bot2 = new ConfigurableBot("B2", "COOPERATE");
        int[] scores = game.run(bot1, bot2);
        assertEquals(5, scores[0]);  // defector gets 5
        assertEquals(0, scores[1]);  // cooperator gets 0
    }

    @Test
    public void testCooperateVsDefectScoring() {
        // Explicitly covers the COOPERATE-vs-DEFECT branch
        PrisonersDilemma game = new PrisonersDilemma(1);
        ConfigurableBot bot1 = new ConfigurableBot("B1", "COOPERATE");
        ConfigurableBot bot2 = new ConfigurableBot("B2", "DEFECT");
        int[] scores = game.run(bot1, bot2);
        assertEquals(0, scores[0]);  // cooperator gets 0
        assertEquals(5, scores[1]);  // defector gets 5
    }

    @Test
    public void testBothCooperateScoring() {
        PrisonersDilemma game = new PrisonersDilemma(1);
        ConfigurableBot bot1 = new ConfigurableBot("B1", "COOPERATE");
        ConfigurableBot bot2 = new ConfigurableBot("B2", "COOPERATE");
        int[] scores = game.run(bot1, bot2);
        assertEquals(3, scores[0]);
        assertEquals(3, scores[1]);
    }

    @Test
    public void testBothDefectScoring() {
        PrisonersDilemma game = new PrisonersDilemma(1);
        ConfigurableBot bot1 = new ConfigurableBot("B1", "DEFECT");
        ConfigurableBot bot2 = new ConfigurableBot("B2", "DEFECT");
        int[] scores = game.run(bot1, bot2);
        assertEquals(1, scores[0]);
        assertEquals(1, scores[1]);
    }
}
