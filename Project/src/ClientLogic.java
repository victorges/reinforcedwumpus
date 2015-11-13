import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ClientLogic {
    private static String fileName;
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
        try {
            File file = new File(fileName);
            FileInputStream fin = new FileInputStream(file);
            QTable qTable;
            if (file.exists() && !file.isDirectory()) {
                qTable = QTable.fromSerialized(fin);
            }
            else {
                qTable = new QTable(0.9f, 1.0f, 0.01f);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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

        File file = new File(fileName);
        try {
            FileOutputStream fout = new FileOutputStream(file, false);
            mQTable.serialize(fout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}