package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import game.*;
import robot.*;

public class GameCopyBotTest {

    @Test
    public void testTitForTatBotVsCooperator() {
        PrisonersDilemma game = new PrisonersDilemma(5);
        TitForTatBot TitForTatBot = new TitForTatBot("CopyBot");
        AlwaysCooperateBot cooperator = new AlwaysCooperateBot("Cooperator");

        int[] scores = game.run(TitForTatBot, cooperator);
        // Both cooperate all 5 rounds: 5 * 3 = 15 each
        assertEquals(15, scores[0]);
        assertEquals(15, scores[1]);
    }

    @Test
    public void testTitForTatBotVsDefector() {
        PrisonersDilemma game = new PrisonersDilemma(5);
        TitForTatBot copyBot = new TitForTatBot("TitForTatBot");
        AlwaysDefectBot defector = new AlwaysDefectBot("Defector");

        int[] scores = game.run(copyBot, defector);
        // Round 1: CopyBot cooperates, Defector defects → TitForTatBot: 0, Defector: 5
        // Rounds 2-5: both defect → 4 * 1 = 4 each
        assertEquals(4, scores[0]);
        assertEquals(9, scores[1]);
    }


    @Test
    public void testGameRecognizesCopyBotAsOpponentAware() {
        // After game.run(), CopyBot should have had setCurrentOpponent called on it,
        // which means it tracked history with the correct opponent name.
        PrisonersDilemma game = new PrisonersDilemma(3);
        TitForTatBot TitForTatBot = new TitForTatBot("TitForTatBot");
        AlwaysCooperateBot cooperator = new AlwaysCooperateBot("Cooperator");

        game.run(TitForTatBot, cooperator);

        // CopyBot should have 3 rounds of history with "Cooperator"
        assertEquals(3, TitForTatBot.getHistory().get("Cooperator").size());
    }
}
