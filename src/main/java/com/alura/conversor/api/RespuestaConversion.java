package com.alura.conversor.api;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/** Respuesta de {@code /pair/{base}/{objetivo}/{cantidad}}. */
public record RespuestaConversion(
        String result,
        @SerializedName("error-type") String errorType,
        @SerializedName("base_code") String baseCode,
        @SerializedName("target_code") String targetCode,
        @SerializedName("conversion_rate") BigDecimal conversionRate,
        @SerializedName("conversion_result") BigDecimal conversionResult
) implements RespuestaApi {
}
