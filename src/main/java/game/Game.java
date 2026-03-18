package game;

import robot.Robot;
import game.listener.MoveListener;
import game.listener.ScoreListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for all games.
 * Implemented Observer pattern as the Subject and Template Method pattern for game-specific rules.
 */
public abstract class Game {
    protected List<String> actions;
    protected List<MoveListener> moveListeners;
    protected List<ScoreListener> scoreListeners;
    protected int currentRound;
    
    public Game() {
        this.actions = new ArrayList<>();
        this.moveListeners = new ArrayList<>();
        this.scoreListeners = new ArrayList<>();
        this.currentRound = 0;
    }
    
    // Template method: runs a complete game between two robots.
    
    public int[] run(Robot robot1, Robot robot2) {
        currentRound = 0;
        
        // Notify any opponent-aware robots of their current opponent
        if (robot1 instanceof robot.OpponentAware) {
            ((robot.OpponentAware) robot1).setCurrentOpponent(robot2.getName());
        }
        if (robot2 instanceof robot.OpponentAware) {
            ((robot.OpponentAware) robot2).setCurrentOpponent(robot1.getName());
        }
        
        while (!checkEnd()) {
            currentRound++;
            
            // Get actions from both robots
            String action1 = robot1.getAction();
            String action2 = robot2.getAction();
            
            // Notify move listeners
            notifyMoveListeners(robot1.getName(), robot2.getName(), action1, action2);
            
            // Calculating outcomes based on game rules
            int[] roundScores = giveOutcome(action1, action2);
            
            // Updating robots with outcomes
            robot1.giveOutcome(robot2.getName(), action1, action2, roundScores[0]);
            robot2.giveOutcome(robot1.getName(), action2, action1, roundScores[1]);
            
            // Notify score listeners
            notifyScoreListeners(robot1.getName(), robot2.getName(), 
                                roundScores[0], roundScores[1]);
        }
        
        // Returns final scores
        return new int[] { robot1.getScore(), robot2.getScore() };
    }
    
    // Abstract method to determine when the game ends.

    protected abstract boolean checkEnd();
    
    
    
    /**
     * Abstract method: calculates scores based on actions.
     * @param action1 action of player 1 same with action2, @return array of scores [player1Score, player2Score]
     */
    protected abstract int[] giveOutcome(String action1, String action2);
    
    // Gets the opponent's name for a given robot.
    public abstract String getOpponentName(Robot robot);
    
    // Observer pattern methods
    public void addMoveListener(MoveListener listener) {
        moveListeners.add(listener);
    }
    
    public void removeMoveListener(MoveListener listener) {
        moveListeners.remove(listener);
    }
    
    public void addScoreListener(ScoreListener listener) {
        scoreListeners.add(listener);
    }
    
    public void removeScoreListener(ScoreListener listener) {
        scoreListeners.remove(listener);
    }
    
    protected void notifyMoveListeners(String p1, String p2, String a1, String a2) {
        for (MoveListener listener : moveListeners) {
            listener.setMoveData(p1, p2, a1, a2);
            listener.update();
        }
    }
    
    protected void notifyScoreListeners(String p1, String p2, int s1, int s2) {
        for (ScoreListener listener : scoreListeners) {
            listener.setScoreData(p1, p2, s1, s2);
            listener.update();
        }
    }
    
    public List<String> getActions() {
        return new ArrayList<>(actions);
    }
}