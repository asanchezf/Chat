package com.example.antonio.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import adaptadores.AdaptadorUsuariosViewHolder;
import modelos.Usuarios;
import referencias.MisReferencias;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static referencias.MisReferencias.REFERENCIA_USUARIOS;

public class Activity_usuarios extends AppCompatActivity {

    RecyclerView lista;
    Toolbar toolbar;

    FirebaseRecyclerAdapter mAdapterUsuarios;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();//BB.DD.

    DatabaseReference ref;
    //Query query;

    String nick_propio = "";
    String email_propio = "";
    private static String NOHAYCHAT="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);
        setTitle("Usuarios para chatear");

        damePreferencias();
        ref = firebaseDatabase.getReference(REFERENCIA_USUARIOS);//Trae todos por orden alfabético por defecto
        //query = ref.orderByChild("nick");//Trae todos

        inicializarComponentes();



    }

    private void inicializarComponentes() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(email_propio);
        mAdapterUsuarios = new AdaptadorUsuariosViewHolder(R.layout.fila_recyclerview_usuarios, ref);

        lista = (RecyclerView) findViewById(R.id.listaUsuarios);
        //lista.setHasFixedSize(true);//Aumenta el rendimiento cuando el tamaño sea fijo
        lista.setLayoutManager(new LinearLayoutManager(this));



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

        lista.setAdapter(mAdapterUsuarios);
        mAdapterUsuarios.notifyDataSetChanged();
        //lista.scrollToPosition(mAdapterUsuarios.getItemCount() - 1);
    }
    private void damePreferencias(){
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

    Intent intent=new Intent(this,Activity_ajustes.class);
        startActivity(intent);
    }

    private void cerrarSesion() {

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Activity_Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        cambiaEstadoUsuario(nick_propio,false);
        finish();

        Toast.makeText(this,"La Sesión se ha cerrado",Toast.LENGTH_SHORT).show();

      /*  Snackbar snack = Snackbar.make(view, "Su terminal no tiene habilitada ninguna conexión wifi para poder acceder a este recurso.", Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snack.getView();
        group.setBackgroundColor(getResources().getColor(R.color.md_blue_600));
        snack.show();*/


    }

    private void cambiaEstadoUsuario(String usuario,boolean estado){

        /*DatabaseReference db=databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(usuario).child("online");
        db.setValue(Usuarios.ONLINE);*/
        //Se navega hasta el usuario logado y se modificar su campo online
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();

        DatabaseReference db=databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(usuario);
         Map<String, Object> update = new HashMap<>();
        if (!estado)
        update.put("online", Usuarios.OFFLINE);
        else
            update.put("online", Usuarios.ONLINE);

        db.updateChildren(update);




        //Toast.makeText(this,"CambiaEstado Activity_Usuarios",Toast.LENGTH_SHORT).show();
        Log.i("Activity_usuarios", "CambiaEstado Activity_Usuarios");

    }

    private void cambiaActivityUsuario(String usuario) {
        //FirebaseHelper firebaseauxiliar;
        //firebaseauxiliar = FirebaseHelper.getInstance();
        //firebaseauxiliar.changeUserConnectionStatus(Usuarios.ONLINE);
        //DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        DatabaseReference databaseReferenceEstado = FirebaseDatabase.getInstance().getReference();
        //DatabaseReference referenciaUsuario=null;
        DatabaseReference db = databaseReferenceEstado.getRoot().child(REFERENCIA_USUARIOS).child(usuario);

        Map<String, Object> update = new HashMap<>();

        update.put("chateaCon",NOHAYCHAT);

        db.updateChildren(update);
        //Toast.makeText(this,"cambiaestado - Activity_Chats",Toast.LENGTH_SHORT).show();
        Log.i("Activity_Chats", "cambiaestado - Activity_Chats");
    }
    @Override
    protected void onResume(){
        super.onResume();
        cambiaEstadoUsuario(nick_propio,true);
        cambiaActivityUsuario(nick_propio);
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
        cambiaEstadoUsuario(nick_propio,false);
        //Toast.makeText(this,"onDestroy Activity_Usuarios",Toast.LENGTH_SHORT).show();
        Log.i("Activity_usuarios", "onDestroy Activity_Usuarios");

       /* if (mAdapterUsuarios != null && mAdapterUsuarios instanceof FirebaseRecyclerAdapter) {
            ((FirebaseRecyclerAdapter) mAdapterUsuarios).cleanup();
        }*/


        if (mAdapterUsuarios != null ) {
            mAdapterUsuarios.cleanup();
        }

        if (firebaseDatabase != null ) {
            firebaseDatabase=null;
        }



    }


}
