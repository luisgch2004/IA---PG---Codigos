import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;
import java.util.Set;

/**
 * Busqueda en amplitud (Breadth-First Search).
 * Estrategia: FIFO (LinkedList usada como cola). Explora el arbol de
 * busqueda nivel por nivel, por lo que es COMPLETA (si el objetivo es
 * alcanzable a profundidad finita, lo encuentra) y OPTIMA cuando el costo
 * de cada accion es uniforme (como en este problema, costo=1 por
 * movimiento): la primera solucion encontrada es de costo minimo.
 *
 * Desventaja principal: la frontera crece exponencialmente con la
 * profundidad (factor de ramificacion ~2.13 en el 8-puzzle), por lo que el
 * consumo de memoria puede ser muy alto en instancias dificiles
 * ("explosion combinatoria").
 */
public class BFS implements Buscador {

    @Override
    public String getNombre() { return "BFS"; }

    @Override
    public ResultadoBusqueda buscar(Estado inicial) {
        long t0 = System.nanoTime();

        Queue<Nodo> frontera = new LinkedList<>();
        Set<String> visitados = new HashSet<>(); // control de estados repetidos

        Nodo raiz = new Nodo(inicial, null, null, 0);
        frontera.add(raiz);
        visitados.add(inicial.clave());

        int nodosExpandidos = 0;
        int nodosGenerados = 1;
        int profundidadMax = 0;

        while (!frontera.isEmpty()) {
            Nodo actual = frontera.poll(); // extrae el mas antiguo (FIFO)

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
