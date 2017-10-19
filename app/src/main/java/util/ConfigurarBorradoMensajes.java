package util;

/**
 * Created by Usuario on 13/10/2017.
 */

public  class ConfigurarBorradoMensajes {

    //Tiempo para borrado de los mensajes...
    //public static String tiempoConfigurado="";

    public static long configurarCombo(String miTiempo){
        long segundos=0;

        switch(miTiempo){

            case ("1 minuto"):
                segundos=60000;
                break;
            case ("3 minutos"):
                segundos=180000;
                break;
            case ("5 minutos"):
                segundos=300000;
                break;
            case ("15 minutos"):
                segundos=900000;
                break;
            case ("30 minutos"):
                segundos=1800000;
                break;
            case ("1 hora"):
                segundos=3600000;
                break;
            case ("2 horas"):
                segundos=7200000;
                break;
        }

        return segundos;
    }

    public static int configurarCombo(int miTiempo){
        int posicion=0;

        switch(miTiempo){

            case (60000):
                posicion=0;
                break;
            case (180000):
                posicion=1;
                break;
            case (300000):
                posicion=2;
                break;
            case (900000):
                posicion=3;
                break;
            case (1800000):
                posicion=4;
                break;
            case (3600000):
                posicion=5;
                break;
            case (7200000):
                posicion=6;
                break;
        }

        return posicion;
    }



}
