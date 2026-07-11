package com.alura.conversor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestorHistorialTest {

    private static final String TEST_FILE = "test_historial.json";
    private GestorHistorial gestorHistorial;

    @BeforeEach
    void setUp() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
        gestorHistorial = new GestorHistorial(TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testAgregarRegistroYObtenerHistorial() {
        RegistroConversion registro = new RegistroConversion("USD", "ARS", 100.0, 80000.0, "2023-10-25 10:00:00");
        gestorHistorial.agregarRegistro(registro);

        List<RegistroConversion> historial = gestorHistorial.obtenerHistorial();
        assertEquals(1, historial.size(), "Debería haber 1 registro");
        assertEquals("USD", historial.get(0).monedaBase());
        
        // Verificamos que se guarde en un archivo
        File file = new File(TEST_FILE);
        assertTrue(file.exists(), "El archivo de historial debería existir");
        
        // Probamos recargar
        GestorHistorial nuevoGestor = new GestorHistorial(TEST_FILE);
        List<RegistroConversion> historialCargado = nuevoGestor.obtenerHistorial();
        assertEquals(1, historialCargado.size(), "Debería cargar 1 registro");
        assertEquals("ARS", historialCargado.get(0).monedaObjetivo());
    }
}
