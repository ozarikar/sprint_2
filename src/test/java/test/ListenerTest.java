package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import game.*;
import game.listener.*;
import robot.*;
import java.io.*;
import java.nio.file.*;

// Testing for the Observer pattern implementation (listeners).

public class ListenerTest {
    
    private PrisonersDilemma game;
    private MoveListener moveListener;
    private ScoreListener scoreListener;
    private String moveFile = "test_moves.log";
    private String scoreFile = "test_scores.log";
    
    @BeforeEach
    public void setUp() {
        game = new PrisonersDilemma(3);
        moveListener = new MoveListener(moveFile);
        scoreListener = new ScoreListener(scoreFile);
    }
    
    @AfterEach
    public void tearDown() {
        moveListener.close();
        scoreListener.close();
        
        // Cleans up test files
        try {
            Files.deleteIfExists(Paths.get(moveFile));
            Files.deleteIfExists(Paths.get(scoreFile));
        } catch (IOException e) {

        }
    }
    
    @Test
    public void testMoveListenerRegistration() {
        game.addMoveListener(moveListener);
        
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysDefectBot("R2");
        game.run(r1, r2);
        
        moveListener.close();
        
        // Check if that file was created and has content
        File file = new File(moveFile);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
    
    @Test
    public void testScoreListenerRegistration() {
        game.addScoreListener(scoreListener);
        
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysDefectBot("R2");
        game.run(r1, r2);
        
        scoreListener.close();
        
        // Check if that file was created and has content
        File file = new File(scoreFile);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
    
    @Test
    public void testMoveListenerContent() throws IOException {
        game.addMoveListener(moveListener);
        
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysDefectBot("R2");
        game.run(r1, r2);
        
        moveListener.close();
        
        String content = new String(Files.readAllBytes(Paths.get(moveFile)));
        
        // Should have 3 rounds logged
        assertTrue(content.contains("Round 1"));
        assertTrue(content.contains("Round 2"));
        assertTrue(content.contains("Round 3"));
        
        // Should contain robot names and actions
        assertTrue(content.contains("R1"));
        assertTrue(content.contains("R2"));
        assertTrue(content.contains("COOPERATE"));
        assertTrue(content.contains("DEFECT"));
    }
    
    @Test
    public void testScoreListenerContent() throws IOException {
        game.addScoreListener(scoreListener);
        
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysDefectBot("R2");
        game.run(r1, r2);
        
        scoreListener.close();
        
        String content = new String(Files.readAllBytes(Paths.get(scoreFile)));
        
        // Should have 3 rounds logged
        assertTrue(content.contains("Round 1 Scores"));
        assertTrue(content.contains("Round 2 Scores"));
        assertTrue(content.contains("Round 3 Scores"));
        
        // R1 always gets 0 vs defector, R2 always gets 5
        assertTrue(content.contains("R1 = 0"));
        assertTrue(content.contains("R2 = 5"));
    }
    
    @Test
    public void testMultipleListeners() {
        MoveListener moveListener2 = new MoveListener("test_moves2.log");
        
        game.addMoveListener(moveListener);
        game.addMoveListener(moveListener2);
        game.addScoreListener(scoreListener);
        
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysCooperateBot("R2");
        game.run(r1, r2);
        
        moveListener2.close();
        
        // Both move files should exist
        assertTrue(new File(moveFile).exists());
        assertTrue(new File("test_moves2.log").exists());
        assertTrue(new File(scoreFile).exists());
        
        try {
            Files.deleteIfExists(Paths.get("test_moves2.log"));
        } catch (IOException e) {
           
        }
    }
    
    @Test
    public void testListenerRemoval() {
        game.addMoveListener(moveListener);
        game.removeMoveListener(moveListener);
        
        Robot r1 = new AlwaysCooperateBot("R1");
        Robot r2 = new AlwaysCooperateBot("R2");
        game.run(r1, r2);
        
        moveListener.close();
        
        // File should be empty or not exist since listener was removed
        File file = new File(moveFile);
        if (file.exists()) {
            assertEquals(0, file.length());
        }
    }
}