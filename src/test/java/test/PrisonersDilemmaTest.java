package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import game.*;
import robot.*;

// Testing for Prisoner's Dilemma game.

public class PrisonersDilemmaTest {

    private PrisonersDilemma game;
    private Robot cooperator;
    private Robot defector;

    @BeforeEach
    public void setUp() {
        game = new PrisonersDilemma(5);
        cooperator = new AlwaysCooperateBot("Cooperator");
        defector = new AlwaysDefectBot("Defector");
    }

    @Test
    public void testGameActions() {
        assertTrue(game.getActions().contains("COOPERATE"));
        assertTrue(game.getActions().contains("DEFECT"));
        assertEquals(2, game.getActions().size());
    }

    @Test
    public void testBothCooperate() {
        int[] scores = game.run(cooperator, new AlwaysCooperateBot("Cooperator2"));
        // Both cooperate for 5 rounds: 5 * 3 = 15 each
        assertEquals(15, scores[0]);
        assertEquals(15, scores[1]);
    }

    @Test
    public void testBothDefect() {
        int[] scores = game.run(defector, new AlwaysDefectBot("Defector2"));
        // Both defect for 5 rounds: 5 * 1 = 5 each
        assertEquals(5, scores[0]);
        assertEquals(5, scores[1]);
    }

    @Test
    public void testCooperateVsDefect() {
        int[] scores = game.run(cooperator, defector);
        // Cooperator always gets 0, __Defector__ always gets 5
        // 5 rounds: Cooperator = 0, __Defector__ = 25
        assertEquals(0, scores[0]);
        assertEquals(25, scores[1]);
    }
    
    @Test
    public void testGetOpponentName() {
        Robot r1 = new AlwaysCooperateBot("Alice");
        Robot r2 = new AlwaysDefectBot("Bob");
        
        game.run(r1, r2);
        
        assertEquals("Bob", game.getOpponentName(r1));
        assertEquals("Alice", game.getOpponentName(r2));
        
        // Robot not in the game should return null
        Robot r3 = new AlwaysCooperateBot("Charlie");
        assertNull(game.getOpponentName(r3));
    }

    @Test
    public void testTitForTatVsDefector() {
        TitForTatBot tft = new TitForTatBot("TitForTat");
        int[] scores = game.run(tft, defector);

        // Round 1: TFT cooperates, __Defector__ defects -> TFT: 0, __Defector__: 5
        // Rounds 2-5: Both defect -> TFT: 4*1=4, __Defector__: 4*1=4
        // Total: TFT: 4, __Defector__: 9
        assertEquals(4, scores[0]);
        assertEquals(9, scores[1]);
    }

    @Test
    public void testTitForTatVsCooperator() {
        TitForTatBot tft = new TitForTatBot("TitForTat");
        int[] scores = game.run(tft, cooperator);

        // Both cooperate all 5 rounds
        assertEquals(15, scores[0]);
        assertEquals(15, scores[1]);
    }

    @Test
    public void testDifferentRoundCounts() {
        // Creating FRESH robots for each game
        Robot coop1 = new AlwaysCooperateBot("C1");
        Robot coop2 = new AlwaysCooperateBot("C2");

        PrisonersDilemma shortGame = new PrisonersDilemma(3);
        int[] scores = shortGame.run(coop1, coop2);
        assertEquals(9, scores[0]);

        // Creating NEW robots for second game
        Robot coop3 = new AlwaysCooperateBot("C3");
        Robot coop4 = new AlwaysCooperateBot("C4");

        PrisonersDilemma longGame = new PrisonersDilemma(10);
        scores = longGame.run(coop3, coop4);
        assertEquals(30, scores[0]);
    }

    @Test
    public void testRobotHistoryAfterGame() {
        Robot robot1 = new AlwaysCooperateBot("R1");
        Robot robot2 = new AlwaysDefectBot("R2");

        game.run(robot1, robot2);

        assertEquals(5, robot1.getHistory().get("R2").size());
        assertEquals(5, robot2.getHistory().get("R1").size());
    }
}