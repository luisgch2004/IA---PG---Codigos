/**
 * Encapsula las métricas de una ejecución de búsqueda: nodos evaluados,
 * tiempo de ejecución y profundidad máxima alcanzada. Se usa tanto para
 * reportar el rendimiento de la jugada realmente elegida como para las
 * corridas de comparación (con poda vs sin poda) que no afectan la partida.
 */
public class SearchStats {
    public long nodesEvaluated = 0;
    public long elapsedNanos = 0;
    public int maxDepthReached = 0;
    public boolean alphaBetaUsed = false;

    public double elapsedMillis() {
        return elapsedNanos / 1_000_000.0;
    }

    @Override
    public String toString() {
        return String.format(
            "nodos=%d, tiempo=%.3f ms, profundidad=%d, poda=%s",
            nodesEvaluated, elapsedMillis(), maxDepthReached, alphaBetaUsed ? "SI" : "NO"
        );
    }
}
