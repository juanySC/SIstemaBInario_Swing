//suma y resta binaria
package org.example.operaacionesAritmeticas;

public class OperacionAritmetica {

    /**completarConCeros me ayudara a rellenear el numero binario obtenido
     * ejemplo - "010" para completar - "0010" se llerellena con ceros a la izquierda**/
    public String completarConCeros(String binario, int tamanioIdeal){

        //Caso 1: si el binario tiene lo deseado simplemente se retorna
        if (binario.length() >= tamanioIdeal){
            return binario;
        }

        //Caso 2:recorre mi binario y saca los ceros que necesita para rellenar
        int ceroNecesarios = tamanioIdeal - binario.length();
        //construyo la cadena para los ceros
        StringBuilder cadenaCeros = new StringBuilder();

        //creo un ciclo para rellenar los ceros faltantes
        for (int i = 0; i < ceroNecesarios; i++) {
            cadenaCeros.append("0");
        }

        return cadenaCeros.toString() + binario;

    }

    /**sumaBinaria contiene la logica para poder sumar dos horas
     * comparando logitudes
     * @param binario1 el primer numero binario
     * @param binario2 pasando el segundo numero
     * @return resultado que me da ya la suma del binario1 y binario2**/
    public String sumaBinaria(String binario1, String binario2){
        //comparo la longitud de ambas cadenas de binarios{
        int longitudMaxima ;
        String b1, b2; // Se usan variables locales para las cadenas ya rellenadas
        if (binario1.length() >= binario2.length()) {
            longitudMaxima = binario1.length();
            b1 = binario1; // se le asigna como temporal
            b2 = completarConCeros(binario2, longitudMaxima);
        } else{
            longitudMaxima = binario2.length();
            b2 = binario2; // lo asigno al temporal
            b1 = completarConCeros(binario1, longitudMaxima);
        }

        //Se necesita llevar el valor de acarreo (0, 1) para la siguient ecolumna
        int acarrero = 0;
        StringBuilder resultado = new StringBuilder();

        //recorro de dereha a izquerda la cadena
        for (int i = longitudMaxima -1; i >= 0 ; i--) {
            // obteno el binraio por ejemplo en posicion '1' - '0' = 1
            // binrario '0' - '0' = 0
            int bit1 = b1.charAt(i) -'0';
            int bit2 = b2.charAt(i) -'0';

            //calculo la seuma
            int suma = bit1 + bit2 + acarrero;

            //se basa en el resultao de la suma
            //obtenemos el bit resultado por ejemlo 2/2 = 1 residuo 0   o  3/2 = 1 residuo 1
            int bitResultante = suma % 2;

            //se basa en el resultao de la suma
            //ejemplo 2/2 =1 acarreo  o   0/2=1 acarreo 0
            acarrero = suma / 2;

            //inserto el bit resultante en la posicion 0
            resultado.insert(0, bitResultante);

        }

        //si al final de la suma el acarreo es 1 se agrega al inicio
        if (acarrero == 1){
            resultado.insert(0, 1);
        }

        //para obtener la cadena que se genero
        return resultado.toString();
    }

    /**complementoAuno : ayudara a tener la inversa por ejemplo: binrario original  0101 (5) a la
     * complementoAuno: 1010 (-5, provisionalmente)**/
    public String complementoAuno(String binario) {
        StringBuilder complemento = new StringBuilder();

        //recorro la cadena binaria
        for (int i = 0; i < binario.length(); i++) {
            //obtengo el bit actual
            char bitActual = binario.charAt(i);
            //si es 0 lo cambio a 1 y viceversa
            if (bitActual == '0') {
                complemento.append('1');
            } else {
                complemento.append('0');
            }
        }

        return complemento.toString();
    }

    /**complementoAdos ayuda a sumar uno al complemento 1 para obtener este**/
    public String complementoAdos(String binario) {
        // LLamo a mi complemento a uno primero
        String complementoUno = complementoAuno(binario);
        // Luego, sumo 1 al complemento a uno para obtener el complemento a dos
        String complementoDos = sumaBinaria(complementoUno, "1");

        //  Si se produce un acarreo extra al sumar 1  de "1111" a "10000"
        // ese bit se ignora para mantener el ancho de bits original (Complemento a Dos Fijo)
        if (complementoDos.length() > binario.length()) {
            // Ignorar el bit más significativo (el acarreo)
            return complementoDos.substring(1);
        }

        return complementoDos;
    }

    /**restaBinaria contiene la logica para poder restar dos horas
     * esto a traves del complemento a dos y complemento a uno
     * @param binario1 el primer numero binario
     * @param binario2 pasando el segundo numero
     * @return resultado que me da ya la suma del binario1 y binario2**/
    public String restaBinaria(String binario1, String binario2) {
        // Determina la longitud máxima para asegurar que ambos operandos estén alineados
        int maxLen;
        String b1Padded, b2Padded;

        if (binario1.length() >= binario2.length()) {
            maxLen = binario1.length();
            b1Padded = binario1;
            b2Padded = completarConCeros(binario2, maxLen);
        } else {
            maxLen = binario2.length();
            b2Padded = binario2;
            b1Padded = completarConCeros(binario1, maxLen);
        }

        // Para restar binario1 - binario2, sumamos binario1 con el complemento a dos de binario2
        String complementoDosBinario2 = complementoAdos(b2Padded);

        //se realiza una suma ya que no existe la resta directa
        String resultadoSuma = sumaBinaria(b1Padded, complementoDosBinario2);

        // MODIFICACIÓN CLAVE 2: En la resta binaria (A + C2(B)), si la suma resulta en un bit de longitud extra (acarreo),
        // significa que el resultado es POSITIVO y se IGNORA ese bit extra para obtener la respuesta dentro del ancho de bits.
        if (resultadoSuma.length() > maxLen) {
            // Ignorar el acarreo (el primer bit)
            return resultadoSuma.substring(1);
        }

        // Si no hay acarreo, se retorna el resultado que contiene el bit de signo (negativo o cero).
        return resultadoSuma;
    }
}