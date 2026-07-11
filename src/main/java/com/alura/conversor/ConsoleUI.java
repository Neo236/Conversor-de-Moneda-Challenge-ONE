package com.alura.conversor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);

    // ANSI escape codes for colors
    private static final String RESET = "\033[0m";
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private static final String YELLOW = "\033[0;33m";
    private static final String BLUE = "\033[0;34m";
    private static final String CYAN = "\033[0;36m";

    private final CurrencyService currencyService;
    private final HistoryRepository historyRepository;
    private final Scanner scanner;

    public ConsoleUI(CurrencyService currencyService, HistoryRepository historyRepository, Scanner scanner) {
        this.currencyService = currencyService;
        this.historyRepository = historyRepository;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println(CYAN + "***************************************************" + RESET);
        System.out.println(GREEN + "Bienvenido al Conversor de Moneda V2 =)" + RESET);
        System.out.println(CYAN + "***************************************************" + RESET);

        buclePrincipal:
        while (true) {
            String monedaBase = "";
            while (monedaBase.length() != 3) {
                monedaBase = leerEntrada("\nElija la moneda de " + BLUE + "ORIGEN" + RESET + " (codigo de 3 letras, ej. USD):");
                if (monedaBase.equals("SALIR")) break buclePrincipal;
                if (monedaBase.length() != 3) System.out.println(RED + "Por favor, ingrese exactamente 3 letras para la moneda." + RESET);
            }

            String monedaObjetivo = "";
            while (monedaObjetivo.length() != 3) {
                monedaObjetivo = leerEntrada("\n¿A que moneda desea " + BLUE + "CONVERTIR" + RESET + "? (ej. ARS, COP):");
                if (monedaObjetivo.equals("SALIR")) break buclePrincipal;
                if (monedaObjetivo.length() != 3) System.out.println(RED + "Por favor, ingrese exactamente 3 letras para la moneda." + RESET);
            }

            double cantidad = 0;
            boolean cantidadValida = false;
            while (!cantidadValida) {
                String entradaCantidad = leerEntrada("\nIngrese la " + YELLOW + "CANTIDAD" + RESET + " a convertir:");
                if (entradaCantidad.equals("SALIR")) break buclePrincipal;

                try {
                    cantidad = Double.parseDouble(entradaCantidad.replace(",", "."));
                    if (cantidad <= 0) {
                        System.out.println(RED + "Por favor, ingrese un valor mayor a cero." + RESET);
                    } else {
                        cantidadValida = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(RED + "Formato invalido. Debe ingresar un numero." + RESET);
                }
            }

            System.out.println(CYAN + "\nProcesando conversion..." + RESET);
            try {
                RespuestaConversion respuesta = currencyService.convertir(monedaBase, monedaObjetivo, cantidad);
                String fechaHoraFormateada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                historyRepository.agregarRegistro(new RegistroConversion(
                        respuesta.baseCode(), respuesta.targetCode(),
                        cantidad, respuesta.conversionResult(), fechaHoraFormateada
                ));

                System.out.println(CYAN + "---------------------------------------------------" + RESET);
                System.out.printf(YELLOW + "Tasa actual: 1 %s = %.6f %s\n" + RESET, respuesta.baseCode(), respuesta.conversionRate(), respuesta.targetCode());
                System.out.printf(GREEN + "RESULTADO: %.2f [%s] equivale a =>>> %.2f [%s]\n" + RESET,
                        cantidad, respuesta.baseCode(), respuesta.conversionResult(), respuesta.targetCode());
                System.out.println(CYAN + "---------------------------------------------------" + RESET);

            } catch (CurrencyConversionException e) {
                System.out.println(RED + "Ocurrio un error en la conversion: " + e.getMessage() + RESET);
                System.out.println(YELLOW + "Asegurese de que los codigos ingresados existan.\n" + RESET);
            } catch (Exception e) {
                logger.error("Error inesperado durante la conversion", e);
                System.out.println(RED + "Ocurrio un error inesperado. Consulte los logs para más detalles." + RESET);
            }
        }

        System.out.println(GREEN + "\nCerrando sistema... ¡Hasta luego!" + RESET);
        scanner.close();
    }

    private String leerEntrada(String mensaje) {
        while (true) {
            System.out.println(mensaje);
            System.out.println(YELLOW + "[Comandos: 'l' (lista) | 'h' (historial) | 'salir']" + RESET);
            System.out.print("> ");

            String entrada = scanner.nextLine().trim().toLowerCase();

            switch (entrada) {
                case "salir" -> { return "SALIR"; }
                case "h", "historial" -> gestorHistorialPaginado();
                case "l", "lista" -> gestorListaMonedas();
                default -> {
                    if (!entrada.isEmpty()) return entrada.toUpperCase();
                }
            }
            System.out.println(CYAN + "\n--- Volviendo a la operacion actual ---" + RESET);
        }
    }

    private void gestorListaMonedas() {
        String[][] todasLasMonedas;
        try {
            System.out.println(CYAN + "\nConsultando base de datos..." + RESET);
            todasLasMonedas = currencyService.obtenerMonedasSoportadas();
        } catch (CurrencyConversionException e) {
            System.out.println(RED + "Error: " + e.getMessage() + RESET);
            return;
        }

        String[][] monedasActuales = todasLasMonedas;
        int paginaActual = 1;
        int itemsPorPagina = 15;

        while (true) {
            int totalPaginas = (int) Math.ceil((double) monedasActuales.length / itemsPorPagina);
            if (totalPaginas == 0) totalPaginas = 1;
            if (paginaActual > totalPaginas) paginaActual = totalPaginas;

            System.out.println(CYAN + "\n===================================================" + RESET);
            System.out.println(YELLOW + "                LISTA DE MONEDAS" + RESET);
            System.out.println(CYAN + "===================================================" + RESET);

            int inicio = (paginaActual - 1) * itemsPorPagina;
            int fin = Math.min(inicio + itemsPorPagina, monedasActuales.length);

            if (monedasActuales.length == 0) {
                System.out.println(RED + "  No se encontraron resultados para tu busqueda." + RESET);
            } else {
                for (int i = inicio; i < fin; i++) {
                    System.out.printf("  %s - %s\n", monedasActuales[i][0], monedasActuales[i][1]);
                }
            }

            System.out.println(CYAN + "---------------------------------------------------" + RESET);
            System.out.printf(GREEN + "               Pagina %d de %d\n" + RESET, paginaActual, totalPaginas);
            System.out.println(CYAN + "---------------------------------------------------" + RESET);
            System.out.println(YELLOW + "[Comandos: 's' (siguiente) | 'a' (anterior) | 'b' (buscar) | 'v' (volver)]" + RESET);
            System.out.print("Lista > ");

            String comando = scanner.nextLine().trim().toLowerCase();

            switch (comando) {
                case "s", "siguiente" -> {
                    if (paginaActual < totalPaginas) paginaActual++;
                    else System.out.println(RED + "Ya estas en la ultima pagina." + RESET);
                }
                case "a", "anterior" -> {
                    if (paginaActual > 1) paginaActual--;
                    else System.out.println(RED + "Ya estas en la primera pagina." + RESET);
                }
                case "b", "buscar" -> {
                    System.out.print("Ingrese el termino a buscar (ej. Peso, Euro): ");
                    String termino = scanner.nextLine().trim();
                    monedasActuales = filtrarMonedas(todasLasMonedas, termino);
                    paginaActual = 1;
                }
                case "v", "volver" -> {
                    return;
                }
                default -> System.out.println(RED + "Comando no reconocido en la lista." + RESET);
            }
        }
    }

    private String[][] filtrarMonedas(String[][] todas, String filtro) {
        if (filtro.isEmpty()) return todas;

        String filtroLower = filtro.toLowerCase();
        int contador = 0;

        for (String[] par : todas) {
            if (par[0].toLowerCase().contains(filtroLower) || par[1].toLowerCase().contains(filtroLower)) contador++;
        }

        String[][] filtradas = new String[contador][2];
        int indice = 0;
        for (String[] par : todas) {
            if (par[0].toLowerCase().contains(filtroLower) || par[1].toLowerCase().contains(filtroLower)) {
                filtradas[indice++] = par;
            }
        }
        return filtradas;
    }

    private void gestorHistorialPaginado() {
        List<RegistroConversion> registros = historyRepository.obtenerHistorial();
        if (registros.isEmpty()) {
            System.out.println(YELLOW + "\n--- HISTORIAL DE CONVERSIONES ---" + RESET);
            System.out.println("Aun no hay conversiones registradas.");
            System.out.println(YELLOW + "---------------------------------" + RESET);
            return;
        }

        int totalRegistros = registros.size();
        int paginaActual = 1;
        int itemsPorPagina = 10;

        while (true) {
            int totalPaginas = (int) Math.ceil((double) totalRegistros / itemsPorPagina);
            if (totalPaginas == 0) totalPaginas = 1;
            if (paginaActual > totalPaginas) paginaActual = totalPaginas;

            System.out.println(CYAN + "\n===================================================" + RESET);
            System.out.println(YELLOW + "             HISTORIAL DE CONVERSIONES" + RESET);
            System.out.println(CYAN + "===================================================" + RESET);

            int inicio = (paginaActual - 1) * itemsPorPagina;
            int fin = Math.min(inicio + itemsPorPagina, totalRegistros);

            for (int i = inicio; i < fin; i++) {
                int indiceReal = totalRegistros - 1 - i;
                RegistroConversion r = registros.get(indiceReal);
                System.out.printf("  %d) [%s] %.2f %s -> %.2f %s\n",
                        indiceReal + 1, r.fechaHora(), r.cantidad(), r.monedaBase(), r.resultado(), r.monedaObjetivo());
            }

            System.out.println(CYAN + "---------------------------------------------------" + RESET);
            System.out.printf(GREEN + "               Pagina %d de %d\n" + RESET, paginaActual, totalPaginas);
            System.out.println(CYAN + "---------------------------------------------------" + RESET);
            System.out.println(YELLOW + "[Comandos: 's' (siguiente) | 'a' (anterior) | 'v' (volver)]" + RESET);
            System.out.print("Historial > ");

            String comando = scanner.nextLine().trim().toLowerCase();

            switch (comando) {
                case "s", "siguiente" -> {
                    if (paginaActual < totalPaginas) paginaActual++;
                    else System.out.println(RED + "Ya estas en la ultima pagina." + RESET);
                }
                case "a", "anterior" -> {
                    if (paginaActual > 1) paginaActual--;
                    else System.out.println(RED + "Ya estas en la primera pagina." + RESET);
                }
                case "v", "volver" -> {
                    return;
                }
                default -> System.out.println(RED + "Comando no reconocido en el historial." + RESET);
            }
        }
    }
}
