import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClientLogic {

    private static final float EPS = 0.0f;
    private static final String FILE_NAME = "wumpus-" + EPS + ".data";

    private static final float ALPHA_DECAY = 0.999f;

    private final boolean mUseStaticStorage = QTableStorageManager.isStaticStorageAvailable();
    private final QTable mQTable;

    private State mLastState;
    private Action mLastAction;

    public ClientLogic(GameInit gameInit) {
        if (mUseStaticStorage) {
            mQTable = QTable.fromSerialized(QTableStorageManager.getStaticContent(), 0);
            mQTable.setExplorationRatio(0);
        } else {
            QTable table;
            try (InputStream in = new FileInputStream(FILE_NAME)) {
                table = QTable.fromSerialized(in, ALPHA_DECAY);
            } catch (IOException e) {
                table = new QTable(0.9f, 1.0f, EPS);
            }
            mQTable = table;
        }
    }


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

    void endOfGame(GameResult result) {
        if (mUseStaticStorage) return;

        int finalReinforcement = mLastState.finalReinforcement(mLastAction, result);
        mQTable.update(mLastState, mLastAction, finalReinforcement);

        try (FileOutputStream fout = new FileOutputStream(FILE_NAME, false)) {
            mQTable.serialize(fout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
