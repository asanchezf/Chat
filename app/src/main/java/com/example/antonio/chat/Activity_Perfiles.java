package com.example.antonio.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import modelos.Chats;
import modelos.Usuarios;
import referencias.FirebaseHelper;
import util.Tratamiento_Imagenes;

import static referencias.MisReferencias.REFERENCIA_USUARIOS;


public class Activity_Perfiles extends AppCompatActivity {

    private ImageView imagenPerfil;
    private ImageButton btn_Llamar, btnCerrar;
    ;
    private String usuario_perfil;
    private String urlDescarga;
    private DatabaseReference databaseReference;
    private DatabaseReference db;
    private String key_Chat;
    private String key_Conversacion;
    private String emisor;
    private String receptor;
    private DatabaseReference databaseReferenceChats;//TRAEMOS DATOS DE LOS CHATS
    //private File imagenAdjuntada=null;
    private String rutaImagenStorage = "";
    private TextView textoImagen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle bundle = getIntent().getExtras();
        usuario_perfil = bundle.getString("parametro_db");
        //key_Chat=bundle.getString("parametro_chat");
        emisor = bundle.getString("parametro_emisor");
        receptor = bundle.getString("parametro_receptor");
        key_Conversacion = bundle.getString("parametro_conversacion");
        //imagenAdjuntada= (File) getIntent().getSerializableExtra("parametro_imagen");
        rutaImagenStorage = bundle.getString("parametro_imagen");
        inicializarComponentes();

       /* btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/


    }

    private void inicializarComponentes() {

        /*Es llamada desde AdaptadorChatsViewHolder_new.Dependiendo de si la imagen está ya descargada o no hará una cosa u otra...*/
        if (usuario_perfil != null && !usuario_perfil.equals("")) {
            setContentView(R.layout.activity_perfiles);
            imagenPerfil = (ImageView) findViewById(R.id.imgPerfiles);
            textoImagen = (TextView) findViewById(R.id.textofoto);
            traerImagenAvatar();

        }  else {
            traeDataBaseReference();
            traerImagenCompartida();
        }
    }


    private void traeDataBaseReference() {
        //Devuelve la ruta correcta al chat elegido. Reordena nick+receptor según corresponda. Devuelve el keychat que se utilizará en la query.
        FirebaseHelper firebaseauxiliar = FirebaseHelper.getInstance();
        databaseReferenceChats = firebaseauxiliar.getChatsReference(emisor, receptor);//databaseReferenceChats recoge los datos para el adaptador AdaptadorChatsViewHolder
    }


    private void traerImagenCompartida() {
        //final String[] miarchivoEnRuta = {""};

        //db = databaseReferenceChats.child(key_Conversacion).child("imagen");
        db = databaseReferenceChats.child(key_Conversacion);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //urlDescarga= dataSnapshot.getValue().toString;

                Chats chats = dataSnapshot.getValue(Chats.class);
                urlDescarga = chats.getImagen();
                final String rutaImagen = chats.getRutaImagen();
                //String miruta= Environment.getExternalStorageDirectory()+"/Chat/Fotos";
                Log.i("VALOR RUTAIMAGEN ", rutaImagen);
                //Toast.makeText(Activity_Perfiles.this, rutaImagen, Toast.LENGTH_SHORT).show();

                //File fichero = new File (rutaImagen);

                //File fileImage=new File(rutaImagen);
         /*       File fileImage = getApplicationContext().getFileStreamPath(rutaImagen);
                if(fileImage.exists()){


                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    //intent.setDataAndType(Uri.parse("file://" + ruta2), "image");
                    intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                    intent.setDataAndType(Uri.parse("file://" + rutaImagen), "image");
                    startActivity(intent);

                }*/


                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Your code goes here
                             /*String archivoEnRuta = Environment.getExternalStorageDirectory()+"/Chat/Fotos/miarchivo.jpg";
                            descargar(urlDescarga, archivoEnRuta);
                            miarchivoEnRuta[0] =archivoEnRuta;*/
                            //String rutaNueva=rutaImagen;
                            Tratamiento_Imagenes.descargarImagen(urlDescarga, rutaImagen);
                            //miarchivoEnRuta[0] =archivoEnRuta;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();


                //String imagen="/storage/emulated/0/Chat/Fotos/IMG-20151231-WA0004.jpg"; //Api23
                //String ruta2="/storage/emulated/0/Chat/Fotos/miarchivo.jpg";//ES LA RUTA CORRECTA DEL DISPOSITIVO. LA IMAGEN DEBE ESTAR DESCARGADA PRREVIAMENTE EN LA GALERÍA...


                //PARA ABRIR LA IMAGEN SI ESTÁ DESCARGADA EN LA GALERÍA. EN ANDROID N HAY QUE HACER CAMBIOS:
                //https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed&usg=ALkJrhhQhXdNXh3XREiEgvkith2kH-UVvw
               /* Intent intent = new Intent(Intent.ACTION_VIEW);
                //File f = new File(Environment.getExternalStorageDirectory(Chat/Fotos/miarchivo.jpg)+"/Chat/Fotos/miarchivo.jpg");
                //File f = new File("/Chat/Fotos/miarchivo.jpg");
                File f = new File(ruta2);
                intent.setDataAndType(Uri.parse("file://" + f.getAbsolutePath()), "image");
                startActivity(intent);
*/
             /*   Intent intent = new Intent(Intent.ACTION_PICK);
                Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Pictures/");
                //Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+"/Chat/Fotos/miarchivo.jpg" );
                intent.setDataAndType(uri, "image");
                startActivity(Intent.createChooser(intent, "Open folder"));*/

                //ABRIMOS LA GALERÍA PARA VER LA IMAGEN SELECCIONADA
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.parse("file://" + ruta2), "image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                intent.setDataAndType(Uri.parse("file://" + rutaImagen), "image/*");
                startActivity(intent);
                finish();//Para que se destruya y desde la galería se vuelva directamente a la activity_chats



              /*  Glide.with(getApplicationContext())
                        //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                        .load(miarchivoEnRuta[0])
                        //.load(Uri.parse(urlDescarga))

                        //.placeholder(R.drawable.loading)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                        .error(R.drawable.chat)//Imagen de sustitución si se ha producido error de carga
                        //.override(400,389)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                        //.centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                        //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                        .into(imagenPerfil);//dónde vamos a mostrar las imágenes*/

       /*         try {
                    Bitmap bitmap= Tratamiento_Imagenes.getThumbnail(Uri.parse(urlDescarga),Activity_Perfiles.this);
                    Bitmap bitmapResize=Tratamiento_Imagenes.redimensionarImagen(bitmap,420,400);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("ERROR TRANSFORMANDO BITMAP",e.getMessage());
                }*/

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void traerImagenAvatar() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //db = databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(usuario_perfil).child("image");
        db = databaseReference.getRoot().child(REFERENCIA_USUARIOS).child(usuario_perfil);


        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Usuarios  usuarios=dataSnapshot.getValue(Usuarios.class);
                //urlDescarga = dataSnapshot.getValue().toString();
                //url= Uri.parse(dataSnapshot.getKey());
                //Toast.makeText(getApplicationContext(),"urlDescarga "+urlDescarga,Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(),"url "+url,Toast.LENGTH_SHORT).show();

                String urlImagenAvatar=usuarios.getImage();
                String stextoImagen=usuarios.getNick();

                Glide.with(getApplicationContext())
                        //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                        //.load(Uri.parse(usuarios.getImage()))
                        .load(Uri.parse(urlImagenAvatar))
                        //.placeholder(R.drawable.loading)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                        .error(R.drawable.chat)//Imagen de sustitución si se ha producido error de carga
                        //.override(600,400)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                        .centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                        //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                        .crossFade(2000)
                        .into(imagenPerfil);//dónde vamos a mostrar las imágenes

                textoImagen.setText(stextoImagen);
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

        if (databaseReference != null) {
            databaseReference = null;
        }

        if (db != null) {
            db = null;
        }

    }
}

