package test;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import robot.*;
// Testing for Robot class and its implementations.
public class RobotTest {
    
    private AlwaysCooperateBot cooperator;
    private AlwaysDefectBot defector;
    private TitForTatBot titForTat;
    
    @BeforeEach
    public void setUp() {
        cooperator = new AlwaysCooperateBot("TestCooperator");
        defector = new AlwaysDefectBot("TestDefector");
        titForTat = new TitForTatBot("TestTitForTat");
    }
    
    @Test
    public void testRobotNames() {
        assertEquals("TestCooperator", cooperator.getName());
        assertEquals("TestDefector", defector.getName());
        assertEquals("TestTitForTat", titForTat.getName());
    }
    
    @Test
    public void testAlwaysCooperateBotAction() {
        assertEquals("COOPERATE", cooperator.getAction());
        // Should always cooperate regardless of history
        cooperator.giveOutcome("Opponent", "COOPERATE", "DEFECT", 0);
        assertEquals("COOPERATE", cooperator.getAction());
    }
    
    @Test
    public void testAlwaysDefectBotAction() {
        assertEquals("DEFECT", defector.getAction());
        // Should always defect regardless of history
        defector.giveOutcome("Opponent", "DEFECT", "COOPERATE", 5);
        assertEquals("DEFECT", defector.getAction());
    }
    
    @Test
    public void testTitForTatBotFirstMove() {
        titForTat.setCurrentOpponent("Opponent");
        // Should cooperate on first move
        assertEquals("COOPERATE", titForTat.getAction());
    }
    
    @Test
    public void testTitForTatBotMimicry() {
        titForTat.setCurrentOpponent("Opponent");
        
        // First move - cooperates
        assertEquals("COOPERATE", titForTat.getAction());
        
        // Opponent defects
        titForTat.giveOutcome("Opponent", "COOPERATE", "DEFECT", 0);
        
        // Should mimic opponent's defection
        assertEquals("DEFECT", titForTat.getAction());
        
        // Opponent cooperates
        titForTat.giveOutcome("Opponent", "DEFECT", "COOPERATE", 5);
        
        // Should mimic opponent's cooperation
        assertEquals("COOPERATE", titForTat.getAction());
    }
    
    @Test
    public void testScoreTracking() {
        assertEquals(0, cooperator.getScore());
        
        cooperator.giveOutcome("Opponent", "COOPERATE", "COOPERATE", 3);
        assertEquals(3, cooperator.getScore());
        
        cooperator.giveOutcome("Opponent", "COOPERATE", "DEFECT", 0);
        assertEquals(3, cooperator.getScore());
        
        cooperator.giveOutcome("Opponent", "COOPERATE", "COOPERATE", 3);
        assertEquals(6, cooperator.getScore());
    }
    
    @Test
    public void testHistoryTracking() {
        assertTrue(cooperator.getHistory().isEmpty());
        
        cooperator.giveOutcome("Opponent1", "COOPERATE", "DEFECT", 0);
        assertEquals(1, cooperator.getHistory().size());
        assertTrue(cooperator.getHistory().containsKey("Opponent1"));
        
        cooperator.giveOutcome("Opponent2", "COOPERATE", "COOPERATE", 3);
        assertEquals(2, cooperator.getHistory().size());
        
        cooperator.giveOutcome("Opponent1", "COOPERATE", "COOPERATE", 3);
        assertEquals(2, cooperator.getHistory().get("Opponent1").size());
    }
    
    @Test
    public void testMultipleOpponents() {
        titForTat.setCurrentOpponent("Opponent1");
        titForTat.giveOutcome("Opponent1", "COOPERATE", "DEFECT", 0);
        assertEquals("DEFECT", titForTat.getAction());
        
        titForTat.setCurrentOpponent("Opponent2");
        // Should cooperate with new opponent
        assertEquals("COOPERATE", titForTat.getAction());
    }
}