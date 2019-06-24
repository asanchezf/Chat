package com.example.antonio.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import modelos.Usuarios;
import referencias.MisReferencias;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static referencias.MisReferencias.REFERENCIA_USUARIOS;


public class Activity_Login extends AppCompatActivity implements View.OnClickListener {

    private EditText txtEmail, txtPass, txtNick;
    private Button btnLogin, btnRegistrar;
    private ProgressBar progressBar;
    private String email;
    private String pass;
    private String nick;
    private String nickPreferences;
    private String emailPreferences;
    private String passPreferences;

    //Listener para saber si hemos iniciado sesión
    private FirebaseAuth.AuthStateListener miListenrSesion;
    private FirebaseUser user;

    private DatabaseReference dbRef;
    //Para las notificaciones desde Servidor
    private String tokenNotify;
    private static String  IMAGE_DEFAULT="default_image";
    private String CONQUIENCHATEA="";
    private boolean ESCRIBE=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //HABILITAMOS LA PERSISTENCIA EN LA APP
        //FirebaseHelper.getDatabasePersistence();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);



            inicializarComponentes();
            damePreferencias();

            //Se ejecuta cuando cambiamos de sesion. Al iniciar o al finalizar
            miListenrSesion = new FirebaseAuth.AuthStateListener() {

                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                    //FirebaseUser user = firebaseAuth.getCurrentUser();
                    user = firebaseAuth.getCurrentUser();

                    if (user != null) {
                        //Tenemos sesión
                        Log.i("SESIOM", "Sesión iniciada con el usuario " + user.getEmail());

                        Intent usuarios_tengo_sesion = new Intent(Activity_Login.this, Activity_usuarios.class);

                        usuarios_tengo_sesion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        //usuarios_tengo_sesion.putExtra("parametro_nick_propio",nick);
                        usuarios_tengo_sesion.putExtra("parametro_nick_propio", nickPreferences);
                        usuarios_tengo_sesion.putExtra("parametro_email_propio", emailPreferences);
                        startActivity(usuarios_tengo_sesion);
                        //cambiaEstadoUsuario(nickPreferences);

                    }

                    else {

                        Log.i("SESIOM", "Se ha cerrado la sesión del usuario ");
                    }

                }
            };



    }//Fin onCreate

    private void inicializarComponentes() {
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPass = (EditText) findViewById(R.id.txtPassword);
        txtNick = (EditText) findViewById(R.id.txtNick);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.barraProgreso);
        //mtorageReference= FirebaseStorage.getInstance().getReference();
       // DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(REFERENCIA_USUARIOS);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnRegistrar:

                showProgress();
                email = txtEmail.getText().toString().trim();
                pass = txtPass.getText().toString().trim();
                nick = txtNick.getText().toString().trim();
                tokenNotify=FirebaseInstanceId.getInstance().getToken();

                if (!nickPreferences.equals("DefaultUser")) {
                    Toast.makeText(btnLogin.getContext(), "La aplicación solo puede ser utilizada con el usuario: " + nickPreferences, Toast.LENGTH_LONG).show();
                    hideProgress();
                    return;
                }


                if (!nick.isEmpty() && !email.isEmpty() && !pass.isEmpty()) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                //String idUsuario=user.getUid();

                                long fechaHora = System.currentTimeMillis();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                                String Stringfechahora = sdf.format(fechaHora);
                                //19 de abril:se introduce el campo ESCRIBE...
                                //14 de Octubre se introduce el campo timeBorrado
                                Usuarios usuario = new Usuarios(nick.trim(), nick.trim(), Stringfechahora, email.trim(), true, tokenNotify,IMAGE_DEFAULT,CONQUIENCHATEA,ESCRIBE,60000);

                                generarPreferencias();
                                crearUsuarioEnBBDD(usuario);
                                //Se ejecuta automáticamente el intent y nos manda a ActivityUsuarios porque se llama a miListenrSesion = new FirebaseAuth.AuthStateListener()==>Línea 276
                                Log.i("SESIOM", "Usuario creado correctamente ");
                                Toast.makeText(btnRegistrar.getContext(), R.string.crearUsuario, Toast.LENGTH_SHORT).show();

                                MisReferencias.USUARIO_CONECTADO = email.trim();
                                hideProgress();
                                finish();

                            } else {

                                Log.e("SESIOM", task.getException().getMessage() + "Error creando usuario");
                                Toast.makeText(btnLogin.getContext(), getString(R.string.errorCreandoUsuario) + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                hideProgress();
                            }

                        }
                    });
                }//Fin if(nick.isEmpty())

                else {
                    Toast.makeText(getBaseContext(), R.string.avisoCreandoUsuario, Toast.LENGTH_LONG).show();
                    hideProgress();
                }

                break;

            case R.id.btnLogin:
                showProgress();
                if (nickPreferences.equals("DefaultUser")) {
                    Toast.makeText(btnLogin.getContext(), R.string.usuarioNoPermisos, Toast.LENGTH_LONG).show();
                    hideProgress();
                    return;
                }

                email = txtEmail.getText().toString().trim();
                pass = txtPass.getText().toString().trim();


                if (!email.isEmpty() && !pass.isEmpty()) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Log.i("SESIOM", "Sesión iniciada correctamente ");
                                Toast.makeText(btnLogin.getContext(), R.string.inicioSesion, Toast.LENGTH_SHORT).show();

                                //Se ejecuta automáticamente el intent y nos manda a ActivityUsuarios porque se llama a miListenrSesion==>onCreate Línea 92
                                MisReferencias.USUARIO_CONECTADO = email.trim();

                                //finish();
                                hideProgress();

                            } else {

                                Log.e("SESION", task.getException().getMessage() + "Error iniciado sesión");
                                Toast.makeText(btnLogin.getContext(), getString(R.string.errorIniciandoSesion) + "\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                hideProgress();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getBaseContext(), R.string.avisoIniciandoSesion, Toast.LENGTH_LONG).show();
                }

                hideProgress();

                break;

            default:
                break;

        }
    }

    @Override//Añadimos nueva fuente
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void damePreferencias() {
        SharedPreferences prefs = getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);
        nickPreferences = prefs.getString("nick", "DefaultUser");
        emailPreferences = prefs.getString("email", "DefaultEmail");
        passPreferences = prefs.getString("password", "DefaultPass");
    }

    private void dameSesion() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailPreferences, passPreferences).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Log.i("SESIOM", "Sesión iniciada correctamente ");
                    Toast.makeText(btnLogin.getContext(), "Se ha iniciado de nuevo la sesión", Toast.LENGTH_SHORT).show();
                    Intent usuarios_desde_login = new Intent(Activity_Login.this, Activity_usuarios.class);
                    //Para que no quede en el stack de activitys
                    /*usuarios_desde_login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);*/

                    startActivity(usuarios_desde_login);
                    usuarios_desde_login.putExtra("parametro_nick_propio", nickPreferences);
                    usuarios_desde_login.putExtra("parametro_email_propio", emailPreferences);
                    startActivity(usuarios_desde_login);
                    //MisReferencias.USUARIO_CONECTADO=email.trim();
                    //finish();
                } else {

                    Log.e("SESIOM", task.getException().getMessage() + "Error iniciado sesión");
                    Toast.makeText(btnLogin.getContext(), "INFORMACIÓN PARA EL USUARIO: " + "\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


    }


    private void cambiaEstadoUsuario(String usuario) {
        //FirebaseHelper firebaseauxiliar;
        //firebaseauxiliar = FirebaseHelper.getInstance();
        //firebaseauxiliar.changeUserConnectionStatus(Usuarios.ONLINE);
        //DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        //String usuario="";
        if (nickPreferences.equals(""))//En el alta de registro nickPreference es nulo todavía...
            nickPreferences = nick;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        //DatabaseReference referenciaUsuario=null;
        DatabaseReference db = databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(nickPreferences);

        Map<String, Object> update = new HashMap<>();
        update.put("online", Usuarios.ONLINE);
        db.updateChildren(update);

    }//Los estados se controlan a partir de la activity_Usuarios

    private void crearUsuarioEnBBDD(Usuarios usuario) {

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        /*DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        db.child(nick.trim()).setValue(usuario);*///Crea un Defaultuser


        /*DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
        DatabaseReference db=databaseReference.getRoot().child(REFERENCIA_USUARIOS);
        db.child(nick).setValue(usuario);*/

         dbRef = FirebaseDatabase.getInstance().getReference().child(REFERENCIA_USUARIOS);
        dbRef.child(nick).setValue(usuario);


    }

    private void generarPreferencias() {
        //1.Guardamos el nick y el email del usuario en preferencias.
        SharedPreferences prefs = getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);//Fichero,modo privado
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nick", nick.trim());
        editor.putString("email", email.trim());
        //editor.putString("password",pass.trim());
        editor.apply();
        nickPreferences = nick;
        //editor.commit();
    }

    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);

    }

    public void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //INicioamos el listener
        FirebaseAuth.getInstance().addAuthStateListener(miListenrSesion);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //damePreferencias();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //
        if (miListenrSesion != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(miListenrSesion);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (miListenrSesion != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(miListenrSesion);
        }

        if (dbRef != null) {
            dbRef=null;
        }


    }


}
