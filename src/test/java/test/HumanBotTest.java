package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import robot.HumanBot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class HumanBotTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    // Helper: creates a HumanBot with pre-loaded stdin input
    private HumanBot botWithInput(String name, String input) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        return new HumanBot(name, scanner);
    }

    @Test
    public void testConstructorSetsName() {
        HumanBot bot = botWithInput("Alice", "COOPERATE\n");
        assertEquals("Alice", bot.getName());
    }

    @Test
    public void testInitialScoreIsZero() {
        HumanBot bot = botWithInput("Alice", "COOPERATE\n");
        assertEquals(0, bot.getScore());
    }

    @Test
    public void testGetActionCooperate() {
        HumanBot bot = botWithInput("Alice", "COOPERATE\n");
        assertEquals("COOPERATE", bot.getAction());
    }

    @Test
    public void testGetActionDefect() {
        HumanBot bot = botWithInput("Alice", "DEFECT\n");
        assertEquals("DEFECT", bot.getAction());
    }

    @Test
    public void testGetActionLowercaseDefect() {
        HumanBot bot = botWithInput("Alice", "defect\n");
        // Input is uppercased, so "defect" becomes "DEFECT"
        assertEquals("DEFECT", bot.getAction());
    }

    @Test
    public void testGetActionGarbageDefaultsToCooperate() {
        HumanBot bot = botWithInput("Alice", "blah\n");
        // Anything that isn't "DEFECT" after uppercasing returns "COOPERATE"
        assertEquals("COOPERATE", bot.getAction());
    }

    @Test
    public void testGetActionPrintsPrompt() {
        HumanBot bot = botWithInput("Alice", "COOPERATE\n");
        bot.getAction();
        String output = outContent.toString();
        assertTrue(output.contains("[HumanBot Alice] Enter action"));
    }

    @Test
    public void testGiveOutcomeUpdatesScore() {
        HumanBot bot = botWithInput("Alice", "");
        bot.giveOutcome("Bob", "COOPERATE", "COOPERATE", 3);
        assertEquals(3, bot.getScore());
    }

    @Test
    public void testGiveOutcomeAccumulatesScore() {
        HumanBot bot = botWithInput("Alice", "");
        bot.giveOutcome("Bob", "COOPERATE", "COOPERATE", 3);
        bot.giveOutcome("Bob", "COOPERATE", "DEFECT", 0);
        bot.giveOutcome("Bob", "COOPERATE", "COOPERATE", 3);
        assertEquals(6, bot.getScore());
    }

    @Test
    public void testGiveOutcomePrintsMessage() {
        HumanBot bot = botWithInput("Alice", "");
        bot.giveOutcome("Bob", "COOPERATE", "DEFECT", 0);
        String output = outContent.toString();
        assertTrue(output.contains("[HumanBot] vs Bob"));
        assertTrue(output.contains("you played COOPERATE"));
        assertTrue(output.contains("they played DEFECT"));
        assertTrue(output.contains("you scored 0"));
    }

    @Test
    public void testGiveOutcomeTracksHistory() {
        HumanBot bot = botWithInput("Alice", "");
        bot.giveOutcome("Bob", "COOPERATE", "DEFECT", 0);
        bot.giveOutcome("Charlie", "DEFECT", "COOPERATE", 5);
        assertEquals(2, bot.getHistory().size());
        assertTrue(bot.getHistory().containsKey("Bob"));
        assertTrue(bot.getHistory().containsKey("Charlie"));
    }

    @Test
    public void testMultipleActionsInSequence() {
        HumanBot bot = botWithInput("Alice", "COOPERATE\nDEFECT\nCOOPERATE\n");
        assertEquals("COOPERATE", bot.getAction());
        assertEquals("DEFECT", bot.getAction());
        assertEquals("COOPERATE", bot.getAction());
    }
}
