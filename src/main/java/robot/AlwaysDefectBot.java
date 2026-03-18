package robot;

// A robot that always defects.

public class AlwaysDefectBot extends Robot {
    private int score;
    
    public AlwaysDefectBot(String name) {
        super(name);
        this.score = 0;
    }
    
    @Override
    public String getAction() {
        return "DEFECT";
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