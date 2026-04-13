 package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import robot.HumanBot;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class HumanBotTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;
    private HumanBot bot;

    @BeforeEach
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        bot = new HumanBot("Alice");
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testConstructorSetsName() {
        assertEquals("Alice", bot.getName());
    }

    @Test
    public void testInitialScoreIsZero() {
        assertEquals(0, bot.getScore());
    }

    @Test
    public void testGiveOutcomeUpdatesScore() {
        bot.giveOutcome("Bob", "COOPERATE", "COOPERATE", 3);
        assertEquals(3, bot.getScore());
    }

    @Test
    public void testGiveOutcomeAccumulatesScore() {
        bot.giveOutcome("Bob", "COOPERATE", "COOPERATE", 3);
        bot.giveOutcome("Bob", "COOPERATE", "DEFECT", 0);
        bot.giveOutcome("Bob", "COOPERATE", "COOPERATE", 3);
        assertEquals(6, bot.getScore());
    }

    @Test
    public void testGiveOutcomePrintsMessage() {
        bot.giveOutcome("Bob", "COOPERATE", "DEFECT", 0);
        String output = outContent.toString();
        assertTrue(output.contains("[HumanBot] vs Bob"));
        assertTrue(output.contains("you played COOPERATE"));
        assertTrue(output.contains("they played DEFECT"));
        assertTrue(output.contains("you scored 0"));
    }

    @Test
    public void testGiveOutcomeTracksHistory() {
        bot.giveOutcome("Bob", "COOPERATE", "DEFECT", 0);
        bot.giveOutcome("Charlie", "DEFECT", "COOPERATE", 5);
        // History should have entries for both opponents
        assertEquals(2, bot.getHistory().size());
        assertTrue(bot.getHistory().containsKey("Bob"));
        assertTrue(bot.getHistory().containsKey("Charlie"));
    }

    @Test
    public void testMultipleRoundsWithSameOpponent() {
        bot.giveOutcome("Bob", "COOPERATE", "COOPERATE", 3);
        bot.giveOutcome("Bob", "COOPERATE", "DEFECT", 0);
        bot.giveOutcome("Bob", "DEFECT", "COOPERATE", 5);
        assertEquals(8, bot.getScore());
        assertEquals(3, bot.getHistory().get("Bob").size());
    }
}
