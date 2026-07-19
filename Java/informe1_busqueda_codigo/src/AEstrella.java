import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Map;

/**
 * Algoritmo A* (A-star).
 * Estrategia: cola de prioridad (PriorityQueue) ordenada por
 * f(n) = g(n) + h(n), donde g(n) es el costo real acumulado desde la raiz
 * y h(n) es el valor heuristico admisible (distancia Manhattan o, para
 * comparar, numero de fichas mal ubicadas).
 *
 * Propiedades:
 *  - COMPLETO: siempre encuentra una solucion si existe (espacio finito).
 *  - OPTIMO: si h(n) es admisible (nunca sobreestima el costo real) y el
 *    grafo se maneja con el criterio de "mejor g conocido", A* garantiza
 *    encontrar la solucion de costo minimo. Ver justificacion teorica en el
 *    informe adjunto.
 *
 * Manejo de repetidos: se guarda el mejor costo g conocido para cada
 * estado en un mapa (map de "distancias"). Si se vuelve a generar el mismo
 * estado con un g menor, se vuelve a insertar en la frontera (esto es
 * necesario para la correccion de A* como busqueda de grafo).
 */
public class AEstrella implements Buscador {

    private final String tipoHeuristica; // "manhattan" o "malubicadas"

    public AEstrella(String tipoHeuristica) {
        this.tipoHeuristica = tipoHeuristica;
    }

    @Override
    public String getNombre() { return "A*(" + tipoHeuristica + ")"; }

    private int heuristica(Estado e) {
        return tipoHeuristica.equals("manhattan") ? e.heuristicaManhattan() : e.heuristicaMalUbicadas();
    }

    @Override
    public ResultadoBusqueda buscar(Estado inicial) {
        long t0 = System.nanoTime();

        PriorityQueue<Nodo> frontera = new PriorityQueue<>(); // orden natural: f(n)=g(n)+h(n)
        Map<String, Integer> mejorCosto = new HashMap<>(); // mejor g(n) conocido por estado
        java.util.Set<String> cerrados = new java.util.HashSet<>(); // estados ya expandidos definitivamente

        Nodo raiz = new Nodo(inicial, null, null, 0);
        raiz.setH(heuristica(inicial));
        frontera.add(raiz);
        mejorCosto.put(inicial.clave(), 0);

        int nodosExpandidos = 0;
        int nodosGenerados = 1;
        int profundidadMax = 0;

        while (!frontera.isEmpty()) {
            Nodo actual = frontera.poll();
            String claveActual = actual.getEstado().clave();

            // Si ya fue cerrado con un costo igual o mejor, se descarta (entrada obsoleta).
            if (cerrados.contains(claveActual)) continue;

            if (actual.getEstado().esMeta()) {
                long t1 = System.nanoTime();
                return new ResultadoBusqueda(getNombre(), true, actual, nodosExpandidos,
                        nodosGenerados, (t1 - t0) / 1_000_000, profundidadMax);
            }

            cerrados.add(claveActual);
            nodosExpandidos++;
            profundidadMax = Math.max(profundidadMax, actual.getG());

            for (Nodo hijo : actual.expandir()) {
                String claveHijo = hijo.getEstado().clave();
                if (cerrados.contains(claveHijo)) continue;

                Integer costoPrevio = mejorCosto.get(claveHijo);
                if (costoPrevio == null || hijo.getG() < costoPrevio) {
                    mejorCosto.put(claveHijo, hijo.getG());
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
