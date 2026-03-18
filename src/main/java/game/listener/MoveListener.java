package game.listener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// Observer that logs each move from in the game.

public class MoveListener implements GameListener {
    private String player1Name;
    private String player2Name;
    private String action1;
    private String action2;
    private int roundNumber;
    private PrintWriter writer;
    
    public MoveListener(String filename) {
        try {
            writer = new PrintWriter(new FileWriter(filename, true));
            roundNumber = 0;
        } catch (IOException e) {
            System.err.println("Error creating MoveListener: " + e.getMessage());
        }
    }
    
    // Updates listener with move information.
    public void setMoveData(String p1Name, String p2Name, String a1, String a2) {
        this.player1Name = p1Name;
        this.player2Name = p2Name;
        this.action1 = a1;
        this.action2 = a2;
        this.roundNumber++;
    }
    
    @Override
    public void update() {
        if (writer != null) {
            writer.println(String.format("Round %d: %s chose %s, %s chose %s",
                roundNumber, player1Name, action1, player2Name, action2));
            writer.flush();
        }
    }
    
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}