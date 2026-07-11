package com.alura.conversor;

public record RegistroConversion(
        String monedaBase,
        String monedaObjetivo,
        double cantidad,
        double resultado,
        String fechaHora
) {
}
