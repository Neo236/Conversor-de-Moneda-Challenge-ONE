package com.alura.conversor.ui;

import com.alura.conversor.api.ConversionMonedaException;
import com.alura.conversor.api.Moneda;
import com.alura.conversor.api.RespuestaConversion;
import com.alura.conversor.api.ServicioDeCambio;
import com.alura.conversor.historial.RegistroConversion;
import com.alura.conversor.historial.RepositorioHistorial;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Maneja la interfaz por teclado y salida simulados. Antes esta clase concentraba el
 * flujo entero en 261 líneas y no tenía un solo test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConversorUITest {

    @Mock
    private ServicioDeCambio servicio;

    @Mock
    private RepositorioHistorial historial;

    private ByteArrayOutputStream salida;

    /** Corre la interfaz con las teclas dadas y devuelve todo lo que imprimió. */
    private String correrCon(String entrada) {
        salida = new ByteArrayOutputStream();
        var consola = new Consola(
                new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8))),
                new PrintStream(salida, true, StandardCharsets.UTF_8));

        new ConversorUI(servicio, historial, consola).iniciar();
        return salida.toString(StandardCharsets.UTF_8);
    }

    private static RespuestaConversion conversionExitosa() {
        return new RespuestaConversion("success", null, "USD", "ARS",
                new BigDecimal("1010.5"), new BigDecimal("101050.00"));
    }

    @Test
    void haceUnaConversionCompletaYLaGuardaEnElHistorial() {
        when(servicio.convertir("USD", "ARS", new BigDecimal("100"))).thenReturn(conversionExitosa());

        var texto = correrCon("USD\nARS\n100\nsalir\n");

        assertTrue(texto.contains("101050.00"), "debe mostrar el resultado");
        var registro = ArgumentCaptor.forClass(RegistroConversion.class);
        verify(historial).agregarRegistro(registro.capture());
        assertEquals("USD", registro.getValue().monedaBase());
        assertEquals(0, new BigDecimal("100").compareTo(registro.getValue().cantidad()));
    }

    @Test
    void noConsultaLaApiSiLaMonedaNoTieneTresLetras() {
        var texto = correrCon("US\nUSD\nARS\n100\nsalir\n");

        assertTrue(texto.contains("exactamente 3 letras"));
        when(servicio.convertir(any(), any(), any())).thenReturn(conversionExitosa());
    }

    @Test
    void rechazaUnaCantidadQueNoEsNumero() {
        when(servicio.convertir(any(), any(), any())).thenReturn(conversionExitosa());

        var texto = correrCon("USD\nARS\nabc\n100\nsalir\n");

        assertTrue(texto.contains("Formato inválido"));
        verify(servicio).convertir("USD", "ARS", new BigDecimal("100"));
    }

    @Test
    void rechazaCantidadesNegativasOCero() {
        when(servicio.convertir(any(), any(), any())).thenReturn(conversionExitosa());

        var texto = correrCon("USD\nARS\n-5\n0\n100\nsalir\n");

        assertTrue(texto.contains("mayor a cero"));
        verify(servicio).convertir("USD", "ARS", new BigDecimal("100"));
    }

    @Test
    void aceptaLaComaComoSeparadorDecimal() {
        when(servicio.convertir(any(), any(), any())).thenReturn(conversionExitosa());

        correrCon("USD\nARS\n10,50\nsalir\n");

        verify(servicio).convertir("USD", "ARS", new BigDecimal("10.50"));
    }

    @Test
    void muestraElErrorDeLaApiSinCaerse() {
        when(servicio.convertir(any(), any(), any()))
                .thenThrow(new ConversionMonedaException("Alguno de los códigos de moneda no existe"));

        var texto = correrCon("USD\nXXX\n100\nsalir\n");

        assertTrue(texto.contains("no existe"));
        assertTrue(texto.contains("Hasta luego"), "la aplicación debe seguir viva y cerrar bien");
        verify(historial, never()).agregarRegistro(any());
    }

    @Test
    void noGuardaEnElHistorialSiLaConversionFalla() {
        when(servicio.convertir(any(), any(), any()))
                .thenThrow(new ConversionMonedaException("Se agotó la cuota"));

        correrCon("USD\nARS\n100\nsalir\n");

        verify(historial, never()).agregarRegistro(any());
    }

    @Test
    void salirCierraLaAplicacion() {
        var texto = correrCon("salir\n");

        assertTrue(texto.contains("Hasta luego"));
        verify(servicio, never()).convertir(any(), any(), any());
    }

    @Test
    void unaEntradaCerradaNoRevientaLaAplicacion() {
        // Regresión: sin nada que leer, Scanner.nextLine lanzaba NoSuchElementException
        // y la excepción escapaba de main con un stack trace en la cara del usuario.
        var texto = correrCon("");

        assertTrue(texto.contains("Entrada finalizada"));
        assertTrue(texto.contains("Hasta luego"));
    }

    @Test
    void elComandoDeListaMuestraLasMonedas() {
        when(servicio.monedasSoportadas()).thenReturn(List.of(
                new Moneda("USD", "United States Dollar"),
                new Moneda("ARS", "Argentine Peso")));

        var texto = correrCon("l\nv\nsalir\n");

        assertTrue(texto.contains("LISTA DE MONEDAS"));
        assertTrue(texto.contains("United States Dollar"));
    }

    @Test
    void elComandoDeHistorialAvisaCuandoEstaVacio() {
        when(historial.obtenerHistorial()).thenReturn(List.of());

        var texto = correrCon("h\nsalir\n");

        assertTrue(texto.contains("Todavía no hay conversiones"));
    }

    @Test
    void elHistorialMuestraLoMasRecientePrimero() {
        when(historial.obtenerHistorial()).thenReturn(List.of(
                new RegistroConversion("USD", "ARS", new BigDecimal("1"), new BigDecimal("1010"), "2026-07-01 10:00:00"),
                new RegistroConversion("EUR", "BRL", new BigDecimal("2"), new BigDecimal("12"), "2026-07-15 10:00:00")));

        var texto = correrCon("h\nv\nsalir\n");

        var posicionReciente = texto.indexOf("2026-07-15");
        var posicionVieja = texto.indexOf("2026-07-01");
        assertTrue(posicionReciente < posicionVieja, "el registro más nuevo debe aparecer primero");
    }
}
