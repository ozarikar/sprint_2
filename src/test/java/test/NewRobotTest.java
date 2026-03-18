package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import robot.*;
import tournament.History;

public class NewRobotTest {

    // --- CollabBot ---

    @Test
    public void testCollabBotAlwaysCooperates() {
    	TitForTatBot bot = new TitForTatBot("C");
        assertEquals("COOPERATE", bot.getAction());
        bot.giveOutcome("X", "COOPERATE", "DEFECT", 0);
        assertEquals("COOPERATE", bot.getAction()); // unchanged after bad outcome
    }

    @Test
    public void testCollabBotScoreTracking() {
    	TitForTatBot bot = new TitForTatBot("C");
        assertEquals(0, bot.getScore());
        bot.giveOutcome("X", "COOPERATE", "COOPERATE", 3);
        assertEquals(3, bot.getScore());
    }

    // --- DefectBot ---



    @Test
    public void testDefectBotScoreTracking() {
    	AlwaysDefectBot bot = new AlwaysDefectBot("D");
        assertEquals(0, bot.getScore());
        bot.giveOutcome("X", "DEFECT", "COOPERATE", 5);
        assertEquals(5, bot.getScore());
    }

    // --- CopyBot ---

    @Test
    public void testCopyBotFirstMoveCooperates() {
    	TitForTatBot bot = new TitForTatBot("T");
        bot.setCurrentOpponent("Opp");
        assertEquals("COOPERATE", bot.getAction());
    }

    @Test
    public void testCopyBotMimicsDefect() {
    	TitForTatBot bot = new TitForTatBot("T");
        bot.setCurrentOpponent("Opp");
        bot.giveOutcome("Opp", "COOPERATE", "DEFECT", 0);
        assertEquals("DEFECT", bot.getAction());
    }

    @Test
    public void testCopyBotMimicsCooperate() {
    	TitForTatBot bot = new TitForTatBot("T");
        bot.setCurrentOpponent("Opp");
        bot.giveOutcome("Opp", "DEFECT", "COOPERATE", 5);
        assertEquals("COOPERATE", bot.getAction());
    }

    @Test
    public void testCopyBotMultipleOpponents() {
    	TitForTatBot bot = new TitForTatBot("T");
        bot.setCurrentOpponent("Opp1");
        bot.giveOutcome("Opp1", "COOPERATE", "DEFECT", 0);
        assertEquals("DEFECT", bot.getAction());

        // Switching opponent resets to COOPERATE (no history with Opp2)
        bot.setCurrentOpponent("Opp2");
        assertEquals("COOPERATE", bot.getAction());
    }

    @Test
    public void testCopyBotScoreTracking() {
    	TitForTatBot bot = new TitForTatBot("T");
        bot.setCurrentOpponent("Opp");
        bot.giveOutcome("Opp", "COOPERATE", "COOPERATE", 3);
        bot.giveOutcome("Opp", "COOPERATE", "COOPERATE", 3);
        assertEquals(6, bot.getScore());
    }

    @Test
    public void testCopyBotImplementsOpponentAware() {
    	TitForTatBot bot = new TitForTatBot("T");
        assertTrue(bot instanceof OpponentAware);
    }

    // --- History record ---

    @Test
    public void testHistoryRecordFields() {
        History h = new History("Alice", "Bob", "COOPERATE", "DEFECT", 0, 5);
        assertEquals("Alice", h.player1());
        assertEquals("Bob", h.player2());
        assertEquals("COOPERATE", h.move1());
        assertEquals("DEFECT", h.move2());
        assertEquals(0, h.result1());
        assertEquals(5, h.result2());
    }

    @Test
    public void testHistoryRecordEquality() {
        History h1 = new History("Alice", "Bob", "COOPERATE", "DEFECT", 0, 5);
        History h2 = new History("Alice", "Bob", "COOPERATE", "DEFECT", 0, 5);
        assertEquals(h1, h2); // Java records auto-generate equals()
    }
}
