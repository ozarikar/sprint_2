package client;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import robot.HumanBot;
import robot.Robot;

import java.util.*;

@SpringBootApplication
@RestController
public class NetworkedTournamentClient implements ApplicationRunner {

   
    private Robot bot;

    // Not closed intentionally — closing Scanner on System.in would close stdin
    private Scanner console;

    public NetworkedTournamentClient() {
        this.console = new Scanner(System.in);
    }

    // Public constructor for testing — allows injecting a custom Scanner
    public NetworkedTournamentClient(Scanner console) {
        this.console = console;
    }

    public static void main(String[] args) {
        SpringApplication.run(NetworkedTournamentClient.class, args);
    }


    @PostMapping("/action")
    public String getActionEndpoint(
            @RequestBody(required = false) Map<String, List<String>> history) {
        if (bot == null) return "COOPERATE";
        bot.setHistory(history != null ? history : new HashMap<>());
        return bot.getAction();
    }

    /**
     * Plain Java accessor used by unit tests that don't want to spin up the
     * full HTTP stack. NOT a Spring endpoint — the wire-facing entry point
     * is {@link #getActionEndpoint(Map)} above.
     */
    public String getAction() {
        return bot != null ? bot.getAction() : "COOPERATE";
    }

    // Public setter for testing — allows setting the bot without run().
    // Accepts any Robot so non-human bots can be hosted by this client.
    public void setBot(Robot bot) {
        this.bot = bot;
    }

    // Runs after Tomcat is up, so /action is live before registration completes
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.print("Server address (e.g. 127.0.0.1:8080): ");
        String serverAddr = console.nextLine().trim();

        RestClient client = RestClient.create();

        List<Map<String, Object>> tournamentList = client.get()
            .uri("http://" + serverAddr + "/tournaments")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        System.out.println("\nAvailable Tournaments:");
        for (int i = 0; i < tournamentList.size(); i++) {
            Map<String, Object> t = tournamentList.get(i);
            String status = Boolean.TRUE.equals(t.get("open")) ? "OPEN" : "CLOSED";
            System.out.println("  " + (i + 1) + ". " + t.get("name") + " [" + status + "]");
        }

        System.out.print("Pick number: ");
        int pick = Integer.parseInt(console.nextLine().trim()) - 1;
        String tournamentName = (String) tournamentList.get(pick).get("name");

        System.out.print("Your robot name: ");
        String robotName = console.nextLine().trim();

        System.out.print("Your IP:port (e.g. 127.0.0.1:8081): ");
        String myIp = console.nextLine().trim();

        // Create the local HumanBot before registering so /action is ready
        bot = new HumanBot(robotName);

        Map<String, String> regBody = new HashMap<>();
        regBody.put("robotName", robotName);
        regBody.put("tournamentName", tournamentName);
        regBody.put("ip", myIp);

        Boolean success = client.post()
            .uri("http://" + serverAddr + "/register")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .body(regBody)
            .retrieve()
            .body(Boolean.class);

        if (Boolean.TRUE.equals(success)) {
            System.out.println("Registered in \"" + tournamentName + "\"! You will be prompted for moves now.");
        } else {
            System.out.println("Registration failed — tournament may be full or not found.");
        }
    }
}
