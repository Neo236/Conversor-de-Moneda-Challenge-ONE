package com.alura.conversor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GestorHistorial implements HistoryRepository {
    private static final Logger logger = LoggerFactory.getLogger(GestorHistorial.class);
    
    private final List<RegistroConversion> historial;
    private final String archivo;
    private final Gson gson;

    public GestorHistorial() {
        this(System.getenv("HISTORY_FILE_PATH") != null ? System.getenv("HISTORY_FILE_PATH") : "historial.json");
    }

    public GestorHistorial(String archivo) {
        this.archivo = archivo;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.historial = cargarHistorial();
    }

    @Override
    public void agregarRegistro(RegistroConversion registro) {
        this.historial.add(registro);
        guardarHistorial();
    }

    @Override
    public List<RegistroConversion> obtenerHistorial() {
        return this.historial;
    }

    private void guardarHistorial() {
        try (FileWriter escritura = new FileWriter(archivo)) {
            gson.toJson(this.historial, escritura);
            logger.info("Historial guardado exitosamente en {}", archivo);
        } catch (IOException e) {
            logger.warn("No se pudo guardar el historial: {}", e.getMessage(), e);
        }
    }

    private List<RegistroConversion> cargarHistorial() {
        try (FileReader lectura = new FileReader(archivo)) {
            Type tipoLista = new TypeToken<ArrayList<RegistroConversion>>(){}.getType();
            List<RegistroConversion> datos = gson.fromJson(lectura, tipoLista);
            if (datos == null) {
                return new ArrayList<>();
            }
            logger.info("Historial cargado con {} registros desde {}.", datos.size(), archivo);
            return datos;
        } catch (IOException e) {
            logger.info("No se encontró archivo de historial o no se pudo leer. Se iniciará uno nuevo.");
            return new ArrayList<>();
        }
    }
}