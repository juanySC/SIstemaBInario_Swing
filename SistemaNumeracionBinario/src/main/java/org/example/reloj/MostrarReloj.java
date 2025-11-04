//ayudara a mostrar la hora actual de la computadora
package org.example.reloj;

import java.time.LocalTime;

public class MostrarReloj {


    //metodos

    /**
     * getHoraActual, obtiene la hora actual de la computadora por medio
     * de un arreglo de int
     **/
    public int[] getHoraActual() {
        //atributos
        LocalTime horaActual = LocalTime.now();
        //utilizo horaActual para obtener los siguientes parametros
        int hora = horaActual.getHour();
        int minuto = horaActual.getMinute();
        int segundo = horaActual.getSecond();
        return new int[]{hora, minuto, segundo};
    }

    /**getFormatoHoraActual manejo de un formato estandar para la hora**/
    public String getFormatoHoraActual() {
        //creando mi vector con la hora
        int []obteniendoHora = getHoraActual();

        //simplemente son las posiciones del vector
        int hora = obteniendoHora[0];
        int minuto = obteniendoHora[1];
        int segundo = obteniendoHora[2];

        // %02d= los espacios que tendran los numeros
        return String.format("%02d:%02d:%02d", hora, minuto, segundo);
    }

}