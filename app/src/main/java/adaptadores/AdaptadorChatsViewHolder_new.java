package adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.antonio.chat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import modelos.Chats;
import referencias.MisReferencias;
import util.Tratamiento_Imagenes;

import static com.example.antonio.chat.R.drawable.ic_person;
import static referencias.MisReferencias.REFERENCIA_USUARIOS;


public class AdaptadorChatsViewHolder_new extends FirebaseRecyclerAdapter<Chats, AdaptadorChatsViewHolder_new.ChatsViewHolder> {

    //CUANDO Activity_chats se abre a partir de una notificación USUARIO_CONECTADO es nulo
    private String emisor = MisReferencias.USUARIO_CONECTADO;
    private String imagenReceptor;
    private String receptor;
    private String urlDescarga;
    private static final String IMAGE_DEFAULT = "default_image";
    private Context context;
    private String email_preferences = "";
    private Toolbar mToolbar, mToolbar2;
    private int timeBorrado = 0;
    private int mitiempoConfiguradoEmisor = 0;
    private int mitiempoConfiguradoReceptor = 0;
    private String nombreEmisor;
    private String nombreReceptor;
    private File miImagen = null;

    //private String rutaImagenStorage = "";
    //private String rutaImagenFirebase = "";
    //private Query databaseReferenceChats;
    //private Chats chatMensaje;
    //private DatabaseReference db;


    public AdaptadorChatsViewHolder_new(int modelLayout, Query ref, Toolbar toolbar, Toolbar toolbar2, int tiempoConfiguradoEmisor, int tiempoConfiguradoReceptor) {
        super(Chats.class, modelLayout, ChatsViewHolder.class, ref);
        mToolbar = toolbar;
        mToolbar2 = toolbar2;
        mitiempoConfiguradoEmisor = tiempoConfiguradoEmisor;
        mitiempoConfiguradoReceptor = tiempoConfiguradoReceptor;

    }


    @Override
    public ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return super.onCreateViewHolder(parent, viewType);
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(mModelLayout, parent, false);

        //CUANDO Activity_chats se abre a partir de una notificación USUARIO_CONECTADO es nulo. Se traen los datos de las preferencias del usuario....
        SharedPreferences prefs = parent.getContext().getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);
        //String nick = prefs.getString("nick", "Usuario");
        email_preferences = prefs.getString("email", "emailpordefecto@gmail.com");

        return new ChatsViewHolder(view);
    }

    private String dameFechaHoraActual() {
        long fechaHora = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String fechahoraActual = sdf.format(fechaHora);
        return fechahoraActual;
    }

    private String diferenciaFechas(String inicio, String llegada) {

        Date fechaInicio = null;
        Date fechaLlegada = null;

        // configuramos el formato en el que esta guardada la fecha en
        //  los strings que nos pasan
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        try {
            // aca realizamos el parse, para obtener objetos de tipo Date de
            // las Strings
            fechaInicio = formato.parse(inicio);
            fechaLlegada = formato.parse(llegada);

        } catch (ParseException e) {
            // Log.e(TAG, "Funcion diferenciaFechas: Error Parse " + e);
        } catch (Exception e) {
            // Log.e(TAG, "Funcion diferenciaFechas: Error " + e);
        }

        // tomamos la instancia del tipo de calendario
        Calendar calendarInicio = Calendar.getInstance();
        Calendar calendarFinal = Calendar.getInstance();

        // Configramos la fecha del calendario, tomando los valores del date que
        // generamos en el parse
        calendarInicio.setTime(fechaInicio);
        calendarFinal.setTime(fechaLlegada);

        // obtenemos el valor de las fechas en milisegundos
        long milisegundos1 = calendarInicio.getTimeInMillis();
        long milisegundos2 = calendarFinal.getTimeInMillis();

        // tomamos la diferencia
        long diferenciaMilisegundos = milisegundos2 - milisegundos1;

        // Despues va a depender en que formato queremos  mostrar esa
        // diferencia, minutos, segundo horas, dias, etc, aca van algunos
        // ejemplos de conversion

        // calcular la diferencia en segundos
        long diffSegundos = Math.abs(diferenciaMilisegundos / 1000);
        /*if (diffSegundos<600000){
            return String.valueOf(diffSegundos+ "Segundos");
        }*/

        // calcular la diferencia en minutos
        long diffMinutos = Math.abs(diferenciaMilisegundos / (60 * 1000));
        long restominutos = diffMinutos % 60;
        /*if (diffMinutos<60000){
            return String.valueOf(diffMinutos+ "Minutos "+restominutos);
        }*/

        // calcular la diferencia en horas
        long diffHoras = (diferenciaMilisegundos / (60 * 60 * 1000));
        long restoHoras = diffHoras % 60;
        /*if (diffHoras<36000){
            return String.valueOf(diffHoras+ "Horas "+restominutos + "m ");
        }*/

        // calcular la diferencia en dias
        long diffdias = Math.abs(diferenciaMilisegundos / (24 * 60 * 60 * 1000));
        /*if (diffdias<24){
            return String.valueOf(diffHoras+ "Horas "+restominutos + "m ");
        }*/

        if (diffMinutos > 0 && diffHoras >= 24 && diffHoras <= 36) {
            return "AYER";

        } else if (diffHoras > 25) {
            return String.valueOf(inicio.substring(0, 10));

        } else if (diffMinutos == 0) {

            //return String.valueOf(diffSegundos + "s ");
            return "Ahora";
        } else if (diffHoras == 0) {
            return String.valueOf(restominutos + " m");
        } else {
            return String.valueOf(diffHoras + "h " + restominutos + "m ");
        }
        // devolvemos el resultado en un string
        //return String.valueOf(diffHoras + "H " + restominutos + "m ");


    }

    private void traerImagenToolbar(final ImageView imagen) {
        //if (emisor.equals(receptor)) {CAMBIO 11 DE ABRIL
        if (!email_preferences.equals(receptor)) {//Solo actualizamos imagen en los mensajes entrantes
            DatabaseReference database;
            DatabaseReference db;
            database = FirebaseDatabase.getInstance().getReference();
            db = database.getRoot().child(REFERENCIA_USUARIOS).child(imagenReceptor).child("image");
            //db.setValue(usuarios);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //Usuarios  usuarios=dataSnapshot.getValue(Usuarios.class);
                    urlDescarga = dataSnapshot.getValue().toString();
                    //url= Uri.parse(dataSnapshot.getKey());
                    //Toast.makeText(getApplicationContext(),"urlDescarga "+urlDescarga,Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(),"url "+url,Toast.LENGTH_SHORT).show();
                    if (!urlDescarga.equals(IMAGE_DEFAULT)) {
                        Glide.with(context)
                                //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                                //.load(Uri.parse(usuarios.getImage()))
                                .load(Uri.parse(urlDescarga))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)

                                //Si hay transformación no se debe poner el placeholder pq lo carga antes de que se carga la imagen transformada...
                                //.override(60,60)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                                // .placeholder(ic_toolbar)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                                .error(ic_person)//Imagen de sustitución si se ha producido error de carga
                                //.centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                                //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                                //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                                // .transform(new util.CircleTransform(Activity_chats.this))
                                .into(imagen);//dónde vamos a mostrar las imágenes
                    }

                }//Fin if imagenReceptor


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Toast.makeText(, "No se ha podido cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            });

            if (database != null) {
                database = null;
            }
            if (db != null) {
                db = null;
            }
        }
    }

    @Override
    protected void populateViewHolder(ChatsViewHolder viewHolder, Chats model, int position) {

        //CARGAMOS DATOS Y LUEGO MOVEMOS SEGÚN SEA EMISOR O RECEPTOR:=========================
        context = viewHolder.cardViewViewHolder.getContext();

        //MENSAJE CON IMAGEN
        if (!model.getImagen().equals("SIN IMAGEN")) {

            if (model.getRutaImagen() != null) {
                miImagen = new File(model.getRutaImagen());//Si está en el HDD: /storage/emulated/0/Chat/Fotos/nombreimagen.jpg

                //rutaImagenStorage = model.getRutaImagen();
            } /*else {
                miImagen = new File(model.getImagen());//NO está descargada, se descargará cuando haga click en la imagen desde la uri guardada en Firebase
            }*/

            if (miImagen.exists()) {//Traemos desde la uri del HDD
                glideTraeImagenDiscoDuro(viewHolder);
            } else if (!miImagen.exists()) {//Descargamos desde la url de Firebase y difuminamos
                glideTraeImagenFirebase(viewHolder, model);

            }
        }

        //Tratamiento del mensaje porque puede ser que tenga contenido html:
        String mensaje = model.getMensaje();
        //Mensaje con contenido html
        if (mensaje.length() > 5) {
            String mensaje_tratado = mensaje.substring(0, 4);
            if (mensaje_tratado.equals("http")) {
                //viewHolder.txtMensajeviewHolder.setText(Html.fromHtml("<a href=model.getMensaje>" + model.getMensaje()));
                String proposicion = "Visita este enlace:";
                viewHolder.txtMensajeviewHolder.setText(Html.fromHtml("<a href=" + model.getMensaje() + ">" + proposicion + "\n " + model.getMensaje()));
                // viewHolder.txtMensajeviewHolder.setText(Html.fromHtml("<a href="+miurlCompartido+">" + proposicion + " " +miurlCompartido));
                //Para poder hacer click si hay un link
                viewHolder.txtMensajeviewHolder.setMovementMethod(LinkMovementMethod.getInstance());

            } else {

                viewHolder.txtMensajeviewHolder.setText(model.getMensaje());
            }
        } else {

            viewHolder.txtMensajeviewHolder.setText(model.getMensaje());
        }


        //PARA MOVER EL CARDVIEW DE UN LADO PARA OTRO DEPENDIENDO DE SI ES EMISOR O RECEPTOR
        // Para layout:fila_recyclerview_chat_new
        /*RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) viewHolder.cardViewViewHolder.getLayoutParams();//Layout padre del CardView
       FrameLayout.LayoutParams layoutPadreLinearLayout = (FrameLayout.LayoutParams) viewHolder.mensajeBG.getLayoutParams();//Layout padre del LinearLayout*/

        //Para layout:fila_recyclerview_chat_new2
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) viewHolder.layoutPadre.getLayoutParams();
        FrameLayout.LayoutParams layoutPadreLinearLayout = (FrameLayout.LayoutParams) viewHolder.mensajeBG.getLayoutParams();

        receptor = model.getEmailEmisor();
        imagenReceptor = model.getEmisor();
        nombreEmisor = model.getEmisor();
        nombreReceptor = model.getReceptor();


//===========================================SE TRATA DE UN MENSAJE ENVIADO=============================================//

        if (email_preferences.equals(receptor)) {//if (emisor.equals(receptor)) {CAMBIO 11 DE ABRIL
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            layoutPadreLinearLayout.gravity = Gravity.END;

            //MENSAJE CON IMAGEN
            if (!model.getImagen().equals("SIN IMAGEN")) {
              /*  viewHolder.imagenAdjuntada.setBackgroundColor(ContextCompat.getColor(context, R.color.md_light_blue_900));
                viewHolder.imagenAdjuntada.setPadding(4 , 4, 4, 4);*/

                conImagenEnviado(viewHolder, position);
            } else {
                //Mensaje sin imagen
                sinImagen(viewHolder, position);

            /*    viewHolder.imagenAdjuntada.setScaleType(ImageView.ScaleType.CENTER);
                viewHolder.imagenAdjuntada.setImageResource(R.drawable.loading);*/


                viewHolder.txtNombreviewHolder.setText(model.getEmisor());
                viewHolder.txtHoraviewHolder.setText(diferenciaFechas(model.getFecha(), dameFechaHoraActual()));


                viewHolder.mensajeBG.setBackgroundResource(R.drawable.blue_in_message_bg);//Cambiamos el fondo del bocadillo:Emisor
                  //viewHolder.cardViewViewHolder.setBackgroundResource(R.drawable.redondea_cardview);

                //viewHolder.txtMensajeviewHolder.setTextColor(res.getColor(R.color.md_grey_500));//res.getColor is deprecated for Android 6
                viewHolder.txtMensajeviewHolder.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000));
                viewHolder.txtNombreviewHolder.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000_50));
                viewHolder.txtHoraviewHolder.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000_50));

                //Si nos envían un link:
                //viewHolder.txtMensajeviewHolder.setLinkTextColor(ContextCompat.getColor(context, R.color.md_text_white_87));
                viewHolder.txtMensajeviewHolder.setLinkTextColor(ContextCompat.getColor(context, R.color.md_blue_400));
                viewHolder.txtMensajeviewHolder.setTextSize(16);

                viewHolder.imagen.setImageResource(R.drawable.ic_toolbar);//Para que no se actualice con la imagen descargada al hacer scroll en el recyclerview


                //Para layout:fila_recyclerview_chat_new2
                viewHolder.txtNombreviewHolder.setText("Tú");
                viewHolder.imagen.setVisibility(View.GONE);

                if (model.isParaBorrar()) {
                    long fecha_mensaje = model.getLongFecha();
                    long fecha_actual = System.currentTimeMillis();
                    //int tiempoConfiguradoEmisor= traeTimeBorrado(model.getEmisor());

                    if ((fecha_actual - fecha_mensaje) > mitiempoConfiguradoEmisor) {
                        Log.i("CONFIGURACION BORRADO PARA EMISOR", String.valueOf(mitiempoConfiguradoEmisor));
                        //Toast.makeText(context, "mitiempoConfiguradoEmisor "+mitiempoConfiguradoEmisor, Toast.LENGTH_SHORT).show();
                        viewHolder.txtMensajeviewHolder.setText(R.string.mensaje_borrado);
                        viewHolder.txtMensajeviewHolder.setTextColor(ContextCompat.getColor(context, R.color.color_text_mensaje_borrado));
                    }
                }
            }//Fin Mensaje sin imagen


//==============================================SE TRATA DE UN MENSAJE RECIBIDO========================================//
        } else {
            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            layoutPadreLinearLayout.gravity = Gravity.START;

            //MENSAJE CON IMAGEN
            if (!model.getImagen().equals("SIN IMAGEN")) {

                conImagenRecibido(viewHolder, position);

            } else {
                //Mensaje sin imagen
                sinImagen(viewHolder, position);

               /* viewHolder.imagenAdjuntada.setScaleType(ImageView.ScaleType.CENTER);
                viewHolder.imagenAdjuntada.setImageResource(R.drawable.loading);*/

                viewHolder.txtNombreviewHolder.setText(model.getEmisor());
                viewHolder.txtHoraviewHolder.setText(diferenciaFechas(model.getFecha(), dameFechaHoraActual()));
                viewHolder.mensajeBG.setBackgroundResource(R.drawable.white_message_bg);//Cambiamos el fondo del bocadillo:Receptor
                viewHolder.txtMensajeviewHolder.setTextColor(ContextCompat.getColor(context, R.color.md_grey_600));//
                viewHolder.txtNombreviewHolder.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                viewHolder.txtHoraviewHolder.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                //Si nos envían un link:
                viewHolder.txtMensajeviewHolder.setLinkTextColor(ContextCompat.getColor(context, R.color.md_blue_600));
                viewHolder.txtMensajeviewHolder.setTextSize(16);

                traerImagenToolbar(viewHolder.imagen);//Solo actualizamos la imagen del mensaje recibido
                //viewHolder.imagenAdjuntada.setImageResource(R.drawable.common_google_signin_btn_icon_dark);//Imagen que puede venir adjuntada...

                //Para layout:fila_recyclerview_chat_new2
                viewHolder.imagen.setVisibility(View.VISIBLE);
                viewHolder.txtNombreviewHolder.setText(model.getEmisor());

                if (model.isParaBorrar()) {
                    long fecha_mensaje = model.getLongFecha();
                    long fecha_actual = System.currentTimeMillis();

                    //int tiempoConfiguradoReceptor= traeTimeBorrado(model.getReceptor());

                    if ((fecha_actual - fecha_mensaje) > mitiempoConfiguradoReceptor) {

                        //Toast.makeText(context, "mitiempoConfiguradoReceptor "+mitiempoConfiguradoReceptor, Toast.LENGTH_SHORT).show();
                        Log.i("CONFIGURACION BORRADO PARA RECEPTOR", String.valueOf(mitiempoConfiguradoReceptor));
                        viewHolder.txtMensajeviewHolder.setText(R.string.mensaje_borrado);
                        viewHolder.txtMensajeviewHolder.setTextColor(ContextCompat.getColor(context, R.color.color_text_mensaje_borrado));
                    }
                }

            }//Fin mensaje sin imagen
        }//Fin mensaje recibido

        //Añadimos reglas para setLayoutParams
        //viewHolder.cardViewViewHolder.setLayoutParams(rl);
        viewHolder.layoutPadre.setLayoutParams(rl);
        viewHolder.mensajeBG.setLayoutParams(layoutPadreLinearLayout);

        //Gestionamos la transparencia del cardView dependiendo de la API
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            viewHolder.cardViewViewHolder.getBackground().setAlpha(0);
        else
            viewHolder.cardViewViewHolder.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
    }


    private void ficheroNoexisteEnHDD(ChatsViewHolder viewHolder) {
        //NO ESTÁ DESCARGADA EN HDD. DIFUMINAMOS LA IMAGEN
        viewHolder.imagenAdjuntada.setScaleType(ImageView.ScaleType.CENTER);
        viewHolder.imagenAdjuntada.setAlpha(0.3F);
    }

    private void ficheroExisteEnHDD(ChatsViewHolder viewHolder) {
        viewHolder.imagenAdjuntada.setScaleType(ImageView.ScaleType.CENTER);
        viewHolder.imagenAdjuntada.setAlpha(1F);
}

    private void glideTraeImagenFirebase(ChatsViewHolder viewHolder, Chats model) {


        Glide.with(context)
                //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                //.load(Uri.parse(usuarios.getImage()))

                .load(Uri.parse(model.getImagen()))
                //.load(miImagen)

                //.placeholder(R.drawable.image_susti)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                //.error(R.drawable.loading)//Imagen de sustitución si se ha producido error de carga
                //.override(600,400)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                .centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                .into(viewHolder.imagenAdjuntada);//dónde vamos a mostrar las imágenesdrawable/common_google_signin_btn_icon_dark
        //miImagen = new File(model.getRutaImagen());
    }


    private void glideTraeImagenDiscoDuro(ChatsViewHolder viewHolder) {
        Glide.with(context)
                //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                //.load(Uri.parse(usuarios.getImage()))

                //.load(Uri.parse(model.getImagen()))
                .load(miImagen)

                //.placeholder(R.drawable.image_susti)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                //.error(R.drawable.loading)//Imagen de sustitución si se ha producido error de carga
                //.override(600,400)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                .centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                .into(viewHolder.imagenAdjuntada);//dónde vamos a mostrar las imágenesdrawable/common_google_signin_btn_icon_dark
    }


    private void sinImagen(ChatsViewHolder holder, int position) {
        holder.mensajeBG.setVisibility(View.VISIBLE);
        holder.txtMensajeviewHolder.setVisibility(View.VISIBLE);
        holder.txtNombreviewHolder.setVisibility(View.VISIBLE);
        holder.txtHoraviewHolder.setVisibility(View.VISIBLE);
        holder.imagenAdjuntada.setVisibility(View.GONE);
        holder.contenedor_imgAdjuntada.setVisibility(View.GONE);
    }

    private void conImagenEnviado(ChatsViewHolder viewHolder, int position) {
        //viewHolder.imagenAdjuntada.setBackgroundColor(ContextCompat.getColor(context, R.color.md_light_blue_900));
        //viewHolder.imagenAdjuntada.setPadding(4, 4, 4, 4);
        viewHolder.contenedor_imgAdjuntada.setBackground(ContextCompat.getDrawable(context,R.drawable.blue_image_bg));
        viewHolder.mensajeBG.setVisibility(View.GONE);
        viewHolder.txtMensajeviewHolder.setVisibility(View.GONE);
        viewHolder.txtNombreviewHolder.setVisibility(View.GONE);
        viewHolder.txtHoraviewHolder.setVisibility(View.GONE);
        viewHolder.imagen.setVisibility(View.GONE);
        viewHolder.imagenAdjuntada.setVisibility(View.VISIBLE);
        viewHolder.contenedor_imgAdjuntada.setVisibility(View.VISIBLE);
        viewHolder.imagenAdjuntada.setScaleType(ImageView.ScaleType.CENTER_CROP);
        viewHolder.imagenAdjuntada.setAlpha(1F);


    }


    private void conImagenRecibido(ChatsViewHolder holder, int position) {
        //holder.imagenAdjuntada.setBackgroundColor(ContextCompat.getColor(context, R.color.md_white_1000));
        //holder.imagenAdjuntada.setPadding(4, 4, 4, 4);
        holder.contenedor_imgAdjuntada.setBackground(ContextCompat.getDrawable(context,R.drawable.white_image_bg));
        holder.mensajeBG.setVisibility(View.GONE);
        holder.txtMensajeviewHolder.setVisibility(View.GONE);
        holder.txtNombreviewHolder.setVisibility(View.GONE);
        holder.txtHoraviewHolder.setVisibility(View.GONE);
        holder.imagen.setVisibility(View.VISIBLE);
        holder.imagenAdjuntada.setVisibility(View.VISIBLE);
        holder.contenedor_imgAdjuntada.setVisibility(View.VISIBLE);
        traerImagenToolbar(holder.imagen);

        //SI LA IMAGEN LA TENEMOS EN EL HDD DEL DISPOSITIVO LA TRAEMOS
        if (miImagen.exists()) {
                  ficheroExisteEnHDD(holder);
        } else {//NO ESTÁ EN EL HDD DEL DISPOSITIVO LA DESCARGAMOS DESDE FIREBASE
                   ficheroNoexisteEnHDD(holder);
        }


    }


    class ChatsViewHolder extends RecyclerView.ViewHolder
            implements
            View.OnClickListener,
            View.OnLongClickListener {

        TextView txtNombreviewHolder;
        TextView txtMensajeviewHolder;
        TextView txtHoraviewHolder;
        CardView cardViewViewHolder;
        RelativeLayout mensajeBG;//Es el fondo de los bocadillos....
        ImageView imagen;
        RelativeLayout layoutPadre;
        ImageView imagenAdjuntada;
        RelativeLayout contenedor_imgAdjuntada;

        ChatsViewHolder(View itemView) {
            super(itemView);

            this.txtNombreviewHolder = (TextView) itemView.findViewById(R.id.tv1);
            this.txtMensajeviewHolder = (TextView) itemView.findViewById(R.id.tv3);
            this.txtHoraviewHolder = (TextView) itemView.findViewById(R.id.tvHora);
            this.cardViewViewHolder = (CardView) itemView.findViewById(R.id.cv);
            this.mensajeBG = (RelativeLayout) itemView.findViewById(R.id.mensajeBG);
            this.imagen = (ImageView) itemView.findViewById(R.id.category);
            this.imagenAdjuntada = (ImageView) itemView.findViewById(R.id.imgAdjuntada);
            this.layoutPadre = (RelativeLayout) itemView.findViewById(R.id.layoutPadre);
            this.contenedor_imgAdjuntada=(RelativeLayout) itemView.findViewById(R.id.contenedor_imgAdjuntada);

            itemView.setOnClickListener(this);
            //itemView.setOnLongClickListener(this);
            imagenAdjuntada.setOnClickListener(this);
            imagenAdjuntada.setOnLongClickListener(this);
            mensajeBG.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View view) {

            //int position = getAdapterPosition();
            //Chats currentItem = (Chats) getItem(position);
            mToolbar.setVisibility(View.VISIBLE);
            mToolbar2.setVisibility(View.GONE);

            // Toast.makeText(context, "Has hecho click fuera", Toast.LENGTH_SHORT).show();

            if (view.getId() == R.id.imgAdjuntada) {
                //Toast.makeText(context, "Has hecho click dentro", Toast.LENGTH_SHORT).show();
                int position = getAdapterPosition();//Posición en el adaptador
                final String post_key = getRef(position).getKey();//Devuelve el árbol seleccionado de la queryref
                //Query rutaImagen= getRef(position) .getKey();
                Chats currentItem = (Chats) getItem(position);
                String mirutaImagen = currentItem.getRutaImagen();
                String urlDescarga = currentItem.getImagen();

                openActivityPerfil(view, urlDescarga, mirutaImagen);
            }
        }

        private void openActivityPerfil(View v, String urlDescarga, String rutaImagenAdjuntada) {

            //Comprobamos si la imagen ya ha sido descargda...
            File fileImage = new File(rutaImagenAdjuntada);
            if (fileImage.exists()) {//Si la imagen ya está descarga en HDD la abrimos en la galería...
             /*   Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.parse("file://" + ruta2), "image*//*");
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                intent.setDataAndType(Uri.parse("file://" + rutaImagenAdjuntada), "image*//*");
                v.getContext().startActivity(intent);*/

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.parse("file://" + ruta2), "image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                    //File fileImage=new File(rutaImagenAdjuntada);
                    String authorities = v.getContext().getPackageName() + ".provider";
                    Uri imageUri = FileProvider.getUriForFile(v.getContext(), authorities, fileImage);
                    //intent.setDataAndType(Uri.parse("file://" + imageUri), "image/*");
                    //Intent i=new Intent(Intent.ACTION_VIEW,
                    FileProvider.getUriForFile(v.getContext(), authorities, fileImage);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(imageUri, "image/*");

                }
                //ANTES DE ANDROID 7
                else {
                    intent.setDataAndType(Uri.parse("file://" + rutaImagenAdjuntada), "image/*");
                }

                v.getContext().startActivity(intent);

            } else {//Si la imagen no ha sido descargada la descargamos y después la abrimos en la galería...

                traeImagenNoDescargada(v, urlDescarga, rutaImagenAdjuntada);

            }

        }

        private void traeImagenNoDescargada(final View view, final String urlDescarga, final String rutaImagenAdjuntada) {
            //Se descarga la imagen mediante un nuevo hilo a partir de la url que haya en el storage de firebase y se GUARDA en HDD en la ruta de las imágenes de la app
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        Tratamiento_Imagenes.descargarImagen(urlDescarga, rutaImagenAdjuntada);
                        mostrarImagenDescargada(rutaImagenAdjuntada,view);



                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();

        }

        private void mostrarImagenDescargada(String rutaImagenAdjuntada, View view) {
            //Una vez descargada abrimos la galería para mostrarla
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            //intent.setDataAndType(Uri.parse("file://" + ruta2), "image/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
                File fileImage=new File(rutaImagenAdjuntada);
                String authorities = view.getContext().getPackageName() + ".provider";
                Uri imageUri = FileProvider.getUriForFile(view.getContext(), authorities, fileImage);
                //intent.setDataAndType(Uri.parse("file://" + imageUri), "image/*");
                //Intent i=new Intent(Intent.ACTION_VIEW,
                FileProvider.getUriForFile(view.getContext(), authorities, fileImage);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(imageUri, "image/*");
            }
            //ANTES DE ANDROID 7
            else {
                intent.setDataAndType(Uri.parse("file://" + rutaImagenAdjuntada), "image/*");
            }

            view.getContext().startActivity(intent);
            //VUELVE AL onResume() de Activity_Chats

        }


        @Override
        public boolean onLongClick(final View view) {
            int position = getAdapterPosition();
            final Chats currentItem = (Chats) getItem(position);
            final DatabaseReference reference = getRef(position);
            //reference.removeValue();

            String imagen = currentItem.getImagen();
            final StorageReference mtorageReference = FirebaseStorage.getInstance().getReference().child("Images")
                    .child(imagenReceptor).child("imagenes").child(imagen);


            String nombreimagen = mtorageReference.getName();
            //String solonombre=nombreimagen.split("android",31);
            String rutaimagen = mtorageReference.getPath();
            String almacenimagen = mtorageReference.getBucket();
            mtorageReference.delete();


            mToolbar.setVisibility(View.GONE);
            mToolbar2.setVisibility(View.VISIBLE);
            //miActivity.setActionBar(mToolbar2);


            //mToolbar2.setSubtitle(estadoReceptor);
            //mToolbar2.setTitleMarginStart(0);
            //mToolbar2.setDisplayHomeAsUpEnabled(true);


            mToolbar2.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (emisor.equals(currentItem.getEmailEmisor())) {//Si el mensaje seleccionado es del usuario le deja borrarlo...
                        borrar(reference, view);
                        // Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                    }
                    mToolbar.setVisibility(View.VISIBLE);
                    //mToolbar.setTitle(receptor);
                    //mToolbar.setSubtitle(estadoReceptor);
                    mToolbar2.setVisibility(View.GONE);

                    return false;
                }
            });

            return true;
        }


        private void borrar(final DatabaseReference reference, final View view) {

            AlertDialog.Builder dialogEliminar = new AlertDialog.Builder(view.getContext());

            //dialogEliminar.setIcon(R.drawable.imagen2);
            dialogEliminar.setTitle(R.string.eliminarmensaje);
            dialogEliminar.setMessage(R.string.textoelimininarmensaje);
            dialogEliminar.setCancelable(false);

            dialogEliminar.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int boton) {

                            reference.removeValue();


                            Toast.makeText(view.getContext(),
                                    R.string.confirmeliminarmensaje,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            dialogEliminar.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mToolbar.setVisibility(View.VISIBLE);
                            mToolbar2.setVisibility(View.GONE);

                        }
                    });

            dialogEliminar.show();

        }
    }


}


