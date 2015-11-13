import java.io.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class QTable {
    private Map<State, EnumMap<Action, Float>> mTable = new HashMap<>();
    private final float mGamma;
    private final float mAlpha;
    private final float mEps;

    public QTable(float gamma, float alpha, float eps) {
        mGamma = gamma;
        mAlpha = alpha;
        mEps = eps;
    }

    public void update(State state, Action action, State nextState) {
        float bestValue = Float.NEGATIVE_INFINITY;
        for (float value : stateActions(nextState).values()) {
            if (value > bestValue) bestValue = value;
        }

        EnumMap<Action, Float> actions = stateActions(state);
        float prevValue = actions.get(action);
        float newValue = nextState.reinforcement + mGamma * bestValue;
        actions.put(action, (1 - mAlpha) * prevValue + mAlpha * newValue);
    }

    public void update(State state, Action action, float reinforcement) {
        EnumMap<Action, Float> actions = stateActions(state);
        float prevValue = actions.get(action);
        actions.put(action, (1 - mAlpha) * prevValue + mAlpha * reinforcement);
    }

    /**
     * Returns the best action to take for a given state, depending on the values already
     * learned from it. If there are multiple equivalent best actions, picks one uniformly
     * between them.
     */
    public Action exploit(State state) {
        Action bestAction = null;
        float bestValue = Float.NEGATIVE_INFINITY;
        int numBestValue = 0;

        for (Map.Entry<Action, Float> entry : stateActions(state).entrySet()) {
//            System.out.println(entry.getKey() + ", " + entry.getValue());
            if (entry.getValue() > bestValue) {
                bestValue = entry.getValue();
                bestAction = entry.getKey();
                numBestValue = 1;
            } else if (entry.getValue() == bestValue && pickOneIn(++numBestValue)) {
                bestAction = entry.getKey();
            }
        }
        return bestAction;
    }

    public Action exploitOrExplore(State state) {
        if (Math.random() < mEps) {
            return explore(state);
        } else {
            return exploit(state);
        }
    };

    private static boolean pickOneIn(int n) {
        return Math.random() * n < 1;
    }

    /**
     * Picks a random action for exploration. Currently just randomly selects an Action
     * from the possible ones, but this could possibly be optimized.
     */
    public Action explore(State state) {
        Action[] actions = Action.values();
        return actions[((int) (Math.random() * actions.length))];
    }

    private EnumMap<Action, Float> stateActions(State state) {
        EnumMap<Action, Float> actions = mTable.get(state);
        if (actions == null) {
            actions = new EnumMap<>(Action.class);
            for (Action a : Action.values()) {
                actions.put(a, 0.0f);
            }
            mTable.put(state, actions);
        }
        return actions;
    }

    static public QTable fromSerialized(InputStream inputStream, float alphaDecay) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Scanner scanner = new Scanner(reader);

        final Map<State, EnumMap<Action, Float>> table = new HashMap<>();

        float gamma = scanner.nextFloat();
        float alpha = scanner.nextFloat() * alphaDecay;
        float eps = scanner.nextFloat();

        while (scanner.hasNext()) {
            State state = State.fromIntValue(scanner.nextInt());
            int nActions = scanner.nextInt();
            EnumMap<Action, Float> actionValues = new EnumMap<>(Action.class);
            for (int i = 0; i < nActions; i++) {
                Action action = Action.findByValue(scanner.nextInt());
                float value = scanner.nextFloat();
                actionValues.put(action, value);
            }
            table.put(state, actionValues);
        }

        QTable qTable = new QTable(gamma, alpha, eps);
        qTable.mTable = table;

        return qTable;
    }

    public void serialize(OutputStream outputStream) {
        PrintWriter printWriter = new PrintWriter(outputStream);
        printWriter.print(String.format("%f %f %f\n", mGamma, mAlpha, mEps));

        for (State state : mTable.keySet()) {
            printWriter.print(state.intValue());
            printWriter.print(" ");
            printWriter.println(mTable.get(state).size());
            EnumMap<Action, Float> actionValues = mTable.get(state);
            for (Action action : actionValues.keySet()) {
                printWriter.print(action.getValue());
                printWriter.print(" ");
                printWriter.print(actionValues.get(action));
                printWriter.print("\n");
            }
        }
        printWriter.flush();
    }
}
