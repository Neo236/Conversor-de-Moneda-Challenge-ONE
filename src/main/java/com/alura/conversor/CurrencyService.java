package com.alura.conversor;

public interface CurrencyService {
    RespuestaConversion convertir(String monedaBase, String monedaObjetivo, double cantidad) throws CurrencyConversionException;
    String[][] obtenerMonedasSoportadas() throws CurrencyConversionException;
}
