/**
 * Interfaz comun para todos los algoritmos de busqueda (BFS, DFS, Greedy, A*).
 * Cada clase concreta implementa buscar(), que recibe el estado inicial y
 * devuelve un ResultadoBusqueda con la solucion (si existe) y las metricas
 * de desempeño solicitadas (nodos expandidos, longitud de la solucion,
 * tiempo de ejecucion, etc.).
 */
public interface Buscador {

    ResultadoBusqueda buscar(Estado inicial);

    /** Nombre descriptivo del algoritmo (para reportes/tablas comparativas). */
    String getNombre();
}
