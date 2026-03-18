package tournament;

// Java Record capturing the result of a single match between two players.
public record History(
    String player1,
    String player2,
    String move1,
    String move2,
    int result1,
    int result2
) {}
