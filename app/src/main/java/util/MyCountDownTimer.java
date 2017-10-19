package util;

import android.app.Activity;
import android.os.CountDownTimer;
import android.widget.Toast;

/*https://es.stackoverflow.com/questions/8597/cerrar-e-ir-a-un-activity-tras-x-tiempo-de-inactividad-del-usuario*/
public class MyCountDownTimer extends CountDownTimer {

    Activity miactivity;
    public MyCountDownTimer(long startTime, long interval, Activity activity) {

        super(startTime, interval);
        miactivity=activity;

    }

    @Override
    public void onFinish() {
        //DO WHATEVER YOU WANT HERE
        // CIERRA LA APP MATANDO EL PROCESO Y VUELVE A ABRIRLO. 
         //android.os.Process.killProcess(android.os.Process.myPid());

        Toast.makeText(miactivity, "Acaba de eliminarse el último mensaje TOP-SECRET que has enviado", Toast.LENGTH_LONG).show();


    }

    @Override
    public void onTick(long millisUntilFinished) {
    }
}