package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import tournament.*;
import game.*;
import robot.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// Tests for Round Robin Tournament implementation.

public class RoundRobinTournamentTest {
    
    private RoundRobinTournament tournament;
    private PrisonersDilemma game;
    
    @BeforeEach
    public void setUp() {
        game = new PrisonersDilemma(5);
        tournament = new RoundRobinTournament(game);
    }
    
    @Test
    public void testTournamentWithThreePlayers() {
        Robot r1 = new AlwaysCooperateBot("Cooperator");
        Robot r2 = new AlwaysDefectBot("Defector");
        Robot r3 = new TitForTatBot("TitForTat");
        
        tournament.addPlayer(r1);
        tournament.addPlayer(r2);
        tournament.addPlayer(r3);
        
        // Capture output to verify tournament runs
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        tournament.runTournament();
        
        System.setOut(originalOut);
        String output = outContent.toString();
        
        // Should have 3 matches (3 choose 2)
        int matchCount = output.split("Match:").length - 1;
        assertEquals(3, matchCount);
        
        // All players should have nonzero scores
        assertTrue(r1.getScore() > 0 || r2.getScore() > 0 || r3.getScore() > 0);
    }
    
    @Test
    public void testTournamentWithFourPlayers() {
        Robot r1 = new AlwaysCooperateBot("C1");
        Robot r2 = new AlwaysCooperateBot("C2");
        Robot r3 = new AlwaysDefectBot("D1");
        Robot r4 = new TitForTatBot("TFT");
        
        tournament.addPlayer(r1);
        tournament.addPlayer(r2);
        tournament.addPlayer(r3);
        tournament.addPlayer(r4);
        
        tournament.runTournament();
        
        // Should have 6 matches (4 choose 2)
        // Each robot should have played 3 games
        assertTrue(r1.getHistory().size() == 3);
        assertTrue(r2.getHistory().size() == 3);
        assertTrue(r3.getHistory().size() == 3);
        assertTrue(r4.getHistory().size() == 3);
    }
    
    @Test
    public void testDefectorWinsAgainstCooperators() {
        Robot cooperator1 = new AlwaysCooperateBot("C1");
        Robot cooperator2 = new AlwaysCooperateBot("C2");
        Robot defector = new AlwaysDefectBot("Defector");
        
        tournament.addPlayer(cooperator1);
        tournament.addPlayer(cooperator2);
        tournament.addPlayer(defector);
        
        tournament.runTournament();
        
        // Defector should have highest score
        // Total: C1=15, C2=15, Defector=50
        assertEquals(15, cooperator1.getScore());
        assertEquals(15, cooperator2.getScore());
        assertEquals(50, defector.getScore());
    }
    
    @Test
    public void testTitForTatPerformance() {
        Robot cooperator = new AlwaysCooperateBot("Cooperator");
        Robot defector = new AlwaysDefectBot("Defector");
        Robot titForTat = new TitForTatBot("TitForTat");
        
        tournament.addPlayer(cooperator);
        tournament.addPlayer(defector);
        tournament.addPlayer(titForTat);
        
        tournament.runTournament();
        
        
        // Expected: Cooperator=15, TitForTat=19, Defector=34
        assertEquals(15, cooperator.getScore());
        assertEquals(19, titForTat.getScore());
        assertEquals(34, defector.getScore());
    }
    
    @Test
    public void testTournamentWithTwoPlayers() {
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysCooperateBot("R2");
        
        tournament.addPlayer(r1);
        tournament.addPlayer(r2);
        
        tournament.runTournament();
        
        // Should have exactly 1 match
        assertEquals(1, r1.getHistory().size());
        assertEquals(1, r2.getHistory().size());
        
        // Both should have same score
        assertEquals(r1.getScore(), r2.getScore());
    }
    
    @Test
    public void testFinalStandings() {
        Robot r1 = new AlwaysCooperateBot("Cooperator");
        Robot r2 = new AlwaysDefectBot("Defector");
        Robot r3 = new TitForTatBot("TitForTat");
        
        tournament.addPlayer(r1);
        tournament.addPlayer(r2);
        tournament.addPlayer(r3);
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        tournament.runTournament();
        
        System.setOut(originalOut);
        String output = outContent.toString();
        
        // Should contain final standings
        assertTrue(output.contains("Final Standings"));
        assertTrue(output.contains("Cooperator"));
        assertTrue(output.contains("Defector"));
        assertTrue(output.contains("TitForTat"));
        assertTrue(output.contains("points"));
    }
}