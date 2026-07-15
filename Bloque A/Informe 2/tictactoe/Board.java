import java.util.ArrayList;
import java.util.List;

/**
 * Representa el estado del tablero de Tic-Tac-Toe (3x3) y las operaciones
 * necesarias para el modelado formal del juego: estado inicial, generación
 * de jugadas legales, aplicación/deshacer de jugadas y función terminal.
 *
 * Formalmente, un estado del juego se define como una matriz 3x3 sobre el
 * alfabeto {X, O, ·}, donde '·' representa una celda vacía.
 */
public class Board {

    public static final char EMPTY = '\u00B7'; // '·'
    public static final char MAX_PLAYER = 'X'; // Máquina (maximiza la utilidad)
    public static final char MIN_PLAYER = 'O'; // Humano u oponente (minimiza la utilidad)

    private final char[][] cells;

    /** Estado inicial: tablero vacío. */
    public Board() {
        cells = new char[3][3];
        for (char[] row : cells) {
            java.util.Arrays.fill(row, EMPTY);
        }
    }

    /** Constructor de copia (para no mutar estados durante la búsqueda). */
    public Board(Board other) {
        cells = new char[3][3];
        for (int i = 0; i < 3; i++) {
            cells[i] = other.cells[i].clone();
        }
    }

    public char get(int row, int col) {
        return cells[row][col];
    }

    public boolean isEmptyCell(int row, int col) {
        return cells[row][col] == EMPTY;
    }

    /** Aplica una jugada (transición de estado). */
    public void placeMove(int row, int col, char player) {
        cells[row][col] = player;
    }

    /** Deshace una jugada (usado por la búsqueda para no copiar tableros en cada nodo). */
    public void undoMove(int row, int col) {
        cells[row][col] = EMPTY;
    }

    /** Devuelve la lista de jugadas legales: cualquier celda vacía. */
    public List<int[]> getLegalMoves() {
        List<int[]> moves = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (cells[r][c] == EMPTY) {
                    moves.add(new int[]{r, c});
                }
            }
        }
        return moves;
    }

    public boolean isFull() {
        for (char[] row : cells) {
            for (char c : row) {
                if (c == EMPTY) return false;
            }
        }
        return true;
    }

    /**
     * Función terminal / de ganador.
     * @return 'X' si gana MAX, 'O' si gana MIN, EMPTY si no hay ganador (aún o empate).
     */
    public char checkWinner() {
        int[][] lines = {
            {0,0,0,1,0,2}, {1,0,1,1,1,2}, {2,0,2,1,2,2}, // filas
            {0,0,1,0,2,0}, {0,1,1,1,2,1}, {0,2,1,2,2,2}, // columnas
            {0,0,1,1,2,2}, {0,2,1,1,2,0}                 // diagonales
        };
        for (int[] l : lines) {
            char a = cells[l[0]][l[1]];
            char b = cells[l[2]][l[3]];
            char c = cells[l[4]][l[5]];
            if (a != EMPTY && a == b && b == c) {
                return a;
            }
        }
        return EMPTY;
    }

    /** true si el estado es terminal (victoria de alguno o empate). */
    public boolean isTerminal() {
        return checkWinner() != EMPTY || isFull();
    }

    /** Todas las líneas del tablero (8 en total), usadas por el evaluador heurístico. */
    public static int[][] allLines() {
        return new int[][] {
            {0,0,0,1,0,2}, {1,0,1,1,1,2}, {2,0,2,1,2,2},
            {0,0,1,0,2,0}, {0,1,1,1,2,1}, {0,2,1,2,2,2},
            {0,0,1,1,2,2}, {0,2,1,1,2,0}
        };
    }

    public char[][] getCells() {
        return cells;
    }

    /** Imprime el tablero en consola con coordenadas de referencia. */
    public void print() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n    0   1   2\n");
        for (int r = 0; r < 3; r++) {
            sb.append(" ").append(r).append("  ");
            for (int c = 0; c < 3; c++) {
                sb.append(cells[r][c]);
                if (c < 2) sb.append(" | ");
            }
            sb.append("\n");
            if (r < 2) sb.append("   ---+---+---\n");
        }
        System.out.println(sb);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (char[] row : cells) {
            for (char c : row) sb.append(c);
        }
        return sb.toString();
    }
}
