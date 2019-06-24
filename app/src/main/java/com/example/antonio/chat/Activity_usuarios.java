package com.example.antonio.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import adaptadores.AdaptadorUsuariosViewHolder;
import modelos.Usuarios;
import referencias.FirebaseHelper;
import referencias.MisReferencias;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static referencias.MisReferencias.REFERENCIA_USUARIOS;

public class Activity_usuarios extends AppCompatActivity {

    private static final String LOGTAG = "android-fcm";
    private FirebaseRecyclerAdapter mAdapterUsuarios;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();//BB.DD.
    private DatabaseReference refUsuarios;
    //private Query mQuery;
    private String nick_propio = "";
    private String email_propio = "";
    private String urlCompartido = "";
    private RecyclerView lista;
    private int tiempoConfiguradoEmisor = 0;
    //private Uri mimageUri = null;
    private Uri imageUri = null;
    //private  Usuarios usuarios;
    private DatabaseReference databaseReferenceEstado;


    private void cambiaEstadoUsuario(String usuario, boolean estado) {

        //Se navega hasta el usuario logado y se modificar su campo online
      /*  DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db = databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(usuario);*/

        //13 de noviembre 2017
        DatabaseReference databaseReference;
        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReference = firebaseauxiliar.getDatosEmisor(usuario);

        Map<String, Object> update = new HashMap<>();
        if (!estado)
            update.put("online", Usuarios.OFFLINE);
        else
            update.put("online", Usuarios.ONLINE);

        databaseReference.updateChildren(update);

        //Toast.makeText(this,"CambiaEstado Activity_Usuarios",Toast.LENGTH_SHORT).show();
        Log.i("Activity_usuarios", "CambiaEstado Activity_Usuarios");

    }

    private void cambiaActivityUsuario(String usuario) {

        DatabaseReference databaseReferenceEstado = FirebaseDatabase.getInstance().getReference();
        //DatabaseReference referenciaUsuario=null;
        DatabaseReference db = databaseReferenceEstado.getRoot().child(REFERENCIA_USUARIOS).child(usuario);

        Map<String, Object> update = new HashMap<>();

        String NOHAYCHAT = "";
        update.put("chateaCon", NOHAYCHAT);

        db.updateChildren(update);
        //Toast.makeText(this,"cambiaestado - Activity_Chats",Toast.LENGTH_SHORT).show();
        Log.i("Activity_Chats", "cambiaestado - Activity_Chats");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);
        setTitle(R.string.app_name);


            damePreferencias();
            refUsuarios = firebaseDatabase.getReference(REFERENCIA_USUARIOS);//Trae todos por orden alfabético por defecto
            //refUsuarios = firebaseDatabase.setPersistenceEnabled(true);


            //traEstadoTodos();Está en pruebas..


            // Get intent, action and MIME type
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            //RECIBE IMAGEN
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    //imageUri=intent.getData();
                    imageUri = (Uri) intent
                            .getParcelableExtra(Intent.EXTRA_STREAM);

                    //Toast.makeText(Activity_usuarios.this, "imageUri " + imageUri, Toast.LENGTH_LONG).show(); // Handle single image being sent

                }
                if (type.startsWith("text/html")) {//RECIBE HTML
                    urlCompartido = intent.getStringExtra(Intent.EXTRA_TEXT);
                }
                if (type.startsWith("text/plain")) {//RECIBE TEXTO PLANO
                    urlCompartido = intent.getStringExtra(Intent.EXTRA_TEXT);
                }
            }

            inicializarComponentes();



    }

    private void comprobarConexion() {

        ConnectivityManager manager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network=manager.getActiveNetworkInfo();
        //if( !( (network==null && !network.isConnected()))){
            if(network!=null && network.isConnected()){
            //traerDatos();
            Toast.makeText(this, "En estos momentos no tienes conexión. Inténtalo más tarde", Toast.LENGTH_SHORT).show();
            return;

        }/*else{

            *//*imgNoInternet.setVisibility(View.VISIBLE);
            txtNoInternet.setVisibility(View.VISIBLE);
            lista.setVisibility(View.INVISIBLE);*//*
        }
*/

    }

    private void traEstadoTodos() {

        //final DatabaseReference[] databaseReferenceEstado = new DatabaseReference[1];
        refUsuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    final Usuarios usuarios = snapshot.getValue(Usuarios.class);
                    String usuario=usuarios.getNick();
                    databaseReferenceEstado =firebaseDatabase.getReference(REFERENCIA_USUARIOS).child(usuario);
                    databaseReferenceEstado.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });





                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void inicializarComponentes() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(email_propio);
        //toolbar.setBackground(ContextCompat.getDrawable(this,R.drawable.degradado));

        /*SI LA LLAMADA SE HACE DESDE ACTIVITYMAIN INICIALIZAMOS ESTE ADAPTADOR PARA EL RECYCLERVIEW*/

        //configuraAdaptador();


        //Ejemplo Adaptador de Firebase específico para el RecyclerView
       /*mAdapterUsuarios = new FirebaseRecyclerAdapter<Usuarios, Items_ListaUsuarios>(
                Usuarios.class, R.layout.fila_recyclerview_usuarios, Items_ListaUsuarios.class, ref) {

            @Override
            protected void populateViewHolder(Items_ListaUsuarios viewHolder, Usuarios model, int position) {
                viewHolder.setFecha(model.getFecha());
                viewHolder.setUsuario(model.getNombre());
                viewHolder.setNick(model.getNick());
            }
        };*/


        traeTimeBorradoEmisor(nick_propio);

     /*   if (urlCompartido != null) {
            avisoCompartirUrl();
        }*/

    }

    private void configuraAdaptador() {

        if (urlCompartido != null && !urlCompartido.equals("")) {/*SI LA LLAMADA SE HACE DESDE UN NAVEGADOR WEB INICIALIZAMOS ESTE ADAPTADOR PARA EL RECYCLERVIEW PASÁNDOLE LA URL DE LA WEB QUE QUEREMOS COMPARTIR...*/
            mAdapterUsuarios = new AdaptadorUsuariosViewHolder(R.layout.fila_recyclerview_usuarios, refUsuarios, urlCompartido, tiempoConfiguradoEmisor);
            Log.i(LOGTAG, "Paso miTiempoBorradoEmisor.Activity_Usuarios" + tiempoConfiguradoEmisor);

        } else if (imageUri != null && !imageUri.equals("")) {//SI LA LLAMADA SE HACE AL COMPARTIR UNA IMAGEN SE LA PASAMOS EN EL ADAPTADOR:imageUri
            mAdapterUsuarios = new AdaptadorUsuariosViewHolder(R.layout.fila_recyclerview_usuarios, refUsuarios, imageUri, tiempoConfiguradoEmisor);
            Log.i(LOGTAG, "Paso miTiempoBorradoEmisor.Activity_Usuarios" + tiempoConfiguradoEmisor);

        } else {/*LLAMADA NORMAL SIN IMAGEN PARA COMPARTIR Y SIN URL PARA COMPARTIR*/
            mAdapterUsuarios = new AdaptadorUsuariosViewHolder(R.layout.fila_recyclerview_usuarios, refUsuarios, tiempoConfiguradoEmisor);
            Log.i(LOGTAG, "Paso miTiempoBorradoEmisor.Activity_Usuarios " + tiempoConfiguradoEmisor);
        }


        lista = (RecyclerView) findViewById(R.id.listaUsuarios);
        //lista.setHasFixedSize(true);//Aumenta el rendimiento cuando el tamaño sea fijo
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setHasFixedSize(true);//El tamaño va a cambiar pocas veces...
        lista.setAdapter(mAdapterUsuarios);
        mAdapterUsuarios.notifyDataSetChanged();
        lista.scrollToPosition(0);//Le situamos en la primera posición
    }

    private void traeTimeBorradoEmisor(final String nick_propio) {

        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        DatabaseReference refUsuarioConectado = firebaseauxiliar.getDatosReceptor(nick_propio);

        refUsuarioConectado.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Toast.makeText(Activity_usuarios.this, "No hay datos...", Toast.LENGTH_SHORT).show();
                    return;
                }

                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);
                tiempoConfiguradoEmisor = usuarios.getTimeBorrado();


                configuraAdaptador();

                if (urlCompartido != null && !urlCompartido.equals("")) {
                    String url = "url";
                    avisoCompartir(url);
                }

                if (imageUri != null && !imageUri.equals("")) {
                    String imagen = "imagen";
                    avisoCompartir(imagen);
                }


                //Toast.makeText(Activity_usuarios.this, "Tiempo configurado emisor: " + nick_propio + " " + tiempoConfiguradoEmisor, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ERROR CARGANDO VALOR TIEMBORRADO", databaseError.getMessage());
            }
        });
    }

    private void avisoCompartir(String mensaje) {
        //Se carga desde traeTimeBorradoEmisor porque  el context que necesita la Snackbar se crea después del resultado
        //del onDataChange del addListenerForSingleValueEvent de Firebase
        int texto;
        if (mensaje.equals("url")) {/*SI LA LLAMADA SE HACE DESDE UN NAVEGADOR WEB */
            texto = R.string.avisoelegirusuariocompartir;
        } else {//SI LA LLAMADA SE HACE AL COMPARTIR UNA IMAGEN
            texto = R.string.avisoelegirusuariocompartirimagen;
        }

        Snackbar snack = Snackbar.make(lista, texto,
                Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snack.getView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            group.setBackground(ContextCompat.getDrawable(this, R.drawable.degradado));

        } else {
            group.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        snack.setDuration(6000);
        snack.show();
    }

    private void damePreferencias() {
        SharedPreferences prefs = getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);
        nick_propio = prefs.getString("nick", "Usuario");//Valor por defecto Usuario que se aplica si no encuentra nada
        email_propio = prefs.getString("email", "email@gmail.com");
        MisReferencias.USUARIO_CONECTADO = email_propio;//Por si entra con la sesión abierta...

    }

    @Override//Añadimos fuente
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activityusuarios, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            cerrarSesion();
        }

        if (id == R.id.action_ajustes) {
            ajustes();
        }

        return super.onOptionsItemSelected(item);
    }


    private void ajustes() {

        Intent intent = new Intent(this, Activity_ajustes.class);
        startActivity(intent);
    }

    private void cerrarSesion() {

        FirebaseAuth.getInstance().signOut();


     /*   mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });*/



        Intent intent = new Intent(this, Activity_Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        cambiaEstadoUsuario(nick_propio, false);
        finish();

        Toast.makeText(this, "La Sesión se ha cerrado", Toast.LENGTH_SHORT).show();

      /*  Snackbar snack = Snackbar.make(view, "Su terminal no tiene habilitada ninguna conexión wifi para poder acceder a este recurso.", Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snack.getView();
        group.setBackgroundColor(getResources().getColor(R.color.md_blue_600));
        snack.show();*/


    }

    @Override
    protected void onResume() {
        super.onResume();
        cambiaEstadoUsuario(nick_propio, true);
        cambiaActivityUsuario(nick_propio);
        //traEstadoTodos();Está en pruebas
        //mAdapterUsuarios.notifyDataSetChanged();
        //Toast.makeText(this,"onResume Activity_Usuarios",Toast.LENGTH_SHORT).show();
        Log.i("Activity_usuarios", "onResume Activity_Usuarios");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //finish();
        //cambiaEstadoUsuario(nick_propio,false);
        //Toast.makeText(this,"onStop Activity_Usuarios",Toast.LENGTH_SHORT).show();
        Log.i("Activity_usuarios", "onStop Activity_Usuarios");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //finish();
        cambiaEstadoUsuario(nick_propio, false);
        //Toast.makeText(this,"onDestroy Activity_Usuarios",Toast.LENGTH_SHORT).show();
        Log.i("Activity_usuarios", "onDestroy Activity_Usuarios");

       /* if (mAdapterUsuarios != null && mAdapterUsuarios instanceof FirebaseRecyclerAdapter) {
            ((FirebaseRecyclerAdapter) mAdapterUsuarios).cleanup();
        }*/


        if (mAdapterUsuarios != null) {
            mAdapterUsuarios.cleanup();
        }

        if (firebaseDatabase != null) {
            firebaseDatabase = null;
        }

    }


}
