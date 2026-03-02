import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsultaMoneda {
    private final HttpClient client;
    private final Gson gson;

    private String[][] monedasDisponiblesCache;

    public ConsultaMoneda() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.monedasDisponiblesCache = null;
    }

    public RespuestaConversion convertir(String monedaBase, String monedaObjetivo, double cantidad) {

        String apiKey = System.getenv("EXCHANGE_RATE_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Error crítico: La variable de entorno EXCHANGE_RATE_API_KEY no está configurada.");
        }

        String direccion = "https://v6.exchangerate-api.com/v6/" + apiKey + "/pair/"
                + monedaBase + "/" + monedaObjetivo + "/" + cantidad;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(direccion))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error en la API: Código " + response.statusCode());
            }

            return gson.fromJson(response.body(), RespuestaConversion.class);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error de conexión al servidor: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Error al leer los datos de la moneda: " + e.getMessage());
        }
    }
    public String[][] obtenerMonedasSoportadas() {
        if (this.monedasDisponiblesCache != null) {
            return this.monedasDisponiblesCache;
        }

        String apiKey = System.getenv("EXCHANGE_RATE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Error crítico: La variable EXCHANGE_RATE_API_KEY no está configurada.");
        }

        String direccion = "https://v6.exchangerate-api.com/v6/" + apiKey + "/codes";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(direccion)).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Error al obtener la lista de códigos: " + response.statusCode());
            }

            RespuestaCodigos respuesta = gson.fromJson(response.body(), RespuestaCodigos.class);

            this.monedasDisponiblesCache = respuesta.supported_codes();

            return this.monedasDisponiblesCache;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error de conexión al obtener códigos: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Error al leer el formato de los códigos: " + e.getMessage());
        }
    }
}