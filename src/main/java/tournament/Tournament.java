package tournament;

import robot.Robot;
import game.Game;
import java.util.List;
import java.util.ArrayList;

/**
 * Abstract class for tournaments.
 * Template Method pattern.
 */
public abstract class Tournament {
    protected Game game;
    protected List<Robot> players;
    protected String name;
    protected ArrayList<History> history;

    // Backward-compatible constructor
    public Tournament(Game game) {
        this(game, "Unnamed Tournament");
    }

    public Tournament(Game game, String name) {
        this.game = game;
        this.name = name;
        this.players = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    // Runs all matches in the tournament and prints standings.
    public void runTournament() {
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
    }

    // Returns true when the tournament is no longer accepting registrations.
    public abstract boolean checkEnd();

    // Returns true while the tournament is still accepting new players.
    public abstract boolean isOpen();

    /**
     * Abstract method that determines matchups for the tournament.
     * @return a list of matchups, each as [player1Index, player2Index]
     */
    protected abstract List<int[]> getBracket();

    public void addPlayer(Robot robot) {
        players.add(robot);
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
