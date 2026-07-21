package com.alura.conversor.ui;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsolaTest {

    private static Consola consolaCon(String entrada) {
        return new Consola(
                new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8))),
                new PrintStream(OutputStream.nullOutputStream()));
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
}
