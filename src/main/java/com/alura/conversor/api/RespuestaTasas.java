package com.alura.conversor.api;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Map;

/** Respuesta de {@code /latest/{base}} del endpoint abierto (open.er-api.com). */
public record RespuestaTasas(
        String result,
        @SerializedName("error-type") String errorType,
        @SerializedName("base_code") String baseCode,
        Map<String, BigDecimal> rates
) implements RespuestaApi {

    /** La tasa hacia la moneda pedida, o null si no está en la tabla. */
    public BigDecimal tasa(String codigo) {
        return rates == null ? null : rates.get(codigo);
    }
}
