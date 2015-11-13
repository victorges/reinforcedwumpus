public class Main {
    public static void main(String[] args) {
        int iterations = args.length > 0 ? Integer.parseInt(args[0]) : 1;

        for (int i = 0; i < iterations; i++) {
            System.out.println("Running simulation " + i);
            Wumpus game = new Wumpus();
            GameResult result = game.runSimulation();
            System.out.println("Finished simulation, score: " + result.score);
        }
    }
}
