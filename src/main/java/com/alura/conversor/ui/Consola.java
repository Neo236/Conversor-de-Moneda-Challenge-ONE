package com.alura.conversor.ui;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Toda la entrada y salida por terminal en un solo lugar. Aislarla permite testear la
 * interfaz inyectando un Scanner y un PrintStream de mentira.
 */
public class Consola {

    /** Ancho de referencia de banners y separadores, en columnas. */
    public static final int ANCHO = 51;

    // La serie brillante (9x): son los tonos de la identidad del proyecto y, a diferencia
    // del azul clásico (34), se leen bien sobre fondo oscuro.
    private static final String RESET = "\033[0m";
    private static final String ROJO = "\033[0;91m";
    private static final String VERDE = "\033[0;92m";
    private static final String AMARILLO = "\033[0;93m";
    private static final String AZUL = "\033[0;94m";
    private static final String CIAN = "\033[0;96m";

    private final Scanner teclado;
    private final PrintStream salida;
    private final boolean conColores;

    public Consola() {
        this(soportaAnsi());
    }

    public Consola(boolean conColores) {
        this(new Scanner(System.in), System.out, conColores);
    }

    /** Sin colores: es el constructor de los tests, que comparan texto plano. */
    public Consola(Scanner teclado, PrintStream salida) {
        this(teclado, salida, false);
    }

    public Consola(Scanner teclado, PrintStream salida, boolean conColores) {
        this.teclado = teclado;
        this.salida = salida;
        this.conColores = conColores;
    }

    /**
     * Colores solo donde van a verse como colores. Sin esto, una consola sin soporte
     * ANSI (cmd.exe clásico, la salida redirigida a un archivo) muestra los escapes
     * literales alrededor de cada línea.
     */
    private static boolean soportaAnsi() {
        if (System.getenv("NO_COLOR") != null || "dumb".equals(System.getenv("TERM"))) {
            return false;
        }
        if (System.console() == null) {
            // La salida está redirigida: los escapes irían crudos al archivo o al pipe.
            return false;
        }
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            // La JVM no activa el procesamiento VT de Windows, así que solo hay colores
            // si la terminal ya lo trae (Windows Terminal, ConEmu, ANSICON, Git Bash).
            return System.getenv("WT_SESSION") != null
                    || System.getenv("ConEmuANSI") != null
                    || System.getenv("ANSICON") != null
                    || System.getenv("TERM") != null;
        }
        return true;
    }

    /** @throws EntradaFinalizadaException si la entrada estándar se cerró */
    public String leerLinea() {
        try {
            return teclado.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaFinalizadaException("La entrada estándar se cerró");
        }
    }

    public void imprimir(String mensaje) {
        salida.println(mensaje);
    }

    public void imprimirSinSalto(String mensaje) {
        salida.print(mensaje);
    }

    public void titulo(String mensaje) {
        salida.println(pintar(CIAN, mensaje));
    }

    public void exito(String mensaje) {
        salida.println(pintar(VERDE, mensaje));
    }

    public void aviso(String mensaje) {
        salida.println(pintar(AMARILLO, mensaje));
    }

    public void error(String mensaje) {
        salida.println(pintar(ROJO, mensaje));
    }

    public String resaltar(String texto) {
        return pintar(AZUL, texto);
    }

    public void separador() {
        titulo("-".repeat(ANCHO));
    }

    /** Centra un texto en el ancho de referencia, para que los banners cierren parejos. */
    public static String centrar(String texto) {
        return " ".repeat(Math.max(0, (ANCHO - texto.length()) / 2)) + texto;
    }

    private String pintar(String color, String mensaje) {
        return conColores ? color + mensaje + RESET : mensaje;
    }
}
