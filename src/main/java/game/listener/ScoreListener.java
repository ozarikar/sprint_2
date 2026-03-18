package game.listener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// Observer that logs scores after each round.
public class ScoreListener implements GameListener {
    private String player1Name;
    private String player2Name;
    private int score1;
    private int score2;
    private int roundNumber;
    private PrintWriter writer;
    
    public ScoreListener(String filename) {
        try {
            writer = new PrintWriter(new FileWriter(filename, true));
            roundNumber = 0;
        } catch (IOException e) {
            System.err.println("Error creating ScoreListener: " + e.getMessage());
        }
    }
    
    // Updates the listener with score information.
    public void setScoreData(String p1Name, String p2Name, int s1, int s2) {
        this.player1Name = p1Name;
        this.player2Name = p2Name;
        this.score1 = s1;
        this.score2 = s2;
        this.roundNumber++;
    }
    
    @Override
    public void update() {
        if (writer != null) {
            writer.println(String.format("Round %d Scores: %s = %d, %s = %d",
                roundNumber, player1Name, score1, player2Name, score2));
            writer.flush();
        }
    }
    
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}