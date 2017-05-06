package com.example.antonio.chat;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static referencias.MisReferencias.REFERENCIA_USUARIOS;

/**
 * Created by Usuario on 24/03/2017.
 */

public class Activity_Perfiles extends AppCompatActivity {

    private ImageView imagenPerfil;
    private ImageButton btn_Llamar,btnCerrar;;
    private String usuario_perfil;


    private String urlDescarga;
    private DatabaseReference databaseReference;
    private DatabaseReference db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfiles);

        Bundle bundle = getIntent().getExtras();
        usuario_perfil = bundle.getString("parametro_db");

        inicializarComponentes();

        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void inicializarComponentes() {
        imagenPerfil=(ImageView)findViewById(R.id.imgPerfiles);
        //btn_Llamar=(ImageButton) findViewById(R.id.btnLlamar);
        btnCerrar=(ImageButton) findViewById(R.id.btnCerrar);
        traerImagen();

    }



    private void traerImagen() {
         databaseReference = FirebaseDatabase.getInstance().getReference();
         db = databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(usuario_perfil).child("image");
        //db.setValue(usuarios);


        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

              //Usuarios  usuarios=dataSnapshot.getValue(Usuarios.class);
                urlDescarga= dataSnapshot.getValue().toString();
                //url= Uri.parse(dataSnapshot.getKey());
                //Toast.makeText(getApplicationContext(),"urlDescarga "+urlDescarga,Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(),"url "+url,Toast.LENGTH_SHORT).show();

                Glide.with(getApplicationContext())
                        //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                        //.load(Uri.parse(usuarios.getImage()))
                        .load(Uri.parse(urlDescarga))
                       //.placeholder(R.drawable.image_susti)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                        .error(R.drawable.chat)//Imagen de sustitución si se ha producido error de carga
                        //.override(600,400)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                        .centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                        //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                        .into(imagenPerfil);//dónde vamos a mostrar las imágenes







            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




       /* try {
            final File localFile = File.createTempFile("images", "jpg");
            mfilepath.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    imagenPerfil.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException e ) {}*/

        // Download directly from StorageReference using Glide
       /* Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(mfilepath)
                .centerCrop()
                .crossFade()
                .error(R.drawable.image5)
                .into(imgFoto);*/


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(databaseReference!=null) {
            databaseReference=null;
        }

        if(db!=null) {
            db=null;
        }


    }
}

