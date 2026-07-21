package com.alura.conversor.ui;

/**
 * La entrada estándar se cerró (Ctrl+D, una tubería que termina, un runner sin terminal).
 * Permite salir ordenadamente en lugar de reventar con {@code NoSuchElementException}.
 */
public class EntradaFinalizadaException extends RuntimeException {

    public EntradaFinalizadaException(String message) {
        super(message);
    }
}
