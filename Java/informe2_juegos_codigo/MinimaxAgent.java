/**
 * Agente inteligente basado en el algoritmo Minimax para juegos de suma
 * cero, de información perfecta y deterministas (Tic-Tac-Toe).
 *
 * Puede operar en dos modos:
 *  - Minimax puro (sin poda): explora todo el árbol de juego hasta la
 *    profundidad máxima configurada.
 *  - Minimax con poda Alfa-Beta: mismo resultado, pero descarta ramas que
 *    no pueden influir en la decisión final (poda por alfa-beta).
 *
 * También soporta profundidad limitada: si se alcanza la profundidad
 * máxima antes de un estado terminal, se usa la función de evaluación
 * heurística {@link Evaluator}.
 */
public class MinimaxAgent {

    private final boolean useAlphaBeta;
    private final int maxDepth; // Integer.MAX_VALUE => sin límite (hasta terminal)
    private final boolean orderMoves; // heurística de ordenamiento de movimientos
    private final java.util.Random random = new java.util.Random();

    public MinimaxAgent(boolean useAlphaBeta, int maxDepth, boolean orderMoves) {
        this.useAlphaBeta = useAlphaBeta;
        this.maxDepth = maxDepth;
        this.orderMoves = orderMoves;
    }

    public MinimaxAgent(boolean useAlphaBeta, int maxDepth) {
        this(useAlphaBeta, maxDepth, true);
    }

    /**
     * Determina la mejor jugada para el jugador MAX ('X') desde el estado dado.
     * Cuando existen varias jugadas con el mismo valor minimax óptimo (empates),
     * se elige una al azar entre ellas: todas son igualmente óptimas desde el
     * punto de vista teórico, por lo que aleatorizar el desempate no compromete
     * la optimalidad de la decisión y evita que la máquina juegue siempre de
     * forma idéntica (por ejemplo, abrir siempre en el centro).
     * @return arreglo {fila, columna} de la jugada elegida, y llena `stats`.
     */
    public int[] findBestMove(Board board, char playerToMove, SearchStats stats) {
        long start = System.nanoTime();
        stats.nodesEvaluated = 0;
        stats.maxDepthReached = 0;
        stats.alphaBetaUsed = useAlphaBeta;

        boolean isMaxTurn = (playerToMove == Board.MAX_PLAYER);
        java.util.List<int[]> bestMoves = new java.util.ArrayList<>();
        int bestValue = isMaxTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int[] move : orderedMoves(board)) {
            board.placeMove(move[0], move[1], playerToMove);
            // IMPORTANTE: cada rama raíz se evalúa con ventana completa (-inf, +inf),
            // NO con el alfa/beta acumulado de hermanos anteriores. Si se reutilizara
            // una ventana ya acotada, un hermano peor podría devolver un valor "podado"
            // (una simple cota, no el valor exacto) que coincidiera numéricamente con
            // bestValue y se confundiera con un empate real, llevando a elegir una
            // jugada subóptima. Con ventana completa, el valor devuelto es siempre
            // exacto (la poda alfa-beta interna de cada subárbol sigue aplicándose y
            // sigue siendo correcta), lo que permite comparar e identificar empates
            // genuinos con total seguridad.
            int value = minimax(board, 1, !isMaxTurn, Integer.MIN_VALUE, Integer.MAX_VALUE, stats);
            board.undoMove(move[0], move[1]);

            boolean better = isMaxTurn ? (value > bestValue) : (value < bestValue);
            boolean tie = (value == bestValue);

            if (better) {
                bestValue = value;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (tie) {
                bestMoves.add(move);
            }
        }

        stats.elapsedNanos = System.nanoTime() - start;
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    /**
     * Núcleo recursivo del algoritmo Minimax (con poda alfa-beta opcional).
     *
     * @param depth        profundidad actual (raíz = 0)
     * @param maximizing   true si es el turno de MAX
     */
    private int minimax(Board board, int depth, boolean maximizing, int alpha, int beta, SearchStats stats) {
        stats.nodesEvaluated++;
        stats.maxDepthReached = Math.max(stats.maxDepthReached, depth);

        char winner = board.checkWinner();
        if (winner == Board.MAX_PLAYER) return 10 - depth;   // victoria más rápida = mejor
        if (winner == Board.MIN_PLAYER) return depth - 10;   // derrota más lenta = menos mala
        if (board.isFull()) return 0;                        // empate
        if (depth >= maxDepth) return Evaluator.evaluate(board); // corte por profundidad

        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int[] move : orderedMoves(board)) {
                board.placeMove(move[0], move[1], Board.MAX_PLAYER);
                int value = minimax(board, depth + 1, false, alpha, beta, stats);
                board.undoMove(move[0], move[1]);
                best = Math.max(best, value);
                if (useAlphaBeta) {
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) break; // PODA: MIN no permitirá llegar aquí
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int[] move : orderedMoves(board)) {
                board.placeMove(move[0], move[1], Board.MIN_PLAYER);
                int value = minimax(board, depth + 1, true, alpha, beta, stats);
                board.undoMove(move[0], move[1]);
                best = Math.min(best, value);
                if (useAlphaBeta) {
                    beta = Math.min(beta, best);
                    if (beta <= alpha) break; // PODA: MAX no permitirá llegar aquí
                }
            }
            return best;
        }
    }

    /**
     * Ordenamiento de movimientos: centro, luego esquinas, luego bordes.
     * En Tic-Tac-Toe esta heurística tiende a encontrar antes las mejores
     * jugadas, lo que maximiza el efecto de la poda alfa-beta (ver informe,
     * sección "Ordenamiento de movimientos").
     */
    private java.util.List<int[]> orderedMoves(Board board) {
        java.util.List<int[]> moves = board.getLegalMoves();
        if (!orderMoves) return moves;

        int[][] priority = {{1,1},{0,0},{0,2},{2,0},{2,2},{0,1},{1,0},{1,2},{2,1}};
        moves.sort((a, b) -> Integer.compare(rank(priority, a), rank(priority, b)));
        return moves;
    }

    private int rank(int[][] priority, int[] move) {
        for (int i = 0; i < priority.length; i++) {
            if (priority[i][0] == move[0] && priority[i][1] == move[1]) return i;
        }
        return priority.length;
    }
}
