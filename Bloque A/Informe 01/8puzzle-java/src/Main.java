import java.util.Scanner;
import java.util.List;


public class Main {

    // Casos de prueba predefinidos (facil, medio, dificil)
    static final int[] FACIL   = {1, 2, 3, 4, 5, 6, 0, 7, 8};   // 2 movimientos de la meta
    static final int[] MEDIO   = {1, 2, 3, 0, 4, 6, 7, 5, 8};   // dificultad intermedia
    static final int[] DIFICIL = {8, 6, 7, 2, 5, 4, 3, 0, 1};   // caso clasico "dificil" (31 movimientos optimos)

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println(" 8-PUZZLE - Comparacion de Algoritmos de Busqueda");
        System.out.println(" Inteligencia Artificial");
        System.out.println("=========================================");

        while (true) {
            System.out.println("\nMENU PRINCIPAL");
            System.out.println("1. Ingresar estado inicial manualmente");
            System.out.println("2. Usar estado predefinido (facil / medio / dificil)");
            System.out.println("3. Ejecutar tabla comparativa (3 casos de prueba, los 4 algoritmos)");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opcion: ");
            String opcion = sc.nextLine().trim();

            switch (opcion) {
                case "1": {
                    int[] tablero = leerTableroManual(sc);
                    if (tablero != null) ejecutarMenuAlgoritmos(sc, tablero);
                    break;
                }
                case "2": {
                    int[] tablero = elegirPredefinido(sc);
                    if (tablero != null) ejecutarMenuAlgoritmos(sc, tablero);
                    break;
                }
                case "3":
                    ejecutarTablaComparativa();
                    break;
                case "4":
                    System.out.println("Fin del programa.");
                    sc.close();
                    return;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    /** Lee 9 numeros (0-8, sin repetir) desde la entrada estandar. */
    static int[] leerTableroManual(Scanner sc) {
        System.out.println("Ingrese los 9 valores del tablero separados por espacio (0 = espacio vacio).");
        System.out.println("Ejemplo: 1 2 3 4 0 5 6 7 8");
        System.out.print("> ");
        String linea = sc.nextLine().trim();
        String[] partes = linea.split("\\s+");
        if (partes.length != 9) {
            System.out.println("Error: se requieren exactamente 9 numeros.");
            return null;
        }
        int[] tablero = new int[9];
        boolean[] usados = new boolean[9];
        try {
            for (int i = 0; i < 9; i++) {
                int v = Integer.parseInt(partes[i]);
                if (v < 0 || v > 8 || usados[v]) {
                    System.out.println("Error: los valores deben ser 0-8 sin repetir.");
                    return null;
                }
                usados[v] = true;
                tablero[i] = v;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: entrada no numerica.");
            return null;
        }
        if (!Estado.esResoluble(tablero)) {
            System.out.println("ADVERTENCIA: este estado NO tiene solucion (paridad de inversiones impar).");
            System.out.println("¿Continuar de todas formas? (s/n)");
            String r = sc.nextLine().trim().toLowerCase();
            if (!r.equals("s")) return null;
        }
        return tablero;
    }

    static int[] elegirPredefinido(Scanner sc) {
        System.out.println("1. Facil   (" + java.util.Arrays.toString(FACIL) + ")");
        System.out.println("2. Medio   (" + java.util.Arrays.toString(MEDIO) + ")");
        System.out.println("3. Dificil (" + java.util.Arrays.toString(DIFICIL) + ")");
        System.out.print("Seleccione: ");
        String op = sc.nextLine().trim();
        switch (op) {
            case "1": return FACIL.clone();
            case "2": return MEDIO.clone();
            case "3": return DIFICIL.clone();
            default:
                System.out.println("Opcion invalida.");
                return null;
        }
    }

    static void ejecutarMenuAlgoritmos(Scanner sc, int[] tablero) {
        System.out.println("\nEstado inicial:");
        System.out.println(new Estado(tablero).toStringTablero());

        System.out.println("Seleccione el algoritmo:");
        System.out.println("1. BFS");
        System.out.println("2. DFS (limite de profundidad 30)");
        System.out.println("3. Greedy Best-First (heuristica Manhattan)");
        System.out.println("4. A* (heuristica Manhattan)");
        System.out.println("5. A* (heuristica fichas mal ubicadas)");
        System.out.println("6. Ejecutar TODOS y comparar");
        System.out.print("> ");
        String op = sc.nextLine().trim();

        Buscador b;
        switch (op) {
            case "1": b = new BFS(); ejecutarYMostrar(b, tablero, sc); break;
            case "2": b = new DFS(30); ejecutarYMostrar(b, tablero, sc); break;
            case "3": b = new Greedy("manhattan"); ejecutarYMostrar(b, tablero, sc); break;
            case "4": b = new AEstrella("manhattan"); ejecutarYMostrar(b, tablero, sc); break;
            case "5": b = new AEstrella("malubicadas"); ejecutarYMostrar(b, tablero, sc); break;
            case "6": {
                Buscador[] todos = { new BFS(), new DFS(30), new Greedy("manhattan"),
                                      new AEstrella("manhattan"), new AEstrella("malubicadas") };
                for (Buscador buscador : todos) {
                    ResultadoBusqueda r = buscador.buscar(new Estado(tablero));
                    System.out.println(r);
                }
                break;
            }
            default:
                System.out.println("Opcion invalida.");
        }
    }

    static void ejecutarYMostrar(Buscador b, int[] tablero, Scanner sc) {
        ResultadoBusqueda r = b.buscar(new Estado(tablero));
        System.out.println("\nResultado: " + r);
        if (r.solucionEncontrada) {
            System.out.print("¿Mostrar solucion paso a paso? (s/n): ");
            String resp = sc.nextLine().trim().toLowerCase();
            if (resp.equals("s")) mostrarPasos(r.nodoSolucion);
        }
    }

    static void mostrarPasos(Nodo solucion) {
        List<Estado> estados = solucion.reconstruirEstados();
        List<String> pasos = solucion.reconstruirCamino();
        System.out.println("\n--- Paso 0 (estado inicial) ---");
        System.out.println(estados.get(0).toStringTablero());
        for (int i = 0; i < pasos.size(); i++) {
            System.out.println("--- Paso " + (i + 1) + ": mover " + pasos.get(i) + " ---");
            System.out.println(estados.get(i + 1).toStringTablero());
        }
    }

    /** Ejecuta los 4 (5, contando las 2 heuristicas de A*) algoritmos sobre los 3 casos y arma una tabla. */
    static void ejecutarTablaComparativa() {
        String[] nombresCasos = {"FACIL", "MEDIO", "DIFICIL"};
        int[][] casos = {FACIL, MEDIO, DIFICIL};

        System.out.println("\n===================================================================================");
        System.out.printf("%-10s %-20s %-10s %-12s %-12s %-10s%n",
                "Caso", "Algoritmo", "Pasos", "Expandidos", "Generados", "Tiempo(ms)");
        System.out.println("===================================================================================");

        for (int i = 0; i < casos.length; i++) {
            Buscador[] algoritmos = {
                new BFS(),
                new DFS(30),
                new Greedy("manhattan"),
                new AEstrella("manhattan"),
                new AEstrella("malubicadas")
            };
            for (Buscador b : algoritmos) {
                ResultadoBusqueda r = b.buscar(new Estado(casos[i]));
                String pasos = r.solucionEncontrada ? String.valueOf(r.getLongitudSolucion()) : "N/A";
                System.out.printf("%-10s %-20s %-10s %-12d %-12d %-10d%n",
                        nombresCasos[i], b.getNombre(), pasos, r.nodosExpandidos, r.nodosGenerados, r.tiempoMs);
            }
            System.out.println("-----------------------------------------------------------------------------------");
        }
    }
}
