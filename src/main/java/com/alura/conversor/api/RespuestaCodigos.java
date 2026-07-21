package com.alura.conversor.api;

import com.google.gson.annotations.SerializedName;

/** Respuesta de {@code /codes}. */
public record RespuestaCodigos(
        String result,
        @SerializedName("error-type") String errorType,
        @SerializedName("supported_codes") String[][] supportedCodes
) implements RespuestaApi {
}
