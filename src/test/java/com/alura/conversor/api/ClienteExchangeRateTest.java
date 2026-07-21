package com.alura.conversor.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El cliente HTTP entra por constructor, así que estos tests ejercitan el cliente de
 * verdad: lo único simulado es la red.
 */
@ExtendWith(MockitoExtension.class)
class ClienteExchangeRateTest {

    @Mock
    private HttpClient http;

    @Mock
    private HttpResponse<String> respuesta;

    private ClienteExchangeRate cliente;

    @BeforeEach
    void setUp() {
        cliente = new ClienteExchangeRate(http, "una-api-key");
    }

    /**
     * Arma una respuesta HTTP completa. El código va con {@code lenient} porque el cliente
     * no siempre llega a leerlo: cuando el cuerpo ya dice qué falló, ese mensaje gana.
     */
    private void responderCon(int codigo, String cuerpo) throws Exception {
        lenient().when(respuesta.statusCode()).thenReturn(codigo);
        when(respuesta.body()).thenReturn(cuerpo);
        when(http.send(any(HttpRequest.class), this.<String>bodyHandler())).thenReturn(respuesta);
    }

    private HttpRequest peticionEnviada() throws Exception {
        var captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(http).send(captor.capture(), this.<String>bodyHandler());
        return captor.getValue();
    }

    @Test
    void convierteUnaRespuestaExitosa() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD","target_code":"ARS",
                 "conversion_rate":1010.5,"conversion_result":101050.0}""");

        var r = cliente.convertir("USD", "ARS", new BigDecimal("100"));

        assertTrue(r.exitosa());
        assertEquals("USD", r.baseCode());
        assertEquals(new BigDecimal("101050.0"), r.conversionResult());
    }

    @Test
    void noMandaLaCantidadEnNotacionCientifica() throws Exception {
        // Regresión: con double, 10000000 se serializaba como "1.0E7" y la API devolvía 404.
        responderCon(200, """
                {"result":"success","base_code":"USD","target_code":"ARS",
                 "conversion_rate":1010.5,"conversion_result":1.01050E10}""");

        cliente.convertir("USD", "ARS", new BigDecimal("10000000"));

        var url = peticionEnviada().uri().toString();
        assertTrue(url.endsWith("/pair/USD/ARS/10000000"), "URL inesperada: " + url);
        assertFalse(url.contains("E7"), "la cantidad no puede viajar en notación científica: " + url);
    }

    @Test
    void conservaLosDecimalesDeLaCantidad() throws Exception {
        responderCon(200, """
                {"result":"success","base_code":"USD","target_code":"ARS",
                 "conversion_rate":1010.5,"conversion_result":133.386}""");

        cliente.convertir("USD", "ARS", new BigDecimal("0.132"));

        assertTrue(peticionEnviada().uri().toString().endsWith("/pair/USD/ARS/0.132"));
    }

    @Test
    void detectaElErrorAunqueLaApiResponda200() throws Exception {
        // ExchangeRate-API contesta 200 con result:"error" en el cuerpo.
        responderCon(200, """
                {"result":"error","error-type":"unsupported-code"}""");

        var error = assertThrows(ConversionMonedaException.class,
                () -> cliente.convertir("USD", "XXX", BigDecimal.ONE));
        assertTrue(error.getMessage().contains("no existe"), error.getMessage());
    }

    @Test
    void traduceElErrorDeCuotaAgotada() throws Exception {
        responderCon(200, """
                {"result":"error","error-type":"quota-reached"}""");

        var error = assertThrows(ConversionMonedaException.class,
                () -> cliente.convertir("USD", "ARS", BigDecimal.ONE));
        assertTrue(error.getMessage().contains("cuota"), error.getMessage());
    }

    @Test
    void fallaSiElCodigoHttpNoEs200YElCuerpoNoDiceNada() throws Exception {
        responderCon(503, "<html>Service Unavailable</html>");

        var error = assertThrows(ConversionMonedaException.class,
                () -> cliente.convertir("USD", "ARS", BigDecimal.ONE));
        assertTrue(error.getMessage().contains("503"));
    }

    @Test
    void explicaElMotivoAunqueLaApiRespondaConUn403() throws Exception {
        // Ante una key inválida la API responde 403 y pone el motivo en el cuerpo.
        // Quedarse en el código HTTP obligaría a mostrar "error 403" pudiendo decir qué pasó.
        responderCon(403, """
                {"result":"error","error-type":"invalid-key"}""");

        var error = assertThrows(ConversionMonedaException.class,
                () -> cliente.convertir("USD", "ARS", BigDecimal.ONE));
        assertTrue(error.getMessage().contains("API Key no es válida"), error.getMessage());
    }

    @Test
    void traduceLosFallosDeRed() throws Exception {
        when(http.send(any(HttpRequest.class), this.<String>bodyHandler()))
                .thenThrow(new IOException("conexión rechazada"));

        assertThrows(ConversionMonedaException.class,
                () -> cliente.convertir("USD", "ARS", BigDecimal.ONE));
    }

    @Test
    void fallaSinApiKeyYSinTocarLaRed() throws Exception {
        var sinKey = new ClienteExchangeRate(http, null);

        assertThrows(ConversionMonedaException.class, () -> sinKey.convertir("USD", "ARS", BigDecimal.ONE));
        verify(http, times(0)).send(any(), any());
    }

    @Test
    void fallaConApiKeyVacia() {
        var keyVacia = new ClienteExchangeRate(http, "  ");

        assertThrows(ConversionMonedaException.class, keyVacia::monedasSoportadas);
    }

    @Test
    void mapeaLasMonedasSoportadas() throws Exception {
        responderCon(200, """
                {"result":"success","supported_codes":[["USD","United States Dollar"],["ARS","Argentine Peso"]]}""");

        var monedas = cliente.monedasSoportadas();

        assertEquals(2, monedas.size());
        assertEquals(new Moneda("USD", "United States Dollar"), monedas.getFirst());
    }

    @Test
    void noRompeSiLaApiNoDevuelveLaListaDeCodigos() throws Exception {
        // Antes, supported_codes ausente daba NullPointerException.
        responderCon(200, """
                {"result":"success"}""");

        assertThrows(ConversionMonedaException.class, cliente::monedasSoportadas);
    }

    @Test
    void lasMonedasSeConsultanUnaSolaVez() throws Exception {
        responderCon(200, """
                {"result":"success","supported_codes":[["USD","United States Dollar"]]}""");

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
