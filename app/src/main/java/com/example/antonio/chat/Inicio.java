package com.example.antonio.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Locale;

import modelos.Usuarios;
import referencias.MisReferencias;

import static referencias.MisReferencias.REFERENCIA_USUARIOS;

public class Inicio extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "Inicio";
    private GoogleApiClient apiClient;
    private static final int RC_SIGN_IN = 1001;

    //03/01/2017==>SE DEJA COMENTADO. AHORA NO TENDRÁ PERSISTENCIA
    //private FirebaseDatabase fdb = DataBaseUtil.getDatabase();//PARA QUE NO DÉ ERROR AL INVOCAR FirebaseDatabase utilizando la persistencia...

    private FirebaseAuth mAuth;
    private String email;
    private String nick;
    //private static String  IMAGE_DEFAULT="default_image";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        //20/12/2017:SE DEFINE COMO MIEMBRO DE LA CLASE Y CON PATRÓN SINGLETON (DataBaseUtil.getDatabase())...lINEA 49
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //FirebaseHelper.getDatabasePersistence();

        init();

/*
        ConnectivityManager manager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network=manager.getActiveNetworkInfo();
        if(network!=null && network.isConnected()){
            hayConexion();
        }else{
            System.exit(0);
            //finish();

            Toast.makeText(this, "No hay conexion", Toast.LENGTH_SHORT).show();
            return;
        }
*/



    /*    if(compruebaConexion(this)){

            hayConexion();

        }else{
            System.exit(0);
            Toast.makeText(this, "No hay conexion", Toast.LENGTH_SHORT).show();
        }*/



    }

    @Override
    protected void onStart() {
        super.onStart();

        ConnectivityManager manager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network=manager.getActiveNetworkInfo();
        if(network!=null && network.isConnected()) {

            //SI EL USUARIO ESTÁ PREVIAMENTE IDENTIFICADO EN GOOGLE ACCEDE DIRECTAMENTE A LA ACTIVITY USUARIOS...
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(apiClient);
            if (opr.isDone()) {
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                //showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        //hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            //SI EL USUARIO ESTÁ PREVIAMENTE IDENTIFICADO EN FIREBASE ACCEDE DIRECTAMENTE A SU CUENTA
            if (currentUser != null) {
                Intent intent = new Intent(Inicio.this, Activity_usuarios.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

            }


        }else{

            //System.exit(0);
            //Toast.makeText(this, "No hay conexion", Toast.LENGTH_SHORT).show();
            finish();


            //return;

        }
    }


    public static boolean compruebaConexion(Context context)
    {
        boolean connected = false;
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Recupera todas las redes (tanto móviles como wifi)
        NetworkInfo[] redes = connec.getAllNetworkInfo();

        for (int i = 0; i < redes.length; i++) {
            // Si alguna red tiene conexión, se devuelve true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
        }
        return connected;
    }

    private void init(){

        SignInButton btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        //btnSignIn.setEnabled(false);
        btnSignIn.setSize(SignInButton.SIZE_WIDE);
        btnSignIn.setColorScheme(SignInButton.COLOR_DARK);
        Button btnLoginInicio = (Button) findViewById(R.id.btnLoginInicio);

        mAuth = FirebaseAuth.getInstance();



        //1-ACCESO A LA APP CON USUARIO Y CONTRASEÑA
        btnLoginInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Inicio.this,Activity_Login.class);
                startActivity(intent);
            }
        });


        //2-ACCESO A LA APP CON GOOGLE SIG-IN
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });


        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(Inicio.this, Activity_usuarios.class);
                    startActivity(intent);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Toast.makeText(getApplicationContext(), "El usuario no ha podido entrar en la app", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            //Usuario logueado --> Mostramos sus datos



            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);

            //nick=account.getFamilyName();//Primer apellido
            nick=account.getGivenName();//Nombre
            //nick = account.getDisplayName();//Nombre y apellidos

            email = account.getEmail();

            String tokenNotify = FirebaseInstanceId.getInstance().getToken();
            String photo= String.valueOf(account.getPhotoUrl());

            long fechaHora = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            String Stringfechahora = sdf.format(fechaHora);

            String CONQUIENCHATEA = "";
            //boolean ESCRIBE = false;
            Usuarios usuario = new Usuarios(nick.trim(), nick.trim(), Stringfechahora, email.trim(), true, tokenNotify,photo, CONQUIENCHATEA, false,60000);

            generarPreferencias();
            crearUsuarioEnBBDD(usuario);
            //Se ejecuta automáticamente el intent y nos manda a ActivityUsuarios porque se llama a miListenrSesion = new FirebaseAuth.AuthStateListener()==>Línea 276
            Log.i("SESION", "Usuario creado correctamente ");
            //Toast.makeText(Inicio.this, R.string.crearUsuario, Toast.LENGTH_SHORT).show();

            MisReferencias.USUARIO_CONECTADO = email.trim();

            Intent intent=new Intent(Inicio.this,Activity_usuarios.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            Log.d(TAG, "onAuthStateChanged:signed_in:" );
        } else {
            //Usuario no logueado --> Lo mostramos como "Desconectado"
            //Toast.makeText(getApplicationContext(), "El usuario no ha podido entrar en la ap", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
}


    private void generarPreferencias() {
        //1.Guardamos el nick y el email del usuario en preferencias.
        SharedPreferences prefs = getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);//Fichero,modo privado
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nick", nick.trim());
        editor.putString("email", email.trim());
        //editor.putString("password",pass.trim());
        editor.apply();
        //String nickPreferences = nick;
        //editor.commit();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Inicio.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //EL USUARIO YA ESTÁ AUTENTICADO CON GOOGLE
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result =
                    Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            handleSignInResult(result);
        }
    }







    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "La conexión está fallando....", Toast.LENGTH_SHORT).show();
    }

    private void crearUsuarioEnBBDD(Usuarios usuario) {

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(REFERENCIA_USUARIOS);
        dbRef.child(nick).setValue(usuario);


    }

}
