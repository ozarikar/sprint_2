package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tournament.*;
import game.*;
import robot.*;

public class TournamentModificationsTest {

    @Test
    public void testTournamentHasName() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5), "My League", 4);
        assertEquals("My League", t.getName());
    }

    @Test
    public void testDefaultNameWhenNotProvided() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5));
        assertEquals("Unnamed Tournament", t.getName());
    }

    @Test
    public void testIsOpenBelowMaxPlayers() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5), "T", 4);
        t.addPlayer(new AlwaysCooperateBot("A"));
        t.addPlayer(new AlwaysCooperateBot("B"));
        assertTrue(t.isOpen());
    }

    @Test
    public void testIsClosedAtMaxPlayers() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5), "T", 2);
        t.addPlayer(new AlwaysCooperateBot("A"));
        t.addPlayer(new AlwaysCooperateBot("B"));
        assertFalse(t.isOpen());
    }

    @Test
    public void testCheckEndFalseWhenOpen() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5), "T", 4);
        t.addPlayer(new AlwaysCooperateBot("A"));
        assertFalse(t.checkEnd());
    }

    @Test
    public void testCheckEndTrueWhenFull() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5), "T", 2);
        t.addPlayer(new AlwaysCooperateBot("A"));
        t.addPlayer(new AlwaysCooperateBot("B"));
        assertTrue(t.checkEnd());
    }

    @Test
    public void testUnlimitedDefaultMaxPlayers() {
        // Default constructor should never close the tournament
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5));
        for (int i = 0; i < 100; i++) {
            t.addPlayer(new AlwaysCooperateBot("Bot" + i));
        }
        assertTrue(t.isOpen());
    }

    @Test
    public void testAddHistoryAndGetHistory() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5), "T", 4);
        History h = new History("Alice", "Bob", "COOPERATE", "DEFECT", 0, 5);
        t.addHistory(h);
        assertEquals(1, t.getHistory().size());
        assertEquals(h, t.getHistory().get(0));
    }

    @Test
    public void testRunTournamentAddsHistory() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(3), "T", 4);
        t.addPlayer(new AlwaysCooperateBot("A"));
        t.addPlayer(new AlwaysCooperateBot("B"));
        t.addPlayer(new AlwaysCooperateBot("C"));
        t.runTournament();
        // 3 players → 3 matches (3 choose 2)
        assertEquals(3, t.getHistory().size());
    }

    @Test
    public void testAddPlayerChangesIsOpen() {
        RoundRobinTournament t = new RoundRobinTournament(new PrisonersDilemma(5), "T", 2);
        assertTrue(t.isOpen());
        t.addPlayer(new AlwaysCooperateBot("A"));
        assertTrue(t.isOpen());
        t.addPlayer(new AlwaysCooperateBot("B"));
        assertFalse(t.isOpen());
    }
}
