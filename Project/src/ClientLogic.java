import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClientLogic {

    private static final float EPS = 0.0f;
    private static final String FILE_NAME = "wumpus-" + EPS + ".data";

    private static final float ALPHA_DECAY = 0.999f;

    private QTable mQTable;
    private State mLastState;
    private Action mLastAction;
    /*
     * Constructor: called at the beginning of the game.
     * You may do initialization here.
     *
     * Parameter:
     *     gameInit - depends on the game. It will contain necessary information for initialization.
     */
    public ClientLogic(GameInit gameInit) {
        try (FileInputStream fin = new FileInputStream(FILE_NAME)) {
            mQTable = QTable.fromSerialized(fin, ALPHA_DECAY);
        } catch (IOException e) {
            mQTable = new QTable(0.9f, 1.0f, EPS);
        }
    }

    /*
     * This method is called once for every turn.
     * This specific example solution returns an empty action.
     *
     * Parameters:
     *     wm   - depends on the game. It will contain the observable part of the world model.
     *     turn - the index of the turn.
     *            If you receive twice the same number, don't worry, just ignore it.
     *
     * Returns:
     *     A Command instance - depends on the game. It's your command for this turn.
     */
    public Command playTurn(WorldModel wm, int turn) {
        State currentState;
        if (mLastState != null) {
            currentState = mLastState.nextState(mLastAction, wm.sensors);
            mQTable.update(mLastState, mLastAction, currentState);
        } else {
            currentState = new State();
        }

        mLastAction = mQTable.exploitOrExplore(currentState);
        mLastState = currentState;
        return new Command(mLastAction);
    }

    /*
     * This method is called at the end of the game.
     *
     * Parameters:
     *     result - depends on the game. It will contain the result of the game.
     */
    void endOfGame(GameResult result) {
        int finalReinforcement = mLastState.finalReinforcement(mLastAction, result);
        mQTable.update(mLastState, mLastAction, finalReinforcement);

        try (FileOutputStream fout = new FileOutputStream(FILE_NAME, false)) {
            mQTable.serialize(fout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}