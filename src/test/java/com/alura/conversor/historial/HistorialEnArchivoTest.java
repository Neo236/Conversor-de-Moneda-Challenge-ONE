package com.alura.conversor.historial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Usa @TempDir en lugar de escribir en el directorio de trabajo: los tests no deberían
 * dejar archivos sueltos en el repositorio ni pisarse entre corridas.
 */
class HistorialEnArchivoTest {

    @TempDir
    Path carpeta;

    private static RegistroConversion registro(String base, String objetivo) {
        return new RegistroConversion(base, objetivo,
                new BigDecimal("100"), new BigDecimal("101050.00"), "2026-07-15 10:00:00");
    }

    @Test
    void guardaYDevuelveLosRegistros() {
        var archivo = carpeta.resolve("historial.json");
        var historial = new HistorialEnArchivo(archivo);

        historial.agregarRegistro(registro("USD", "ARS"));

        assertEquals(1, historial.obtenerHistorial().size());
        assertEquals("USD", historial.obtenerHistorial().getFirst().monedaBase());
        assertTrue(Files.exists(archivo), "el historial debe quedar escrito en disco");
    }

    @Test
    void loGuardadoSobreviveAReabrirElArchivo() {
        var archivo = carpeta.resolve("historial.json");
        new HistorialEnArchivo(archivo).agregarRegistro(registro("USD", "ARS"));

        var recargado = new HistorialEnArchivo(archivo);

        assertEquals(1, recargado.obtenerHistorial().size());
        assertEquals("ARS", recargado.obtenerHistorial().getFirst().monedaObjetivo());
    }

    @Test
    void noPierdePrecisionAlPersistir() {
        var archivo = carpeta.resolve("historial.json");
        var exacto = new BigDecimal("0.1");
        new HistorialEnArchivo(archivo).agregarRegistro(
                new RegistroConversion("USD", "ARS", exacto, new BigDecimal("101.05"), "2026-07-15 10:00:00"));

        var recargado = new HistorialEnArchivo(archivo);

        assertEquals(0, exacto.compareTo(recargado.obtenerHistorial().getFirst().cantidad()));
    }

    @Test
    void empiezaVacioSiElArchivoNoExiste() {
        var historial = new HistorialEnArchivo(carpeta.resolve("no-existe.json"));

        assertTrue(historial.obtenerHistorial().isEmpty());
    }

    @Test
    void unHistorialCorruptoNoImpideArrancar() throws Exception {
        // Antes, un JSON inválido lanzaba JsonSyntaxException desde el constructor
        // y tiraba la aplicación abajo antes de mostrar el menú.
        var archivo = carpeta.resolve("historial.json");
        Files.writeString(archivo, "{ esto no es json", StandardCharsets.UTF_8);

        var historial = new HistorialEnArchivo(archivo);

        assertTrue(historial.obtenerHistorial().isEmpty());
    }

    @Test
    void unArchivoVacioTampocoRompe() throws Exception {
        var archivo = carpeta.resolve("historial.json");
        Files.writeString(archivo, "", StandardCharsets.UTF_8);

        assertTrue(new HistorialEnArchivo(archivo).obtenerHistorial().isEmpty());
    }

    @Test
    void elHistorialQueSeDevuelveNoSePuedeModificarDesdeAfuera() {
        var historial = new HistorialEnArchivo(carpeta.resolve("historial.json"));
        historial.agregarRegistro(registro("USD", "ARS"));

        assertThrows(UnsupportedOperationException.class,
                () -> historial.obtenerHistorial().add(registro("EUR", "BRL")));
    }
}
