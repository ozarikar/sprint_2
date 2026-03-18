package game;

import robot.Robot;
import java.util.Arrays;

/**
 * Implementation of the Prisoner's Dilemma game.
 * Scoring matrix:
 * Both COOPERATE: (3, 3)
 * Both DEFECT: (1, 1)
 * One COOPERATE, one DEFECT: (0, 5) - defector gets 5, cooperator gets 0
 */
public class PrisonersDilemma extends Game {
    private int maxRounds;
    private Robot player1;
    private Robot player2;
    
    public PrisonersDilemma(int maxRounds) {
        super();
        this.maxRounds = maxRounds;
        this.actions = Arrays.asList("COOPERATE", "DEFECT");
    }
    
    @Override
    public int[] run(Robot robot1, Robot robot2) {
        this.player1 = robot1;
        this.player2 = robot2;
        return super.run(robot1, robot2);
    }
    
    @Override
    protected boolean checkEnd() {
        return currentRound >= maxRounds;
    }
    
    @Override
    protected int[] giveOutcome(String action1, String action2) {
        int score1 = 0;
        int score2 = 0;
        
        if (action1.equals("COOPERATE") && action2.equals("COOPERATE")) {
            score1 = 3;
            score2 = 3;
        } else if (action1.equals("DEFECT") && action2.equals("DEFECT")) {
            score1 = 1;
            score2 = 1;
        } else if (action1.equals("COOPERATE") && action2.equals("DEFECT")) {
            score1 = 0;
            score2 = 5;
        } else if (action1.equals("DEFECT") && action2.equals("COOPERATE")) {
            score1 = 5;
            score2 = 0;
        }
        
        return new int[] { score1, score2 };
    }
    
    @Override
    public String getOpponentName(Robot robot) {
        if (robot == player1) {
            return player2.getName();
        } else if (robot == player2) {
            return player1.getName();
        }
        return null;
    }
}