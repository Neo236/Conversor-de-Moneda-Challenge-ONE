package com.alura.conversor.api;

import java.math.BigDecimal;
import java.util.List;

public interface ServicioDeCambio {

    RespuestaConversion convertir(String monedaBase, String monedaObjetivo, BigDecimal cantidad);

    List<Moneda> monedasSoportadas();
}
