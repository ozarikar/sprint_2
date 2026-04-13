package robot;

import java.util.Scanner;

/**
 * A robot controlled by a human player.
 * getAction() blocks and reads from stdin — the human types COOPERATE or DEFECT.
 * When used with NetworkedTournamentClient, the HTTP thread blocks until input arrives.
 */
public class HumanBot extends Robot {
    private int score;
    private final Scanner scanner;

    public HumanBot(String name) {
        this(name, new Scanner(System.in));
    }

    // Public constructor for testing — allows injecting a custom Scanner
    public HumanBot(String name, Scanner scanner) {
        super(name);
        this.score = 0;
        this.scanner = scanner;
    }

    @Override
    public String getAction() {
        System.out.print("[HumanBot " + getName() + "] Enter action (COOPERATE or DEFECT): ");
        String input = scanner.nextLine().trim().toUpperCase();
        if (input.equals("DEFECT")) {
            return "DEFECT";
        }
        return "COOPERATE";
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
        System.out.println("[HumanBot] vs " + opponentName + ": you played " + myAction +
            ", they played " + opponentAction + ", you scored " + myScore);
    }
}
