package robot;


 // A robot that cooperates on the first move, then mimics the opponent's last action.

public class TitForTatBot extends Robot implements OpponentAware {
    private int score;
    private String currentOpponent;
    
    public TitForTatBot(String name) {
        super(name);
        this.score = 0;
    }
    
    //Since getAction() doesn't take parameters. We are Setting the current opponent for this round here.
    
    public void setCurrentOpponent(String opponentName) {
        this.currentOpponent = opponentName;
    }
    
    @Override
    public String getAction() {
        if (currentOpponent == null) {
            return "COOPERATE"; // default to cooperate if no opponent set
        }
        
        String lastAction = getLastOpponentAction(currentOpponent);
        if (lastAction == null) {
            return "COOPERATE"; // cooperate on first encounter
        }
        return lastAction; // mimic opponent's last move
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