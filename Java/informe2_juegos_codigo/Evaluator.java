/**
 * Función de evaluación heurística, usada únicamente cuando la búsqueda se
 * corta antes de alcanzar un estado terminal (profundidad máxima limitada).
 *
 * Heurística: para cada una de las 8 líneas del tablero (filas, columnas,
 * diagonales) que todavía están "abiertas" (no contienen fichas de ambos
 * jugadores a la vez), se cuenta como potencial de MAX si solo contiene
 * fichas de MAX (o está vacía) y como potencial de MIN si solo contiene
 * fichas de MIN (o está vacía).
 *
 * Evaluación(s) = (líneas potenciales de MAX) - (líneas potenciales de MIN)
 *
 * Este valor heurístico se mantiene deliberadamente en un rango pequeño
 * (típicamente [-8, 8]) para no solaparse con las utilidades terminales
 * (+10 / -10 / 0), preservando la consistencia del algoritmo Minimax.
 */
public class Evaluator {

    public static int evaluate(Board board) {
        int maxLines = 0;
        int minLines = 0;
        char[][] cells = board.getCells();

        for (int[] line : Board.allLines()) {
            char a = cells[line[0]][line[1]];
            char b = cells[line[2]][line[3]];
            char c = cells[line[4]][line[5]];

            boolean hasMax = (a == Board.MAX_PLAYER || b == Board.MAX_PLAYER || c == Board.MAX_PLAYER);
            boolean hasMin = (a == Board.MIN_PLAYER || b == Board.MIN_PLAYER || c == Board.MIN_PLAYER);

            if (hasMax && !hasMin) {
                maxLines++;
            } else if (hasMin && !hasMax) {
                minLines++;
            }
        }
        return maxLines - minLines;
    }
}
