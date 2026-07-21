package com.alura.conversor.historial;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Historial persistido como JSON en disco. */
public class HistorialEnArchivo implements RepositorioHistorial {

    private static final Logger logger = LoggerFactory.getLogger(HistorialEnArchivo.class);
    private static final Type TIPO_LISTA = new TypeToken<ArrayList<RegistroConversion>>() {}.getType();

    private final List<RegistroConversion> historial;
    private final Path archivo;
    private final Gson gson;

    public HistorialEnArchivo() {
        this(Path.of(rutaPorDefecto()));
    }

    public HistorialEnArchivo(Path archivo) {
        this.archivo = archivo;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.historial = cargarHistorial();
    }

    private static String rutaPorDefecto() {
        var configurada = System.getenv("HISTORY_FILE_PATH");
        return configurada != null && !configurada.isBlank() ? configurada : "historial.json";
    }

    @Override
    public void agregarRegistro(RegistroConversion registro) {
        historial.add(registro);
        guardarHistorial();
    }

    @Override
    public List<RegistroConversion> obtenerHistorial() {
        return Collections.unmodifiableList(historial);
    }

    private void guardarHistorial() {
        // UTF-8 explícito: con el charset de la plataforma, un historial escrito en
        // Windows y leído en Linux rompe los nombres con acentos.
        try (var escritura = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            gson.toJson(historial, escritura);
        } catch (IOException e) {
            logger.warn("No se pudo guardar el historial en {}", archivo, e);
        }
    }

    /**
     * Nunca propaga: esto corre desde el constructor y un historial ilegible no es motivo
     * para que la aplicación no arranque. Se empieza de cero y se avisa por el log.
     */
    private List<RegistroConversion> cargarHistorial() {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        try (var lectura = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            List<RegistroConversion> datos = gson.fromJson(lectura, TIPO_LISTA);
            if (datos == null) {
                return new ArrayList<>();
            }
            logger.info("Historial cargado con {} registros desde {}", datos.size(), archivo);
            return new ArrayList<>(datos);
        } catch (IOException e) {
            logger.warn("No se pudo leer el historial de {}. Se empieza uno nuevo.", archivo, e);
            return new ArrayList<>();
        } catch (JsonSyntaxException e) {
            logger.warn("El historial de {} está corrupto. Se empieza uno nuevo.", archivo, e);
            return new ArrayList<>();
        }
    }
}
