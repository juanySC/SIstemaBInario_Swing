package org.example.reloj;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Scanner;

public class RelojEstatico {

    public static void main(String[] args) throws InterruptedException, IOException {
        Scanner sc = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            // Obtener la hora actual
            LocalTime ahora = LocalTime.now();
            String horaFormateada = String.format("%02d:%02d:%02d",
                    ahora.getHour(), ahora.getMinute(), ahora.getSecond());

            // Mostrar el reloj en la misma línea
            System.out.print("\rRELOJ: " + horaFormateada);

            // Simular menú debajo del reloj
            System.out.println("\n1. Opción A");
            System.out.println("2. Opción B");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");

            // Aquí vamos a leer la opción pero sin bloquear el reloj
            if (System.in.available() > 0) {
                int opcion = sc.nextInt();
                if (opcion == 3) {
                    salir = true;
                }
            }

            // Esperar 1 segundo antes de actualizar
            Thread.sleep(1000);

            // Borrar líneas del menú para que no se acumulen
            System.out.print("\033[3A"); // Mover 3 líneas hacia arriba
            System.out.print("\033[J");  // Borrar desde el cursor hasta el final
        }

        System.out.println("\n¡Hasta luego!");
        sc.close();
    }

}