package com.alura.conversor.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El modo sin clave: el cliente del endpoint abierto baja la tabla de tasas y convierte
 * localmente. Igual que en {@link ClienteExchangeRateTest}, lo único simulado es la red.
 */
@ExtendWith(MockitoExtension.class)
class ClienteTasasAbiertasTest {

    @Mock
    private HttpClient http;

    @Mock
    private HttpResponse<String> respuesta;

    private ClienteTasasAbiertas cliente;

    @BeforeEach
    void setUp() {
        cliente = new ClienteTasasAbiertas(http);
    }

    private void responderCon(int codigo, String cuerpo) throws Exception {
        lenient().when(respuesta.statusCode()).thenReturn(codigo);
        when(respuesta.body()).thenReturn(cuerpo);
        when(http.send(any(HttpRequest.class), this.<String>bodyHandler())).thenReturn(respuesta);
    }

    @Test
    void convierteConLaTasaDelDia() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD","rates":{"ARS":1480.18,"BRL":5.0766}}""");

        var r = cliente.convertir("USD", "BRL", new BigDecimal("250"));

        assertTrue(r.exitosa());
        assertEquals("USD", r.baseCode());
        assertEquals("BRL", r.targetCode());
        assertEquals(new BigDecimal("5.0766"), r.conversionRate());
        assertEquals(0, new BigDecimal("1269.15").compareTo(r.conversionResult()));
    }

    @Test
    void elResultadoEsElProductoExactoSinRedondear() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD","rates":{"ARS":1480.185}}""");

        var r = cliente.convertir("USD", "ARS", BigDecimal.ONE);

        assertEquals(0, new BigDecimal("1480.185").compareTo(r.conversionResult()));
    }

    @Test
    void unResultadoChicoNoSeDestruye() throws Exception {
        // Regresión: un redondeo a 2 decimales convertía 1 ARS en 0,00 USD, y ese 0,00
        // se persistía en el historial.
        responderCon(200, """
                {"result":"success","base_code":"ARS","rates":{"USD":0.000676}}""");

        var r = cliente.convertir("ARS", "USD", BigDecimal.ONE);

        assertEquals(0, new BigDecimal("0.000676").compareTo(r.conversionResult()));
    }

    @Test
    void consultaElEndpointAbiertoConLaMonedaDeOrigen() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"EUR","rates":{"ARS":1689.78}}""");

        cliente.convertir("EUR", "ARS", BigDecimal.ONE);

        var captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(http).send(captor.capture(), this.<String>bodyHandler());
        var url = captor.getValue().uri().toString();
        assertTrue(url.endsWith("/v6/latest/EUR"), "URL inesperada: " + url);
    }

    @Test
    void fallaConMensajeClaroSiLaMonedaObjetivoNoEstaEnLaTabla() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD","rates":{"ARS":1480.18}}""");

        var error = assertThrows(ConversionMonedaException.class,
                () -> cliente.convertir("USD", "XXX", BigDecimal.ONE));
        assertTrue(error.getMessage().contains("no existe"), error.getMessage());
    }

    @Test
    void traduceElErrorDeUnaMonedaBaseDesconocida() throws Exception {
        responderCon(404, """
                {"result":"error","error-type":"unsupported-code"}""");

        var error = assertThrows(ConversionMonedaException.class,
                () -> cliente.convertir("XXX", "USD", BigDecimal.ONE));
        assertTrue(error.getMessage().contains("no existe"), error.getMessage());
    }

    @Test
    void fallaSiElEndpointNoDevuelveLaTablaDeTasas() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD"}""");

        assertThrows(ConversionMonedaException.class,
                () -> cliente.convertir("USD", "ARS", BigDecimal.ONE));
    }

    @Test
    void cacheaLasTasasPorMonedaDeOrigen() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD","rates":{"ARS":1480.18,"BRL":5.0766}}""");

        cliente.convertir("USD", "ARS", BigDecimal.ONE);
        cliente.convertir("USD", "BRL", BigDecimal.TEN);

        verify(http, times(1)).send(any(HttpRequest.class), this.<String>bodyHandler());
    }

    @Test
    void armaLaListaDeMonedasDesdeLaTablaConNombresDelJdk() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD","rates":{"USD":1,"FOK":0.79,"ARS":1480.18}}""");

        var monedas = cliente.monedasSoportadas();

        assertEquals(3, monedas.size());
        assertEquals("ARS", monedas.getFirst().codigo(), "la lista sale ordenada por código");
        assertTrue(monedas.getFirst().nombre() != null && !monedas.getFirst().nombre().isBlank());
        // FOK no es ISO 4217 para el JDK: queda el código como nombre.
        assertTrue(monedas.stream().anyMatch(m -> m.codigo().equals("FOK") && m.nombre().equals("FOK")));
    }

    @Test
    void lasMonedasSeConsultanUnaSolaVez() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD","rates":{"USD":1}}""");

        var primera = cliente.monedasSoportadas();
        var segunda = cliente.monedasSoportadas();

        assertSame(primera, segunda);
        verify(http, times(1)).send(any(HttpRequest.class), this.<String>bodyHandler());
    }

    /** Fija el tipo del matcher para que {@code send} devuelva un HttpResponse&lt;String&gt;. */
    private <T> HttpResponse.BodyHandler<T> bodyHandler() {
        return any();
    }
}
