package com.alura.conversor.api;

/**
 * Lo que toda respuesta de ExchangeRate-API tiene en común.
 *
 * <p>La API informa los fallos en el cuerpo, con {@code result: "error"} y un
 * {@code error-type}, tanto cuando responde 200 como cuando responde 4xx. Tener esto
 * en una interfaz permite leer el motivo real del error una sola vez, sin importar
 * qué endpoint se consultó.
 */
public sealed interface RespuestaApi permits RespuestaConversion, RespuestaCodigos, RespuestaTasas {

    String result();

    String errorType();

    default boolean exitosa() {
        return "success".equals(result());
    }
}
