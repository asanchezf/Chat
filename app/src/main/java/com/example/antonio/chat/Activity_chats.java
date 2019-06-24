package com.example.antonio.chat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.greysonparrelli.permiso.Permiso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adaptadores.AdaptadorChatsViewHolder_new;
import id.zelory.compressor.Compressor;
import modelos.Chats;
import modelos.Usuarios;
import referencias.FirebaseHelper;
import referencias.MisReferencias;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import util.FileUtil;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static referencias.MisReferencias.REFERENCIA_USUARIOS;

public class Activity_chats extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "";
    private static final String LOGTAG = "android-fcm";
    private static final int TRAER_DE_GALERÍA = 100;
    private final int TRAER_DE_CAMARA = 20;
    private final int PERMISOS = 200;
    private EditText txtEscribir;
    private RecyclerView lista;
    private Toolbar toolbar, toolbar2;
    private ImageView imgToolbar;
    private FirebaseRecyclerAdapter mAdapter;
    private String nick;
    private String email_preferences;
    private DatabaseReference databaseReferenceChats;//TRAEMOS DATOS DE LOS CHATS
    private DatabaseReference databaseReferenceUsuarios;//TRAEMOS DATOS DE LOS USUARIOS

    //CONSULTAS: 1-REFERENCIA 2--FUNCION DE ORDENACIÓN 3--FUNCION DE CONSULTA

    private String receptor;
    private String mensaje = "";
    private String emisor;
    private int notificacion_id;//Para anular la notificación que abre la activity
    //Para las notificaciones
    private String tokenEmisor;
    private String tokenReceptor;
    //Parametros enviados al WS.
    private static final String TOKEN = "token";
    private static final String EMISOR = "emisor";
    private static final String MENSAJE = "mensaje";
    private static final String IMAGEN_ICONO = "imagen_icono";
    //Para la imagen del icono en toolbar
    private String urlDescarga;
    private static final String IMAGE_DEFAULT = "default_image";
    //Para recoger la url del icono del emisor...
    private String urlIconoEmisor;
    //Para el subtitulo de la toolbar
    private String estadoReceptor;
    private String emisor_chateacon = "";
    private String receptor_chateacon = "";
    private String escribiendo = "";
    private String miurlCompartido = "";
    //private MyCountDownTimer cuentaTiempo = null;
    //private int timeBorrado=0;
    private int tiempoConfiguradoEmisor = 0;
    private int tiempoConfiguradoReceptor = 0;
    private int miTiempoBorradoEmisor = 0;

    //Subir imágenes al storage de Firebase
    private StorageReference mtorageReference;
    private Uri mimageUri = null;
    private Uri imageUri = null;
    private static int GALLERY = 1400;
    //private ProgressDialog progressDialog;
    private String urlDescargaImgAdjuntada;
    //private Bitmap mbitmap;
    private File fileImagen;
    private File compressedImage;

    private Chats chatMensaje;
    //private ChatsAdapter mChatsAdapter;//PARA EL ADAPTADOR NORMAL ChatsAdapter.java
    //private List<Chats>mchats;//PARA EL ADAPTADOR NORMAL ChatsAdapter.java
    private Uri miimageUriGaleria = null;//Si adejuntamos una imagen desde el dispositivo. La variable se pasa entre distintas activitys
    private static final String SOLO_IMAGEN = "NO";
    //RUTA PARA ALOJAR LAS IMÁGNES DE LA APP...
    private String NOMBRE_APP = "/Chat";
    private final String SEPARATOR = "/";
    private final String CARPETA_RAIZ = NOMBRE_APP + SEPARATOR;
    private final String CARPETA_FOTOS = "ImagenesChat";
    private final String CARPETA_FOTOS_COMPRIMIDAS = "ImagenesComprimidas";
    private final String RUTA_IMAGENES_APP = CARPETA_RAIZ + CARPETA_FOTOS;
    private final String RUTA_IMAGENES_APP_COMPRIMIDAS = CARPETA_RAIZ + CARPETA_FOTOS_COMPRIMIDAS;
    private ImageButton btnAdjuntar;
    private String urlImagenAvatarReceptor = "";
    private ImageButton btnHacerFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);
        nick = prefs.getString("nick", "Usuario");//Valor por defecto 1000 que se aplica si no encuentra nada
        email_preferences = prefs.getString("email", "emailpordefecto@gmail.com");

        //FirebaseHelper.getDatabasePersistence();

      /*  Puede venir desde
        1-AdaptadorUsuariosViewHolder(Funciona correcto como siempre)
        2-Desde el servicio que recoge las notificaciones: FCMService al pulsar el botón de responder desde la notificación abrimos también esta activity
        en el segundo caso el parámetro parametro_receptor_token es realmente el del emisor del mensaje para poder seguir chateando con él...
        3-Cuando desde un navegador web compartimos una url. Entraría por Activity_usuarios y la url se pasaría como parámetro en el bundle: miurlCompartido
        4-Cuando compartimos una imagen.Entraría por Activity_usuarios y la imagen se pasaría como parámetro en el bundle: miimageUriGaleria */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            receptor = bundle.getString("parametro_receptor");
            String emailReceptor = bundle.getString("parametro_receptor_email");
            tokenReceptor = bundle.getString("parametro_receptor_token");
            notificacion_id = bundle.getInt("parametro_notificacion");
            miurlCompartido = bundle.getString("parametro_receptor_urlcompartido");
            miTiempoBorradoEmisor = bundle.getInt("parametro_emisor_tiempoBorrado");
            miimageUriGaleria = (bundle.getParcelable("parametro_receptor_imagen"));//Cuando se comparte imagen desde el dispositivo y fuera de la app
            urlImagenAvatarReceptor = bundle.getString("parametro_receptor_avatar");

            Log.i(LOGTAG, "Paso miTiempoBorradoEmisor Activity_Chats " + miTiempoBorradoEmisor);
            Log.i(LOGTAG, "Paso miimageUriGaleria Activity_Chats " + miimageUriGaleria);
        }


        traeDataBaseReference();//Devuelve la ruta correcta al chat elegido: emisor_receptor

        // Si la activity se ha abierto a partir de una notificación, cancelamos la notificación
        if (notificacion_id > 0) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(notificacion_id);
            }
        }

        Log.i(LOGTAG, "Paso por el onCreate");

        //mchats=new ArrayList<>();//PARA EL ADAPTADOR NORMAL
        inicializarComponentes();

        //GESTIONAMOS LOS PERMISOS CON LA LIBRERÍA PERMISOS.... https://github.com/greysonp/permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Permiso.getInstance().setActivity(this);
            solicitarPermisos();
        }else{

            File carpetaImagenes = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGENES_APP);
            if(!carpetaImagenes.exists()){
                carpetaImagenes.mkdirs();
            }
            File carpetaFotosComprimidas = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGENES_APP_COMPRIMIDAS);
            if(!carpetaFotosComprimidas.exists()){
                carpetaFotosComprimidas.mkdirs();
            }

        }


        //ESCUCHAR MENSAJES EN EL CHAT... Y OBTIENE LOS DATOS QUE SE NECESITEN...
        escuchadorMensajes();
        //Gestiona el valor escribiendo en la toolbar
        gestionaCambiaEscribeUsuario();
    }

    private void solicitarPermisos() {


        Permiso.getInstance().
                requestPermissions(new Permiso.IOnPermissionResult() {
                                       @Override
                                       public void onPermissionResult(Permiso.ResultSet resultSet) {
                                           //PERMISO PARA LEER/ESCRIBIR EN EXTERNAL STORAGE
                                           if (resultSet.isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                               //Toast.makeText(Activity_chats.this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                                               btnAdjuntar.setEnabled(true);
                                               File carpetaImagenes = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGENES_APP);
                                               if(!carpetaImagenes.exists()){
                                                   carpetaImagenes.mkdirs();
                                               }

                                           } else if (resultSet.isPermissionPermanentlyDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                               Toast.makeText(Activity_chats.this, "Se ha denegado permanentemente el permiso para escribir en la tarjeta SD", Toast.LENGTH_SHORT).show();
                                               btnAdjuntar.setEnabled(false);
                                           } else {
                                               Toast.makeText(Activity_chats.this, "Permiso denegado para el acceso a los archivos de imagen", Toast.LENGTH_SHORT).show();
                                               btnAdjuntar.setEnabled(false);
                                               //solicitarPermisosManualmente();
                                           }

                                           //PERMISO PARA EL MANEJO DE LA CÁMARA
                                           if (resultSet.isPermissionGranted(Manifest.permission.CAMERA)) {
                                               //Toast.makeText(Activity_chats.this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                                               btnHacerFoto.setEnabled(true);
                                               File carpetaFotos = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGENES_APP);
                                               if(!carpetaFotos.exists()){
                                                   carpetaFotos.mkdirs();
                                               }
                                               File carpetaFotosComprimidas = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGENES_APP_COMPRIMIDAS);
                                               if(!carpetaFotosComprimidas.exists()){
                                                   carpetaFotosComprimidas.mkdirs();
                                               }
                                           } else if (resultSet.isPermissionPermanentlyDenied(Manifest.permission.CAMERA)) {
                                               Toast.makeText(Activity_chats.this, "Se ha denegado permanentemente el permiso para manejar la cámara", Toast.LENGTH_SHORT).show();
                                               btnHacerFoto.setEnabled(false);
                                               solicitarPermisosManualmente();
                                           } else {
                                               Toast.makeText(Activity_chats.this, "Permiso denegado para el manejo de la cámara", Toast.LENGTH_SHORT).show();
                                               btnHacerFoto.setEnabled(false);
                                           }


                                       }

    @Override
    public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
        Permiso.getInstance().showRationaleInDialog("Los permisos están desactivados", "Debes aceptar los permisos solicitados para el correcto funcionamiento de la App", null, callback);
    }
},
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA);
        }


private void gestionaCambiaEscribeUsuario() {
//Gestiona el valor escribiendo en la toolbar mediante los eventos del editText que crea los mensajes...
        txtEscribir.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //se ejecuta el instante antes de que se cambie el texto.
                /*cambiaEscribeUsuario(nick,false);
                toolbar.setSubtitle(escribiendo);*/
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //se ejecuta durante el cambio de texto
                //if(escribiendo)toolbar.setSubtitle("Escribiendo...");
                /*cambiaEscribeUsuario(nick,true);
                toolbar.setSubtitle(escribiendo);*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                //se ejecuta inmediatamente después de que se cambia el texto

                String aux = s.toString();
                if (aux.length() > 0) {
                    cambiaEscribeUsuario(nick, true);
                    btnHacerFoto.setVisibility(View.GONE);
                    //toolbar.setSubtitle(escribiendo);
                    //traeEscribeReceptor();

                } else {
                    cambiaEscribeUsuario(nick, false);
                    btnHacerFoto.setVisibility(View.VISIBLE);
                    //toolbar.setSubtitle(estadoReceptor);
                    //traeDatosReceptor();
                }

                traeEscribeReceptor();

            }
        });

    }

    private void escuchadorMensajes() {
        //ESCUCHAR MENSAJES EN EL CHAT... Y OBTIENE LOS DATOS QUE SE NECESITEN...
        databaseReferenceChats.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // for (DataSnapshot data : dataSnapshot.getChildren()) {
                chatMensaje = dataSnapshot.getValue(Chats.class);
                //mchats.add(chatMensaje);/PARA EL ADAPTADOR NORMAL ChatsAdapter.java
                emisor = chatMensaje.getEmisor();

                //OJO: Si no se pone, al enviar un nuevo mensaje no se actualiza la activity y el mensaje enviado queda tapado
                //porque la RecyclerView acaba perdiendo su configuración inicial
                // CON ADAPTADOR RECYCLEVIEW FIREBASE
                mAdapter.notifyDataSetChanged();
                lista.scrollToPosition(mAdapter.getItemCount() - 1);
                // CON ADAPTADOR RECYCLEVIEW NORMAL ChatsAdapter.java
               /* mChatsAdapter.notifyDataSetChanged();
                lista.scrollToPosition(mChatsAdapter.getItemCount() - 1);*/
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //////////VIENE DE LA APP CASAROZAS===============================================
    private void validarPermisos() {

        //Versión anterior a M. NO hacen falta los permisos en tiempo de ejecución
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        //YA TIENE LOS PERMISOS
        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            recogerImagen();
        }

        //NO TIENE LOS PERMISOS. RECOMENDACIÓN Y PERMISOS
        if ((shouldShowRequestPermissionRationale(CAMERA)) ||
                (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))) {
            recomendaciónPermisos();
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
        }


    }

    private void recomendaciónPermisos() {
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(Activity_chats.this);
        alertDialog.setTitle("Permisos desactivados");
        alertDialog.setMessage("Debes aceptar los permisos solicitados para el correcto funcionamiento de la App");
        alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, PERMISOS);

            }
        });
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

   /*     if(requestCode==PERMISOS){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED
                    && grantResults[1]==PackageManager.PERMISSION_GRANTED){

                recogerImagen();
            }


            else{
                solicitarPermisosManualmente();
            }
        }*/
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);

    }

    private void solicitarPermisosManualmente() {
        final CharSequence[] opciones = {"Si", "No"};
        final android.support.v7.app.AlertDialog.Builder alertOpciones = new android.support.v7.app.AlertDialog.Builder(Activity_chats.this);
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Si")) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Los permisos no fueron aceptados. La aplicación no funcionará correctamente.", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();

    }

    private void recogerImagen() {

        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            //Gestionamos las opciones creando un array....
            final CharSequence[] opciones = {"Hacer una fotografía", "Cargar una imagen de la galería", "Cancelar"};

            //En el AlertDialog incluimos las opciones...
            final android.support.v7.app.AlertDialog.Builder alertOpciones = new android.support.v7.app.AlertDialog.Builder(Activity_chats.this);
            //alertOpciones.setIcon(R.drawable.camera);
            alertOpciones.setTitle("Selecciona una opción...");
            alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (opciones[i].equals("Cargar una imagen de la galería")) {
                        // 1- Traer las imágenes de la Galería o de otros directorios :Intent.ACTION_PICK-----ACTION_GET_CONTENT
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        //Nombre que va a tener el archivo de imagen
                        //image_name = (System.currentTimeMillis() / 1000) + ".jpg";
                        //btnGuardar.setEnabled(true);
                        startActivityForResult(intent.createChooser(intent, "Selecciona una aplicación para realizar la acción"), TRAER_DE_GALERÍA);

                    } else if (opciones[i].equals("Hacer una fotografía")) {

                        hacerFotografia();
                        //btnGuardar.setEnabled(true);
                    } else {
                        //btnGuardar.setEnabled(false);
                        dialogInterface.dismiss();
                    }

                }
            });

            alertOpciones.show();
            //FIN DEL ALERTDIALOG

        }//Fin if si tiene permisos

        else {//Todavía no tiene permisos. Le volvemos a mostrar el diálogo....
            validarPermisos();
        }
    }

    private void inicializarComponentes() {

        ImageButton btnEnviar = (ImageButton) findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(this);
        btnHacerFoto = (ImageButton) findViewById(R.id.btnhacerfoto);
        btnHacerFoto.setOnClickListener(this);
        btnAdjuntar = (ImageButton) findViewById(R.id.btnAdjuntar);
        btnAdjuntar.setOnClickListener(this);

        txtEscribir = (EditText) findViewById(R.id.txtEscribir);

        toolbar = (Toolbar) findViewById(R.id.toolbarChat);
        imgToolbar = (ImageView) findViewById(R.id.imgToolbar);
        setSupportActionBar(toolbar);
        setTitle(receptor);
        toolbar.setSubtitle(estadoReceptor);
        toolbar.setTitleMarginStart(6);
        //toolbar.setBackground(ContextCompat.getDrawable(this,R.drawable.degradado));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //SEGUNDA TOOLBAR PARA QUE APAREZCA EL ICONO DE BORRAR MENSAJE AL HACER LONGCLICK
        toolbar2 = (Toolbar) findViewById(R.id.toolbarChat2);
        toolbar2.inflateMenu(R.menu.menu_activitychats_bis);
        toolbar2.setTitle(R.string.eliminarmensaje);


        lista = (RecyclerView) findViewById(R.id.lista);
        //lista.setHasFixedSize(true);//VER =========================================================
        LinearLayoutManager lm;
        lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);//Muestra todos los items que tenga el adaptador posicionándose en el último
        //lm.setReverseLayout(true);//Muestra todos los items que tenga el adaptador posicionándose en el primero
        lista.setLayoutManager(lm);

        // CON ADAPTADOR RECYCLEVIEW FIREBASE
        mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReferenceChats, toolbar, toolbar2, miTiempoBorradoEmisor, tiempoConfiguradoReceptor);
        lista.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        lista.scrollToPosition(mAdapter.getItemCount() - 1);

        // CON ADAPTADOR RECYCLEVIEW NORMAL ChatsAdapter.java
       /* mChatsAdapter = new ChatsAdapter( mchats, toolbar, toolbar2, miTiempoBorradoEmisor, tiempoConfiguradoReceptor);
        lista.setAdapter(mChatsAdapter);
        mChatsAdapter.notifyDataSetChanged();
        lista.scrollToPosition(mChatsAdapter.getItemCount() - 1);*/


        Log.i(LOGTAG, "Paso por el inicializarComponentes");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//POne la flecha EN LA TOOLBAR

        //setSupportActionBar(toolbar2);

        //TOLLBAR SIN ICONO
       /* getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        //TOOLBAR CON ICONO:
        //getSupportActionBar().setDisplayShowHomeEnabled(true);//El icono toma funcionalidad

        //getSupportActionBar().setDisplayUseLogoEnabled(true);

        //getSupportActionBar().setIcon(ic_toolbar);
        //traerImagenToolbar();

        // toolbar.setLogo(imgToolbar.getDrawable());
   /*     getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);//El icono toma funcionalidad
        toolbar.setTitleMarginStart(30);*/
        //toolbar.setOverflowIcon(imgToolbar.getDrawable());//Sale al final de la toolbar
        //traeTokenReceptor();

        /*tiempoConfiguradoEmisor= traeTimeBorrado(nick);
        tiempoConfiguradoReceptor= traeTimeBorrado(receptor);*/

        traerImagenToolbar();
        traerIconoEmisor();
        traeDatosReceptor();
        traeEscribeReceptor();


        if (miurlCompartido != null && !miurlCompartido.equals("")) {
            avisoCompartirUrl();
        }

        if (miimageUriGaleria != null && !miimageUriGaleria.equals("")) {
            avisoCompartirImagen();
        }

    }

    private void avisoCompartirImagen() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.compartirimagen) + " " + receptor);
        //dialog.setMessage("Vas a compartir esta url con "+ receptor);
        dialog.setMessage(getString(R.string.confirmcompartirimagen));
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCancelable(false);

        dialog.setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int boton) {
                        //enviarMensajeUrlCompartida();
                        //Toast.makeText(Activity_chats.this, "Pues a acabar con el desarrollo", Toast.LENGTH_SHORT).show();
                        compartirImagen();
                    }
                });

        dialog.setNegativeButton(android.R.string.no, null);

        dialog.show();
    }

    private void avisoSubirImagen() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.compartirimagen) + " " + receptor);
        //dialog.setMessage("Vas a compartir esta url con "+ receptor);
        dialog.setMessage(getString(R.string.confirmcompartirimagen));
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCancelable(false);

        dialog.setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int boton) {

                        subirImagen();
                    }
                });

        dialog.setNegativeButton(android.R.string.no, null);

        dialog.show();
    }

    private void compartirImagen() {
    /*    Uri uri_bitMap = miimageUriGaleria;
        //1-FORMA: Convertimos la Uri en un bitmap para poder redimensionarlo y luego guardarlo en el storage de Firebase
        try {
            mbitmap = Tratamiento_Imagenes.getThumbnail(uri_bitMap, this);//Bitmap a partir de una Uri

        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
        }*/

        //2-FORMA: convertir la Uri en un  ARRAY DE BYTES COMPRIMIDO y luego guardarlo en el storage de Firebase
    /*    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        bmOptions.inSampleSize = 1;
        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmOptions.inJustDecodeBounds = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mbitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        final byte[] foto = baos.toByteArray();*/


/*
        File mediaFile;
        mediaFile = new File(mbitmap+ ".jpg");*/

        //mimageUri = result.getUri();
        // mimageUri = data.getData();

        //mimageUri=miimageUriGaleria;//Igualamos la Uri traida de la galería con la Uri genérica...


        try {

            fileImagen = FileUtil.from(this, miimageUriGaleria);
            comprimirImagen();//Tenemos compressedImage
            //avisoSubirImagen();
            subirImagen();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //subirImagen();

    }


    private void subirImagen() {

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Transfiriendo datos al Servidor... espera por favor.");
        pDialog.show();

        if (compressedImage != null) {

            mtorageReference = FirebaseStorage.getInstance().getReference();
/*
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resized_bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);//0-100. 100 calidad máxima. SE REDUCE PARA QUE AL GUARDA EN SERVIDOR EL ARCHIVO TENGA MENOS PESO
                resized_bitmap.recycle();//reciclamos memoria
                byte[] array = stream.toByteArray();
                mimageUri = Uri.parse(Base64.encodeToString(array, 0));*/

            //2-FORMA: convertir la Uri en un  ARRAY DE BYTES COMPRIMIDO y luego guardarlo en el storage de Firebase
           /* BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            bmOptions.inSampleSize = 1;
            bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bmOptions.inJustDecodeBounds = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Bitmap.createScaledBitmap(mbitmap, 750, 600, false);//establecemos el tamaño EXACTO PARA CUANDO SE VAYA A MOSTRAR EN EL RECYCLERVIEW(300dp *200dp)
            mbitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//0-100. 100 calidad máxima. SE REDUCE PARA QUE AL GUARDA EN SERVIDOR EL ARCHIVO TENGA MENOS PESO
            mbitmap.recycle();//reciclamos memoria
            final byte[] miArrayBytes = baos.toByteArray();*/


            //https://firebase.google.com/docs/storage/android/upload-files?hl=es-419

           /* BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            bmOptions.inSampleSize = 1;
            bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bmOptions.inJustDecodeBounds = false;
            Bitmap resized_bitmap = Bitmap.createScaledBitmap(mbitmap, 450, 300, false);*///establecemos el tamaño EXACTO PARA CUANDO SE VAYA A MOSTRAR EN EL RECYCLERVIEW

            //FUNCIONA PERO HAY MUCHA PÉRDIDA DE CALIDAD EN LA IMAGEN
            /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap miBitmapReducido= Tratamiento_Imagenes.redimensionarImagen(mbitmap,400,300);
            miBitmapReducido.compress(Bitmap.CompressFormat.JPEG, 100, stream);//0-100. 100 calidad máxima. SE REDUCE PARA QUE AL GUARDA EN SERVIDOR EL ARCHIVO TENGA MENOS PESO
            miBitmapReducido.recycle();//reciclamos memoria
            byte[] miArrayBytes = stream.toByteArray();*/

            //https://code.tutsplus.com/es/tutorials/firebase-for-android-file-storage--cms-27376
            //SUBIMOS LA IMAGEN ADJUNTADA AL REPOSITORIO del receptor del mensaje en FIREBASE
            final StorageReference mfilepath = mtorageReference.child("Images").child(nick).child("imagenes").child(compressedImage.toString());
            //final StorageReference mfilepath = mtorageReference.child("Images").child(nick).child("imagenes").child("miArchvo.jpg");
            //mfilepath.putFile(miArrayBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {//SUBIMOS UNA URI
            //mfilepath.putFile(mimageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {//SUBIMOS UNA URI
            mfilepath.putFile(Uri.fromFile(compressedImage)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {//SUBIMOS UN ARCHIVO
                                                                                      //mfilepath.putBytes(miArrayBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {//SUBIDA DE UN ARRAY DE BYTES
                                                                                      @Override
                                                                                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {//Subimos un File

                                                                                          //Uri dowloadUri = taskSnapshot.getDownloadUrl();
                                                                                          String dowloadString = taskSnapshot.getDownloadUrl().toString();//NO DA ERROR aunque subraye en rojo.

                                                                                          mensajeconImagen(dowloadString);

                                                                                      }//Fin onSuccess

                                                                                  }//Fin  mfilepath.putFile

            )
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Log.w(TAG, "uploadPhoto:onError", e);
                            pDialog.dismiss();
                            Toast.makeText(Activity_chats.this, "No se ha podido compartir la imagen",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

            pDialog.dismiss();

      /*  if (databaseReference != null) databaseReference = null;
        if (db != null) db = null;*/
        }
    }

    private void traeDataBaseReference() {
        //Devuelve la ruta correcta al chat elegido. Reordena nick+receptor según corresponda. Devuelve el keychat que se utilizará en la query.
        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();

        databaseReferenceChats = firebaseauxiliar.getChatsReference(nick, receptor);//databaseReferenceChats recoge los datos para el adaptador AdaptadorChatsViewHolder
    }

    //Actualiza el subtítulo de la toolbar con el estado del usuario receptor del mensaje
    private void traeDatosReceptor() {

        //ANTES:
     /*   DatabaseReference database;
        DatabaseReference db;
        database = FirebaseDatabase.getInstance().getReference();
        db = database.getRoot().child(REFERENCIA_USUARIOS).child(receptor);*/

        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceUsuarios = firebaseauxiliar.getDatosReceptor(receptor);
        databaseReferenceUsuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() == null) salidaObligatoria();

                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);

                if (usuarios != null) {
                    //Con quien está chateando...
                    receptor_chateacon = usuarios.getChateaCon();
                    tokenReceptor = usuarios.getTokenNotify();
                    tiempoConfiguradoReceptor = usuarios.getTimeBorrado();
                    //Estado del receptor:
                    boolean estadoRecibido = (boolean) usuarios.isOnline();
                    if (estadoRecibido) {
                        //estadoReceptor = "En línea";
                        estadoReceptor = getString(R.string.enlinea);
                        toolbar.setSubtitle(estadoReceptor);
                    } else {
                        estadoReceptor = getString(R.string.noenlinea);
                        toolbar.setSubtitle(estadoReceptor);
                    }

                }//Fin if (usuarios!=null)

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.getMessage();
            }
        });

//        if (database != null) database = null;
//        if (db != null) db = null;

    }


    private void enviarMensajeUrlCompartida() {

        mensaje = miurlCompartido;
        //traerIconoEmisor();
        long fechaHora = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String Stringfechahora = sdf.format(fechaHora);

        //Recogemos el tocken
        tokenEmisor = FirebaseInstanceId.getInstance().getToken();
        Log.i(LOGTAG, "Token creado al enviar mensaje: " + "EMISOR: " + tokenEmisor + " RECEPTOR: " + tokenReceptor);

        String noImagen = "SIN IMAGEN";
        //emisor,fecha, mensaje,receptor,emailEmisor,longFecha(para borrado del mensaje),paraBorrar(boolean),imagen(ruta)
        Chats michat = new Chats(nick.trim(), Stringfechahora, mensaje.trim(), receptor, email_preferences, fechaHora, false, noImagen);
        //Chats michat = new Chats(nick.trim(), Stringfechahora, mensaje.trim(), receptor, email_preferences);//Con formato fecha String
        //root=misChats.getRoot().child(REFERENCIA_CHATS).child(keyChat);
        //root.push().setValue(michat);
        databaseReferenceChats.push().setValue(michat);

        //cambiaActivityUsuario(emisor_chateacon);

        //ENVIAMOS NOTIFICACIÓN SOLO SI EMISOR Y RECEPTOR ESTÁN EN ACTIVITYS DIFERENTES...
        if (!emisor_chateacon.equals(receptor_chateacon)) {
            envioNotificacionVolley();

        } else if (miurlCompartido != null && !miurlCompartido.equals("")) {
            envioNotificacionVolley();
        }

        txtEscribir.setText("");

    }

    private void avisoCompartirUrl() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.compartirlink) + " " + receptor);
        //dialog.setMessage("Vas a compartir esta url con "+ receptor);
        dialog.setMessage(getString(R.string.compartirurl) + " " + miurlCompartido);
        dialog.setIcon(R.drawable.image5);
        dialog.setCancelable(false);

        dialog.setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int boton) {
                        enviarMensajeUrlCompartida();

                    }
                });

        dialog.setNegativeButton(android.R.string.no, null);

        dialog.show();
    }

    //Actualiza el subtítulo de la toolbar con el estado del usuario receptor del mensaje
    private void traeEscribeReceptor() {

        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceUsuarios = firebaseauxiliar.getDatosReceptor(receptor);
        databaseReferenceUsuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);

                if (usuarios != null) {

                    //Si está escribiendo en estos momentos...
                    boolean estadoescribiendo = (boolean) usuarios.isEscribe();
                    if (estadoescribiendo) {

                        escribiendo = getString(R.string.escribiendo);
                        toolbar.setSubtitle(escribiendo);
                    } else {
                        //escribiendo = "";
                        escribiendo = getString(R.string.noescribiendo);
                        //toolbar.setSubtitle(escribiendo);
                        toolbar.setSubtitle(estadoReceptor);
                    }

                }//Fin if (usuarios!=null)
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*if (database != null) database = null;
        if (db != null) db = null;*/
    }

    //Recupera la imagen de perfil del receptor
    private void traerImagenToolbar() {

        //13 de noviembre 2017 ahora la url de la imagen viene por parámetro desde el adaptador AdaptadorUsuariosViewHolder.
        if (urlImagenAvatarReceptor != null) {
            Glide.with(getApplicationContext())
                    .load(Uri.parse(urlImagenAvatarReceptor))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    //Si hay transformación no se debe poner el placeholder pq lo carga antes de que se carga la imagen transformada...
                    //.override(20,20)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                    // .placeholder(ic_toolbar)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                    .error(R.drawable.nuevo)//Imagen de sustitución si se ha producido error de carga
                    //.centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                    //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                    // .transform(new util.CircleTransform(Activity_chats.this))
                    .into(imgToolbar);//dónde vamos a mostrar las imágenes

        } else {//SI ENTRAMOS DESDE UNA NOTIFIACIÓN DEBEMOS ACCEDER A LA IMAGEN
            FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
            databaseReferenceUsuarios = firebaseauxiliar.getDatosReceptor(receptor);
            databaseReferenceUsuarios = databaseReferenceUsuarios.getRoot().child(REFERENCIA_USUARIOS).child(receptor).child("image");//TRAE SOLO LA IMAGEN

            databaseReferenceUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() == null) {
                        //salidaObligatoria();
                        return;
                    }
                    urlDescarga = dataSnapshot.getValue().toString();//SI TRAEMOS SOLO LA IMAGEN...

                    if (!urlDescarga.equals(IMAGE_DEFAULT)) {
                        Glide.with(getApplicationContext())
                                .load(Uri.parse(urlDescarga))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)

                                //Si hay transformación no se debe poner el placeholder pq lo carga antes de que se carga la imagen transformada...
                                //.override(4,4)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                                // .placeholder(ic_toolbar)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                                .error(R.drawable.nuevo)//Imagen de sustitución si se ha producido error de carga
                                //.centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                                //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                                .diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                                // .transform(new util.CircleTransform(Activity_chats.this))
                                .into(imgToolbar);//dónde vamos a mostrar las imágenes
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(Activity_chats.this, "No se ha podido cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            });

        }




        /*if (database != null) database = null;
        if (db != null) db = null;*/


    }

    //CUANDO DESDE UNA APLICACIÓN DE AGENDA SE INTENTA DIRECTAMENTE ENTRAR EN EL CHAT... CONTROLA SI EL USUARIO AL QUE QUIERE ENVIAR EL MENSAJE EXISTE EN EL CHAT...
    private void salidaObligatoria() {
        Toast.makeText(getBaseContext(),
                R.string.usuarioNoExiste,
                Toast.LENGTH_LONG).show();

      /*  Snackbar snack = Snackbar.make(lista, R.string.noexisteusuario,
                Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snack.getView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            group.setBackground(ContextCompat.getDrawable(this, R.drawable.degradado));
        } else {
            group.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        snack.setDuration(7000);
        snack.show();*/

        Intent intent = new Intent(Activity_chats.this, Activity_usuarios.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    //BORRA UN MENSAJE SELECCIONADO MEDIANTE LONGCLICK
    public void mensajeParaBorrar() {
         /*long startTime=15*60*1000; // 15 MINS IDLE TIME
         final long interval = 1 * 1000;*/
        mensaje = txtEscribir.getText().toString();

        if (!mensaje.isEmpty()) {
            //lLAMADA A LA CLASE QUE CONTROLA EL TIEMPO
            /*long startTime = 60 * 1000; // 1 MINUTO HASTA QUE SE EJECUTE
            final long interval = 1 * 1000;//SE PUEDE MOSTRAR EL INTERVALO DE SEGUNDO EN SEGUNDO
            MyCountDownTimer cuentaTiempo = new MyCountDownTimer(startTime, interval, Activity_chats.this);
            cuentaTiempo.start();*/
            //traeTimeBorrado();
            //boolean paraBorrar = true;
            long fechaHora = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String Stringfechahora = sdf.format(fechaHora);
            //Recogemos el tocken
            tokenEmisor = FirebaseInstanceId.getInstance().getToken();
            Log.i(LOGTAG, "Token creado al enviar mensaje: " + "EMISOR: " + tokenEmisor + " RECEPTOR: " + tokenReceptor);

            String noImagen = "SIN IMAGEN";
            Chats michat = new Chats(nick.trim(), Stringfechahora, mensaje.trim(), receptor, email_preferences, fechaHora, true, noImagen);
            databaseReferenceChats.push().setValue(michat);

            //ENVIAMOS NOTIFICACIÓN SOLO SI EMISOR Y RECEPTOR ESTÁN EN ACTIVITYS DIFERENTES...
            if (!emisor_chateacon.equals(receptor_chateacon)) {
                //Toast.makeText(Activity_chats.this,"RECEPTOR CHATEA: "+receptor_chateacon,Toast.LENGTH_SHORT).show();
                envioNotificacionVolley();
            }

            txtEscribir.setText("");
            //Ocultamos el teclado
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            Snackbar snack = Snackbar.make(lista, R.string.aviso_borrado_auto,
                    Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                group.setBackground(ContextCompat.getDrawable(this, R.drawable.degradado));
            } else {
                group.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            }
            snack.setDuration(5000);
            snack.show();
        }

    }

    private void mensajeconImagen(String dowloadString) {

        long fechaHora = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String Stringfechahora = sdf.format(fechaHora);

        //Recogemos el tocken
        tokenEmisor = FirebaseInstanceId.getInstance().getToken();
        Log.i(LOGTAG, "Token creado al enviar mensaje: " + "EMISOR: " + tokenEmisor + " RECEPTOR: " + tokenReceptor);
        final String mensajeImagen = "Te he enviado una imagen";

        if (mensaje.equals("")) {
            mensaje = mensajeImagen;
        }


        //Chats michat = new Chats(nick.trim(), Stringfechahora, mensajeImagen, receptor, email_preferences, fechaHora, false,dowloadString);
        Chats michat = new Chats(nick.trim(), Stringfechahora, mensajeImagen, receptor, email_preferences, fechaHora, false, dowloadString, String.valueOf(compressedImage));
        //root=misChats.getRoot().child(REFERENCIA_CHATS).child(keyChat);
        //root.push().setValue(michat);

        //8/11/17=> Desde que se introduce la persistencia en Firebase a veces viene a nulo y provoca un error si la app está cerrada
        if (databaseReferenceChats != null) {
            databaseReferenceChats.push().setValue(michat);

        }


        //cambiaActivityUsuario(emisor_chateacon);
        //ENVIAMOS NOTIFICACIÓN SOLO SI EMISOR Y RECEPTOR ESTÁN EN ACTIVITYS DIFERENTES...
        if (!emisor_chateacon.equals(receptor_chateacon)) {
            //Toast.makeText(Activity_chats.this,"RECEPTOR CHATEA: "+receptor_chateacon,Toast.LENGTH_SHORT).show();
            envioNotificacionVolley();
        }

        //txtEscribir.setText("");

        //Ocultamos el teclado
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                   /* mAdapter.notifyDataSetChanged();
                    lista.scrollToPosition(mAdapter.getItemCount() - 1);*/
        Log.i(LOGTAG, "Paso por el onCick");

    }

    //ENVIAR MENSAJE NORMAL
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnviar:
                mensaje = txtEscribir.getText().toString();

                if (!mensaje.isEmpty()) {

                    long fechaHora = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    String Stringfechahora = sdf.format(fechaHora);

                    //Recogemos el tocken
                    tokenEmisor = FirebaseInstanceId.getInstance().getToken();
                    Log.i(LOGTAG, "Token creado al enviar mensaje: " + "EMISOR: " + tokenEmisor + " RECEPTOR: " + tokenReceptor);
                    String noImagen = "SIN IMAGEN";

                    //emisor,fecha, mensaje,receptor,emailEmisor,longFecha(para borrado del mensaje),paraBorrar(boolean),imagen(ruta)
                    Chats michat = new Chats(nick.trim(), Stringfechahora, mensaje.trim(), receptor, email_preferences, fechaHora, false, noImagen);
                    //root=misChats.getRoot().child(REFERENCIA_CHATS).child(keyChat);
                    //root.push().setValue(michat);
                    databaseReferenceChats.push().setValue(michat);

                    //cambiaActivityUsuario(emisor_chateacon);
                    //ENVIAMOS NOTIFICACIÓN SOLO SI EMISOR Y RECEPTOR ESTÁN EN ACTIVITYS DIFERENTES...
                    if (!emisor_chateacon.equals(receptor_chateacon)) {
                        //Toast.makeText(Activity_chats.this,"RECEPTOR CHATEA: "+receptor_chateacon,Toast.LENGTH_SHORT).show();
                        envioNotificacionVolley();
                    }

                    txtEscribir.setText("");
                    //Ocultamos el teclado
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                   /* mAdapter.notifyDataSetChanged();
                    lista.scrollToPosition(mAdapter.getItemCount() - 1);*/
                    Log.i(LOGTAG, "Paso por el onCick");

                }
                break;

            case R.id.btnAdjuntar:
                traerImagen();
                break;
            case R.id.btnhacerfoto:
                hacerFotografia();

            default:
                break;
        }
    }

    private void hacerFotografia() {

        File carpetaImagenes = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGENES_APP);
        String image_name = "";
        boolean isCreada = carpetaImagenes.exists();
        //String nombreImagen="";
        if (!isCreada) {
            isCreada = carpetaImagenes.mkdirs();
        }

        if (isCreada) {
            image_name = (System.currentTimeMillis() / 1000) + ".jpg";
        }

        //Ruta de almacenamiento
        String path = Environment.getExternalStorageDirectory() +
                File.separator + RUTA_IMAGENES_APP + File.separator + image_name;

        File imagen = new File(path);

        //NECESARIO PARA DIFERENCIAR DE ANDROID 7 EN ADELANTE
        //https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed&usg=ALkJrhhQhXdNXh3XREiEgvkith2kH-UVvw
        //https://www.youtube.com/watch?v=At0UmHXMMU8&index=100&list=PLAg6Lv5BbjjdvIcLQdVg4ROZnfuuQcqXB
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authorities = getApplicationContext().getPackageName() + ".provider";
            Uri imageUri = FileProvider.getUriForFile(this, authorities, imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }
        //ANTES DE ANDROID 7
        else {
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        }
        //startActivityForResult(intent, TRAER_DE_CAMARA);

        fileImagen = imagen;

        startActivityForResult(intent, TRAER_DE_CAMARA);

    }

    private void traerImagen() {

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, GALLERY);
    }

    //2
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("RESULT", "onActivityResult " + requestCode + resultCode + data);


        if (requestCode == GALLERY && resultCode == RESULT_OK) {

            //COMRPOBAMOS SI ESTÁ CREADA PREVIAMENTE LA CARPETA EN LA QUE LA APP GUARDARÁ LAS IMÁGENES...
            File carpetaImagenes = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGENES_APP);
            boolean isCreada = carpetaImagenes.exists();

            if (!isCreada) {

                isCreada = carpetaImagenes.mkdirs();
            }

            //else {//Si la carpeta ya se ha creado seguimos...

            try {

                fileImagen = FileUtil.from(this, data.getData());
                comprimirImagen();//Tenemos compressedImage
                avisoSubirImagen();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //}//Fin else

        } else if (requestCode == TRAER_DE_CAMARA && resultCode == RESULT_OK) {

            /*File carpetaImagenes = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGENES_APP);
            String image_name="";
            boolean isCreada = carpetaImagenes.exists();
            //String nombreImagen="";
            if (isCreada == false) {
                isCreada = carpetaImagenes.mkdirs();
            }

            if (isCreada == true) {
                 image_name = (System.currentTimeMillis() / 1000) + ".jpg";
            }

            //Ruta de almacenamiento
            String path = Environment.getExternalStorageDirectory() +
                    File.separator + RUTA_IMAGENES_APP + File.separator + image_name;

            //File imagen = new File(path);

            fileImagen = new File(path);

            //NECESARIO PARA DIFERENCIAR DE ANDROID 7 EN ADELANTE
//https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed&usg=ALkJrhhQhXdNXh3XREiEgvkith2kH-UVvw
            //https://www.youtube.com/watch?v=At0UmHXMMU8&index=100&list=PLAg6Lv5BbjjdvIcLQdVg4ROZnfuuQcqXB
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
            {
                String authorities=getApplicationContext().getPackageName()+".provider";
                Uri imageUri= FileProvider.getUriForFile(this,authorities,fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }
            //ANTES DE ANDROID 7
            else
            {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }*/

     /*       fileImagen = FileUtil.from(this, data.getData());
            fileImagen=data.getData();*/

            comprimirImagen();//Tenemos fileImagen y devuelve compressedImage
            avisoSubirImagen();


        }


    }

    private void comprimirImagen() {

        //validarPermisos();

        if (fileImagen == null) {
            //showError("Please choose an image!");
            Toast.makeText(this, "No hay imagen para comprimir", Toast.LENGTH_SHORT).show();
        } else {
            // Compress image in main thread using custom Compressor
            try {
                compressedImage = new Compressor(this)
                        .setMaxWidth(640)//640
                        .setMaxHeight(480)//480
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)//Formato octeto
                        //.setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        //Octubre 2017 se cambia el directorio donde se alojan las imágenes. Antes era PICTURES ahoro es Chat/Fotos
                        .setDestinationDirectoryPath(Environment.getExternalStorageDirectory() + RUTA_IMAGENES_APP_COMPRIMIDAS)

                        .compressToFile(fileImagen);

                //setCompressedImage();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ERROR COMPRIMIENDO IMAGEN", e.getMessage());
                //showError(e.getMessage());
            }

        }
    }


    //DESPUÉS DE ENVIAR EL MENSAJE SE ENVÍA LA NOTIFIACIÓN...
    private void envioNotificacionVolley() {

        //Si solo introducimos una imagen el mensaje está vacío y la notificiación da error al enviarse porque falta un parámetro.
        //if(i==1) mensaje="NO";

        //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
        RequestQueue requestQueue;//Cola de peticiones
        requestQueue = Volley.newRequestQueue(Activity_chats.this);
        String tag_json_obj_actual = "json_obj_req_actual";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, MisReferencias.PUSH_URL_POST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("!_@@_SUCESS", response + "");
                        //Toast.makeText(Activity_chats.this,"Se ha enviado notificación desde el servidor php",Toast.LENGTH_LONG).show();
                        mensaje = "";
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("!_@@_Errors--", error + "");
                        Toast.makeText(Activity_chats.this, R.string.mensaje_sin_notificacion, Toast.LENGTH_LONG).show();
                    }
                })

        {
            //CABECERA DE LA PETICIÓN
           /* @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }*/

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(TOKEN, tokenReceptor);
                //params.put(EMISOR, emisor);
                params.put(EMISOR, nick);
                params.put(MENSAJE, mensaje.trim());
                params.put(IMAGEN_ICONO, urlIconoEmisor);

                return params;
            }
        };

        // Añadir petición a la cola
        //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
        MyAplication.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);
        //RequestQueue requestQueue = Volley.newRequestQueue(this);

        //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
       /* int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);*/
    }

    //RECOGEMOS LA URL DEL ICONO DEL EMISOR. Se utilizará para enviarlo como parámetro al servicio de notificaciones...
    private void traerIconoEmisor() {

        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceUsuarios = firebaseauxiliar.getDatosEmisor(nick);
        databaseReferenceUsuarios = databaseReferenceUsuarios.getRoot().child(REFERENCIA_USUARIOS).child(nick).child("image");//TRAE SOLO LA IMAGEN


        databaseReferenceUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                urlIconoEmisor = dataSnapshot.getValue().toString();
                if (urlIconoEmisor.equals(IMAGE_DEFAULT)) {
                    urlIconoEmisor = MisReferencias.URL_MI_ICONO_DEFAULT_FIREBASE;
                    //urlIconoEmisor = "https://firebasestorage.googleapis.com/v0/b/chat-8addb.appspot.com/o/Images%2FAvatar%2Fdefault.png?alt=media&token=932dc535-3998-45c9-97e7-60be8e53dfb7";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Activity_chats.this, "No se ha podido cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });

      /*  if (midatabase != null) midatabase = null;
        if (midb != null) midb = null;*/
    }


    @Override//Añadimos fuente
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void cambiaEstadoUsuario(String usuario, boolean estado) {

       /* DatabaseReference databaseReferenceEstado = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db = databaseReferenceEstado.getRoot().child(REFERENCIA_USUARIOS).child(usuario);*/

        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceUsuarios = firebaseauxiliar.getDatosReceptor(usuario);

        Map<String, Object> update = new HashMap<>();
        if (!estado)
            update.put("online", Usuarios.OFFLINE);
        else
            update.put("online", Usuarios.ONLINE);
        databaseReferenceUsuarios.updateChildren(update);
        //Toast.makeText(this,"cambiaestado - Activity_Chats",Toast.LENGTH_SHORT).show();
        Log.i("Activity_Chats", "cambiaestado - Activity_Chats");
    }

    //GRABA LA ACITIVIDAD EN LA QUE ESTÁ EL USUARIO PARA SABER CON QUIEN ESTÁ CHATEANDO...
    private void cambiaActivityUsuario(String chateacon) {


        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceUsuarios = firebaseauxiliar.getDatosReceptor(nick);

        Map<String, Object> update = new HashMap<>();
        update.put("chateaCon", chateacon);
        databaseReferenceUsuarios.updateChildren(update);
        //Toast.makeText(this,"cambiaestado - Activity_Chats",Toast.LENGTH_SHORT).show();
        Log.i("Activity_Chats", "cambiaestado - Activity_Chats");
    }

    //CUANDO ESTÁ ESCRIBIENDO SE LE COMUNICA AL RECEPTOR EN EL SUBTÍTULO DE LA TOOLBAR...
    private void cambiaEscribeUsuario(String usuario, boolean escribe) {

        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceUsuarios = firebaseauxiliar.getDatosReceptor(usuario);

        Map<String, Object> update = new HashMap<>();
        if (!escribe)
            update.put("escribe", false);
        else
            update.put("escribe", true);

        databaseReferenceUsuarios.updateChildren(update);
        //Toast.makeText(this,"escribiendo - Activity_Chats",Toast.LENGTH_SHORT).show();
        Log.i("Activity_Chats", "escribiendo - Activity_Chats");
    }

    @Override
    protected void onResume() {
        super.onResume();
        cambiaEstadoUsuario(nick, true);
        emisor_chateacon = databaseReferenceChats.toString();//USUARIO QUE HA ELEGIDO PARA CHATEAR
        cambiaActivityUsuario(emisor_chateacon);

        //6 de noviembre: se pone para que se recarge el adaptador al descargar las imágenes y verlas en la galería
        //mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReferenceChats, toolbar, toolbar2, miTiempoBorradoEmisor, tiempoConfiguradoReceptor);
        lista.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        lista.scrollToPosition(mAdapter.getItemCount() - 1);
        //6 de noviembre: se pone para que se recarge el adaptador al descargar las imágenes y verlas en la galería
        Permiso.getInstance().setActivity(this);

        Log.i("Activity_Chats", "onResume Activity_Chats");
    }


    @Override
    protected void onStop() {
        super.onStop();
        cambiaEscribeUsuario(nick, false);
        //finish();
        //cambiaEstadoUsuario(nick,false);
        //Toast.makeText(this,"onStop Activity_Chats",Toast.LENGTH_SHORT).show();
        Log.i("Activity_Chats", "onStop Activity_Chats");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAdapter != null) {
            mAdapter.cleanup();
        }

        if (databaseReferenceChats != null) {
            databaseReferenceChats = null;
        }

        if (databaseReferenceUsuarios != null) {
            databaseReferenceUsuarios = null;
        }

       /* if (cuentaTiempo != null) {

            cuentaTiempo.cancel();
        }*/

        //firebaseauxiliar=null;
        // Toast.makeText(this,"onDestroy Activity_Chats",Toast.LENGTH_SHORT).show();
        Log.i("Activity_Chats", "onDestroy Activity_Chats");
    }

    @Override
    public void onBackPressed() {
        //FirebaseAuth.getInstance().signOut();
        //Toast.makeText(this,"Sesión cerrada",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Activity_chats.this, Activity_usuarios.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activitychats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            mensajeParaBorrar();
        }

        return super.onOptionsItemSelected(item);
    }

    private void controlaTiempo() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            /*Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish();*/
            }
        }, 8000);//splash de 8 segundos.


    }

    private void traeTokenReceptor() {

        DatabaseReference database;
        DatabaseReference db;
        database = FirebaseDatabase.getInstance().getReference();
        //db = database.getRoot().child(REFERENCIA_USUARIOS).child(receptor).child("online");
        db = database.getRoot().child(REFERENCIA_USUARIOS).child(receptor);





    /*    Usuarios usuarios=new Usuarios();
        usuarios=db.setValue(Usuarios.class);*/

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) salidaObligatoria();
                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);
                tokenReceptor = usuarios.getTokenNotify();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (database != null) database = null;
        if (db != null) db = null;

        //return tokenReceptor;
    }

    private void comprobarExisteUsuarioReceptor() {

        DatabaseReference database;
        DatabaseReference db;
        database = FirebaseDatabase.getInstance().getReference();
        //db=database.getRoot().child(REFERENCIA_USUARIOS).child(receptor);//TRAE TODOS LOS DATOS DEL USUARIO
        db = database.getRoot().child(REFERENCIA_USUARIOS).child(receptor);


        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String valor = "";
                valor = dataSnapshot.toString();
                boolean boool = dataSnapshot.exists();
                if (dataSnapshot.getValue() == null) salidaObligatoria();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //PARA COMPROBAR EL NOMBRE DE LA CLASE QUE ESTÁ UTILIZADNO EL USUARIO EN EL MOMENTO ACTUAL...
    public boolean comprobarActivityALaVista(
            Context context, String nombreClase) {

        // Obtenemos nuestro manejador de activitys
        ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        // obtenemos la informacion de la tarea que se esta ejecutando
        // actualmente
        List<ActivityManager.RunningTaskInfo> taskInfo =
                am.getRunningTasks(1);
        // Creamos una variable donde vamos a almacenar
        // la activity que se encuentra a la vista
        String nombreClaseActual = null;

        try {
            // Creamos la variable donde vamos a guardar el objeto
            // del que vamos a tomar el nombre
            ComponentName componentName = null;
            // si pudimos obtener la tarea actual, vamos a intentar cargar
            // nuestro objeto
            if (taskInfo != null && taskInfo.get(0) != null) {
                componentName = taskInfo.get(0).topActivity;
            }
            // Si pudimos cargar nuestro objeto, vamos a obtener
            // el nombre con el que vamos a comparar
            if (componentName != null) {
                nombreClaseActual = componentName.getClassName();
            }

        } catch (NullPointerException e) {

            Log.e(TAG, "Error al tomar el nombre de la clase actual " + e);
            return false;
        }

        // devolvemos el resultado de la comparacion
        return nombreClase.equals(nombreClaseActual);
    }
}
