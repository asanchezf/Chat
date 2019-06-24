package servicios;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.antonio.chat.Activity_chats;
import com.example.antonio.chat.Activity_usuarios;
import com.example.antonio.chat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import util.Tratamiento_Imagenes;

import static com.example.antonio.chat.R.drawable.email;
import static referencias.MisReferencias.REFERENCIA_USUARIOS;

//AQUÍ ES DONDE LLEGAN LAS NOTIFICACIONES DIRECTAMENTE....
//Se encarga de gestionar los mensajes en el método onMessageReceived
public class FCMService extends FirebaseMessagingService {

    private static final String LOGTAG = "android-fcm";
    private static final String TAG = "FirebaseMessageService";
    Bitmap bitmap;
    private static final String IMAGE_DEFAULT = "default_image";
    public static final int NOTIFICATION_ID = 123;
    private int notificationCount = 0;

    private String emisor;
    private String tokenEmisor;

    public FCMService() {
    }


    //Gestionamos el envio del mensaje cuando la aplicación esté en primer plano
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //RECOGEMOS LOS DATOS DEL SERVICIO EnviarNotificacionesDesdePHP.php

        //Si el servicio utiliza en cuerpo del envío el objeto 'notification' => array....Ahora el servicio no los tiene.
        if (remoteMessage.getNotification() != null) {

            //TRATAMIENTO DEL MENSAJE SI ES DEL TIPO NOTIFICACIÓN
            String titulo = remoteMessage.getNotification().getTitle();
            String texto = remoteMessage.getNotification().getBody();
            String from = remoteMessage.getFrom();//Emisor
            String to = remoteMessage.getTo();//Receptor
            String datos = remoteMessage.getData().get("arraydedatos");
            Log.d(LOGTAG, "NOTIFICACION RECIBIDA");
            Log.d(LOGTAG, "Título: " + titulo);
            Log.d(LOGTAG, "Texto: " + texto);

            //Opcional: mostramos la notificación en la barra de estado
            //showNotification(titulo, texto);
            //sendNotification(texto,titulo);//Notificación modelo a partir de api19
        }





        //TRATAMIENTO DEL MENSAJE SI EL SERVICIO UTILIZA "data" EN EL CUERPO DEL ENVÍO PARA QUE PUEDA TENER MÁS PARÁMETROS:
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            //imageUri will contain URL of the image to be displayed with Notification
            String imageUri = remoteMessage.getData().get("image");
            String token=remoteMessage.getData().get("token");//Ver si es correcto...
             emisor=remoteMessage.getData().get("emisor");
            String to=remoteMessage.getTo();//Receptor
            String from = remoteMessage.getFrom();//Emisor


            traerTokenEmisor();

            //Si el emisor ha puesto icono en su perfil es que su imageUri es diferente de "default_image"
            if (!imageUri.equals(IMAGE_DEFAULT)) {
                bitmap = getBitmapfromUrl(imageUri);
            }





            //sendNotification(remoteMessage.getData().get("body"), remoteMessage.getData().get("title"), remoteMessage.getData().get("tickerText"),
                    //bitmap, token);
            sendNotification(remoteMessage.getData().get("body"), remoteMessage.getData().get("title"), remoteMessage.getData().get("tickerText"),
                    bitmap, token,tokenEmisor,emisor);
        }

    }



    private  void traerTokenEmisor() {
        //RECOGEMOS el token del emisor. Se utilizará para enviarlo como parámetro al servicio de notificaciones...


        DatabaseReference database;
        DatabaseReference db;
        database = FirebaseDatabase.getInstance().getReference();
        db = database.getRoot().child(REFERENCIA_USUARIOS).child(emisor).child("tokenNotify");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                tokenEmisor = dataSnapshot.getValue().toString();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "No se ha podido cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });


    }



    /*
       *To get a Bitmap image from the URL received
       * */
    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            //REdeondeamos la imagen:
            //getRoundedCornerBitmap(bitmap,true);


            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

 /*   public void sendNotifications(ArrayList<NotificationCompat.MessagingStyle.Message> messages) {
        int notifyID = 0;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        for (NotificationCompat.MessagingStyle.Message message : messages) {
            Intent notificationIntent = new Intent(getApplicationContext(), Activity_chats.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack
            stackBuilder.addParentStack(Activity_Login.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(notificationIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
             Resources res = getApplicationContext().getResources();
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
             builder.setContentIntent(contentIntent)
                     .setSmallIcon(R.drawable.ic_launcher)
                     .setLargeIcon(BitmapFactory.decodeResource(res, messages.media))
                     .setTicker("Got a new message") .setWhen(System.currentTimeMillis())
                     .setAutoCancel(true)
                     .setContentTitle(message.getText())
                     .setContentText(message.getText())
                     .setNumber(notificationCount++);
            mNotificationManager.notify(NOTIFICATION_ID, mNotificationManager.build());


        }

    }*/

    private void sendNotification(String messageBody, String title, String ticker, Bitmap
            bitmap, String token,String tokenEmisor,String emisor){


        Intent intent = new Intent(this, Activity_chats.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//FLAG_ACTIVITY_CLEAR_TOP)

        //Enviamos parámetros para que los recoja Activity_chats
        intent.putExtra("parametro_receptor", emisor);
        intent.putExtra("parametro_receptor_email", email);
        intent.putExtra("parametro_receptor_token", tokenEmisor);//Al abrir Activity_chats será realmente el emisor del mensaje puesto que es un mensaje enviado por...
        intent.putExtra("parametro_notificacion", NOTIFICATION_ID);//Pasamos el códifo de la notificación para que al abrirse la activity del intent, se pueda cancelar la notificación..


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);//FLAG_ONE_SHOT

        Intent ignorar = new Intent();
        PendingIntent pendinIgnorar = PendingIntent.getActivity(this, 0, ignorar, PendingIntent.FLAG_CANCEL_CURRENT);

        //Log.e("inside notification", "method");
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);



/*        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)

                //PARA LA IMAGEN Grande con icono y con subtexto.
              *//* .setLargeIcon(bitmap)//Notification icon image
                .setStyle(new NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap))
                .setSubText(messageBody)*//*

                .setLargeIcon(bitmap)//Notification icon image
                .setSmallIcon(R.drawable.notify)
                .setColor(getResources().getColor(R.color.md_purple_500))
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setLights(Color.BLUE, 3000, 3000)
                .setTicker(ticker)
                .setVibrate(new long[]{0, 300, 200, 300}
                );*/

        String GRUPO_NOTIFICACIONES = tokenEmisor;
        //El objeto es Builder. Pero traemos su paquete NotificationCompact pq existen muchos objetos Builder para no confundirnos.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        //PARA LA IMAGEN Grande con icono y con subtexto.
              /* notificationBuilder.setLargeIcon(bitmap)//Notification icon image
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                notificationBuilder.bigPicture(bitmap))
                notificationBuilder.setSubText(messageBody)*/

        if (bitmap == null) {

            notificationBuilder.setSmallIcon(R.drawable.image5);//Notification sin imagen desde el WS
            //notificationBuilder.setLargeIcon(Bitmap.);

        } else {
            //Reducimos el tamaño del bitmap 140
            Bitmap bitmatReducido = Tratamiento_Imagenes.redimensionarImagenMaximo(bitmap, 140, 140);
            //Redondeamos el bitmap
            Bitmap bitmatRedondo = Tratamiento_Imagenes.getRoundedCornerBitmap(bitmatReducido, true);

            notificationBuilder.setLargeIcon(bitmatRedondo);//Foto. Imagen bajada desde el WS
            notificationBuilder.setSmallIcon(R.drawable.notify);//Icono pequeño al lado de la foto
        }

        //notificationBuilder.setSmallIcon(R.drawable.notify);
        // notificationBuilder.setColor(getResources().getColor(R.color.md_purple_500));

        //notificationBuilder.setContentText(messageBody);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        //notificationBuilder.setAutoCancel(true);

            /*notificationBuilder.setOngoing(false);
            notificationBuilder.setAutoCancel(true);*/
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setSound(defaultSoundUri);
        //notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setLights(Color.BLUE, 3000, 3000);
        notificationBuilder.setTicker(ticker);
        notificationBuilder.setVibrate(new long[]{0, 300, 200, 300});//Para,vibra,para,vibra...
        notificationBuilder.setContentText(messageBody);

        //Agrupamos los notificaciones:

        //notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0));

        //Personalización; tamaño grande con dos botones...
       notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                   .setNumber(notificationCount++)
               .addAction(R.drawable.ic_reply_white_24dp,
                        "RESPONDER", pendingIntent)

                .addAction(R.drawable.ic_merge_type_white_24dp,
                        "IGNORAR", pendinIgnorar)

                .setAutoCancel(true)
                //.setContentInfo("Info")
                //.setOngoing(false)
                .setContentIntent(pendingIntent);



     /*   if(notificationCount==1) {
            notificationBuilder.setContentTitle(title);
        } else{
            notificationBuilder.setContentTitle("Tienes "+ notificationCount + " mensajes.");
        }*/


       /*    notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(messageBody))

                    .setNumber(notificationCount++)
                    .addAction(R.drawable.notify,
                            "RESPONDER", pendingIntent)

                    .addAction(R.drawable.ic_done,
                            "IGNORAR", pendinIgnorar)
           .setAutoCancel(true);*/






      /*  for (NotificationCompat.MessagingStyle.Message message : arraydata) {
            //Agrupamos las notificaciones por token del usuario que ha enviado los mensajes...
            notificationBuilder.setStyle(
                    new NotificationCompat.InboxStyle()
                            .addLine(messageBody)
                            .addLine(messageBody)
                            .addLine(messageBody)
                            .addLine(messageBody)
                            .addLine(messageBody)

                            .setBigContentTitle(title)
            );

        }
            notificationBuilder.setGroup(GRUPO_NOTIFICACIONES);
            notificationBuilder.setGroupSummary(true);*/


        // .build();
        //.setOngoing(true);//Para que la notificación quede anclada

        //notificationBuilder.build();



       /* if (Build.VERSION.SDK_INT >= 19) {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));
            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap));
        }*/

          /*  NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);*/
       /* Random rn = new Random();//Si necesitamos que el id sea diferente en cada notificación para que las notificaciones se apilen una detrás de otra.
        int id = rn.nextInt(100 + 1);*/
        //int id = 1;
        //notificationManager.cancelAll();

        Notification notification=notificationBuilder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

        //Mostrar por pantalla la notificación.

        notificationManager.notify(NOTIFICATION_ID, notification);


        //LA NOTIFICACIÓN YA NO SALDRÍA
        //notificationManager.cancel(123);




    }

    private void sendNotification (String messageBody, String data, String state, Class < ?>
            activityCompat){
        int requestID = (int) System.currentTimeMillis();
        Intent intent = new Intent(this, activityCompat);
        Bundle bundle = new Bundle();
        bundle.putString("message", data);
        bundle.putString("state", state);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.notify)
                .setContentTitle("Title")
                .setContentText(messageBody)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setTicker(messageBody);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        Notification notification = notificationBuilder.build();
        //notificationManager.notify((int) Calendar.getInstance().getTimeInMillis(), notification);
    }
        private void showNotification (String title, String text){


            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)

                            //.setSmallIcon(R.drawable.image3)//En vez de utilizar el icono de la app
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.notify)//Obligatorio
                            //.setLargeIcon(R.drawable.image3)
                            .setColor(getResources().getColor(R.color.md_purple_500))
                            //.setTicker()
                            //.setLights(NotificationCompat.DEFAULT_LIGHTS,NotificationCompat.DEFAULT_LIGHTS,NotificationCompat.DEFAULT_LIGHTS)
                            .setLights(Color.BLUE, 3000, 3000)
                            .setContentTitle(title)//Obligatorio
                            //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setVibrate(new long[]{0, 300, 200, 300})
                            .setContentText(text);//Obligatorio

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //notificationBuilder.setSound(alarmSound);


            //Hacia dónde nos va a redirigir cuando presionemos la notificación...
            Intent notIntent = new Intent(this, Activity_usuarios.class);

            PendingIntent contIntent = PendingIntent.getActivity(getApplicationContext(), 0, notIntent, PendingIntent.FLAG_ONE_SHOT);//Antes 0

            notificationBuilder.setContentIntent(contIntent);
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            notificationBuilder.setSound(alarmSound);
            //notificationBuilder.setLights(NotificationCompat.DEFAULT_LIGHTS,NotificationCompat.DEFAULT_LIGHTS,NotificationCompat.DEFAULT_LIGHTS);

            if (Build.VERSION.SDK_INT >= 19) {
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                );
                //.bigPicture(img));
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, notificationBuilder.build());


        }

//OTRO EJEMPLO RECOGIENDO DATA DESDE EL WS...
 /*   @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            // Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
        if (remoteMessage.getData().get("state").toString().equals("Request")) {
            sendNotification(remoteMessage.getData().get("body").toString(), remoteMessage.getData().get("message").toString(), remoteMessage.getData().get("state").toString(), Activity_usuarios.class);

            String titulo = remoteMessage.getNotification().getTitle();
            String texto = remoteMessage.getNotification().getBody();
            String from = remoteMessage.getFrom();//Emisor
            String to = remoteMessage.getTo();//Receptor
            String datos = remoteMessage.getData().get("arraydedatos");
        }
    }*/




    }
