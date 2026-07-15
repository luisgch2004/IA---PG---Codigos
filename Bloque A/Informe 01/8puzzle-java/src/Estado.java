import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa un ESTADO del problema: una configuracion del tablero 3x3
 * del 8-puzzle, codificada como un arreglo de 9 enteros (recorrido fila-mayor).
 * El valor 0 representa el espacio vacio (hueco).
 *
 * Indices del arreglo -> posicion (fila, columna):
 *   0:(0,0) 1:(0,1) 2:(0,2)
 *   3:(1,0) 4:(1,1) 5:(1,2)
 *   6:(2,0) 7:(2,1) 8:(2,2)
 *
 * Esta clase NO conoce nada sobre la busqueda (no tiene padre, ni costo
 * acumulado, ni operador aplicado): esa informacion vive en la clase Nodo,
 * siguiendo la separacion clasica Estado/Nodo de Russell & Norvig.
 */
public class Estado {

    /** Estado meta del problema: 1,2,3 / 4,5,6 / 7,8,_ */
    public static final int[] META = {1, 2, 3, 4, 5, 6, 7, 8, 0};
    public static final int N = 3; // dimension del tablero (3x3)

    private final int[] tablero; // configuracion de las 9 casillas
    private final int posVacio;  // indice (0..8) donde se encuentra el 0

    public Estado(int[] tablero) {
        this.tablero = tablero;
        this.posVacio = indexOf(tablero, 0);
    }

    private static int indexOf(int[] arr, int valor) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == valor) return i;
        throw new IllegalArgumentException("El tablero no contiene el valor " + valor);
    }


    /** Comprueba si este estado es el estado meta. */
    public boolean esMeta() {
        return Arrays.equals(tablero, META);
    }

    /**
     * OPERADORES/ACCIONES del problema: genera los estados sucesores validos
     * moviendo el espacio vacio ARRIBA, ABAJO, IZQUIERDA o DERECHA, siempre
     * que el movimiento no salga del tablero. Devuelve pares (operador, estado).
     */
    public List<Sucesor> generarSucesores() {
        List<Sucesor> sucesores = new ArrayList<>(4);
        int fila = posVacio / N;
        int col = posVacio % N;

        // {deltaFila, deltaColumna, codigoOperador}
        int[][] movimientos = {
            {-1, 0, 0}, // ARRIBA     (el hueco sube; la ficha de arriba baja)
            {1, 0, 1},  // ABAJO
            {0, -1, 2}, // IZQUIERDA
            {0, 1, 3}   // DERECHA
        };
        String[] nombres = {"ARRIBA", "ABAJO", "IZQUIERDA", "DERECHA"};

        for (int[] mov : movimientos) {
            int nf = fila + mov[0];
            int nc = col + mov[1];
            if (nf < 0 || nf >= N || nc < 0 || nc >= N) continue; // fuera del tablero: invalido

            int nuevaPos = nf * N + nc;
            int[] nuevoTablero = tablero.clone();
            nuevoTablero[posVacio] = nuevoTablero[nuevaPos];
            nuevoTablero[nuevaPos] = 0;

            sucesores.add(new Sucesor(nombres[mov[2]], new Estado(nuevoTablero)));
        }
        return sucesores;
    }

    /** Heuristica h1: distancia Manhattan total (suma de |dx|+|dy| de cada ficha a su meta). */
    public int heuristicaManhattan() {
        int total = 0;
        for (int i = 0; i < tablero.length; i++) {
            int valor = tablero[i];
            if (valor == 0) continue; // el hueco no se penaliza
            int filaActual = i / N, colActual = i % N;
            int posMeta = indexOf(META, valor);
            int filaMeta = posMeta / N, colMeta = posMeta % N;
            total += Math.abs(filaActual - filaMeta) + Math.abs(colActual - colMeta);
        }
        return total;
    }

    /** Heuristica h2: numero de fichas mal ubicadas (no cuenta el hueco). */
    public int heuristicaMalUbicadas() {
        int total = 0;
        for (int i = 0; i < tablero.length; i++) {
            if (tablero[i] != 0 && tablero[i] != META[i]) total++;
        }
        return total;
    }

    /** Clave unica de texto, usada en HashSet/HashMap de estados visitados. */
    public String clave() {
        return Arrays.toString(tablero);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Estado)) return false;
        return Arrays.equals(tablero, ((Estado) o).tablero);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(tablero);
    }

    /** Representacion visual del tablero 3x3 ("_" para el hueco). */
    public String toStringTablero() {
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < N; f++) {
            for (int c = 0; c < N; c++) {
                int v = tablero[f * N + c];
                sb.append(v == 0 ? "_" : v).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Determina si un tablero es resoluble usando la regla de paridad de
     * inversiones: para un tablero 3x3, el estado es resoluble si y solo si
     * el numero de inversiones (pares fuera de orden, ignorando el 0) es PAR.
     */
    public static boolean esResoluble(int[] tablero) {
        int[] sinCero = new int[8];
        int idx = 0;
        for (int v : tablero) if (v != 0) sinCero[idx++] = v;
        int inversiones = 0;
        for (int i = 0; i < sinCero.length; i++)
            for (int j = i + 1; j < sinCero.length; j++)
                if (sinCero[i] > sinCero[j]) inversiones++;
        return inversiones % 2 == 0;
    }

    /** Par (operador aplicado, estado resultante), usado al expandir un Estado. */
    public static class Sucesor {
        public final String operador;
        public final Estado estado;
        public Sucesor(String operador, Estado estado) {
            this.operador = operador;
            this.estado = estado;
        }
    }
}
