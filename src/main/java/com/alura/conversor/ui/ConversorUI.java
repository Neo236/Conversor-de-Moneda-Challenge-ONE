package com.alura.conversor.ui;

import com.alura.conversor.api.ConversionMonedaException;
import com.alura.conversor.api.Moneda;
import com.alura.conversor.api.RespuestaConversion;
import com.alura.conversor.api.ServicioDeCambio;
import com.alura.conversor.historial.RegistroConversion;
import com.alura.conversor.historial.RepositorioHistorial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

/** Flujo de la aplicación: pedir datos, convertir y mostrar el resultado. */
public class ConversorUI {

    private static final Logger logger = LoggerFactory.getLogger(ConversorUI.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SALIR = "SALIR";

    // "100.000" en es-AR es cien mil: tomar el punto como decimal convertiría otro monto
    // en silencio. Con coma decimal presente ("1.234,56") los puntos son de miles y se
    // pueden sacar sin dudas; sin ella, mejor pedir el monto de nuevo.
    private static final Pattern MILES_Y_COMA = Pattern.compile("\\d{1,3}(\\.\\d{3})+,\\d+");
    private static final Pattern SOLO_MILES = Pattern.compile("\\d{1,3}(\\.\\d{3})+");

    private final ServicioDeCambio servicio;
    private final RepositorioHistorial historial;
    private final Consola consola;

    /** La última conversión exitosa, para poder repetirla o invertirla. */
    private RegistroConversion ultimaConversion;

    public ConversorUI(ServicioDeCambio servicio, RepositorioHistorial historial, Consola consola) {
        this.servicio = servicio;
        this.historial = historial;
        this.consola = consola;
    }

    public void iniciar() {
        consola.titulo("***************************************************");
        consola.exito("Bienvenido al Conversor de Moneda =)");
        consola.titulo("***************************************************");

        try {
            bucle();
        } catch (EntradaFinalizadaException e) {
            consola.aviso("\nEntrada finalizada.");
        }

        consola.exito("\nCerrando sistema... ¡Hasta luego!");
    }

    private void bucle() {
        while (true) {
            try {
                var monedaBase = pedirMoneda("\nElegí la moneda de " + consola.resaltar("ORIGEN")
                        + " (código de 3 letras, ej. USD):");
                if (monedaBase.isEmpty()) {
                    return;
                }

                var monedaObjetivo = pedirMoneda("\n¿A qué moneda querés " + consola.resaltar("CONVERTIR")
                        + "? (ej. ARS, COP):");
                if (monedaObjetivo.isEmpty()) {
                    return;
                }

                var cantidad = pedirCantidad();
                if (cantidad.isEmpty()) {
                    return;
                }

                convertir(monedaBase.get(), monedaObjetivo.get(), cantidad.get());
            } catch (OperacionCancelada e) {
                consola.aviso("Operación cancelada: volvés al inicio.");
            }
        }
    }

    /** Vacío si el usuario pidió salir. */
    private Optional<String> pedirMoneda(String mensaje) {
        while (true) {
            var entrada = leerEntrada(mensaje);
            if (SALIR.equals(entrada)) {
                return Optional.empty();
            }
            if (esCodigoDeMoneda(entrada)) {
                return Optional.of(entrada);
            }
            consola.error("Ingresá exactamente 3 letras para la moneda (ej. USD).");
        }
    }

    /** Un código ISO 4217 son tres letras. La API igual lo rechaza, pero atajarlo acá evita el viaje. */
    private static boolean esCodigoDeMoneda(String entrada) {
        return entrada.length() == 3 && entrada.chars().allMatch(Character::isLetter);
    }

    /** Vacío si el usuario pidió salir. */
    private Optional<BigDecimal> pedirCantidad() {
        while (true) {
            var entrada = leerEntrada("\nIngresá la CANTIDAD a convertir:");
            if (SALIR.equals(entrada)) {
                return Optional.empty();
            }
            if (MILES_Y_COMA.matcher(entrada).matches()) {
                entrada = entrada.replace(".", "");
            } else if (SOLO_MILES.matcher(entrada).matches()) {
                consola.error("Escribí el monto sin separador de miles (ej. 100000 o 100000,50).");
                continue;
            }
            try {
                // La coma decimal es lo natural en es-AR; BigDecimal solo entiende el punto.
                var cantidad = new BigDecimal(entrada.replace(",", "."));
                if (cantidad.signum() <= 0) {
                    consola.error("Ingresá un valor mayor a cero.");
                } else {
                    return Optional.of(cantidad);
                }
            } catch (NumberFormatException e) {
                consola.error("Formato inválido. Tiene que ser un número.");
            }
        }
    }

    private void convertir(String base, String objetivo, BigDecimal cantidad) {
        consola.titulo("\nProcesando conversión...");
        try {
            RespuestaConversion respuesta = servicio.convertir(base, objetivo, cantidad);

            var registro = new RegistroConversion(
                    respuesta.baseCode(), respuesta.targetCode(),
                    cantidad, respuesta.conversionResult(),
                    LocalDateTime.now().format(FORMATO_FECHA));
            historial.agregarRegistro(registro);
            ultimaConversion = registro;

            consola.separador();
            consola.aviso(String.format("Tasa actual: 1 %s = %s %s",
                    respuesta.baseCode(), respuesta.conversionRate().toPlainString(), respuesta.targetCode()));
            consola.exito(String.format("RESULTADO: %s [%s] equivale a =>>> %s [%s]",
                    cantidad.toPlainString(), respuesta.baseCode(),
                    respuesta.conversionResult().toPlainString(), respuesta.targetCode()));
            consola.separador();

        } catch (ConversionMonedaException e) {
            consola.error("No se pudo hacer la conversión: " + e.getMessage());
        } catch (EntradaFinalizadaException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado durante la conversión", e);
            consola.error("Ocurrió un error inesperado. Mirá los logs para más detalles.");
        }
    }

    /**
     * Lee un valor, atendiendo los comandos que se pueden escribir en cualquier momento.
     * Devuelve {@link #SALIR} si el usuario quiere terminar.
     */
    private String leerEntrada(String mensaje) {
        while (true) {
            consola.imprimir(mensaje);
            consola.aviso("[Comandos: l lista | h historial | r repetir | i invertir | c cancelar | salir]");
            consola.imprimirSinSalto("> ");

            var entrada = consola.leerLinea().toLowerCase();

            switch (entrada) {
                case "salir" -> {
                    return SALIR;
                }
                case "c", "cancelar" -> throw new OperacionCancelada();
                case "h", "historial" -> {
                    if (mostrarHistorial()) {
                        return SALIR;
                    }
                }
                case "l", "lista" -> {
                    if (mostrarMonedas()) {
                        return SALIR;
                    }
                }
                case "r", "repetir" -> repetirUltima(false);
                case "i", "invertir" -> repetirUltima(true);
                default -> {
                    if (!entrada.isEmpty()) {
                        return entrada.toUpperCase();
                    }
                }
            }
            consola.titulo("\n--- Volviendo a la operación actual ---");
        }
    }

    /** Rehace la última conversión; invertida, convierte el resultado de vuelta. */
    private void repetirUltima(boolean invertida) {
        if (ultimaConversion == null) {
            consola.aviso("Todavía no hiciste ninguna conversión.");
            return;
        }
        var r = ultimaConversion;
        if (invertida) {
            convertir(r.monedaObjetivo(), r.monedaBase(), r.resultado());
        } else {
            convertir(r.monedaBase(), r.monedaObjetivo(), r.cantidad());
        }
    }

    /** @return {@code true} si el usuario pidió salir desde adentro de la lista. */
    private boolean mostrarMonedas() {
        try {
            consola.titulo("\nConsultando la lista de monedas...");
            var monedas = servicio.monedasSoportadas();

            return new Paginador<Moneda>(consola, "LISTA DE MONEDAS", 15,
                    m -> String.format("%s - %s", m.codigo(), m.nombre()),
                    Moneda::coincideCon)
                    .recorrer(monedas);

        } catch (ConversionMonedaException e) {
            consola.error("No se pudo obtener la lista: " + e.getMessage());
            return false;
        }
    }

    /** @return {@code true} si el usuario pidió salir desde adentro del historial. */
    private boolean mostrarHistorial() {
        var registros = new ArrayList<>(historial.obtenerHistorial());
        if (registros.isEmpty()) {
            consola.aviso("\n--- HISTORIAL DE CONVERSIONES ---");
            consola.imprimir("Todavía no hay conversiones registradas.");
            consola.aviso("---------------------------------");
            return false;
        }

        // Lo más reciente primero: es lo que a uno le interesa mirar.
        java.util.Collections.reverse(registros);

        return new Paginador<RegistroConversion>(consola, "HISTORIAL DE CONVERSIONES", 10,
                r -> String.format("[%s] %s %s -> %s %s",
                        r.fechaHora(), r.cantidad().toPlainString(), r.monedaBase(),
                        r.resultado().toPlainString(), r.monedaObjetivo()))
                .recorrer(registros);
    }

    /** Señal interna: el usuario canceló la operación en curso para volver al inicio. */
    private static class OperacionCancelada extends RuntimeException {
    }
}
