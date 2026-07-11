package com.alura.conversor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsultaMonedaTest {

    private HttpClient httpClientMock;

    @BeforeEach
    void setUp() {
        httpClientMock = Mockito.mock(HttpClient.class);
    }

    @Test
    void convertirLanzaExcepcionSinApiKey() {
        ConsultaMoneda sinApiKey = new ConsultaMoneda(httpClientMock, null);
        assertThrows(CurrencyConversionException.class,
                () -> sinApiKey.convertir("USD", "ARS", 100));
    }

    @Test
    void obtenerMonedasLanzaExcepcionConApiKeyVacia() {
        ConsultaMoneda apiKeyVacia = new ConsultaMoneda(httpClientMock, "");
        assertThrows(CurrencyConversionException.class,
                apiKeyVacia::obtenerMonedasSoportadas);
    }
}
