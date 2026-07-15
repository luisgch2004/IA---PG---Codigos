import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Fuerza salida en UTF-8 para mostrar correctamente tildes y el carácter '·'
        // independientemente de la codificación por defecto del sistema operativo.
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        Scanner scanner = new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8);

        java.util.Map<String, String> opts = parseArgs(args);

        if (!opts.isEmpty() && opts.containsKey("mode")) {
            runFromArgs(opts, scanner);
            return;
        }

        interactiveMenu(scanner);
    }

    private static java.util.Map<String, String> parseArgs(String[] args) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("--")) {
                map.put(args[i].substring(2), args[i + 1]);
            }
        }
        return map;
    }

    private static void runFromArgs(java.util.Map<String, String> opts, Scanner scanner) {
        String mode = opts.getOrDefault("mode", "hvm");
        boolean alphaBeta = !opts.getOrDefault("algo", "alphabeta").equalsIgnoreCase("minimax");
        int depth = parseDepth(opts.getOrDefault("depth", "full"));

        switch (mode) {
            case "hvm" -> {
                char first = parseStartingPlayer(opts.getOrDefault("first", "machine"));
                new Game(scanner).playHumanVsMachine(alphaBeta, depth, first);
            }
            case "mvm" -> new Game(scanner).playMachineVsMachine(depth, true, null);
            case "experiments" -> new ExperimentRunner().runAll();
            default -> System.out.println("Modo desconocido: " + mode);
        }
    }

    private static char parseStartingPlayer(String value) {
        return switch (value.toLowerCase()) {
            case "human", "humano" -> Board.MIN_PLAYER;
            case "random", "aleatorio" -> new java.util.Random().nextBoolean() ? Board.MAX_PLAYER : Board.MIN_PLAYER;
            default -> Board.MAX_PLAYER; // "machine" / valor por defecto
        };
    }

    private static int parseDepth(String value) {
        if (value.equalsIgnoreCase("full")) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("Profundidad inválida, usando búsqueda completa.");
            return Integer.MAX_VALUE;
        }
    }

    private static void interactiveMenu(Scanner scanner) {
        System.out.println("=================================================");
        System.out.println(" AGENTE INTELIGENTE PARA TIC-TAC-TOE (MINIMAX)");
        System.out.println(" Curso: Inteligencia Artificial");
        System.out.println("=================================================");

        while (true) {
            System.out.println("\nSeleccione una opción:");
            System.out.println("1) Jugar Humano (O) vs Máquina (X)");
            System.out.println("2) Simular Máquina (X) vs Máquina (O)");
            System.out.println("3) Ejecutar batería de experimentos (100 partidas, profundidades, posición específica)");
            System.out.println("4) Salir");
            System.out.print("Opción: ");
            if (!scanner.hasNextLine()) {
                System.out.println("\n[Aviso] Entrada agotada (EOF). Fin del programa.");
                return;
            }
            String option = safeReadLine(scanner);

            switch (option) {
                case "1" -> {
                    boolean ab = askAlgorithm(scanner);
                    int depth = askDepth(scanner);
                    char first = askStartingPlayer(scanner);
                    new Game(scanner).playHumanVsMachine(ab, depth, first);
                }
                case "2" -> {
                    int depth = askDepth(scanner);
                    new Game(scanner).playMachineVsMachine(depth, true, new long[5]);
                }
                case "3" -> new ExperimentRunner().runAll();
                case "4" -> {
                    System.out.println("Fin del programa.");
                    return;
                }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    /** Lee una línea de forma segura; devuelve cadena vacía si la entrada se agotó (EOF). */
    private static String safeReadLine(Scanner scanner) {
        if (!scanner.hasNextLine()) return "";
        return scanner.nextLine().trim();
    }

    private static boolean askAlgorithm(Scanner scanner) {
        System.out.print("Algoritmo: (1) Minimax sin poda  (2) Minimax con poda Alfa-Beta [default 2]: ");
        String v = safeReadLine(scanner);
        return !v.equals("1");
    }

    private static char askStartingPlayer(Scanner scanner) {
        System.out.print("¿Quién empieza? (1) Máquina  (2) Humano  (3) Al azar  [default 1]: ");
        String v = safeReadLine(scanner);
        return switch (v) {
            case "2" -> Board.MIN_PLAYER;
            case "3" -> new java.util.Random().nextBoolean() ? Board.MAX_PLAYER : Board.MIN_PLAYER;
            default -> Board.MAX_PLAYER;
        };
    }

    private static int askDepth(Scanner scanner) {
        System.out.print("Profundidad máxima de búsqueda (ENTER para búsqueda completa hasta terminal): ");
        String v = safeReadLine(scanner);
        if (v.isEmpty()) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido, se usará búsqueda completa.");
            return Integer.MAX_VALUE;
        }
    }
}
