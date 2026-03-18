package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import game.*;
import game.listener.*;
import robot.*;
import tournament.*;
import java.io.*;
import java.nio.file.*;

// Testing the interaction between all components.

public class IntegrationTest {
    
    private String moveFile = "integration_moves.log";
    private String scoreFile = "integration_scores.log";
    
    @AfterEach
    public void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(moveFile));
            Files.deleteIfExists(Paths.get(scoreFile));
        } catch (IOException e) {
            // Ignore
        }
    }
    
    @Test
    public void testCompleteSystemWithLogging() {
        // Creates game
        PrisonersDilemma game = new PrisonersDilemma(10);
        
        // Sets up logging
        MoveListener moveListener = new MoveListener(moveFile);
        ScoreListener scoreListener = new ScoreListener(scoreFile);
        game.addMoveListener(moveListener);
        game.addScoreListener(scoreListener);
        
        // Creates robots
        Robot cooperator = new AlwaysCooperateBot("Cooperator");
        Robot defector = new AlwaysDefectBot("Defector");
        Robot titForTat = new TitForTatBot("TitForTat");
        
        // Creates tournament
        Tournament tournament = new RoundRobinTournament(game);
        tournament.addPlayer(cooperator);
        tournament.addPlayer(defector);
        tournament.addPlayer(titForTat);
        
        // Runs tournament
        tournament.runTournament();
        
        // Cleans up
        moveListener.close();
        scoreListener.close();
        
        // Verifying if logging files exist
        assertTrue(new File(moveFile).exists());
        assertTrue(new File(scoreFile).exists());
        
        // Verify if scores are calculated
        assertTrue(cooperator.getScore() > 0);
        assertTrue(defector.getScore() > 0);
        assertTrue(titForTat.getScore() > 0);
        
        // Verify if history is tracked
        assertEquals(2, cooperator.getHistory().size());
        assertEquals(2, defector.getHistory().size());
        assertEquals(2, titForTat.getHistory().size());
    }
    
    @Test
    public void testSystemWithoutLogging() {
        // System should work without listeners
        PrisonersDilemma game = new PrisonersDilemma(5);
        
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysDefectBot("R2");
        
        Tournament tournament = new RoundRobinTournament(game);
        tournament.addPlayer(r1);
        tournament.addPlayer(r2);
        
        // Should complete without errors
        tournament.runTournament();
        
        assertEquals(0, r1.getScore());
        assertEquals(25, r2.getScore());
    }
    
    @Test
    public void testDifferentGameLengths() {
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysCooperateBot("R2");
        
        // Test short game
        PrisonersDilemma shortGame = new PrisonersDilemma(1);
        Tournament shortTournament = new RoundRobinTournament(shortGame);
        shortTournament.addPlayer(r1);
        shortTournament.addPlayer(r2);
        shortTournament.runTournament();

        assertEquals(3, r1.getScore());
        assertEquals(3, r2.getScore());

        // Reset robots
        r1 = new AlwaysCooperateBot("R1");
        r2 = new AlwaysCooperateBot("R2");

        // Test long game
        PrisonersDilemma longGame = new PrisonersDilemma(100);
        Tournament longTournament = new RoundRobinTournament(longGame);
        longTournament.addPlayer(r1);
        longTournament.addPlayer(r2);
        longTournament.runTournament();
        
        assertEquals(300, r1.getScore());
        assertEquals(300, r2.getScore());
    }
    
    @Test
    public void testRobotBehaviorConsistency() {
        // Verifying that robot strategies are consistent across multiple games
        AlwaysDefectBot defector = new AlwaysDefectBot("Defector");
        
        for (int i = 0; i < 10; i++) {
            assertEquals("DEFECT", defector.getAction());
        }
        
        AlwaysCooperateBot cooperator = new AlwaysCooperateBot("Cooperator");
        for (int i = 0; i < 10; i++) {
            assertEquals("COOPERATE", cooperator.getAction());
        }
    }
    
    @Test
    public void testTournamentScalability() {
        PrisonersDilemma game = new PrisonersDilemma(5);
        Tournament tournament = new RoundRobinTournament(game);
        
        // Add 5 players
        for (int i = 0; i < 5; i++) {
            tournament.addPlayer(new AlwaysCooperateBot("C" + i));
        }
        
        // Should handle 10 matches
        tournament.runTournament();
        
        assertTrue(true); // If yes, then the tournament completed successfully
    }
}