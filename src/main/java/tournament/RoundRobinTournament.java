package tournament;

import game.Game;
import java.util.ArrayList;
import java.util.List;

/**
 * Round Robin tournament format.
 * Every player plays against every other player exactly once.
 * Supports a maxPlayers cap for server-side registration management.
 */
public class RoundRobinTournament extends Tournament {
    private int maxPlayers;

    // Backward-compatible constructor — unlimited players
    public RoundRobinTournament(Game game) {
        super(game);
        this.maxPlayers = Integer.MAX_VALUE;
    }

    public RoundRobinTournament(Game game, String name, int maxPlayers) {
        super(game, name);
        this.maxPlayers = maxPlayers;
    }

    @Override
    public boolean isOpen() {
        return players.size() < maxPlayers;
    }

    // Returns true when the tournament is full (registration closed).
    @Override
    public boolean checkEnd() {
        return !isOpen();
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
