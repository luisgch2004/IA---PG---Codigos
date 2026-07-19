import java.util.Deque;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Busqueda en profundidad (Depth-First Search), con limite de profundidad
 * (Depth-Limited DFS) para evitar recorrer caminos infinitos o
 * excesivamente largos, dado que el espacio de estados del 8-puzzle
 * contiene ciclos (se puede volver a un estado ya visto moviendo el hueco
 * de un lado a otro).
 *
 * Estrategia: LIFO (se usa un ArrayDeque como pila).
 * NO es completa en general (si el limite es menor que la profundidad de la
 * solucion, no la encuentra) y NO es optima (la primera solucion hallada
 * puede no ser la de menor costo, ya que DFS se compromete con una rama
 * antes de comparar contra las demas).
 */
public class DFS implements Buscador {

    private final int profundidadMaxima;

    public DFS(int profundidadMaxima) {
        this.profundidadMaxima = profundidadMaxima;
    }

    @Override
    public String getNombre() { return "DFS"; }

    @Override
    public ResultadoBusqueda buscar(Estado inicial) {
        long t0 = System.nanoTime();

        Deque<Nodo> pila = new ArrayDeque<>(); // pila (push/pop) = LIFO
        Set<String> enPila = new HashSet<>();  // estados presentes en la rama actual (evita ciclos)

        Nodo raiz = new Nodo(inicial, null, null, 0);
        pila.push(raiz);

        int nodosExpandidos = 0;
        int nodosGenerados = 1;
        int profundidadMax = 0;

        // Para controlar ciclos en DFS se usa el conjunto de visitados global;
        // esto convierte la busqueda en un DFS de grafo (mas eficiente en la practica).
        Set<String> visitadosGlobal = new HashSet<>();
        visitadosGlobal.add(inicial.clave());

        while (!pila.isEmpty()) {
            Nodo actual = pila.pop(); // extrae el mas reciente (LIFO)

            if (actual.getEstado().esMeta()) {
                long t1 = System.nanoTime();
                return new ResultadoBusqueda(getNombre(), true, actual, nodosExpandidos,
                        nodosGenerados, (t1 - t0) / 1_000_000, profundidadMax);
            }

            profundidadMax = Math.max(profundidadMax, actual.getG());

            if (actual.getG() >= profundidadMaxima) {
                continue; // corte por profundidad: no se expande mas esta rama
            }

            nodosExpandidos++;

            for (Nodo hijo : actual.expandir()) {
                String clave = hijo.getEstado().clave();
                if (!visitadosGlobal.contains(clave)) {
                    visitadosGlobal.add(clave);
                    pila.push(hijo);
                    nodosGenerados++;
                }
            }
        }

        long t1 = System.nanoTime();
        return new ResultadoBusqueda(getNombre(), false, null, nodosExpandidos, nodosGenerados,
                (t1 - t0) / 1_000_000, profundidadMax);
    }
}
