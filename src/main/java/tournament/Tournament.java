package tournament;

import robot.Robot;
import game.Game;
import java.util.List;
import java.util.ArrayList;

/**
 * Abstract class for tournaments.
 * Template Method pattern.
 *
 * Lifecycle (Sprint 2 R-SITS):
 *   REGISTRATION -> RUNNING -> FINISHED
 *
 * During REGISTRATION the tournament accepts new players via addPlayer() but
 * does NOT run any matches. closeRegistration() ends the phase explicitly
 * (matching the PDF's "you can use a method to end the registration phase").
 * runTournament() may only be called once registration is closed.
 */
public abstract class Tournament {

    public enum Phase { REGISTRATION, RUNNING, FINISHED }

    protected Game game;
    protected List<Robot> players;
    protected String name;
    protected ArrayList<History> history;
    private Phase phase;

    // Backward-compatible constructor
    public Tournament(Game game) {
        this(game, "Unnamed Tournament");
    }

    public Tournament(Game game, String name) {
        this.game = game;
        this.name = name;
        this.players = new ArrayList<>();
        this.history = new ArrayList<>();
        this.phase = Phase.REGISTRATION;
    }

    /**
     * Runs all matches in the tournament and prints standings.
     * If still in REGISTRATION, the phase is auto-closed first — explicit
     * closeRegistration() is still supported (idempotent), but callers that
     * fill the bracket and immediately run don't need a second method call.
     */
    public void runTournament() {
        if (phase == Phase.REGISTRATION) {
            closeRegistration();
        }
        if (phase == Phase.FINISHED) {
            throw new IllegalStateException(
                "Tournament '" + name + "' has already finished.");
        }
        // phase == RUNNING from here on
        List<int[]> bracket = getBracket();

        for (int[] matchup : bracket) {
            Robot robot1 = players.get(matchup[0]);
            Robot robot2 = players.get(matchup[1]);

            int before1 = robot1.getScore();
            int before2 = robot2.getScore();

            System.out.println("\nMatch: " + robot1.getName() + " vs " + robot2.getName());
            game.run(robot1, robot2);

            int match1 = robot1.getScore() - before1;
            int match2 = robot2.getScore() - before2;

            addHistory(new History(robot1.getName(), robot2.getName(), "N/A", "N/A", match1, match2));

            System.out.println("Match Score - " + robot1.getName() + ": " + match1
                             + ", " + robot2.getName() + ": " + match2);
        }

        printFinalStandings();
        phase = Phase.FINISHED;
    }

    /**
     * Returns true when the tournament's *registration* phase has been
     * explicitly closed AND the tournament has space for more players in its
     * format. Kept abstract because individual formats (round-robin, knockout,
     * etc.) might still cap players even while in REGISTRATION.
     *
     * Historically named "checkEnd"; in Sprint 2 it now means "is the
     * registration window done?" (either explicitly closed or full).
     */
    public abstract boolean checkEnd();

    /**
     * @return true while the tournament is in the REGISTRATION phase AND
     *         the format still has room for another player. Returning false
     *         can mean either "registration was explicitly closed" or
     *         "the format is full".
     */
    public abstract boolean isOpen();

    /**
     * Abstract method that determines matchups for the tournament.
     * @return a list of matchups, each as [player1Index, player2Index]
     */
    protected abstract List<int[]> getBracket();

    public void addPlayer(Robot robot) {
        if (phase != Phase.REGISTRATION) {
            throw new IllegalStateException(
                "Cannot add player '" + robot.getName() + "' to '" + name
              + "': tournament is no longer in registration phase (current=" + phase + ").");
        }
        players.add(robot);
    }

    /**
     * Ends the registration phase explicitly. After this call:
     *   - addPlayer() will throw
     *   - isOpen() will return false
     *   - runTournament() may be called
     * Idempotent: calling twice is a no-op.
     */
    public void closeRegistration() {
        if (phase == Phase.REGISTRATION) {
            phase = Phase.RUNNING;
        }
        // If we're already RUNNING or FINISHED, do nothing — closing twice
        // is harmless. Throwing here would punish a server that retries
        // close requests over the network.
    }

    public Phase getPhase() {
        return phase;
    }

    /** True only while accepting new registrations. */
    public boolean isAcceptingRegistrations() {
        return phase == Phase.REGISTRATION;
    }

    public List<Robot> getPlayers() {
        return new ArrayList<>(players);
    }

    public void addHistory(History h) {
        history.add(h);
    }

    public String getName() {
        return name;
    }

    public ArrayList<History> getHistory() {
        return new ArrayList<>(history);
    }

    protected void printFinalStandings() {
        System.out.println("\n ## Final Standings ##");
        List<Robot> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((r1, r2) -> r2.getScore() - r1.getScore());

        int rank = 1;
        for (Robot robot : sortedPlayers) {
            System.out.println(rank + ". " + robot.getName() + ": " + robot.getScore() + " points");
            rank++;
        }
    }
}
