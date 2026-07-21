package com.alura.conversor.ui;

import com.alura.conversor.api.Moneda;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginadorTest {

    private ByteArrayOutputStream salida;

    private Paginador<Moneda> paginadorCon(String entrada) {
        salida = new ByteArrayOutputStream();
        var consola = new Consola(
                new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8))),
                new PrintStream(salida, true, StandardCharsets.UTF_8));
        return new Paginador<>(consola, "MONEDAS", 2,
                m -> m.codigo() + " - " + m.nombre(), Moneda::coincideCon);
    }

    private static List<Moneda> monedas(int cantidad) {
        return IntStream.rangeClosed(1, cantidad)
                .mapToObj(i -> new Moneda("M" + i, "Moneda " + i))
                .toList();
    }

    private String texto() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void muestraSoloLaPrimeraPagina() {
        paginadorCon("v\n").recorrer(monedas(5));

        assertTrue(texto().contains("M1"));
        assertTrue(texto().contains("M2"));
        assertFalse(texto().contains("M3"), "M3 pertenece a la segunda página");
    }

    @Test
    void calculaBienElTotalDePaginas() {
        paginadorCon("v\n").recorrer(monedas(5));

        assertTrue(texto().contains("Página 1 de 3"), texto());
    }

    @Test
    void avanzaYRetrocede() {
        paginadorCon("s\na\nv\n").recorrer(monedas(5));

        assertTrue(texto().contains("Página 2 de 3"));
        assertTrue(texto().contains("M3"), "la segunda página debe mostrar M3");
    }

    @Test
    void avisaEnLosBordes() {
        paginadorCon("a\nv\n").recorrer(monedas(3));

        assertTrue(texto().contains("primera página"));
    }

    @Test
    void avisaAlLlegarALaUltimaPagina() {
        paginadorCon("s\ns\nv\n").recorrer(monedas(3));

        assertTrue(texto().contains("última página"));
    }

    @Test
    void filtraPorTermino() {
        paginadorCon("b\nMoneda 3\nv\n").recorrer(monedas(5));

        assertTrue(texto().contains("M3"));
        assertTrue(texto().contains("Página 1 de 1"), "tras filtrar debe quedar una sola página");
    }

    @Test
    void avisaCuandoElFiltroNoEncuentraNada() {
        paginadorCon("b\nzzz\nv\n").recorrer(monedas(5));

        assertTrue(texto().contains("No se encontraron resultados"));
    }

    @Test
    void unaListaVaciaNoRompeElCalculoDePaginas() {
        paginadorCon("v\n").recorrer(List.of());

        assertTrue(texto().contains("Página 1 de 1"));
        assertTrue(texto().contains("No se encontraron resultados"));
    }

    @Test
    void rechazaComandosDesconocidos() {
        paginadorCon("x\nv\n").recorrer(monedas(3));

        assertTrue(texto().contains("Comando no reconocido"));
    }
}
