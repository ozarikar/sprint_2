package server;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import robot.RemoteBot;
import robot.TitForTatBot;
import tournament.RoundRobinTournament;
import tournament.Tournament;
import game.PrisonersDilemma;

import java.util.*;

@SpringBootApplication
@RestController
public class NetworkedTournamentServer {

    private final List<Tournament> tournaments = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(NetworkedTournamentServer.class, args);
    }

    @PostConstruct
    public void init() {
        // Each tournament holds 3 players: 1 human + 2 auto-fill bots
        seedTournaments();
    }

    // Extracted so tests can reset the in-memory state between methods without a full
    // Spring context rebuild. Clears current list and re-adds the default tournaments.
    public synchronized void seedTournaments() {
        tournaments.clear();
        tournaments.add(new RoundRobinTournament(new PrisonersDilemma(5),  "Alpha League", 3));
        tournaments.add(new RoundRobinTournament(new PrisonersDilemma(10), "Beta Cup",     3));
        tournaments.add(new RoundRobinTournament(new PrisonersDilemma(3),  "Gamma Open",   3));
    }

    @GetMapping("/tournaments")
    public List<Map<String, Object>> getTournaments() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tournament t : tournaments) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", t.getName());
            entry.put("open", t.isOpen());
            result.add(entry);
        }
        return result;
    }

    @PostMapping("/register")
    public synchronized boolean register(@RequestBody Map<String, String> body) {
        String robotName      = body.get("robotName");
        String tournamentName = body.get("tournamentName");
        String ip             = body.getOrDefault("ip", "");

        Tournament target = tournaments.stream()
            .filter(t -> t.getName().equals(tournamentName))
            .findFirst().orElse(null);

        if (target == null || !target.isOpen()) return false;

        // Add the human as a RemoteBot — server calls back to client's /action each round
        target.addPlayer(new RemoteBot(robotName, ip));

        // Fill remaining spots with CopyBots, then start immediately
        int i = 0;
        while (target.isOpen()) {
            target.addPlayer(new TitForTatBot("AutoBot" + i++));
        }
        // Daemon thread so it won't block JVM shutdown (e.g. at end of test runs)
        Thread runner = new Thread(target::runTournament, "tournament-" + target.getName());
        runner.setDaemon(true);
        runner.start();

        return true;
    }
}
