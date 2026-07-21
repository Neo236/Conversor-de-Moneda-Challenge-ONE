package com.alura.conversor.historial;

import java.util.List;

public interface RepositorioHistorial {

    void agregarRegistro(RegistroConversion registro);

    List<RegistroConversion> obtenerHistorial();
}
