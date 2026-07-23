package com.alura.conversor.api;

/** Una moneda soportada por la API: su código ISO 4217 y su nombre. */
public record Moneda(String codigo, String nombre) {

    public boolean coincideCon(String termino) {
        var t = termino.toLowerCase(java.util.Locale.ROOT);
        return codigo.toLowerCase(java.util.Locale.ROOT).contains(t)
                || nombre.toLowerCase(java.util.Locale.ROOT).contains(t);
    }
}
