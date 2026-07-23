package com.alura.conversor.ui;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Recorre una lista por páginas en la terminal.
 *
 * <p>La lista de monedas y el historial tenían cada uno su propio bucle de paginación:
 * cuarenta líneas casi calcadas, con los mismos comandos y los mismos bordes. Esta clase
 * es ese bucle, una sola vez.
 *
 * @param <T> tipo de los elementos a mostrar
 */
public class Paginador<T> {

    private final Consola consola;
    private final String titulo;
    private final int porPagina;
    private final Function<T, String> formatear;

    /** Cómo decidir si un elemento coincide con lo buscado; null desactiva la búsqueda. */
    private final BiPredicate<T, String> coincide;

    public Paginador(Consola consola, String titulo, int porPagina, Function<T, String> formatear) {
        this(consola, titulo, porPagina, formatear, null);
    }

    public Paginador(Consola consola, String titulo, int porPagina,
                     Function<T, String> formatear, BiPredicate<T, String> coincide) {
        this.consola = consola;
        this.titulo = titulo;
        this.porPagina = porPagina;
        this.formatear = formatear;
        this.coincide = coincide;
    }

    /**
     * Muestra la lista hasta que el usuario elija volver.
     *
     * @return {@code true} si en vez de volver pidió salir de la aplicación: quien
     *         llama decide cómo propagar el cierre.
     */
    public boolean recorrer(List<T> items) {
        var visibles = items;
        var pagina = 1;

        while (true) {
            var totalPaginas = Math.max(1, (int) Math.ceil((double) visibles.size() / porPagina));
            pagina = Math.min(pagina, totalPaginas);

            mostrarPagina(visibles, pagina, totalPaginas);

            var comando = pedirComando();
            switch (comando) {
                case "s", "siguiente" -> {
                    if (pagina < totalPaginas) {
                        pagina++;
                    } else {
                        consola.error("Ya estás en la última página.");
                    }
                }
                case "a", "anterior" -> {
                    if (pagina > 1) {
                        pagina--;
                    } else {
                        consola.error("Ya estás en la primera página.");
                    }
                }
                case "b", "buscar" -> {
                    if (coincide == null) {
                        consola.error("Comando no reconocido.");
                    } else {
                        visibles = filtrar(items);
                        pagina = 1;
                    }
                }
                case "v", "volver" -> {
                    return false;
                }
                case "salir" -> {
                    return true;
                }
                default -> consola.error("Comando no reconocido.");
            }
        }
    }

    private void mostrarPagina(List<T> visibles, int pagina, int totalPaginas) {
        consola.titulo("\n===================================================");
        consola.aviso("                " + titulo);
        consola.titulo("===================================================");

        if (visibles.isEmpty()) {
            consola.error("  No se encontraron resultados.");
        } else {
            var desde = (pagina - 1) * porPagina;
            var hasta = Math.min(desde + porPagina, visibles.size());
            visibles.subList(desde, hasta).forEach(item -> consola.imprimir("  " + formatear.apply(item)));
        }

        consola.separador();
        consola.exito(String.format("               Página %d de %d", pagina, totalPaginas));
        consola.separador();
    }

    private String pedirComando() {
        var comandos = coincide == null
                ? "[Comandos: s siguiente | a anterior | v volver | salir]"
                : "[Comandos: s siguiente | a anterior | b buscar | v volver | salir]";
        consola.aviso(comandos);
        consola.imprimirSinSalto("> ");
        return consola.leerLinea().toLowerCase();
    }

    private List<T> filtrar(List<T> items) {
        consola.imprimirSinSalto("Ingresá el término a buscar (ej. Peso, Euro): ");
        var termino = consola.leerLinea();
        if (termino.isEmpty()) {
            return items;
        }
        return items.stream().filter(item -> coincide.test(item, termino)).toList();
    }
}
