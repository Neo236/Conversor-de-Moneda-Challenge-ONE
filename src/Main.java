import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    static void main() {
        Scanner teclado = new Scanner(System.in);
        ConsultaMoneda consulta = new ConsultaMoneda();
        int opcion = 0;

        String menu = """
                ***************************************************
                Sea bienvenido/a al Conversor de Moneda =]
                
                1) Dólar =>> Peso argentino
                2) Peso argentino =>> Dólar
                3) Dólar =>> Real brasileño
                4) Real brasileño =>> Dólar
                5) Dólar =>> Peso colombiano
                6) Peso colombiano =>> Dólar
                7) Salir
                Elija una opción válida:
                ***************************************************
                """;

        while (opcion != 7) {
            System.out.println(menu);
            try {
                opcion = teclado.nextInt();

                if (opcion >= 1 && opcion <= 6) {
                    System.out.println("Ingrese el valor que deseas convertir:");
                    double cantidad = teclado.nextDouble();
                    RespuestaConversion resultado = null;

                    switch (opcion) {
                        case 1 -> resultado = consulta.convertir("USD", "ARS", cantidad);
                        case 2 -> resultado = consulta.convertir("ARS", "USD", cantidad);
                        case 3 -> resultado = consulta.convertir("USD", "BRL", cantidad);
                        case 4 -> resultado = consulta.convertir("BRL", "USD", cantidad);
                        case 5 -> resultado = consulta.convertir("USD", "COP", cantidad);
                        case 6 -> resultado = consulta.convertir("COP", "USD", cantidad);
                    }

                    if (resultado != null) {
                        System.out.printf("El valor %.2f [%s] corresponde al valor final de =>>> %.2f [%s]\n\n",
                                cantidad, resultado.base_code(), resultado.conversion_result(), resultado.target_code());
                    }
                } else if (opcion != 7) {
                    System.out.println("Opción no válida. Por favor, elija un número del 1 al 7.\n");
                }

            } catch (InputMismatchException e) {
                System.out.println("Error: Formato inválido. Debes ingresar un número.\n");
                teclado.nextLine();
            } catch (RuntimeException e) {
                System.out.println("Error en la conversión: " + e.getMessage() + "\n");
            }
        }

        System.out.println("Gracias por usar el conversor de monedas. ¡Hasta luego!");
        teclado.close();
    }
}