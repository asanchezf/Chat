package servicios;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FCMInstanceIdService extends FirebaseInstanceIdService {
    String refreshedToken;

    public FCMInstanceIdService() {
    }


    private static final String LOGTAG = "android-fcm";

    //Se ejecuta cuando nos asigen un tocken. Cada vez que esto suceda
    @Override
    public void onTokenRefresh() {
        //Se obtiene el token actualizado
         refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken);
        Log.i(LOGTAG, "Token actualizado: " + refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        //Ahora podemos utilizar el token de la notificación....
        //POR EJEMPLO PODEMOS REGISTRAR EL TOKEN EN EL SERVIDOR....

    }




}