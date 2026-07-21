package com.alura.conversor.historial;

import java.math.BigDecimal;

public record RegistroConversion(
        String monedaBase,
        String monedaObjetivo,
        BigDecimal cantidad,
        BigDecimal resultado,
        String fechaHora
) {
}
