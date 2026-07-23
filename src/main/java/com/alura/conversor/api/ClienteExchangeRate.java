package com.alura.conversor.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.List;

/** Cliente de <a href="https://www.exchangerate-api.com/">ExchangeRate-API</a> v6. */
public class ClienteExchangeRate implements ServicioDeCambio {

    private static final Logger logger = LoggerFactory.getLogger(ClienteExchangeRate.class);
    private static final String URL_BASE = "https://v6.exchangerate-api.com/v6/";

    private final TransporteApi api;
    private final String apiKey;
    private List<Moneda> monedasCache;

    public ClienteExchangeRate(String apiKey) {
        this.api = TransporteApi.porDefecto();
        this.apiKey = apiKey;
    }

    public ClienteExchangeRate(HttpClient client, String apiKey) {
        this.api = new TransporteApi(client);
        this.apiKey = apiKey;
    }

    @Override
    public RespuestaConversion convertir(String monedaBase, String monedaObjetivo, BigDecimal cantidad) {
        validarApiKey();

        // toPlainString y no toString: con double, 10000000 se serializaba como "1.0E7"
        // y la API respondía 404 ante /pair/USD/ARS/1.0E7.
        var url = URL_BASE + apiKey + "/pair/" + monedaBase + "/" + monedaObjetivo + "/" + cantidad.toPlainString();

        logger.info("Consultando conversión de {} a {}", monedaBase, monedaObjetivo);
        return api.pedir(url, RespuestaConversion.class);
    }

    @Override
    public List<Moneda> monedasSoportadas() {
        if (monedasCache != null) {
            return monedasCache;
        }
        validarApiKey();

        logger.info("Consultando la lista de monedas soportadas");
        var respuesta = api.pedir(URL_BASE + apiKey + "/codes", RespuestaCodigos.class);

        if (respuesta.supportedCodes() == null) {
            throw new ConversionMonedaException("La API no devolvió la lista de monedas");
        }

        monedasCache = Arrays.stream(respuesta.supportedCodes())
                .filter(par -> par != null && par.length >= 2)
                .map(par -> new Moneda(par[0], par[1]))
                .toList();
        return monedasCache;
    }

    private void validarApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ConversionMonedaException(
                    "No hay API Key configurada. Definí la variable de entorno EXCHANGE_RATE_API_KEY.");
        }
    }
}
