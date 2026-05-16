package tournament;

import game.Game;
import java.util.ArrayList;
import java.util.List;

/**
 * Round Robin tournament format.
 * Every player plays against every other player exactly once.
 * Supports a maxPlayers cap so the registration phase has an explicit end
 * state for formats that need it.
 */
public class RoundRobinTournament extends Tournament {
    private final int maxPlayers;

    // Backward-compatible constructor — unlimited players
    public RoundRobinTournament(Game game) {
        super(game);
        this.maxPlayers = Integer.MAX_VALUE;
    }

    public RoundRobinTournament(Game game, String name, int maxPlayers) {
        super(game, name);
        if (maxPlayers < 2) {
            throw new IllegalArgumentException(
                "maxPlayers must be >= 2 for a round-robin tournament, got " + maxPlayers);
        }
        this.maxPlayers = maxPlayers;
    }

    /**
     * True while the tournament is in registration phase AND has room.
     * The professor's user story "client can register for any tournament"
     * means the server must answer this exactly.
     */
    @Override
    public boolean isOpen() {
        return isAcceptingRegistrations() && players.size() < maxPlayers;
    }

    /**
     * Registration phase is over when either the format is full or
     * the server has explicitly closed registration.
     */
    @Override
    public boolean checkEnd() {
        return !isOpen();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    protected List<int[]> getBracket() {
        List<int[]> matchups = new ArrayList<>();
        // Generate all unique pairs
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                matchups.add(new int[] { i, j });
            }
        }
        return matchups;
    }
}
