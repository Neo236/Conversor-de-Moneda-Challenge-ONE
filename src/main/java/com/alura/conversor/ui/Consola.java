package com.alura.conversor.ui;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Toda la entrada y salida por terminal en un solo lugar. Aislarla permite testear la
 * interfaz inyectando un Scanner y un PrintStream de mentira.
 */
public class Consola {

    private static final String RESET = "\033[0m";
    private static final String ROJO = "\033[0;31m";
    private static final String VERDE = "\033[0;32m";
    private static final String AMARILLO = "\033[0;33m";
    private static final String AZUL = "\033[0;34m";
    private static final String CIAN = "\033[0;36m";

    private final Scanner teclado;
    private final PrintStream salida;

    public Consola() {
        this(new Scanner(System.in), System.out);
    }

    public Consola(Scanner teclado, PrintStream salida) {
        this.teclado = teclado;
        this.salida = salida;
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
        salida.println(CIAN + mensaje + RESET);
    }

    public void exito(String mensaje) {
        salida.println(VERDE + mensaje + RESET);
    }

    public void aviso(String mensaje) {
        salida.println(AMARILLO + mensaje + RESET);
    }

    public void error(String mensaje) {
        salida.println(ROJO + mensaje + RESET);
    }

    public String resaltar(String texto) {
        return AZUL + texto + RESET;
    }

    public void separador() {
        titulo("---------------------------------------------------");
    }
}
