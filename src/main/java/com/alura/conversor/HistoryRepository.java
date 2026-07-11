package com.alura.conversor;

import java.util.List;

public interface HistoryRepository {
    void agregarRegistro(RegistroConversion registro);
    List<RegistroConversion> obtenerHistorial();
}
