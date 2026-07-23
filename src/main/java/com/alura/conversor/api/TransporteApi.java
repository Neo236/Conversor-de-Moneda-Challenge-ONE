package com.alura.conversor.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Lo que comparten los dos clientes de ExchangeRate-API: hacer la petición, interpretar
 * el JSON y traducir los códigos de error a un mensaje que le sirva a quien está en la
 * terminal.
 */
final class TransporteApi {

    private static final Logger logger = LoggerFactory.getLogger(TransporteApi.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient client;
    private final Gson gson = new Gson();

    TransporteApi(HttpClient client) {
        this.client = client;
    }

    static TransporteApi porDefecto() {
        return new TransporteApi(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
    }

    /**
     * Consulta la API y devuelve la respuesta ya validada.
     *
     * <p>El cuerpo se interpreta antes de mirar el código HTTP a propósito: ante una key
     * inválida la API responde 403 con {@code error-type: invalid-key}, y quedarse en el
     * código HTTP obligaría a mostrar "error 403" pudiendo decir qué pasó realmente.
     */
    <T extends RespuestaApi> T pedir(String url, Class<T> tipo) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConversionMonedaException("La petición fue interrumpida", e);
        } catch (IOException e) {
            throw new ConversionMonedaException("No se pudo conectar con la API: " + e.getMessage(), e);
        }

        var respuesta = interpretar(response.body(), tipo);

        if (respuesta != null && !respuesta.exitosa()) {
            throw new ConversionMonedaException(describirError(respuesta.errorType()));
        }
        if (response.statusCode() != 200) {
            throw new ConversionMonedaException("La API respondió con el código " + response.statusCode());
        }
        if (respuesta == null) {
            throw new ConversionMonedaException("La API devolvió una respuesta vacía");
        }
        return respuesta;
    }

    /** Devuelve null si el cuerpo no es JSON: el código HTTP dirá qué pasó. */
    private <T extends RespuestaApi> T interpretar(String cuerpo, Class<T> tipo) {
        try {
            return gson.fromJson(cuerpo, tipo);
        } catch (JsonSyntaxException e) {
            logger.warn("La API devolvió un cuerpo que no se pudo interpretar", e);
            return null;
        }
    }

    /** Traduce los códigos de error de la API a algo que le sirva a quien está en la terminal. */
    private static String describirError(String errorType) {
        if (errorType == null) {
            return "La API devolvió una respuesta inesperada";
        }
        return switch (errorType) {
            case "unsupported-code" -> "Alguno de los códigos de moneda no existe";
            case "invalid-key" -> "La API Key no es válida";
            case "inactive-account" -> "La cuenta de la API no está activa (falta confirmar el email)";
            case "quota-reached" -> "Se agotó la cuota de consultas del plan";
            case "malformed-request" -> "La petición está mal formada";
            default -> "La API devolvió el error: " + errorType;
        };
    }
}
