package test;

import client.NetworkedTournamentClient;
import org.junit.jupiter.api.Test;
import robot.Robot;
import robot.TitForTatBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug-fix tests for the {@code /action} endpoint on
 * {@link NetworkedTournamentClient}. The endpoint now takes a history payload
 * (POST body) and must hand it to the local bot before delegating — that's
 * the only way a strategy bot hosted on the client side can see what the
 * opponent did last round.
 *
 * These tests work against the Java entry point ({@code getActionEndpoint})
 * directly rather than going over the wire, so they isolate the
 * history-application logic from Spring's HTTP layer. Wire-level coverage
 * lives in {@link RemoteBotHistoryTest}.
 */
public class NetworkedClientActionHistoryTest {

    /** Test bot that records whatever history was set on it before getAction(). */
    private static final class HistorySpyBot extends Robot {
        Map<String, List<String>> seenHistory;
        private final String actionToReturn;

        HistorySpyBot(String name, String actionToReturn) {
            super(name);
            this.actionToReturn = actionToReturn;
        }

        @Override
        public String getAction() {
            // Snapshot what the framework has loaded into us at call time
            seenHistory = getHistory();
            return actionToReturn;
        }

        @Override
        public int getScore() { return 0; }
    }

    @Test
    public void endpointPassesHistoryToBotBeforeCallingGetAction() {
        HistorySpyBot spy = new HistorySpyBot("Spy", "DEFECT");
        NetworkedTournamentClient client = new NetworkedTournamentClient();
        client.setBot(spy);

        Map<String, List<String>> history = new HashMap<>();
        history.put("Rival", new ArrayList<>(List.of("COOPERATE,DEFECT,0")));

        String result = client.getActionEndpoint(history);

        assertEquals("DEFECT", result);
        assertNotNull(spy.seenHistory, "bot.getAction() must run AFTER setHistory()");
        assertTrue(spy.seenHistory.containsKey("Rival"),
            "Endpoint must apply incoming history to local bot; bot saw: " + spy.seenHistory);
        assertEquals(List.of("COOPERATE,DEFECT,0"), spy.seenHistory.get("Rival"));
    }

    @Test
    public void endpointWithNullBodyAppliesEmptyHistory() {
        HistorySpyBot spy = new HistorySpyBot("Spy", "COOPERATE");
        NetworkedTournamentClient client = new NetworkedTournamentClient();
        // Pre-load some bogus history so we can prove the endpoint cleared it
        Map<String, List<String>> stale = new HashMap<>();
        stale.put("Ghost", new ArrayList<>(List.of("STALE,STALE,99")));
        spy.setHistory(stale);
        client.setBot(spy);

        String result = client.getActionEndpoint(null);

        assertEquals("COOPERATE", result);
        assertNotNull(spy.seenHistory);
        assertTrue(spy.seenHistory.isEmpty(),
            "Null body must reset bot history to empty; bot saw: " + spy.seenHistory);
    }

    @Test
    public void endpointReturnsCooperateWhenBotNull() {
        NetworkedTournamentClient client = new NetworkedTournamentClient();
        // Never call setBot — bot is null

        String result = client.getActionEndpoint(new HashMap<>());

        assertEquals("COOPERATE", result);
    }

    /**
     * End-to-end strategy check: a TitForTatBot hosted on the client must
     * defect after the opponent defected last round. Pre-fix this could not
     * possibly work because the bot's history was never populated on the
     * client side.
     */
    @Test
    public void titForTatBotHostedOnClientReactsToHistory() {
        TitForTatBot tft = new TitForTatBot("Tit");
        // TitForTat requires the opponent name to be set per round; the
        // server-side Game does this via OpponentAware before sending each
        // request. We replicate the contract here.
        tft.setCurrentOpponent("Rival");

        NetworkedTournamentClient client = new NetworkedTournamentClient();
        client.setBot(tft);

        // Round 1: empty history -> TitForTat cooperates
        String round1 = client.getActionEndpoint(new HashMap<>());
        assertEquals("COOPERATE", round1,
            "TitForTat cooperates on first encounter (no history)");

        // Round 2: opponent defected last round -> TitForTat must defect
        Map<String, List<String>> history = new HashMap<>();
        history.put("Rival", new ArrayList<>(List.of("COOPERATE,DEFECT,0")));
        String round2 = client.getActionEndpoint(history);
        assertEquals("DEFECT", round2,
            "TitForTat must mimic opponent's last move (DEFECT) on round 2");

        // Round 3: opponent cooperated last -> TitForTat cooperates
        history.put("Rival", new ArrayList<>(List.of(
            "COOPERATE,DEFECT,0",
            "DEFECT,COOPERATE,5"
        )));
        String round3 = client.getActionEndpoint(history);
        assertEquals("COOPERATE", round3,
            "TitForTat must mimic opponent's last move (COOPERATE) on round 3");
    }

    @Test
    public void setHistoryReplacesPriorHistory() {
        TitForTatBot tft = new TitForTatBot("Tit");
        tft.setCurrentOpponent("Rival");

        Map<String, List<String>> first = new HashMap<>();
        first.put("Rival", new ArrayList<>(List.of("COOPERATE,DEFECT,0")));
        tft.setHistory(first);
        assertEquals("DEFECT", tft.getAction());

        // Replace, not merge — the old entry must no longer drive behaviour
        Map<String, List<String>> second = new HashMap<>();
        second.put("Rival", new ArrayList<>(List.of("DEFECT,COOPERATE,5")));
        tft.setHistory(second);
        assertEquals("COOPERATE", tft.getAction());
    }

    @Test
    public void setHistoryIsDefensiveCopy() {
        TitForTatBot tft = new TitForTatBot("Tit");
        tft.setCurrentOpponent("Rival");

        Map<String, List<String>> source = new HashMap<>();
        List<String> entries = new ArrayList<>();
        entries.add("COOPERATE,DEFECT,0");
        source.put("Rival", entries);

        tft.setHistory(source);

        // Mutate the source AFTER handing it to the bot. If setHistory just
        // stored the reference, the bot's view of the opponent would change
        // underneath it.
        source.clear();
        entries.clear();

        assertEquals("DEFECT", tft.getAction(),
            "setHistory must deep-copy so external mutation can't change bot state");
    }

    @Test
    public void setHistoryNullResetsToEmpty() {
        TitForTatBot tft = new TitForTatBot("Tit");
        tft.setCurrentOpponent("Rival");
        tft.giveOutcome("Rival", "COOPERATE", "DEFECT", 0);
        assertEquals("DEFECT", tft.getAction(), "sanity: history was populated");

        tft.setHistory(null);

        assertEquals("COOPERATE", tft.getAction(),
            "setHistory(null) must clear history -> first-round COOPERATE");
        assertTrue(tft.getHistory().isEmpty());
    }

    /**
     * Cross-check that the existing no-arg {@code getAction()} accessor still
     * works for tests that don't care about history (i.e. doesn't accidentally
     * require the endpoint path).
     */
    @Test
    public void noArgGetActionStillDelegatesToBot() {
        AtomicReference<String> sawAction = new AtomicReference<>();
        Robot stub = new Robot("Stub") {
            @Override public String getAction() { sawAction.set("called"); return "DEFECT"; }
            @Override public int getScore() { return 0; }
        };
        NetworkedTournamentClient client = new NetworkedTournamentClient();
        client.setBot(stub);

        assertEquals("DEFECT", client.getAction());
        assertEquals("called", sawAction.get());
    }
}
