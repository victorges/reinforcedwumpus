import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class QTable {
    private final Map<State, EnumMap<Action, Float>> mTable = new HashMap<>();
    private final float mGamma;
    private final float mAlpha;

    public QTable(float gamma, float alpha) {
        mGamma = gamma;
        mAlpha = alpha;
    }

    public void update(State state, Action action, State nextState) {
        float bestValue = Float.MIN_VALUE;
        for (float value : stateActions(nextState).values()) {
            if (value > bestValue) bestValue = value;
        }

        EnumMap<Action, Float> actions = stateActions(state);
        float prevValue = actions.get(action);
        float newValue = nextState.reinforcement + mGamma * bestValue;
        actions.put(action, (1 - mAlpha) * prevValue + mAlpha * newValue);
    }

    /**
     * Returns the best action to take for a given state, depending on the values already
     * learned from it. If there are multiple equivalent best actions, picks one uniformly
     * between them.
     */
    public Action exploit(State state) {
        Action bestAction = null;
        float bestValue = Float.MIN_VALUE;
        int numBestValue = 0;

        for (Map.Entry<Action, Float> entry : stateActions(state).entrySet()) {
            if (entry.getValue() > bestValue) {
                bestAction = entry.getKey();
                numBestValue = 1;
            } else if (entry.getValue() == bestValue && pickOneIn(numBestValue++)) {
                bestAction = entry.getKey();
            }
        }
        return bestAction;
    }

    private static boolean pickOneIn(int n) {
        return n < 1 / Math.random();
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
}
