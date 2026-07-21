package com.alura.conversor.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonedaTest {

    private final Moneda peso = new Moneda("ARS", "Argentine Peso");

    @Test
    void coincidePorCodigo() {
        assertTrue(peso.coincideCon("ARS"));
    }

    @Test
    void coincidePorNombre() {
        assertTrue(peso.coincideCon("Argentine"));
    }

    @Test
    void noDistingueMayusculas() {
        assertTrue(peso.coincideCon("peso"));
        assertTrue(peso.coincideCon("ars"));
    }

    @Test
    void coincideConUnaParteDelTexto() {
        assertTrue(peso.coincideCon("gentine"));
    }

    @Test
    void noCoincideConOtraCosa() {
        assertFalse(peso.coincideCon("dollar"));
    }
}
