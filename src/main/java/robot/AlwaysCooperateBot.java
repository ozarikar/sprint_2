package robot;

// A robot that always cooperates.

public class AlwaysCooperateBot extends Robot {
    private int score;
    
    public AlwaysCooperateBot(String name) {
        super(name);
        this.score = 0;
    }
    
    @Override
    public String getAction() {
        return "COOPERATE";
    }
    
    @Override
    public int getScore() {
        return score;
    }
    
    @Override
    public void giveOutcome(String opponentName, String myAction, 
                           String opponentAction, int myScore) {
        super.giveOutcome(opponentName, myAction, opponentAction, myScore);
        this.score += myScore;
    }
}