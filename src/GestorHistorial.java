import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GestorHistorial {
    private List<RegistroConversion> historial;
    private static final String ARCHIVO = "historial.json";
    private final Gson gson;

    public GestorHistorial() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.historial = cargarHistorial();
    }

    public void agregarRegistro(RegistroConversion registro) {
        this.historial.add(registro);
        guardarHistorial();
    }

    public List<RegistroConversion> obtenerHistorial() {
        return this.historial;
    }

    private void guardarHistorial() {
        try (FileWriter escritura = new FileWriter(ARCHIVO)) {
            gson.toJson(this.historial, escritura);
        } catch (IOException e) {
            System.out.println("Advertencia: No se pudo guardar el historial - " + e.getMessage());
        }
    }

    private List<RegistroConversion> cargarHistorial() {
        try (FileReader lectura = new FileReader(ARCHIVO)) {
            Type tipoLista = new TypeToken<ArrayList<RegistroConversion>>(){}.getType();
            List<RegistroConversion> datos = gson.fromJson(lectura, tipoLista);

            if (datos == null) {
                return new ArrayList<>();
            }
            return datos;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}