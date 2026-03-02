import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        ConsultaMoneda consulta = new ConsultaMoneda();
        GestorHistorial historial = new GestorHistorial();

        System.out.println("***************************************************");
        System.out.println("Bienvenido al Conversor de Moneda V2 =)");
        System.out.println("***************************************************");

        buclePrincipal:
        while (true) {
            String monedaBase = "";
            while (monedaBase.length() != 3) {
                monedaBase = leerEntrada(teclado, consulta, historial, "\nElija la moneda de ORIGEN (codigo de 3 letras, ej. USD):");
                if (monedaBase.equals("SALIR")) break buclePrincipal;
                if (monedaBase.length() != 3) System.out.println("Por favor, ingrese exactamente 3 letras para la moneda.");
            }

            String monedaObjetivo = "";
            while (monedaObjetivo.length() != 3) {
                monedaObjetivo = leerEntrada(teclado, consulta, historial, "\n¿A que moneda desea CONVERTIR? (ej. ARS, COP):");
                if (monedaObjetivo.equals("SALIR")) break buclePrincipal;
                if (monedaObjetivo.length() != 3) System.out.println("Por favor, ingrese exactamente 3 letras para la moneda.");
            }

            double cantidad = 0;
            boolean cantidadValida = false;
            while (!cantidadValida) {
                String entradaCantidad = leerEntrada(teclado, consulta, historial, "\nIngrese la CANTIDAD a convertir:");
                if (entradaCantidad.equals("SALIR")) break buclePrincipal;

                try {
                    cantidad = Double.parseDouble(entradaCantidad.replace(",", "."));
                    if (cantidad <= 0) {
                        System.out.println("Por favor, ingrese un valor mayor a cero.");
                    } else {
                        cantidadValida = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Formato invalido. Debe ingresar un numero.");
                }
            }

            System.out.println("\nProcesando conversion...");
            try {
                RespuestaConversion respuesta = consulta.convertir(monedaBase, monedaObjetivo, cantidad);
                String fechaHoraFormateada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                historial.agregarRegistro(new RegistroConversion(
                        respuesta.base_code(), respuesta.target_code(),
                        cantidad, respuesta.conversion_result(), fechaHoraFormateada
                ));

                System.out.println("---------------------------------------------------");
                System.out.printf("Tasa actual: 1 %s = %.6f %s\n", respuesta.base_code(), respuesta.conversion_rate(), respuesta.target_code());
                System.out.printf("RESULTADO: %.2f [%s] equivale a =>>> %.2f [%s]\n",
                        cantidad, respuesta.base_code(), respuesta.conversion_result(), respuesta.target_code());
                System.out.println("---------------------------------------------------");

            } catch (RuntimeException e) {
                System.out.println("Ocurrio un error en la conversion: " + e.getMessage());
                System.out.println("Asegurese de que los codigos ingresados existan.\n");
            }
        }

        System.out.println("\nCerrando sistema... ¡Hasta luego!");
        teclado.close();
    }

    private static String leerEntrada(Scanner teclado, ConsultaMoneda consulta, GestorHistorial historial, String mensaje) {
        while (true) {
            System.out.println(mensaje);
            System.out.println("[Comandos: 'l' (lista) | 'h' (historial) | 'salir']");
            System.out.print("> ");

            String entrada = teclado.nextLine().trim().toLowerCase();

            switch (entrada) {
                case "salir" -> { return "SALIR"; }
                case "h", "historial" -> gestorHistorialPaginado(historial, teclado);
                case "l", "lista" -> gestorListaMonedas(consulta, teclado);
                default -> {
                    if (!entrada.isEmpty()) return entrada.toUpperCase();
                }
            }
            System.out.println("\n--- Volviendo a la operacion actual ---");
        }
    }

    private static void gestorListaMonedas(ConsultaMoneda consulta, Scanner teclado) {
        String[][] todasLasMonedas;
        try {
            System.out.println("\nConsultando base de datos...");
            todasLasMonedas = consulta.obtenerMonedasSoportadas();
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        String[][] monedasActuales = todasLasMonedas;
        int paginaActual = 1;
        int itemsPorPagina = 15;

        while (true) {
            int totalPaginas = (int) Math.ceil((double) monedasActuales.length / itemsPorPagina);
            if (totalPaginas == 0) totalPaginas = 1;
            if (paginaActual > totalPaginas) paginaActual = totalPaginas;

            System.out.println("\n===================================================");
            System.out.println("                LISTA DE MONEDAS");
            System.out.println("===================================================");

            int inicio = (paginaActual - 1) * itemsPorPagina;
            int fin = Math.min(inicio + itemsPorPagina, monedasActuales.length);

            if (monedasActuales.length == 0) {
                System.out.println("  No se encontraron resultados para tu busqueda.");
            } else {
                for (int i = inicio; i < fin; i++) {
                    System.out.printf("  %s - %s\n", monedasActuales[i][0], monedasActuales[i][1]);
                }
            }

            System.out.println("---------------------------------------------------");
            System.out.printf("               Pagina %d de %d\n", paginaActual, totalPaginas);
            System.out.println("---------------------------------------------------");
            System.out.println("[Comandos: 's' (siguiente) | 'a' (anterior) | 'b' (buscar) | 'v' (volver)]");
            System.out.print("Lista > ");

            String comando = teclado.nextLine().trim().toLowerCase();

            switch (comando) {
                case "s", "siguiente" -> {
                    if (paginaActual < totalPaginas) paginaActual++;
                    else System.out.println("Ya estas en la ultima pagina.");
                }
                case "a", "anterior" -> {
                    if (paginaActual > 1) paginaActual--;
                    else System.out.println("Ya estas en la primera pagina.");
                }
                case "b", "buscar" -> {
                    System.out.print("Ingrese el termino a buscar (ej. Peso, Euro): ");
                    String termino = teclado.nextLine().trim();
                    monedasActuales = filtrarMonedas(todasLasMonedas, termino);
                    paginaActual = 1;
                }
                case "v", "volver" -> {
                    return;
                }
                default -> System.out.println("Comando no reconocido en la lista.");
            }
        }
    }

    private static String[][] filtrarMonedas(String[][] todas, String filtro) {
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

    private static void gestorHistorialPaginado(GestorHistorial historial, Scanner teclado) {
        List<RegistroConversion> registros = historial.obtenerHistorial();
        if (registros.isEmpty()) {
            System.out.println("\n--- HISTORIAL DE CONVERSIONES ---");
            System.out.println("Aun no hay conversiones registradas.");
            System.out.println("---------------------------------");
            return;
        }

        int totalRegistros = registros.size();
        int paginaActual = 1;
        int itemsPorPagina = 10;

        while (true) {
            int totalPaginas = (int) Math.ceil((double) totalRegistros / itemsPorPagina);
            if (totalPaginas == 0) totalPaginas = 1;
            if (paginaActual > totalPaginas) paginaActual = totalPaginas;

            System.out.println("\n===================================================");
            System.out.println("             HISTORIAL DE CONVERSIONES");
            System.out.println("===================================================");

            int inicio = (paginaActual - 1) * itemsPorPagina;
            int fin = Math.min(inicio + itemsPorPagina, totalRegistros);

            for (int i = inicio; i < fin; i++) {
                int indiceReal = totalRegistros - 1 - i;
                RegistroConversion r = registros.get(indiceReal);
                System.out.printf("  %d) [%s] %.2f %s -> %.2f %s\n",
                        indiceReal + 1, r.fechaHora(), r.cantidad(), r.monedaBase(), r.resultado(), r.monedaObjetivo());
            }

            System.out.println("---------------------------------------------------");
            System.out.printf("               Pagina %d de %d\n", paginaActual, totalPaginas);
            System.out.println("---------------------------------------------------");
            System.out.println("[Comandos: 's' (siguiente) | 'a' (anterior) | 'v' (volver)]");
            System.out.print("Historial > ");

            String comando = teclado.nextLine().trim().toLowerCase();

            switch (comando) {
                case "s", "siguiente" -> {
                    if (paginaActual < totalPaginas) paginaActual++;
                    else System.out.println("Ya estas en la ultima pagina.");
                }
                case "a", "anterior" -> {
                    if (paginaActual > 1) paginaActual--;
                    else System.out.println("Ya estas en la primera pagina.");
                }
                case "v", "volver" -> {
                    return;
                }
                default -> System.out.println("Comando no reconocido en el historial.");
            }
        }
    }
}