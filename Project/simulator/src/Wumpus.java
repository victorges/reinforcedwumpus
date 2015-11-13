public class Wumpus {

    private static final boolean DEBUG = false;

    public enum TileType {
        EMPTY, PIT, GOLD, WUMPUS, ENTRY
    }

    private static final TileType[][] TABLE = {
            {TileType.ENTRY , TileType.EMPTY, TileType.PIT  , TileType.EMPTY},
            {TileType.EMPTY , TileType.EMPTY, TileType.EMPTY, TileType.EMPTY},
            {TileType.WUMPUS, TileType.GOLD , TileType.PIT  , TileType.EMPTY},
            {TileType.EMPTY , TileType.EMPTY, TileType.EMPTY, TileType.PIT}
    };
    private static final int WUMPUS_X, WUMPUS_Y;
    static {
        int x = 0, y;
        outer: for (y = 0; y < TABLE.length; y++) {
            for (x = 0; x < TABLE[y].length; x++) {
                if (tileAt(x, y) == TileType.WUMPUS) break outer;
            }
        }
        WUMPUS_X = x; WUMPUS_Y = y;
    }

    private ClientLogic mClientLogic;

    private int mPlayerX = 0, mPlayerY = 0;
    private int mDirectionX = 1, mDirectionY = 0; // initially pointed right
    private boolean mHasGold = false;
    private boolean mWumpusAlive = true;
    private boolean mHasArrow = true;

    private transient boolean mBumpedWall, mWumpusDied, mEndOfGame;

    public GameResult runSimulation() {
        if (mClientLogic != null) throw new IllegalStateException("Simulation already run");

        GameInit init = new GameInit(new GameInfo(GameStatus.RUNNING, null, 0, 0, 0, true, null), new GameDescription(PlayerType.PLAYER));
        mClientLogic = new ClientLogic(init);

        int score = 0;
        for (int turn = 0; turn < 150; turn++) {
            Command command = mClientLogic.playTurn(new WorldModel(playerSensors()), turn);
            logDebug(turn + ": " + command.action);
            score += handleCommand(command) - 1; // every action deducts score by 1

            if (mEndOfGame) break;
        }
        GameResult result = new GameResult(score);
        mClientLogic.endOfGame(result);
        return result;
    }

    private int handleCommand(Command command) {
        mWumpusDied = mBumpedWall = mEndOfGame = false;

        switch (command.action) {
            case FORWARD:
                int newX = limit(0, 3, mPlayerX + mDirectionX);
                int newY = limit(0, 3, mPlayerY + mDirectionY);
                if (newX == mPlayerX && newY == mPlayerY) mBumpedWall = true;
                mPlayerX = newX;
                mPlayerY = newY;
                if (playerTile() == TileType.PIT || (playerTile() == TileType.WUMPUS && mWumpusAlive)) {
                    mEndOfGame = true;
                    return -1000;
                }
                logDebug("Moved to x: " + mPlayerX + " y: " + mPlayerY);
                return 0;
            case TURNRIGHT:
                int prevX = mDirectionX;
                mDirectionX = mDirectionY;
                mDirectionY = -prevX;
                logDebug("Turned right, dx: " + mDirectionX + " dy: " + mDirectionY);
                return 0;
            case TURNLEFT:
                prevX = mDirectionX;
                mDirectionX = -mDirectionY;
                mDirectionY = prevX;
                logDebug("Turned left, dx: " + mDirectionX + " dy: " + mDirectionY);
                return 0;
            case SHOOT:
                if (!mHasArrow) return 0;
                logDebug("Shot an arrow!");
                int diffX = WUMPUS_X - mPlayerX, diffY = WUMPUS_Y - mPlayerY;
                if (Math.signum(diffX) == Math.signum(mDirectionX) && Math.signum(diffY) == Math.signum(mDirectionY)) {
                    mWumpusDied = true;
                    mWumpusAlive = false;
                    logDebug("Killed the Wumpus!");
                } else {
                    logDebug("Miss!");
                }
                mHasArrow = false;
                return -10;
            case GRAB:
                if (playerTile() == TileType.GOLD) {
                    if (!mHasGold) logDebug("Grabbed the gold!");
                    mHasGold = true;
                }
                return 0;
            case CLIMB:
                if (playerTile() != TileType.ENTRY || !mHasGold) return 0;
                logDebug("Climbed out" + (mHasGold ? " with the gold!" : "!"));
                mEndOfGame = true;
                return mHasGold ? 1000 : 0;
            case STAY:
                return 0;
            default: throw new IllegalArgumentException();
        }
    }

    private TileType playerTile() {
        return tileAt(mPlayerX, mPlayerY);
    }

    private Sensors playerSensors() {
        Sensors sensors = new Sensors();

        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dy = -1; dy <= 1; dy += 2) {
                int posX = mPlayerX + dx, posY = mPlayerY + dy;
                if (tileAt(posX, posY) == TileType.WUMPUS) sensors.setStench(true);
                if (tileAt(posX, posY) == TileType.PIT) sensors.setBreeze(true);
            }
        }
        sensors.setGlitter(playerTile() == TileType.GOLD && !mHasGold);
        sensors.setScream(mWumpusDied);
        sensors.setBump(mBumpedWall);
        return sensors;
    }

    private static int limit(int min, int max, int value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static TileType tileAt(int x, int y) {
        if (y < 0 || y > 3 || x < 0 || x > 3) return TileType.EMPTY;
        return TABLE[y][x];
    }

    private static void logDebug(String s) {
        if (DEBUG) {
            System.out.println(s);
        }
    }
}
