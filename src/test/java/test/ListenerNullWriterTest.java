package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import game.listener.MoveListener;
import game.listener.ScoreListener;

public class ListenerNullWriterTest {

    @Test
    public void testMoveListenerWithInvalidFile() {
        // Path that cannot be created — triggers IOException, writer stays null
        MoveListener listener = new MoveListener("/invalid_path/no_such_dir/moves.log");
        // update() should not throw even when writer is null
        listener.setMoveData("A", "B", "COOPERATE", "DEFECT");
        listener.update();  // exercises the writer == null branch
        listener.close();   // exercises the writer == null branch in close()
    }

    @Test
    public void testScoreListenerWithInvalidFile() {
        // Path that cannot be created — triggers IOException, writer stays null
        ScoreListener listener = new ScoreListener("/invalid_path/no_such_dir/scores.log");
        listener.setScoreData("A", "B", 3, 3);
        listener.update();  // exercises the writer == null branch
        listener.close();   // exercises the writer == null branch in close()
    }
}
