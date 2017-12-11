package adaptadores;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.antonio.chat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import modelos.Chats;
import referencias.MisReferencias;

import static com.example.antonio.chat.R.drawable.ic_person;
import static referencias.MisReferencias.REFERENCIA_USUARIOS;

/**
 * Created by Usuario on 23/10/2017.
 */

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder>{

    //Context mContext;
    List<Chats> mchats;

    //CUANDO Activity_chats se abre a partir de una notificación USUARIO_CONECTADO es nulo
    private String emisor = MisReferencias.USUARIO_CONECTADO;
    private String imagenReceptor;
    private String receptor;
    private String urlDescarga;
    private static final String IMAGE_DEFAULT = "default_image";
    private Context context;
    private String email_preferences = "";
    private Toolbar mToolbar, mToolbar2;
    private int timeBorrado =0;
    private int mitiempoConfiguradoEmisor=0;
    private int mitiempoConfiguradoReceptor=0;

    public ChatsAdapter(List<Chats> chats,Toolbar toolbar, Toolbar toolbar2,int tiempoConfiguradoEmisor, int tiempoConfiguradoReceptor) {
        //mchats=new ArrayList<Chats>();
        //mContext = context;
        mchats = chats;
        mToolbar = toolbar;
        mToolbar2 = toolbar2;
        mitiempoConfiguradoEmisor=tiempoConfiguradoEmisor;
        mitiempoConfiguradoReceptor=tiempoConfiguradoReceptor;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_recyclerview_chat_new2,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        //CUANDO Activity_chats se abre a partir de una notificación USUARIO_CONECTADO es nulo. Se traen los datos de las preferencias del usuario....
        SharedPreferences prefs = parent.getContext().getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);
        //String nick = prefs.getString("nick", "Usuario");
        email_preferences = prefs.getString("email", "emailpordefecto@gmail.com");

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        context = holder.cardViewViewHolder.getContext();
    //CARGAMOS DATOS Y LUEGO MOVEMOS SEGÚN SEA EMISOR O RECEPTOR:
    //MENSAJE CON IMAGEN
        if( !mchats.get(position).getImagen().equals("SIN IMAGEN")   ){
            Glide.with(context)
                    //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                    //.load(Uri.parse(usuarios.getImage()))
                    .load(Uri.parse(mchats.get(position).getImagen()))
                    //.placeholder(R.drawable.image_susti)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                    .error(R.drawable.ic_action)//Imagen de sustitución si se ha producido error de carga
                    //.override(600,400)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                    .centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                    //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                    .into(holder.imagenAdjuntada);//dónde vamos a mostrar las imágenes

           // hiloMostrarImagenes();
        }

        //Tratamiento del mensaje porque puede ser que tenga contenido html:
        String mensaje = mchats.get(position).getMensaje();
        //Mensaje con contenido html
        if (mensaje.length() > 5) {
            String mensaje_tratado = mensaje.substring(0, 4);
            if (mensaje_tratado.equals("http")) {

                //viewHolder.txtMensajeviewHolder.setText(Html.fromHtml("<a href=model.getMensaje>" + model.getMensaje()));
                String proposicion = "Visita este enlace:";
                holder.txtMensajeviewHolder.setText(Html.fromHtml("<a href=" + mchats.get(position).getMensaje() + ">" + proposicion + "\n " + mchats.get(position).getMensaje()));
                // viewHolder.txtMensajeviewHolder.setText(Html.fromHtml("<a href="+miurlCompartido+">" + proposicion + " " +miurlCompartido));
                //Para poder hacer click si hay un link
                holder.txtMensajeviewHolder.setMovementMethod(LinkMovementMethod.getInstance());

            } else {

                holder.txtMensajeviewHolder.setText(mchats.get(position).getMensaje());
            }
        } else {

            holder.txtMensajeviewHolder.setText(mchats.get(position).getMensaje());
        }

        //Para layout:fila_recyclerview_chat_new2
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) holder.layoutPadre.getLayoutParams();
        FrameLayout.LayoutParams layoutPadreLinearLayout = (FrameLayout.LayoutParams) holder.mensajeBG.getLayoutParams();

        receptor = mchats.get(position).getEmailEmisor();
        imagenReceptor = mchats.get(position).getEmisor();

//===========================================SE TRATA DE UN MENSAJE ENVIADO=============================================//

        if (email_preferences.equals(receptor)) {//if (emisor.equals(receptor)) {CAMBIO 11 DE ABRIL
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            layoutPadreLinearLayout.gravity = Gravity.END;

            //MENSAJE CON IMAGEN
            if( !mchats.get(position).getImagen().equals("SIN IMAGEN")){
             /*   holder.mensajeBG.setVisibility(View.GONE);
                holder.txtMensajeviewHolder.setVisibility(View.GONE);
                holder.txtNombreviewHolder.setVisibility(View.GONE);
                holder.txtHoraviewHolder.setVisibility(View.GONE);
                holder.imagen.setVisibility(View.GONE);
                holder.imagenAdjuntada.setVisibility(View.VISIBLE);*/

                conImagen(holder, position);

            }
            else{
                //Mensaje sin imagen
               /* holder.mensajeBG.setVisibility(View.VISIBLE);
                holder.txtMensajeviewHolder.setVisibility(View.VISIBLE);
                holder.txtNombreviewHolder.setVisibility(View.VISIBLE);
                holder.txtHoraviewHolder.setVisibility(View.VISIBLE);
                holder.imagenAdjuntada.setVisibility(View.VISIBLE);
                holder.imagenAdjuntada.setVisibility(View.GONE);*/
                sinImagen(holder,position);


                holder.txtNombreviewHolder.setText(mchats.get(position).getEmisor());
                holder.txtHoraviewHolder.setText(diferenciaFechas(mchats.get(position).getFecha(), dameFechaHoraActual()));

                holder.mensajeBG.setBackgroundResource(R.drawable.blue_in_message_bg);//Cambiamos el fondo del bocadillo:Emisor
                //viewHolder.txtMensajeviewHolder.setTextColor(res.getColor(R.color.md_grey_500));//res.getColor is deprecated for Android 6
                holder.txtMensajeviewHolder.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000));
                holder.txtNombreviewHolder.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000_50));
                holder.txtHoraviewHolder.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000_50));

                //Si nos envían un link:
                //viewHolder.txtMensajeviewHolder.setLinkTextColor(ContextCompat.getColor(context, R.color.md_text_white_87));
                holder.txtMensajeviewHolder.setLinkTextColor(ContextCompat.getColor(context, R.color.md_blue_400));
                holder.txtMensajeviewHolder.setTextSize(16);

                holder.imagen.setImageResource(R.drawable.ic_toolbar);//Para que no se actualice con la imagen descargada al hacer scroll en el recyclerview
                //viewHolder.imagenAdjuntada.setImageResource(R.drawable.imagen_adjuntada);//Imagen que puede venir adjuntada...

                //Para layout:fila_recyclerview_chat_new2.
                holder.txtNombreviewHolder.setText("Tú");
                holder.imagen.setVisibility(View.GONE);

                if (mchats.get(position).isParaBorrar()) {
                    long fecha_mensaje = mchats.get(position).getLongFecha();
                    long fecha_actual = System.currentTimeMillis();
                    //int tiempoConfiguradoEmisor= traeTimeBorrado(model.getEmisor());

                    if ((fecha_actual - fecha_mensaje) > mitiempoConfiguradoEmisor) {
                        Log.i("CONFIGURACION BORRADO PARA EMISOR", String.valueOf(mitiempoConfiguradoEmisor));
                        //Toast.makeText(context, "mitiempoConfiguradoEmisor "+mitiempoConfiguradoEmisor, Toast.LENGTH_SHORT).show();
                        holder.txtMensajeviewHolder.setText(R.string.mensaje_borrado);

                        holder.txtMensajeviewHolder.setTextColor(ContextCompat.getColor(context, R.color.color_text_mensaje_borrado));
                    }
                }

            }//Fin Mensaje sin imagen


//==============================================SE TRATA DE UN MENSAJE RECIBIDO========================================//
        } else {
            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            layoutPadreLinearLayout.gravity = Gravity.START;

            //MENSAJE CON IMAGEN
            if(!mchats.get(position).getImagen().equals("SIN IMAGEN")){

              /*  holder.mensajeBG.setVisibility(View.GONE);
                holder.txtMensajeviewHolder.setVisibility(View.GONE);
                holder.txtMensajeviewHolder.setVisibility(View.GONE);
                holder.txtHoraviewHolder.setVisibility(View.GONE);
                holder.cardViewViewHolder.setVisibility(View.GONE);
                holder.imagenAdjuntada.setVisibility(View.VISIBLE);
                holder.imagen.setVisibility(View.GONE);*/
                conImagen(holder, position);

            }else{
                //Mensaje sin imagen
                /*holder.mensajeBG.setVisibility(View.VISIBLE);
                holder.txtMensajeviewHolder.setVisibility(View.VISIBLE);
                holder.txtNombreviewHolder.setVisibility(View.VISIBLE);
                holder.txtHoraviewHolder.setVisibility(View.VISIBLE);
                holder.imagenAdjuntada.setVisibility(View.VISIBLE);
                holder.imagenAdjuntada.setVisibility(View.GONE);*/
                sinImagen(holder,position);

                holder.txtNombreviewHolder.setText(mchats.get(position).getEmisor());
                holder.txtHoraviewHolder.setText(diferenciaFechas(mchats.get(position).getFecha(), dameFechaHoraActual()));

                holder.mensajeBG.setBackgroundResource(R.drawable.white_message_bg);//Cambiamos el fondo del bocadillo:Receptor
                holder.txtMensajeviewHolder.setTextColor(ContextCompat.getColor(context, R.color.md_grey_600));//
                holder.txtNombreviewHolder.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                holder.txtHoraviewHolder.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                //Si nos envían un link:
                holder.txtMensajeviewHolder.setLinkTextColor(ContextCompat.getColor(context, R.color.md_blue_600));
                holder.txtMensajeviewHolder.setTextSize(16);

                traerImagen(holder.imagen);//Solo actualizamos la imagen del mensaje recibido
                //viewHolder.imagenAdjuntada.setImageResource(R.drawable.common_google_signin_btn_icon_dark);//Imagen que puede venir adjuntada...

                //Para layout:fila_recyclerview_chat_new2
                holder.imagen.setVisibility(View.VISIBLE);
                holder.txtNombreviewHolder.setText(mchats.get(position).getEmisor());

                if (mchats.get(position).isParaBorrar()) {
                    long fecha_mensaje = mchats.get(position).getLongFecha();
                    long fecha_actual = System.currentTimeMillis();

                    //int tiempoConfiguradoReceptor= traeTimeBorrado(model.getReceptor());

                    if ((fecha_actual - fecha_mensaje) > mitiempoConfiguradoReceptor) {

                        //Toast.makeText(context, "mitiempoConfiguradoReceptor "+mitiempoConfiguradoReceptor, Toast.LENGTH_SHORT).show();
                        Log.i("CONFIGURACION BORRADO PARA RECEPTOR", String.valueOf(mitiempoConfiguradoReceptor));
                        holder.txtMensajeviewHolder.setText(R.string.mensaje_borrado);
                        holder.txtMensajeviewHolder.setTextColor(ContextCompat.getColor(context, R.color.color_text_mensaje_borrado));
                    }
                }


            }//Fin mensaje sin imagen
        }//Fin mensaje recibido

        //Añadimos reglas para setLayoutParams
        //viewHolder.cardViewViewHolder.setLayoutParams(rl);
        holder.layoutPadre.setLayoutParams(rl);
        holder.mensajeBG.setLayoutParams(layoutPadreLinearLayout);


        //Gestionamos la transparencia del cardView dependiendo de la API
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            holder.cardViewViewHolder.getBackground().setAlpha(0);
        else
            holder.cardViewViewHolder.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));

    }

    private void sinImagen(ViewHolder holder, int position) {
        holder.mensajeBG.setVisibility(View.VISIBLE);
        holder.txtMensajeviewHolder.setVisibility(View.VISIBLE);
        holder.txtNombreviewHolder.setVisibility(View.VISIBLE);
        holder.txtHoraviewHolder.setVisibility(View.VISIBLE);
        holder.imagenAdjuntada.setVisibility(View.VISIBLE);
        holder.imagenAdjuntada.setVisibility(View.GONE);
    }

    private void conImagen(ViewHolder holder, int position) {
        holder.mensajeBG.setVisibility(View.GONE);
        holder.txtMensajeviewHolder.setVisibility(View.GONE);
        holder.txtNombreviewHolder.setVisibility(View.GONE);
        holder.txtHoraviewHolder.setVisibility(View.GONE);
        holder.imagen.setVisibility(View.GONE);
        holder.imagenAdjuntada.setVisibility(View.VISIBLE);

    }

    private void hiloMostrarUI() {

    new UI_Imagenes().execute();

    }

    @Override
    public int getItemCount() {
        return mchats.size();
    }

    private void traerImagen(final ImageView imagen) {
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


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener{
        TextView txtNombreviewHolder;
        TextView txtMensajeviewHolder;
        TextView txtHoraviewHolder;
        CardView cardViewViewHolder;
        RelativeLayout mensajeBG;//Es el fondo de los bocadillos....
        ImageView imagen;
        RelativeLayout layoutPadre;
        ImageView imagenAdjuntada;

        public ViewHolder(View itemView) {
            super(itemView);

            this.txtNombreviewHolder = (TextView) itemView.findViewById(R.id.tv1);
            this.txtMensajeviewHolder = (TextView) itemView.findViewById(R.id.tv3);
            this.txtHoraviewHolder = (TextView) itemView.findViewById(R.id.tvHora);
            this.cardViewViewHolder = (CardView) itemView.findViewById(R.id.cv);
            this.mensajeBG = (RelativeLayout) itemView.findViewById(R.id.mensajeBG);
            this.imagen = (ImageView) itemView.findViewById(R.id.category);
            this.imagenAdjuntada = (ImageView) itemView.findViewById(R.id.imgAdjuntada);

            this.layoutPadre = (RelativeLayout) itemView.findViewById(R.id.layoutPadre);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mToolbar.setVisibility(View.VISIBLE);
            mToolbar2.setVisibility(View.GONE);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            final Chats currentItem = (Chats) mchats.get(position);
            //final DatabaseReference reference = getRef(position);
            //reference.removeValue();

            mToolbar.setVisibility(View.GONE);
            mToolbar2.setVisibility(View.VISIBLE);


            mToolbar2.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (emisor.equals(currentItem.getEmailEmisor())) {//Si el mensaje seleccionado es del usuario le deja borrarlo...
                        //borrar(reference, view);
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


            //return false;
        }
    }

    private class UI_Imagenes extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //solicitud_Volley();
        }
    }


}
