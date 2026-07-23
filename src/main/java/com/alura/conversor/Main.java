package com.alura.conversor;

import com.alura.conversor.api.ClienteExchangeRate;
import com.alura.conversor.historial.HistorialEnArchivo;
import com.alura.conversor.ui.Consola;
import com.alura.conversor.ui.ConversorUI;
import com.alura.conversor.ui.EntradaFinalizadaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/** Punto de entrada: arma las piezas y arranca la interfaz. */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Iniciando el Conversor de Moneda");

        // --no-color apaga los ANSI a mano; NO_COLOR y la autodetección viven en Consola.
        var consola = Arrays.asList(args).contains("--no-color") ? new Consola(false) : new Consola();
        var apiKey = obtenerApiKey(consola);

        new ConversorUI(new ClienteExchangeRate(apiKey), new HistorialEnArchivo(), consola).iniciar();

        logger.info("Aplicación finalizada");
    }

    /**
     * Prioriza la variable de entorno y, si no está, la pide por teclado. La clave nunca
     * se escribe a disco ni se registra en el log.
     */
    private static String obtenerApiKey(Consola consola) {
        var deEntorno = System.getenv("EXCHANGE_RATE_API_KEY");
        if (deEntorno != null && !deEntorno.isBlank()) {
            return deEntorno;
        }

        consola.aviso("No se encontró la variable de entorno EXCHANGE_RATE_API_KEY.");
        consola.imprimirSinSalto("Ingresá tu API Key de ExchangeRate-API para esta sesión: ");

        try {
            var manual = consola.leerLinea();
            if (manual.isEmpty()) {
                consola.error("No ingresaste ninguna API Key: las consultas van a fallar.");
                return null;
            }
            consola.exito("API Key configurada en memoria.");
            return manual;
        } catch (EntradaFinalizadaException e) {
            return null;
        }
    }
}
