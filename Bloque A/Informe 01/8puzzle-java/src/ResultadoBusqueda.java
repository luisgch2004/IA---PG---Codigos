/**
 * Encapsula el resultado de ejecutar un algoritmo de busqueda sobre una
 * instancia del 8-puzzle, junto con las metricas de comparacion pedidas:
 *  - nodos expandidos (estados generados y evaluados)
 *  - longitud/costo de la solucion (numero de movimientos)
 *  - tiempo de ejecucion en milisegundos
 *  - si se encontro solucion (completitud practica en esta ejecucion)
 */
public class ResultadoBusqueda {

    public final String algoritmo;
    public final boolean solucionEncontrada;
    public final Nodo nodoSolucion;      // null si no se encontro solucion
    public final int nodosExpandidos;
    public final int nodosGenerados;
    public final long tiempoMs;
    public final int profundidadMaximaAlcanzada;

    public ResultadoBusqueda(String algoritmo, boolean solucionEncontrada, Nodo nodoSolucion,
                              int nodosExpandidos, int nodosGenerados, long tiempoMs,
                              int profundidadMaximaAlcanzada) {
        this.algoritmo = algoritmo;
        this.solucionEncontrada = solucionEncontrada;
        this.nodoSolucion = nodoSolucion;
        this.nodosExpandidos = nodosExpandidos;
        this.nodosGenerados = nodosGenerados;
        this.tiempoMs = tiempoMs;
        this.profundidadMaximaAlcanzada = profundidadMaximaAlcanzada;
    }

    public int getLongitudSolucion() {
        return nodoSolucion == null ? -1 : nodoSolucion.getG();
    }

    @Override
    public String toString() {
        if (!solucionEncontrada) {
            return String.format("%-10s | SIN SOLUCION | expandidos=%-7d generados=%-7d tiempo=%dms",
                    algoritmo, nodosExpandidos, nodosGenerados, tiempoMs);
        }
        return String.format("%-10s | pasos=%-4d | expandidos=%-7d generados=%-7d tiempo=%-6dms | prof.max=%d",
                algoritmo, getLongitudSolucion(), nodosExpandidos, nodosGenerados, tiempoMs,
                profundidadMaximaAlcanzada);
    }
}
