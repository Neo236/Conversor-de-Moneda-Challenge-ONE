package com.alura.conversor.ui;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsolaTest {

    private static Consola consolaCon(String entrada) {
        return new Consola(
                new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8))),
                new PrintStream(OutputStream.nullOutputStream()));
    }

    private static String escrito(boolean conColores) {
        var salida = new ByteArrayOutputStream();
        var consola = new Consola(
                new Scanner(new ByteArrayInputStream(new byte[0])),
                new PrintStream(salida, true, StandardCharsets.UTF_8),
                conColores);
        consola.titulo("hola");
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void leeUnaLineaYLeSacaLosEspacios() {
        assertEquals("USD", consolaCon("  USD  \n").leerLinea());
    }

    @Test
    void lanzaEntradaFinalizadaCuandoNoHayNadaQueLeer() {
        assertThrows(EntradaFinalizadaException.class, () -> consolaCon("").leerLinea());
    }

    @Test
    void lanzaEntradaFinalizadaAlAgotarseLaEntrada() {
        var consola = consolaCon("USD\n");

        assertEquals("USD", consola.leerLinea());
        assertThrows(EntradaFinalizadaException.class, consola::leerLinea);
    }

    @Test
    void emiteEscapesAnsiConLosColoresActivos() {
        assertTrue(escrito(true).contains("\033["));
    }

    @Test
    void noEmiteEscapesConLosColoresApagados() {
        // Es la degradación limpia: en cmd.exe clásico o con la salida redirigida,
        // los escapes se verían literales alrededor de cada línea.
        assertFalse(escrito(false).contains("\033["));
    }

    @Test
    void centraUnTextoEnElAnchoDeReferencia() {
        var centrado = Consola.centrar("abc");

        assertTrue(centrado.startsWith(" ".repeat((Consola.ANCHO - 3) / 2)));
        assertTrue(centrado.endsWith("abc"));
    }

    @Test
    void noDescentraUnTextoMasAnchoQueLaReferencia() {
        var largo = "x".repeat(Consola.ANCHO + 10);

        assertEquals(largo, Consola.centrar(largo));
    }
}
