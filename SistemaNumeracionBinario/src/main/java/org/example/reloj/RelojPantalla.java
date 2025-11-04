package org.example.reloj;

//esta clase ayudara a limpiar el reloj en consola para que se pueda ir actualizando en sus diferentes
//bases 2, 8, 10 y 16

public class RelojPantalla {
    //atributos
    private final MostrarReloj reloj;
    private final ConversorBase conversor;

    public RelojPantalla(MostrarReloj reloj, ConversorBase conversor) {
        this.reloj = reloj;
        this.conversor = conversor;
    }

    // Dibuja el reloj una sola vez (bases 10, 2, 8 y 16)
    public void mostrarReloj() {
        int[] horaActual = reloj.getHoraActual();
        int h = horaActual[0];
        int m = horaActual[1];
        int s = horaActual[2];

        String base10 = reloj.getFormatoHoraActual();
        String base2 = String.format("%s:%s:%s",
                conversor.decimalABase(h, 2),
                conversor.decimalABase(m, 2),
                conversor.decimalABase(s, 2));
        String base8 = String.format("%s:%s:%s",
                conversor.decimalABase(h, 8),
                conversor.decimalABase(m, 8),
                conversor.decimalABase(s, 8));
        String base16 = String.format("%s:%s:%s",
                conversor.decimalABase(h, 16),
                conversor.decimalABase(m, 16),
                conversor.decimalABase(s, 16));

        System.out.println("___________________________________");
        System.out.println("              RELOJ ACTUAL");
        System.out.println("___________________________________");
        System.out.printf("Base 10: %s\n", base10);
        System.out.printf("Base  2: %s\n", base2);
        System.out.printf("Base  8: %s\n", base8);
        System.out.printf("Base 16: %s\n", base16);
        System.out.println("====================================");
    }

    // Método para mantener el reloj actualizándose sin borrar el menú
    public void iniciarRelojConsola() throws InterruptedException {
        while (true) {
            mostrarReloj();
            //se usaran hilos para poder mantener el reloj y el menu
            Thread.sleep(1000);
            // Mueve el cursor hacia arriba 7 líneas para reescribir el reloj
            System.out.print("\033[7A");
        }
    }
}
