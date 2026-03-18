package robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Abstract base class for all robotic participants.

public abstract class Robot {
    private String name;
    // History maps opponent names to their interaction history
    private Map<String, List<String>> history;
    
    public Robot(String name) {
        this.name = name;
        this.history = new HashMap<>();
    }
    
    /**
     * Subclasses implement this to define their strategy.
     * @return the action as a String (example: "COOPERATE" or "DEFECT")
     */
    public abstract String getAction();
    
    // Gets the current score (for tracking purposes), @return current score
    public abstract int getScore();
    
    /**
     * Updates the robot with the outcome of a round.
     * @param opponentName name of the opponent, @param myAction action this robot took
     * @param opponentAction action the opponent took, @param myScore score this robot received
     */
    public void giveOutcome(String opponentName, String myAction, 
                           String opponentAction, int myScore) {
        if (!history.containsKey(opponentName)) {
            history.put(opponentName, new ArrayList<>());
        }
        String interaction = myAction + "," + opponentAction + "," + myScore;
        history.get(opponentName).add(interaction);
    }
    
    /**
     * Gets the interaction history with a specific opponent.
     * @param opponentName name of the opponent, @return list of past interactions
     */
    protected List<String> getHistoryWith(String opponentName) {
        return history.getOrDefault(opponentName, new ArrayList<>());
    }
    
    /**
     * Gets the last action taken by an opponent.
     * @param opponentName name of the opponent, @return the last action, or null if no history
     */
    protected String getLastOpponentAction(String opponentName) {
        List<String> opponentHistory = getHistoryWith(opponentName);
        if (opponentHistory.isEmpty()) {
            return null;
        }
        String lastInteraction = opponentHistory.get(opponentHistory.size() - 1);
        String[] parts = lastInteraction.split(",");
        return parts[1]; // opponent action is the second element
    }
    
    public String getName() {
        return name;
    }
    
    public Map<String, List<String>> getHistory() {
        return new HashMap<>(history);
    }
}