class State {
    public enum Direction {
        UP(0), LEFT(1), DOWN(2), RIGHT(3);

        private final int int_v;
        Direction(int int_v) {this.int_v = int_v;}

        public Direction rotateLeft() {
            switch (this) {
                case UP: return LEFT;
                case LEFT: return DOWN;
                case DOWN: return RIGHT;
                case RIGHT: return UP;
                default: throw new IllegalStateException();
            }
        }

        public Direction rotateRight() {
            switch (this) {
                case UP: return RIGHT;
                case RIGHT: return DOWN;
                case DOWN: return LEFT;
                case LEFT: return UP;
                default: throw new IllegalStateException();
            }
        }

        public void apply(int[] coords) {
            switch (this) {
                case UP: coords[1]++; break;
                case RIGHT: coords[0]++; break;
                case DOWN: coords[1]--; break;
                case LEFT: coords[0]--; break;
                default: throw new IllegalStateException();
            }
        }

        public int intValue() {
            return int_v;
        }

        public static Direction fromInt(int int_v) {
            switch (int_v) {
                case 0: return UP;
                case 1: return LEFT;
                case 2: return DOWN;
                case 3: return RIGHT;
                default: throw new IllegalArgumentException();
            }
        }
    }

    public final int x, y;
    public final Direction facingDirection;

    public static final int HAS_GOLD_MASK       = 0x01;
    public static final int USED_ARROW_MASK     = 0x02;
    public static final int WUMPUS_DEAD_MASK    = 0x04;

    public final int gameStateFlags;

    // these won't go to the serialization of the state, nor the hashCode/equals methods
    public final transient int reinforcement;
    public final transient Sensors sensors;

    // Creates initial state at bottom-left of 4x4 grid
    public State() {
        this(0, 0, Direction.RIGHT, 0, new Sensors(), 0);
    }

    private State(int x, int y, Direction facingDirection, int gameStateFlags, Sensors sensors, int reinforcement) {
        this.x = x;
        this.y = y;
        this.facingDirection = facingDirection;
        this.gameStateFlags = gameStateFlags;

        this.sensors = sensors;
        this.reinforcement = reinforcement;
    }

    public boolean hasUsedArrow() {
        return (gameStateFlags & USED_ARROW_MASK) > 0;
    }

    public boolean hasGold() {
        return (gameStateFlags & HAS_GOLD_MASK) > 0;
    }

    public boolean isWumpusDead() {
        return (gameStateFlags & WUMPUS_DEAD_MASK) > 0;
    }

    public State nextState(Action action, Sensors sensors) {
        switch (action) {
            case FORWARD:
                if (sensors.bump) break;

                int[] coords = {x, y};
                facingDirection.apply(coords);
                return new State(coords[0], coords[1], facingDirection, gameStateFlags, sensors, -1);
            case TURNRIGHT:
                return new State(x, y, facingDirection.rotateRight(), gameStateFlags, sensors, -1);
            case TURNLEFT:
                return new State(x, y, facingDirection.rotateLeft(), gameStateFlags, sensors, -1);
            case SHOOT:
                if (hasUsedArrow()) break;

                int state = gameStateFlags | USED_ARROW_MASK;
                if (sensors.scream) state |= WUMPUS_DEAD_MASK;
                return new State(x, y, facingDirection, state, sensors, -11);
            case GRAB:
                if (!this.sensors.glitter) break;

                // The actual points for the gold are only given when the player exits the cave, though we give half of
                // them once the user collects the gold to guide the reinforcement learning to the gold quicker.
                return new State(x, y, facingDirection, gameStateFlags | HAS_GOLD_MASK, sensors, -1);
        }
        // Default to STAY action
        return new State(x, y, facingDirection, gameStateFlags, sensors, -1);
    }

    public int finalReinforcement(Action action, GameResult result) {
        if (action == Action.CLIMB && result.score > 0) {
            return 999;
        } else if (action == Action.FORWARD && result.score <= 1000) {
            return -1001;
        } else {
            return -1;
        }
    }

    public int intValue() {
        // 4 bits for each field
        assert (x & 0xF) == 0;
        assert (y & 0xF) == 0;
        assert (facingDirection.intValue() & 0xF) == 0;
        assert (gameStateFlags & 0xF) == 0;
        return (x << 12) | (y << 8) | (facingDirection.intValue() << 4) | gameStateFlags;
    }

    public static State fromIntValue(int int_v) {
        int stateFlags = int_v & 0xF;
        Direction dir = Direction.fromInt((int_v >> 4) & 0xF);
        int x = (int_v >> 12) & 0xF, y = (int_v >> 8) & 0xF;

        return new State(x, y, dir, stateFlags, null, 0);
    }

    @Override
    public int hashCode() {
        return ((Integer)intValue()).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;

        return intValue() == ((State) o).intValue();
    }
}
