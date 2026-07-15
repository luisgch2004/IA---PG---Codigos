import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;

/**
 * Busqueda Voraz Primero el Mejor (Greedy Best-First Search).
 * Estrategia: cola de prioridad (PriorityQueue) ordenada UNICAMENTE por
 * h(n), el valor heuristico (distancia Manhattan al estado meta). Ignora
 * por completo g(n), el costo ya recorrido.
 *
 * Consecuencias practicas:
 *  - Es rapida y expande pocos nodos porque siempre avanza "hacia donde
 *    parece mas prometedor" segun la heuristica.
 *  - NO es optima: puede encontrar una solucion mas larga de lo necesario,
 *    porque nunca reconsidera el costo ya invertido.
 *  - Es completa en espacios de estados finitos si se controlan los
 *    estados repetidos (como se hace aqui con un HashSet de visitados).
 */
public class Greedy implements Buscador {

    private final String tipoHeuristica; // "manhattan" o "malubicadas"

    public Greedy(String tipoHeuristica) {
        this.tipoHeuristica = tipoHeuristica;
    }

    @Override
    public String getNombre() { return "Greedy(" + tipoHeuristica + ")"; }

    private int heuristica(Estado e) {
        return tipoHeuristica.equals("manhattan") ? e.heuristicaManhattan() : e.heuristicaMalUbicadas();
    }

    @Override
    public ResultadoBusqueda buscar(Estado inicial) {
        long t0 = System.nanoTime();

        // Orden solo por h(n): la esencia de "voraz" (greedy)
        PriorityQueue<Nodo> frontera = new PriorityQueue<>(Comparator.comparingInt(Nodo::getH));
        Set<String> visitados = new HashSet<>();

        Nodo raiz = new Nodo(inicial, null, null, 0);
        raiz.setH(heuristica(inicial));
        frontera.add(raiz);
        visitados.add(inicial.clave());

        int nodosExpandidos = 0;
        int nodosGenerados = 1;
        int profundidadMax = 0;

        while (!frontera.isEmpty()) {
            Nodo actual = frontera.poll();

            if (actual.getEstado().esMeta()) {
                long t1 = System.nanoTime();
                return new ResultadoBusqueda(getNombre(), true, actual, nodosExpandidos,
                        nodosGenerados, (t1 - t0) / 1_000_000, profundidadMax);
            }

            nodosExpandidos++;
            profundidadMax = Math.max(profundidadMax, actual.getG());

            for (Nodo hijo : actual.expandir()) {
                String clave = hijo.getEstado().clave();
                if (!visitados.contains(clave)) {
                    visitados.add(clave);
                    hijo.setH(heuristica(hijo.getEstado()));
                    frontera.add(hijo);
                    nodosGenerados++;
                }
            }
        }

        long t1 = System.nanoTime();
        return new ResultadoBusqueda(getNombre(), false, null, nodosExpandidos, nodosGenerados,
                (t1 - t0) / 1_000_000, profundidadMax);
    }
}
