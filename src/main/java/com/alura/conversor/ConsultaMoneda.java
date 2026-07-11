package com.alura.conversor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsultaMoneda implements CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(ConsultaMoneda.class);
    
    private final HttpClient client;
    private final Gson gson;
    private final String apiKey;
    private String[][] monedasDisponiblesCache;

    public ConsultaMoneda(String apiKey) {
        this(HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build(), apiKey);
    }

    public ConsultaMoneda(HttpClient client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
        this.gson = new Gson();
        this.monedasDisponiblesCache = null;
    }

    private void validarApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("API key not configured.");
            throw new CurrencyConversionException("Error crítico: No hay API Key configurada. Defina la variable de entorno EXCHANGE_RATE_API_KEY.");
        }
    }

    @Override
    public RespuestaConversion convertir(String monedaBase, String monedaObjetivo, double cantidad) {
        validarApiKey();

        String direccion = "https://v6.exchangerate-api.com/v6/" + apiKey + "/pair/"
                + monedaBase + "/" + monedaObjetivo + "/" + cantidad;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(direccion))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

        try {
            logger.info("Realizando petición a la API para conversión de {} a {}", monedaBase, monedaObjetivo);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Error en la API. Código HTTP: {}", response.statusCode());
                throw new CurrencyConversionException("Error en la API: Código " + response.statusCode());
            }

            return gson.fromJson(response.body(), RespuestaConversion.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Petición interrumpida: {}", e.getMessage(), e);
            throw new CurrencyConversionException("La petición fue interrumpida: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Error de conexión al servidor: {}", e.getMessage(), e);
            throw new CurrencyConversionException("Error de conexión al servidor: " + e.getMessage(), e);
        } catch (JsonSyntaxException e) {
            logger.error("Error al leer los datos de la moneda: {}", e.getMessage(), e);
            throw new CurrencyConversionException("Error al leer los datos de la moneda: " + e.getMessage(), e);
        }
    }

    @Override
    public String[][] obtenerMonedasSoportadas() {
        if (this.monedasDisponiblesCache != null) {
            return this.monedasDisponiblesCache;
        }

        validarApiKey();

        String direccion = "https://v6.exchangerate-api.com/v6/" + apiKey + "/codes";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(direccion)).timeout(java.time.Duration.ofSeconds(10)).build();

        try {
            logger.info("Realizando petición a la API para obtener monedas soportadas.");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.error("Error en la API. Código HTTP: {}", response.statusCode());
                throw new CurrencyConversionException("Error al obtener la lista de códigos: " + response.statusCode());
            }

            RespuestaCodigos respuesta = gson.fromJson(response.body(), RespuestaCodigos.class);
            this.monedasDisponiblesCache = respuesta.supportedCodes();
            return this.monedasDisponiblesCache;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Petición interrumpida al obtener códigos: {}", e.getMessage(), e);
            throw new CurrencyConversionException("La petición fue interrumpida: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Error de conexión al obtener códigos: {}", e.getMessage(), e);
            throw new CurrencyConversionException("Error de conexión al obtener códigos: " + e.getMessage(), e);
        } catch (JsonSyntaxException e) {
            logger.error("Error al leer el formato de los códigos: {}", e.getMessage(), e);
            throw new CurrencyConversionException("Error al leer el formato de los códigos: " + e.getMessage(), e);
        }
    }
}