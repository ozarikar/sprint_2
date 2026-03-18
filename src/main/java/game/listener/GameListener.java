package game.listener;

// interface for all game listeners (Observer pattern).
public interface GameListener {
    // Called when the listener should update based on game events.
    void update();
}