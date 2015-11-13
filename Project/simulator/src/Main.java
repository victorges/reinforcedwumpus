public class Main {
    public static void main(String[] args) {
        int iterations = args.length > 0 ? Integer.parseInt(args[0]) : 1;

        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < iterations; i++) {
            System.out.println("Running simulation " + i);
            Wumpus game = new Wumpus();
            GameResult result = game.runSimulation();

            if (result.score > bestScore) bestScore = result.score;
            System.out.println("Finished simulation, best score til now: " + bestScore);
        }
    }
}
