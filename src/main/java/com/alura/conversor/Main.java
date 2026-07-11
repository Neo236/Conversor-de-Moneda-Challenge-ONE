package com.alura.conversor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String RESET = "\033[0m";
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private static final String YELLOW = "\033[0;33m";
    private static final String CYAN = "\033[0;36m";

    public static void main(String[] args) {
        logger.info("Iniciando aplicación Conversor de Moneda");

        Scanner scanner = new Scanner(System.in);
        String apiKey = obtenerApiKey(scanner);

        CurrencyService currencyService = new ConsultaMoneda(apiKey);
        HistoryRepository historyRepository = new GestorHistorial();

        ConsoleUI consoleUI = new ConsoleUI(currencyService, historyRepository, scanner);
        consoleUI.start();

        logger.info("Aplicación finalizada");
    }

    private static String obtenerApiKey(Scanner scanner) {
        String envKey = System.getenv("EXCHANGE_RATE_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey;
        }

        System.out.println(YELLOW + "ATENCION: La variable de entorno EXCHANGE_RATE_API_KEY no fue detectada." + RESET);
        System.out.print(CYAN + "Por favor, ingrese su API Key de ExchangeRate-API para esta sesion: " + RESET);
        String manualKey = scanner.nextLine().trim();

        if (manualKey.isEmpty()) {
            System.out.println(RED + "No se ingreso API Key. El programa puede fallar al consultar monedas." + RESET);
            return null;
        }

        System.out.println(GREEN + "API Key configurada en memoria." + RESET);
        return manualKey;
    }
}
