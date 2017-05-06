package servicios;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import referencias.MisReferencias;

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



        //sendFCMPush();
        //sendHostPush();
    }




    private void sendHostPush() {

/*        String Legacy_SERVER_KEY = MisReferencias.SERVER_KEY;
        String msg = "this is test message,.,,.,.";
        String title = "my title";*/

        //String token =  tokenEnviandoMensajr;
   /*     String token ="eUIaMQ1zq6s:APA91bGmDhKdhrAFEi58DjOpgm--ioHJgJ-0iE1zao-JRe4ioI5-4RAL_7SIxtZACYL1TuiZk2u6Eo9nu2RUT-k-98y7oSbq00rG903oLW1UlW_4RRUXdOMHVSiVy6ldT9nlmGYqeR-8";
        JSONObject obj = null;

        JSONObject objData = null;
        try {
            obj = new JSONObject();
            objData = new JSONObject();
            objData.put("body", msg);
            objData.put("title", title);
            obj.put("to", token);
            obj.put("notification", objData);
        } catch (JSONException e) {

            e.printStackTrace();
        }*/


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, MisReferencias.PUSH_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("!_@@_SUCESS", response + "");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("!_@@_Errors--", error + "");
            }
        })
                ;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);


    }

    private void sendFCMPush() {
        String Legacy_SERVER_KEY = MisReferencias.SERVER_KEY;
        String msg = "this is test message,.,,.,.";
        String title = "my title";
        String token = refreshedToken;
        JSONObject obj = null;
        JSONObject objData = null;
        try {
            obj = new JSONObject();
            objData = new JSONObject();
            objData.put("body", msg);
            objData.put("title", title);
            obj.put("to", token);
            obj.put("notification", objData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, MisReferencias.FCM_PUSH_URL, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("!_@@_SUCESS", response + "");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("!_@@_Errors--", error + "");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + MisReferencias.SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT); jsObjRequest.setRetryPolicy(policy); requestQueue.add(jsObjRequest);


    }

}