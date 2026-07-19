/**
 * Ejecuta las pruebas de rendimiento requeridas en el proyecto:
 *  1) 100 partidas Máquina vs Máquina, promediando nodos evaluados.
 *  2) Comparación de nodos evaluados para profundidades 3, 5, 7, 9 desde
 *     el estado inicial (Minimax puro vs Alfa-Beta).
 *  3) Evaluación de poda desde una posición específica (mitad de partida).
 */
public class ExperimentRunner {

    public void runAll() {
        experimentoPartidasMultiples(100);
        experimentoProfundidades(new int[]{3, 5, 7, 9});
        experimentoPosicionEspecifica();
    }

    /** Prueba 1: N partidas Máquina vs Máquina, promedio de nodos y tiempos. */
    public void experimentoPartidasMultiples(int n) {
        System.out.println("\n================ EXPERIMENTO 1: " + n + " partidas Máquina vs Máquina ================");
        Game game = new Game(null);
        long[] acc = new long[5]; // nodosNoPoda, nodosPoda, tiempoNoPoda, tiempoPoda, numJugadas
        int xWins = 0, oWins = 0, draws = 0;

        for (int i = 0; i < n; i++) {
            char winner = game.playMachineVsMachine(Integer.MAX_VALUE, false, acc);
            if (winner == Board.MAX_PLAYER) xWins++;
            else if (winner == Board.MIN_PLAYER) oWins++;
            else draws++;
        }

        long totalMoves = acc[4];
        System.out.println("Resultados: X gana=" + xWins + ", O gana=" + oWins + ", empates=" + draws);
        System.out.println("Total de jugadas (máquina) evaluadas en las " + n + " partidas: " + totalMoves);
        System.out.printf("Nodos totales SIN poda: %d  (promedio/jugada: %.1f)%n", acc[0], acc[0] / (double) totalMoves);
        System.out.printf("Nodos totales CON poda:  %d  (promedio/jugada: %.1f)%n", acc[1], acc[1] / (double) totalMoves);
        Game.printReduction(acc[0], acc[1]);
        System.out.printf("Tiempo total SIN poda: %.3f ms | CON poda: %.3f ms%n", acc[2] / 1_000_000.0, acc[3] / 1_000_000.0);
    }

    /** Prueba 2: comparar nodos evaluados sin/con poda para varias profundidades máximas. */
    public void experimentoProfundidades(int[] depths) {
        System.out.println("\n================ EXPERIMENTO 2: comparación por profundidad (desde estado inicial) ================");
        System.out.printf("%-12s%-18s%-18s%-15s%-18s%-18s%n",
            "Profundidad", "Nodos sin poda", "Nodos con poda", "Reducción(%)", "Tiempo sin poda", "Tiempo con poda");

        for (int d : depths) {
            Board board = new Board();
            SearchStats statsNoPoda = new SearchStats();
            new MinimaxAgent(false, d).findBestMove(new Board(board), Board.MAX_PLAYER, statsNoPoda);

            Board board2 = new Board();
            SearchStats statsPoda = new SearchStats();
            new MinimaxAgent(true, d).findBestMove(board2, Board.MAX_PLAYER, statsPoda);

            double reduction = 100.0 * (statsNoPoda.nodesEvaluated - statsPoda.nodesEvaluated) / statsNoPoda.nodesEvaluated;
            System.out.printf("%-12d%-18d%-18d%-15.2f%-18.4f%-18.4f%n",
                d, statsNoPoda.nodesEvaluated, statsPoda.nodesEvaluated, reduction,
                statsNoPoda.elapsedMillis(), statsPoda.elapsedMillis());
        }
    }

    /** Prueba 3: desde una posición específica a mitad de partida, medir la poda producida. */
    public void experimentoPosicionEspecifica() {
        System.out.println("\n================ EXPERIMENTO 3: poda desde una posición específica ================");
        Board board = new Board();
        // Posición de ejemplo tras 4 jugadas:
        // X . .
        // . O .
        // . . X
        board.placeMove(0, 0, Board.MAX_PLAYER);
        board.placeMove(1, 1, Board.MIN_PLAYER);
        board.placeMove(2, 2, Board.MAX_PLAYER);
        board.placeMove(0, 2, Board.MIN_PLAYER);
        System.out.println("Posición evaluada:");
        board.print();
        System.out.println("Turno: Máquina (X)");

        SearchStats statsNoPoda = new SearchStats();
        int[] moveNoPoda = new MinimaxAgent(false, Integer.MAX_VALUE).findBestMove(new Board(board), Board.MAX_PLAYER, statsNoPoda);

        SearchStats statsPoda = new SearchStats();
        int[] movePoda = new MinimaxAgent(true, Integer.MAX_VALUE).findBestMove(new Board(board), Board.MAX_PLAYER, statsPoda);

        System.out.println("Jugada elegida (sin poda): (" + moveNoPoda[0] + "," + moveNoPoda[1] + ") | " + statsNoPoda);
        System.out.println("Jugada elegida (con poda):  (" + movePoda[0] + "," + movePoda[1] + ") | " + statsPoda);
        Game.printReduction(statsNoPoda.nodesEvaluated, statsPoda.nodesEvaluated);
    }
}
