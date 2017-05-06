package com.example.antonio.chat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

public class Inicio extends AppCompatActivity {
//implements GoogleApiClient.OnConnectionFailedListener
    private static final String TAG = "Inicio";
    private SignInButton btnSignIn;
    private Button btnLoginInicio;
    private GoogleApiClient apiClient;
    private static final int RC_SIGN_IN = 1001;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        btnSignIn = (SignInButton)findViewById(R.id.sign_in_button);
        btnSignIn.setSize(SignInButton.SIZE_WIDE);
        btnSignIn.setColorScheme(SignInButton.COLOR_DARK);
        btnLoginInicio = (Button) findViewById(R.id.btnLoginInicio);

        mAuth = FirebaseAuth.getInstance();




        btnLoginInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Inicio.this,Activity_Login.class);
                startActivity(intent);
            }
        });


        //SE DEJA COMENTADA LA IMPLEMENTACIÓN PARA GOOGLE SIG-IN

         /*GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });



      mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    *//*Intent intent=new Intent(Inicio.this,Activity_usuarios.class);
                    startActivity(intent);*//*
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    *//*Toast.makeText(getApplicationContext(), "El usuario no ha podido entrar en la app", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onAuthStateChanged:signed_out");*//*
                }
                // ...
            }
        };*/




    }

/*    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            //Usuario logueado --> Mostramos sus datos
            GoogleSignInAccount acct = result.getSignInAccount();
            *//*Intent intent=new Intent(Inicio.this,Activity_usuarios.class);
            startActivity(intent);*//*
            Log.d(TAG, "onAuthStateChanged:signed_in:" );
        } else {
            //Usuario no logueado --> Lo mostramos como "Desconectado"
           *//* Toast.makeText(getApplicationContext(), "El usuario no ha podido entrar en la app", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onAuthStateChanged:signed_out");*//*
        }
}*/

    //SE DEJA COMENTADA LA IMPLEMENTACIÓN PARA GOOGLE SIG-IN

    /*@Override
    protected void onStart() {
        super.onStart();

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result =
                    Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            handleSignInResult(result);
        }
    }*/

    //SE DEJA PQ LA ACTIVITY IMPLEMENTA GoogleApiClient.OnConnectionFailedListener

}
