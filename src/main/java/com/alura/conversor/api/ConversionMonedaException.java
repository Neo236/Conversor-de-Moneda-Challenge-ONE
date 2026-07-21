package com.alura.conversor.api;

/** Falló una consulta a la API de cotizaciones. */
public class ConversionMonedaException extends RuntimeException {

    public ConversionMonedaException(String message) {
        super(message);
    }

    public ConversionMonedaException(String message, Throwable cause) {
        super(message, cause);
    }
}
