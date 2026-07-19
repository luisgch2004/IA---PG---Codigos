import java.util.List;
import java.util.Scanner;

/**
 * Orquesta una partida de Tic-Tac-Toe en sus distintas modalidades:
 * Humano vs Máquina y Máquina vs Máquina. Se encarga de mostrar el
 * tablero, validar jugadas humanas y reportar estadísticas al final.
 */
public class Game {

    private final Scanner scanner;

    public Game(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Humano ('O') vs Máquina ('X').
     * @param startingPlayer Board.MAX_PLAYER si la máquina abre el juego,
     *                       Board.MIN_PLAYER si el humano abre el juego.
     */
    public void playHumanVsMachine(boolean useAlphaBeta, int maxDepth, char startingPlayer) {
        Board board = new Board();
        MinimaxAgent machine = new MinimaxAgent(useAlphaBeta, maxDepth);
        // Agente "espejo" solo para fines estadísticos (no decide jugadas)
        MinimaxAgent comparisonAgent = new MinimaxAgent(!useAlphaBeta, maxDepth);

        char turn = startingPlayer;
        System.out.println("=== Humano (O) vs Máquina (X) ===");
        System.out.println("Algoritmo de la máquina: " + (useAlphaBeta ? "Minimax + Poda Alfa-Beta" : "Minimax puro"));
        System.out.println("Empieza: " + (startingPlayer == Board.MAX_PLAYER ? "la Máquina (X)" : "el Humano (O)"));
        board.print();

        long totalNodesMachine = 0;
        long totalNodesComparison = 0;
        long totalTimeMachine = 0;
        int moveCount = 0;

        while (!board.isTerminal()) {
            if (turn == Board.MAX_PLAYER) {
                SearchStats stats = new SearchStats();
                int[] move = machine.findBestMove(board, Board.MAX_PLAYER, stats);
                board.placeMove(move[0], move[1], Board.MAX_PLAYER);

                SearchStats compStats = new SearchStats();
                Board beforeMove = new Board(board);
                beforeMove.undoMove(move[0], move[1]);
                comparisonAgent.findBestMove(beforeMove, Board.MAX_PLAYER, compStats); // no se usa el resultado, solo estadística

                totalNodesMachine += stats.nodesEvaluated;
                totalNodesComparison += compStats.nodesEvaluated;
                totalTimeMachine += stats.elapsedNanos;
                moveCount++;

                System.out.printf("Máquina juega en (%d,%d) | %s%n", move[0], move[1], stats);
                board.print();
            } else {
                int[] move = readHumanMove(board);
                board.placeMove(move[0], move[1], Board.MIN_PLAYER);
                System.out.printf("Humano juega en (%d,%d)%n", move[0], move[1]);
                board.print();
            }
            turn = (turn == Board.MAX_PLAYER) ? Board.MIN_PLAYER : Board.MAX_PLAYER;
        }

        reportResult(board);
        System.out.println("\n--- Estadísticas acumuladas de la partida (jugadas de la máquina) ---");
        System.out.println("Jugadas realizadas por la máquina: " + moveCount);
        System.out.println("Nodos evaluados con el algoritmo usado (" + (useAlphaBeta ? "Alfa-Beta" : "Minimax puro") + "): " + totalNodesMachine);
        System.out.println("Nodos que hubiera evaluado el algoritmo contrario (" + (!useAlphaBeta ? "Alfa-Beta" : "Minimax puro") + "): " + totalNodesComparison);
        printReduction(useAlphaBeta ? totalNodesComparison : totalNodesMachine, useAlphaBeta ? totalNodesMachine : totalNodesComparison);
        System.out.printf("Tiempo total de cómputo de la máquina: %.3f ms%n", totalTimeMachine / 1_000_000.0);
    }

    /** Máquina vs Máquina; retorna el ganador ('X','O' o EMPTY=empate) y acumula stats en el arreglo dado (index0=nodos noPoda,1=nodos poda,2=tiempoNoPoda,3=tiempoPoda,4=numJugadas). */
    public char playMachineVsMachine(int maxDepth, boolean verbose, long[] accumulator) {
        Board board = new Board();
        MinimaxAgent agentX = new MinimaxAgent(true, maxDepth);   // decide con poda (más rápido)
        MinimaxAgent agentO = new MinimaxAgent(true, maxDepth);
        MinimaxAgent noPodaShadow = new MinimaxAgent(false, maxDepth); // solo estadística

        char turn = Board.MAX_PLAYER;
        if (verbose) {
            System.out.println("=== Máquina (X) vs Máquina (O) ===");
            board.print();
        }

        while (!board.isTerminal()) {
            MinimaxAgent agent = (turn == Board.MAX_PLAYER) ? agentX : agentO;
            SearchStats stats = new SearchStats();
            int[] move = agent.findBestMove(board, turn, stats);
            board.placeMove(move[0], move[1], turn);

            if (accumulator != null) {
                SearchStats shadowStats = new SearchStats();
                Board copy = new Board(board);
                copy.undoMove(move[0], move[1]);
                noPodaShadow.findBestMove(copy, turn, shadowStats);
                accumulator[0] += shadowStats.nodesEvaluated; // sin poda
                accumulator[1] += stats.nodesEvaluated;       // con poda
                accumulator[2] += shadowStats.elapsedNanos;
                accumulator[3] += stats.elapsedNanos;
                accumulator[4] += 1;
            }

            if (verbose) {
                System.out.printf("%s juega en (%d,%d) | %s%n", turn, move[0], move[1], stats);
                board.print();
            }
            turn = (turn == Board.MAX_PLAYER) ? Board.MIN_PLAYER : Board.MAX_PLAYER;
        }

        if (verbose) reportResult(board);
        return board.checkWinner();
    }

    private int[] readHumanMove(Board board) {
        while (true) {
            System.out.print("Ingrese su jugada (fila columna), ej. '1 2': ");
            if (!scanner.hasNextLine()) {
                System.out.println();
                System.out.println("[Aviso] Entrada agotada (EOF); se asigna la primera celda libre disponible.");
                return board.getLegalMoves().get(0);
            }
            try {
                String line = scanner.nextLine().trim();
                String[] parts = line.split("\\s+");
                if (parts.length != 2) throw new IllegalArgumentException("Formato inválido");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                if (row < 0 || row > 2 || col < 0 || col > 2) {
                    throw new IllegalArgumentException("Coordenadas fuera de rango (0-2)");
                }
                if (!board.isEmptyCell(row, col)) {
                    throw new IllegalArgumentException("Celda ocupada");
                }
                return new int[]{row, col};
            } catch (Exception e) {
                System.out.println("Entrada inválida (" + e.getMessage() + "). Intente de nuevo.");
            }
        }
    }

    private void reportResult(Board board) {
        char winner = board.checkWinner();
        if (winner == Board.MAX_PLAYER) System.out.println("\n>>> Resultado: gana la Máquina (X)");
        else if (winner == Board.MIN_PLAYER) System.out.println("\n>>> Resultado: gana el Humano (O)");
        else System.out.println("\n>>> Resultado: EMPATE");
    }

    public static void printReduction(long noPoda, long conPoda) {
        double reduction = noPoda == 0 ? 0 : (100.0 * (noPoda - conPoda) / noPoda);
        System.out.println("Nodos podados: " + (noPoda - conPoda));
        System.out.printf("Reducción de nodos por poda Alfa-Beta: %.2f%%%n", reduction);
    }
}
