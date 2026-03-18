package robot;

/**
 * Interface for robots that need to know their current opponent
 * before getAction() is called.
 * Avoids instanceof chains in Game.java — any robot that copies
 * opponent behavior can implement this instead.
 */
public interface OpponentAware {
    void setCurrentOpponent(String opponentName);
}
