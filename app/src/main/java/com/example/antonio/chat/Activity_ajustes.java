package com.example.antonio.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

import modelos.Usuarios;
import referencias.MisReferencias;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import util.ConfigurarBorradoMensajes;
import util.Tratamiento_Imagenes;

import static com.theartofdev.edmodo.cropper.CropImageView.CropShape.OVAL;
import static referencias.MisReferencias.REFERENCIA_USUARIOS;
import static util.ConfigurarBorradoMensajes.configurarCombo;

/**
 * Created by Usuario on 21/03/2017.
 */

public class Activity_ajustes extends AppCompatActivity {

    Toolbar toolbar;
    Spinner spinner_tiempo;
    ImageView imgFoto;
    TextView tvNombre, tvNick;
    Button btnSetup;


    String nick_propio = "";
    String email_propio = "";

    //Subir imágenes al storage de Firebase
    private StorageReference mtorageReference;
    private Uri mimageUri = null;
    private Uri imageUri = null;
    private static int GALLERY = 1400;
    private ProgressDialog progressDialog;
    private String urlDescarga;
    private Bitmap mbitmap;
   // private String tiempo;
    private long timeBorrado=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);
        setTitle("Ajustes de tu perfil");
        damePreferencias();
        inicializarComponentes();


        imgFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ponerAvatar();
            }
        });

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modificarCuentaUsuario();
            }
        });


        spinner_tiempo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //ConfigurarBorradoMensajes.tiempoConfigurado = (String) parent.getItemAtPosition(position);
                 //Toast.makeText(Activity_ajustes.this, "El tiempo de borrado es: "+parent.getItemAtPosition(position), Toast.LENGTH_LONG).show();
                //Log.i("TIEMPO",ConfigurarBorradoMensajes.tiempoConfigurado);
                //timeBorrado=ConfigurarBorradoMensajes.configurarCombo(ConfigurarBorradoMensajes.tiempoConfigurado);
                String devuelvePosicion = (String) parent.getItemAtPosition(position);
                timeBorrado= configurarCombo(devuelvePosicion);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
/**/
    }

    @Override//Añadimos fuente
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void damePreferencias() {
        SharedPreferences prefs = getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);
        nick_propio = prefs.getString("nick", "Usuario");//Valor por defecto Usuario que se aplica si no encuentra nada
        email_propio = prefs.getString("email", "email@gmail.com");
        MisReferencias.USUARIO_CONECTADO = email_propio;//Por si entra con la sesión abierta...

    }

    private void inicializarComponentes() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setBackground(ContextCompat.getDrawable(this,R.drawable.degradado));
        imgFoto = (ImageView) findViewById(R.id.imgFoto);
        //imgFoto.setImageURI(imageUri);
        btnSetup = (Button) findViewById(R.id.btnSetup);
        tvNombre = (TextView) findViewById(R.id.tvNombre);
        tvNick = (TextView) findViewById(R.id.tvNick);
        tvNombre.setText(nick_propio);
        tvNick.setText(email_propio);
        spinner_tiempo= (Spinner) findViewById(R.id.spinnertiempo);
        //CREANDO EL ARRAY PARA EL ADAPTADOR DEL SPINNER DESDE LOS RECURSOS:
        ArrayAdapter adaptadorMeses = ArrayAdapter.createFromResource(this, R.array.tiempo_borrado, R.layout.support_simple_spinner_dropdown_item);
        spinner_tiempo.setAdapter(adaptadorMeses);
        mtorageReference = FirebaseStorage.getInstance().getReference();

        traerImagen();
        traerValorSpinner();

    }

    private void traerValorSpinner() {
        DatabaseReference database;
        DatabaseReference db;
        database = FirebaseDatabase.getInstance().getReference();
        db = database.getRoot().child(REFERENCIA_USUARIOS).child(nick_propio);
        //db.keepSynced(true);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Toast.makeText(Activity_ajustes.this, "NO hay datos...", Toast.LENGTH_SHORT).show();
                    return;
                }

                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);

                int tiempoConfigurado=usuarios.getTimeBorrado();
                int valorDevuelto=ConfigurarBorradoMensajes.configurarCombo(tiempoConfigurado);
                spinner_tiempo.setSelection(valorDevuelto);
                //Toast.makeText(Activity_ajustes.this, "valorDevuelto: "+valorDevuelto, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ERROR CARGANDO VALOR TIEMBORRADO", databaseError.getMessage());
            }
        });

        if (database != null) database = null;
        if (db != null) db = null;




    }

    private void traerImagenold() {

        StorageReference mfilepath = mtorageReference.child("Images").child(nick_propio).child("avatar");//No trae nada...


        try {
            final File localFile = File.createTempFile("images", "jpg");
            mfilepath.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    imgFoto.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException e) {
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activityajustes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_modificar_perfil) {
            modificarCuentaUsuario();
        }




        return super.onOptionsItemSelected(item);
    }


    private void traerImagen() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db = databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(nick_propio).child("image");
        //db.setValue(usuarios);


        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Usuarios  usuarios=dataSnapshot.getValue(Usuarios.class);
                urlDescarga = dataSnapshot.getValue().toString();

                //Si el usuario no ha puesto imagen para el perfil saldría la imagen por defecto...
                Glide.with(getApplicationContext())
                        //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                        //.load(Uri.parse(usuarios.getImage()))
                        .load(Uri.parse(urlDescarga))
                        //.placeholder(R.drawable.image_susti)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                        .error(R.drawable.ic_action)//Imagen de sustitución si se ha producido error de carga
                        //.override(600,400)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                        .centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                        //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                        .into(imgFoto);//dónde vamos a mostrar las imágenes


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(databaseReference!=null){

            databaseReference = null;
        }

        if(db != null){

            db = null;
        }


    }


    //1
    private void ponerAvatar() {

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, GALLERY);

    }

    //2
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("RESULT", "onActivityResult " + requestCode + resultCode + data);

        //Si no hacemos modificaciones en la imagen
        if (requestCode == GALLERY && resultCode == RESULT_OK) {
            imageUri = data.getData();

            //Activity perteneciente a la librería CropImage
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setCropShape(OVAL)
                    //.setAspectRatio()
                    //.cropInitialCropWindowPaddingRatio(0)//El rectángulo ocupa toda la imagen
                    // .setOutputCompressFormat()
                    //.setOutputCompressQuality(50)
                    //.setMaxCropResultSize(500,500)

                    .setActivityTitle("Imagen elegida")
                    .setBorderCornerColor(getResources().getColor(R.color.md_purple_500))
                    .start(this);

        }

        //Si hacemos modificaciones en la imagen
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri uri_bitMap = result.getUri();
                try {
                    //Convertimos la Uri en un bitmap para poder redimensionarlo y luego guardarlo en el storage de Firebase
                    mbitmap = Tratamiento_Imagenes.getThumbnail(uri_bitMap, this);



                    mimageUri = result.getUri();
                    //imgFoto.setImageURI(mimageUri);
                    imgFoto.setImageBitmap(mbitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    //3
    private void modificarCuentaUsuario() {
        //Se navega hasta el usuario logado y se modificar su campo image
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference db = databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(nick_propio);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Actualizando tu pefil espera por favor...");
        progressDialog.show();

        if (mbitmap != null) {

            //Renombrando el archivo:
          /*  StorageReference mfilepath = mtorageReference.child("Images").child(nick_propio).child("avatar").child("avatar.jpeg");
            File file = null;
            try {
                file = File.createTempFile("avatar", "jpeg");
                Toast.makeText(Activity_ajustes.this, "Imagen subida al servidor",
                        Toast.LENGTH_LONG).show();
            } catch( IOException e ) {

            }

            UploadTask uploadTask = mfilepath.putFile(Uri.fromFile(file));*/
            //1ª forma según el vídeo: Simple Blog App - Part 4

            //Redimensionamos el bitmap para que ocupe menos espacion antes de subirlo a Firebase
            //Bitmap bitmatReducido = Tratamiento_Imagenes.redimensionarImagenMaximo(mbitmap, 40, 40);



            final StorageReference mfilepath = mtorageReference.child("Images").child(nick_propio).child("avatar").child("miAvatar.jpeg");//Siempre se llamará igual...

            mfilepath.putFile(mimageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //Uri dowloadUri = taskSnapshot.getDownloadUrl();
                    String dowloadString = taskSnapshot.getDownloadUrl().toString();

                    //Se navega hasta el usuario logado y se modificar su campo image
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference db = databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(nick_propio);

                    db.child("image").setValue(dowloadString);


                  /* Toast.makeText(Activity_ajustes.this, "Imagen subida al servidor",
                            Toast.LENGTH_LONG).show();*/



/*                    Intent modificarCuenta = new Intent(Activity_ajustes.this, Activity_usuarios.class);
                    modificarCuenta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(modificarCuenta);*/

                }//Fin onSuccess

            }//Fin  mfilepath.putFile

            )
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Log.w(TAG, "uploadPhoto:onError", e);
                            progressDialog.dismiss();
                            Toast.makeText(Activity_ajustes.this, "Upload failed",
                                    Toast.LENGTH_LONG).show();
                        }
                    });


//2ª forma según archivos subidos a Github:  FirebaseUI-Android/app/src/main/java/com/firebase/uidemo/storage/ImageActivity.java
            //Hace un randon de la ruta donde se van a subir las imágenes...
            //String uuid = UUID.randomUUID().toString();

    /*        mtorageReference = FirebaseStorage.getInstance().getReference(uuid);
            mtorageReference.putFile(imageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //noinspection LogConditional
                            //Log.d(TAG, "uploadPhoto:onSuccess:" + taskSnapshot.getMetadata().getReference().getPath());
                            Toast.makeText(Activity_Login.this, "Image uploaded",
                                    Toast.LENGTH_SHORT).show();

                            //showDownloadUI();
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Log.w(TAG, "uploadPhoto:onError", e);
                            Toast.makeText(Activity_Login.this, "Upload failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });*/
        }else {//Modificamos otro campo
            db.child("timeBorrado").setValue(timeBorrado);
        }

        progressDialog.dismiss();

        Toast.makeText(this, R.string.modificacion_perfil, Toast.LENGTH_SHORT).show();


        Intent modificarCuenta = new Intent(Activity_ajustes.this, Activity_usuarios.class);
        modificarCuenta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(modificarCuenta);

        if (databaseReference != null) databaseReference = null;
        if (db != null) db = null;
    }
}
