package com.example.antonio.chat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adaptadores.AdaptadorChatsViewHolder_new;
import modelos.Chats;
import modelos.Usuarios;
import referencias.FirebaseHelper;
import referencias.MisReferencias;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.example.antonio.chat.R.drawable.ic_person;
import static referencias.MisReferencias.REFERENCIA_USUARIOS;

public class Activity_chats extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "";
    private static final String LOGTAG = "android-fcm";
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
    private String mensaje;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);
        nick = prefs.getString("nick", "Usuario");//Valor por defecto 1000 que se aplica si no encuentra nada
        email_preferences = prefs.getString("email", "emailpordefecto@gmail.com");

      /*  Puede venir desde
        1-AdaptadorUsuariosViewHolder(Funciona correcto como siempre)
        2-Desde el servicio que recoge las notificaciones: FCMService al pulsar el botón de responder desde la notificación abrimos también esta activity
        en el segundo caso el parámetro parametro_receptor_token es realmente el del emisor del mensaje para poder seguir chateando con él...
        3-Cuando desde un navegador web compartimos una url. Entraría por Activity_usuarios y la url se pasaría como parámetro en el bundle: miurlCompartido */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            receptor = bundle.getString("parametro_receptor");
            String emailReceptor = bundle.getString("parametro_receptor_email");
            tokenReceptor = bundle.getString("parametro_receptor_token");
            notificacion_id = bundle.getInt("parametro_notificacion");
            miurlCompartido = bundle.getString("parametro_receptor_urlcompartido");
            miTiempoBorradoEmisor = bundle.getInt("parametro_emisor_tiempoBorrado");
            Log.i(LOGTAG, "Paso miTiempoBorradoEmisor Activity_Chats " + miTiempoBorradoEmisor);
        }

        //devuelveTiempoBorrado(receptor);

        //traeTimeBorradoReceptor(receptor);Ahora desde onCreate,,,traeDatosReceptor()
        //traeTimeBorradoEmisor(nick);

        traeDataBaseReference();//Devuelve la ruta correcta al chat elegido: emisor_receptor


        // Si la activity se ha abierto a partir de una notificación, cancelamos la notificación
        if (notificacion_id > 0) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(notificacion_id);

        }

        Log.i(LOGTAG, "Paso por el onCreate");


        inicializarComponentes();

        //ESCUCHAR MENSAJES EN EL CHAT... Y OBTIENE LOS DATOS QUE SE NECESITEN...
        databaseReferenceChats.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //for (DataSnapshot data : dataSnapshot.getChildren()) {
                //}
                //txtTexto.append(dataSnapshot.getValue().toString()+" \n");

                Chats chatMensaje = dataSnapshot.getValue(Chats.class);
                emisor = chatMensaje.getEmisor();


                //OJO: Si no se pone, al enviar un nuevo mensaje no se actualiza la activity y el mensaje enviado queda tapado
                //porque la RecyclerView acaba perdiendo su configuración inicial

                mAdapter.notifyDataSetChanged();
                lista.scrollToPosition(mAdapter.getItemCount() - 1);


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
                    //toolbar.setSubtitle(escribiendo);
                    //traeEscribeReceptor();

                } else {
                    cambiaEscribeUsuario(nick, false);
                    //toolbar.setSubtitle(estadoReceptor);
                    //traeDatosReceptor();
                }

                traeEscribeReceptor();

            }
        });

    }

    private void configuraRecyclerView() {

        lista = (RecyclerView) findViewById(R.id.lista);
        LinearLayoutManager lm;
        lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);//Muestra todos los items que tenga el adaptador posicionándose en el último
        //lm.setReverseLayout(true);//Muestra todos los items que tenga el adaptador posicionándose en el primero
        lista.setLayoutManager(lm);
        mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReferenceChats, toolbar, toolbar2, miTiempoBorradoEmisor, tiempoConfiguradoReceptor);
        lista.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        lista.scrollToPosition(mAdapter.getItemCount() - 1);

    }

    private void inicializarComponentes() {
        ImageButton btnEnviar = (ImageButton) findViewById(R.id.btnEnviar);
        txtEscribir = (EditText) findViewById(R.id.txtEscribir);
        btnEnviar.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbarChat);
        imgToolbar = (ImageView) findViewById(R.id.imgToolbar);
        setSupportActionBar(toolbar);
        setTitle(receptor);
        toolbar.setSubtitle(estadoReceptor);
        toolbar.setTitleMarginStart(0);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //SEGUNDA TOOLBAR PARA QUE APAREZCA EL ICONO DE BORRAR MENSAJE AL HACER LONGCLICK
        toolbar2 = (Toolbar) findViewById(R.id.toolbarChat2);
        toolbar2.inflateMenu(R.menu.menu_activitychats_bis);
        toolbar2.setTitle("Eliminar mensaje");


        lista = (RecyclerView) findViewById(R.id.lista);
        LinearLayoutManager lm;
        lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);//Muestra todos los items que tenga el adaptador posicionándose en el último
        //lm.setReverseLayout(true);//Muestra todos los items que tenga el adaptador posicionándose en el primero
        lista.setLayoutManager(lm);
        mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReferenceChats, toolbar, toolbar2, miTiempoBorradoEmisor, tiempoConfiguradoReceptor);
        lista.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        lista.scrollToPosition(mAdapter.getItemCount() - 1);


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


        //cambiaActivityUsuario(databaseReference.toString());
      /*  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_toolbar);*/
        //Casi bien...
    /*    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_toolbar));*/

        /*//Devuelve la ruta correcta al chat elegido. Reordena nick+receptor según corresponda. Devuelve el keychat que se utilizará en la query.
        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReference = firebaseauxiliar.getChatsReference(nick, receptor);//databaseReference recoge los datos para el adaptador AdaptadorChatsViewHolder*/
        //query=misChats.child(keyChat);
        //mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReference);
        //mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReference, toolbar, toolbar2, Activity_chats.this);
        //mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReference, toolbar, toolbar2,nick);
        //mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReference, toolbar, toolbar2);
       /* if(tiempoConfiguradoEmisor==0) tiempoConfiguradoEmisor= traeTimeBorrado(nick);
        if(tiempoConfiguradoReceptor==0) tiempoConfiguradoReceptor= traeTimeBorrado(receptor);*/


        //mAdapter = new AdaptadorChatsViewHolder_new(R.layout.fila_recyclerview_chat_new2, databaseReference, toolbar, toolbar2,tiempoConfiguradoEmisor,tiempoConfiguradoReceptor);

       /* lista.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();*/

        //lista.scrollToPosition(mAdapter.getItemCount() - 1);

        if (miurlCompartido != null && !miurlCompartido.equals("")) {
            //traerIconoEmisor();
            //enviarMensajeUrlCompartida();
            avisoCompartirUrl();
        }
    }

    private void devuelveTiempoBorrado(String receptor) {
        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        tiempoConfiguradoReceptor = firebaseauxiliar.getDevuelveTiempoBorrado(receptor);
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
                        estadoReceptor = "En línea";
                        toolbar.setSubtitle(estadoReceptor);
                    } else {
                        estadoReceptor = "";
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

    private void traeTimeBorradoEmisor(String usuario) {

        /*DatabaseReference database1;
        DatabaseReference db;
        database1 = FirebaseDatabase.getInstance().getReference();
        db = database1.getRoot().child(REFERENCIA_USUARIOS).child(usuario);*/


        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceUsuarios = firebaseauxiliar.getDatosReceptor(usuario);

        databaseReferenceUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Toast.makeText(Activity_chats.this, "NO hay datos...", Toast.LENGTH_SHORT).show();
                    return;
                }

                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);
                tiempoConfiguradoEmisor = usuarios.getTimeBorrado();

                /*El Adapter y la RecyclerView se inicializan aquí porque el Adapter espera valores de Firebase para poder inicializarse y porque la RecyclerView
                acaba perdiendo su configuración inicial y debe volver a inicializarse en el databaseReferenceChats.addChildEventListener del onCreate()*/

                if (databaseReferenceChats != null) {//
                    configuraRecyclerView();
                    Log.i(LOGTAG, "Paso por traeTimeBorradoEmisor //databaseReferenceChats!=null");
                }

                Log.i(LOGTAG, "Paso por traeTimeBorradoEmisor");

                //Toast.makeText(Activity_chats.this, "Tiempo configurado emisor: " + nick + " " + tiempoConfiguradoEmisor, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ERROR CARGANDO VALOR TIEMBORRADO", databaseError.getMessage());
            }
        });

        //COMO LA INSTANCIA databaseReferenceUsuarios SE CREA A NIVEL GENERAL SE DESTRUYE EN EL onDestroy()
       /* if (database1 != null) database1 = null;
        if (db != null) db = null;*/


    }

    private void traeTimeBorradoReceptor(String usuario) {
        // final int[] timeBorrado = {0};
       /* DatabaseReference database;
        DatabaseReference db;
        database = FirebaseDatabase.getInstance().getReference();
        db = database.getRoot().child(REFERENCIA_USUARIOS).child(usuario);*/

        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceUsuarios = firebaseauxiliar.getDatosReceptor(usuario);

        databaseReferenceUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Toast.makeText(Activity_chats.this, "NO hay datos...", Toast.LENGTH_SHORT).show();
                    return;
                }

                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);
                /*timeBorrado[0] =usuarios.getTimeBorrado();
                tiempoConfiguradoReceptor= timeBorrado[0];*/
                tiempoConfiguradoReceptor = usuarios.getTimeBorrado();

                //Toast.makeText(Activity_chats.this, "Tiempo configurado receptor: " + receptor + " " + tiempoConfiguradoReceptor, Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ERROR CARGANDO VALOR TIEMBORRADO", databaseError.getMessage());
            }
        });

        //COMO LA INSTANCIA databaseReferenceUsuarios SE CREA A NIVEL GENERAL SE DESTRUYE EN EL onDestroy()
      /*  if (database != null) database = null;
        if (db != null) db = null;*/


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


        Chats michat = new Chats(nick.trim(), Stringfechahora, mensaje.trim(), receptor, email_preferences);//Con formato fecha String
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

      /*  DatabaseReference database;
        DatabaseReference db;
        database = FirebaseDatabase.getInstance().getReference();
        db = database.getRoot().child(REFERENCIA_USUARIOS).child(receptor);*/

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
                        escribiendo = "";
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
     /*   DatabaseReference database;
        DatabaseReference db;
        database = FirebaseDatabase.getInstance().getReference();
        db = database.getRoot().child(REFERENCIA_USUARIOS).child(receptor).child("image");//TRAE SOLO LA IMAGEN*/

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
                //url= Uri.parse(dataSnapshot.getKey());
                //Toast.makeText(getApplicationContext(),"urlDescarga "+urlDescarga,Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(),"url "+url,Toast.LENGTH_SHORT).show();
                if (!urlDescarga.equals(IMAGE_DEFAULT)) {
                    Glide.with(getApplicationContext())
                            .load(Uri.parse(urlDescarga))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)

                            //Si hay transformación no se debe poner el placeholder pq lo carga antes de que se carga la imagen transformada...
                            //.override(60,60)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                            // .placeholder(ic_toolbar)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                            .error(ic_person)//Imagen de sustitución si se ha producido error de carga
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

            Chats michat = new Chats(nick.trim(), Stringfechahora, mensaje.trim(), receptor, email_preferences, fechaHora, true);
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

    //ENVIAR MENSAJE NORMAL
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnviar:
                mensaje = txtEscribir.getText().toString();

             /*   http://www.victorpascual.es/2012/09/26/crear-un-enlace-de-texto-en-android-usando-un-textview/
                String url="http://www.elmundo.es/";
                txtEscribir.setText(Html.fromHtml("Este texto lleva un " + url));
                txtEscribir.setMovementMethod(LinkMovementMethod.getInstance());*/

                if (!mensaje.isEmpty()) {

                    long fechaHora = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    String Stringfechahora = sdf.format(fechaHora);

                    //Recogemos el tocken
                    tokenEmisor = FirebaseInstanceId.getInstance().getToken();
                    Log.i(LOGTAG, "Token creado al enviar mensaje: " + "EMISOR: " + tokenEmisor + " RECEPTOR: " + tokenReceptor);

                    Chats michat = new Chats(nick.trim(), Stringfechahora, mensaje.trim(), receptor, email_preferences, fechaHora, false);//Con formato fecha String
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

            default:
                break;
        }
    }

    //DESPUÉS DE ENVIAR EL MENSAJE SE ENVÍA LA NOTIFIACIÓN...
    private void envioNotificacionVolley() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MisReferencias.PUSH_URL_POST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("!_@@_SUCESS", response + "");
                        //Toast.makeText(Activity_chats.this,"Se ha enviado notificación desde el servidor php",Toast.LENGTH_LONG).show();
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
        //AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj_actual);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    //RECOGEMOS LA URL DEL ICONO DEL EMISOR. Se utilizará para enviarlo como parámetro al servicio de notificaciones...
    private void traerIconoEmisor() {

      /*  DatabaseReference midatabase;
        DatabaseReference midb;
        midatabase = FirebaseDatabase.getInstance().getReference();
        midb = midatabase.getRoot().child(REFERENCIA_USUARIOS).child(nick).child("image");*/

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

        /*DatabaseReference databaseReferenceEstado = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db = databaseReferenceEstado.getRoot().child(REFERENCIA_USUARIOS).child(nick);*/

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

       /* DatabaseReference databaseReferenceEstado = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db = databaseReferenceEstado.getRoot().child(REFERENCIA_USUARIOS).child(usuario);*/

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
        //if(emisor_chateacon.equals(receptor_chateacon))escribiendo=true;
        // Toast.makeText(Activity_chats.this,"EMISOR CHATEA-ONRESUME: "+emisor_chateacon,Toast.LENGTH_SHORT).show();
        //Toast.makeText(this,"onResume Activity_Chats",Toast.LENGTH_SHORT).show();
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

    private void sendHostPushToken() {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, MisReferencias.PUSH_URL_POST, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("!_@@_SUCESS", response + "");
                Toast.makeText(Activity_chats.this, "Se ha enviado notificación desde el servidor php", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("!_@@_Errors--", error + "");
                Toast.makeText(Activity_chats.this, "ERROR ENVIADO NOTIFICACIÓN DESDE PHP", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(TOKEN, tokenReceptor);
                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);


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


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, MisReferencias.PUSH_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("!_@@_SUCESS", response + "");
                Toast.makeText(Activity_chats.this, "Se ha enviado notificación desde el servidor php", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("!_@@_Errors--", error + "");
                Toast.makeText(Activity_chats.this, "ERROR ENVIADO NOTIFICACIÓN DESDE PHP", Toast.LENGTH_LONG).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);


    }

    private void sendFCMPush() {//NO FUNCIONA
        String Legacy_SERVER_KEY = MisReferencias.SERVER_KEY;
        String msg = "this is test message,.,,.,.";
        String title = "my title";

        //String token =  tokenEnviandoMensajr;
        String token = "dDEDmJPZzxo:APA91bHxsrcCQdQ0OkOjD64Dp97EiqYP-KqWGA4Xj85_pBJU--piz6g5o3RZtqn2mHX9eALjbusOWNgn-r7qjOC56K_QfOrZ0M6ptlYtNr6xZcEHfl-zzeJYqyVikl20h3Y4gWG8RA5U";
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
