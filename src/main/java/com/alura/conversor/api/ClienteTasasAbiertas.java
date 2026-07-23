package com.alura.conversor.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Cliente del endpoint abierto de ExchangeRate-API (open.er-api.com): funciona sin
 * registrarse, con tasas que se actualizan una vez al día. Es el modo sin clave de la
 * aplicación: se baja la tabla de tasas de la moneda de origen y la conversión se
 * calcula localmente.
 *
 * <p>Los términos del endpoint piden atribución («Rates By Exchange Rate API»,
 * https://www.exchangerate-api.com); la interfaz la muestra al arrancar en este modo.
 */
public class ClienteTasasAbiertas implements ServicioDeCambio {

    private static final Logger logger = LoggerFactory.getLogger(ClienteTasasAbiertas.class);
    private static final String URL_BASE = "https://open.er-api.com/v6/latest/";

    private final TransporteApi api;

    /** Tabla de tasas por moneda de origen: una consulta por sesión y por origen. */
    private final Map<String, RespuestaTasas> cachePorOrigen = new HashMap<>();
    private List<Moneda> monedasCache;

    public ClienteTasasAbiertas() {
        this.api = TransporteApi.porDefecto();
    }

    public ClienteTasasAbiertas(HttpClient client) {
        this.api = new TransporteApi(client);
    }

    @Override
    public RespuestaConversion convertir(String monedaBase, String monedaObjetivo, BigDecimal cantidad) {
        var tasa = tasasDe(monedaBase).tasa(monedaObjetivo);
        if (tasa == null) {
            throw new ConversionMonedaException("Alguno de los códigos de moneda no existe");
        }

        // El endpoint pair redondea el resultado a 2 decimales; acá igual, para que los
        // dos modos de la aplicación se vean iguales.
        var resultado = cantidad.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        return new RespuestaConversion("success", null, monedaBase, monedaObjetivo, tasa, resultado);
    }

    @Override
    public List<Moneda> monedasSoportadas() {
        if (monedasCache == null) {
            monedasCache = tasasDe("USD").rates().keySet().stream()
                    .sorted()
                    .map(codigo -> new Moneda(codigo, nombreDe(codigo)))
                    .toList();
        }
        return monedasCache;
    }

    private RespuestaTasas tasasDe(String monedaBase) {
        return cachePorOrigen.computeIfAbsent(monedaBase, base -> {
            logger.info("Consultando las tasas del día con base {}", base);
            var respuesta = api.pedir(URL_BASE + base, RespuestaTasas.class);
            if (respuesta.rates() == null) {
                throw new ConversionMonedaException("La API no devolvió la tabla de tasas");
            }
            return respuesta;
        });
    }

    /** El endpoint abierto no trae nombres de moneda; el JDK los sabe, y en español. */
    private static String nombreDe(String codigo) {
        try {
            return Currency.getInstance(codigo).getDisplayName(Locale.of("es", "AR"));
        } catch (IllegalArgumentException e) {
            // Monedas que el JDK no conoce (FOK, KID): queda el código.
            return codigo;
        }
    }
}
