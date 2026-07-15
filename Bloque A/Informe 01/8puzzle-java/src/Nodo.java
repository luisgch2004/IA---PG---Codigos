/**
 * Representa un NODO del arbol/grafo de busqueda. Envuelve un Estado y
 * agrega toda la informacion propia de la busqueda: el nodo padre, el
 * operador que se aplico para llegar a este nodo, el costo acumulado
 * g(n) (= profundidad, ya que el costo por accion es uniforme = 1) y el
 * valor heuristico h(n) (calculado solo cuando el algoritmo lo requiere).
 *
 * f(n) = g(n) + h(n) se expone como metodo de conveniencia para A*.
 */
public class Nodo implements Comparable<Nodo> {

    private final Estado estado;
    private final Nodo padre;
    private final String operador; // movimiento aplicado por el padre para generar este nodo
    private final int g;           // costo acumulado desde la raiz (profundidad)
    private int h;                 // valor heuristico h(n); 0 si el algoritmo no usa heuristica

    public Nodo(Estado estado, Nodo padre, String operador, int g) {
        this.estado = estado;
        this.padre = padre;
        this.operador = operador;
        this.g = g;
        this.h = 0;
    }

    public Estado getEstado() { return estado; }
    public Nodo getPadre() { return padre; }
    public String getOperador() { return operador; }
    public int getG() { return g; }
    public int getH() { return h; }
    public void setH(int h) { this.h = h; }
    public int getF() { return g + h; }

    /** Expande este nodo generando sus nodos hijos (uno por cada sucesor valido del estado). */
    public java.util.List<Nodo> expandir() {
        java.util.List<Nodo> hijos = new java.util.ArrayList<>();
        for (Estado.Sucesor s : estado.generarSucesores()) {
            hijos.add(new Nodo(s.estado, this, s.operador, this.g + 1));
        }
        return hijos;
    }

    /** Reconstruye la secuencia de operadores desde la raiz hasta este nodo. */
    public java.util.List<String> reconstruirCamino() {
        java.util.LinkedList<String> camino = new java.util.LinkedList<>();
        Nodo actual = this;
        while (actual.padre != null) {
            camino.addFirst(actual.operador);
            actual = actual.padre;
        }
        return camino;
    }

    /** Reconstruye la secuencia de estados (tableros) desde la raiz hasta este nodo. */
    public java.util.List<Estado> reconstruirEstados() {
        java.util.LinkedList<Estado> camino = new java.util.LinkedList<>();
        Nodo actual = this;
        while (actual != null) {
            camino.addFirst(actual.estado);
            actual = actual.padre;
        }
        return camino;
    }

    /** Orden natural por f(n) = g(n) + h(n); usado por la PriorityQueue de A*. */
    @Override
    public int compareTo(Nodo o) {
        return Integer.compare(this.getF(), o.getF());
    }
}
