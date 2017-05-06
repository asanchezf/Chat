package adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import modelos.Chats;
import referencias.MisReferencias;

import static com.example.antonio.chat.R.drawable.ic_person;
import static referencias.MisReferencias.REFERENCIA_USUARIOS;

/**
 * Created by Usuario on 09/02/2017.
 */

public class AdaptadorChatsViewHolder_new extends FirebaseRecyclerAdapter<Chats, AdaptadorChatsViewHolder_new.ChatsViewHolder> {

    //CUANDO Activity_chats se abre a partir de una notificación USUARIO_CONECTADO es nulo
    String emisor = MisReferencias.USUARIO_CONECTADO;
    String imagenReceptor;
    String receptor;
    private String urlDescarga;
    private static final String IMAGE_DEFAULT = "default_image";
    Context context;

    String nick="";
    String email_preferences="";




    public AdaptadorChatsViewHolder_new(int modelLayout, Query ref) {
        super(Chats.class, modelLayout, ChatsViewHolder.class, ref);




    }


    @Override
    public ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return super.onCreateViewHolder(parent, viewType);
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(mModelLayout, parent, false);

        //CUANDO Activity_chats se abre a partir de una notificación USUARIO_CONECTADO es nulo. Se traen los datos de las preferencias del usuario....
        SharedPreferences prefs = parent.getContext().getSharedPreferences("ficheroconfiguracion", Context.MODE_PRIVATE);
       nick = prefs.getString("nick", "Usuario");//Valor por defecto 1000 que se aplica si no encuentra nada
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

    @Override
    protected void populateViewHolder(ChatsViewHolder viewHolder, Chats model, int position) {







        //Context context;
        viewHolder.txtNombreviewHolder.setText(model.getEmisor());
        viewHolder.txtMensajeviewHolder.setText(model.getMensaje());
        viewHolder.txtHoraviewHolder.setText(diferenciaFechas(model.getFecha(), dameFechaHoraActual()));

        //No se muestra el receptor...
        context = viewHolder.cardViewViewHolder.getContext();
        //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.cardViewViewHolder.getLayoutParams();

        //PARA MOVER EL CARDVIEW DE UN LADO PARA OTRO DEPENDIENDO DE SI ES EMISOR O RECEPTOR
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) viewHolder.cardViewViewHolder.getLayoutParams();//Layout padre del CardView
        FrameLayout.LayoutParams layoutPadreLinearLayout = (FrameLayout.LayoutParams) viewHolder.mensajeBG.getLayoutParams();//Layout padre del LinearLayout


        /*RelativeLayout.LayoutParams llMensaje = (RelativeLayout.LayoutParams) viewHolder.txtMensajeviewHolder.getLayoutParams();
        RelativeLayout.LayoutParams llHora = (RelativeLayout.LayoutParams) viewHolder.txtHoraviewHolder.getLayoutParams();*/


        Resources res = context.getResources();

        //String emisor=MisReferencias.USUARIO_CONECTADO;
         receptor = model.getEmailEmisor();
        imagenReceptor = model.getEmisor();

        // traerImagen(viewHolder.imagen);

        //SE TRATA DE UN MENSAJE ENVIADO:Compara los dos emails
        //if (emisor.equals(receptor)) {CAMBIO 11 DE ABRIL
        if (email_preferences.equals(receptor)) {
            //viewHolder.mensajeBG.setBackgroundResource(R.drawable.in_message_bg);
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            layoutPadreLinearLayout.gravity = Gravity.RIGHT;

            //BIS:
            //viewHolder.mensajeBG.setBackgroundColor(res.getColor(R.color.colorPrimaryDark));
            viewHolder.mensajeBG.setBackgroundResource(R.drawable.blue_in_message_bg);//Cambiamos el fondo del bocadillo:Emisor


            viewHolder.txtMensajeviewHolder.setTextColor(res.getColor(R.color.md_white_1000));

            viewHolder.txtNombreviewHolder.setTextColor(res.getColor(R.color.md_white_1000_50));
            viewHolder.txtHoraviewHolder.setTextColor(res.getColor(R.color.md_white_1000_50));

            //BIS:
            viewHolder.imagen.setImageResource(R.drawable.ic_toolbar);//Para que no se actualice con la imagen descargada al hacer scroll en el recyclerview
            //viewHolder.imagen.setVisibility(View.GONE);


            /*llMensaje.gravity = Gravity.RIGHT;
            llHora.gravity = Gravity.RIGHT;*/
           /* llMensaje.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            llMensaje.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);*/
            /*llHora.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            llHora.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);*/


//            llHora.addRule(RelativeLayout.ALIGN_END,R.id.tv3);

            //viewHolder.txtMensajeviewHolder.setGravity(Gravity.RIGHT);

           /* viewHolder.imagen.setVisibility(View.GONE);
            viewHolder.txtNombreviewHolder.setVisibility(View.INVISIBLE);*/

        }

        //else if(!emisor.equals(receptor)){

        else {//SE TRATA DE UN MENSAJE RECIBIDO

            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            layoutPadreLinearLayout.gravity = Gravity.LEFT;

            //BIS:
            //viewHolder.mensajeBG.setBackgroundColor(res.getColor(R.color.md_white_1000));
            viewHolder.mensajeBG.setBackgroundResource(R.drawable.white_message_bg);//Cambiamos el fondo del bocadillo:Receptor

            viewHolder.txtMensajeviewHolder.setTextColor(res.getColor(R.color.md_grey_500));
            viewHolder.txtNombreviewHolder.setTextColor(res.getColor(R.color.colorAccent));
            viewHolder.txtHoraviewHolder.setTextColor(res.getColor(R.color.colorAccent));


           /* llMensaje.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            llMensaje.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
            llHora.addRule(RelativeLayout.ALIGN_PARENT_LEFT);*/


            /*viewHolder.txtMensajeviewHolder.setGravity(Gravity.LEFT);*/

            traerImagen(viewHolder.imagen);//Solo actualizamos la imagen del mensaje recibido

           /* viewHolder.imagen.setVisibility(View.VISIBLE);
            viewHolder.txtNombreviewHolder.setVisibility(View.VISIBLE);*/


        }

        //Añadimos reglas para setLayoutParams
        viewHolder.cardViewViewHolder.setLayoutParams(rl);
        viewHolder.mensajeBG.setLayoutParams(layoutPadreLinearLayout);

       /* viewHolder.txtMensajeviewHolder.setLayoutParams(llMensaje);
        viewHolder.txtHoraviewHolder.setLayoutParams(llHora);*/

        //Gestionamos la transparencia del cardView dependiendo de la API
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            viewHolder.cardViewViewHolder.getBackground().setAlpha(0);
        else
            viewHolder.cardViewViewHolder.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));

    }


    class ChatsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
            View.OnLongClickListener {

        TextView txtNombreviewHolder;
        TextView txtMensajeviewHolder;
        //TextView txtFechaviewHolder;
        TextView txtHoraviewHolder;
        //ImageView imagenviewHolder;
        CardView cardViewViewHolder;
        RelativeLayout mensajeBG;//Es el fondo de los bocadillos....
        ImageView imagen;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            this.txtNombreviewHolder = (TextView) itemView.findViewById(R.id.tv1);
            this.txtMensajeviewHolder = (TextView) itemView.findViewById(R.id.tv3);
            //this.txtFechaviewHolder = (TextView) itemView.findViewById(R.id.tv2);
            this.txtHoraviewHolder = (TextView) itemView.findViewById(R.id.tvHora);
            //this.imagenviewHolder = (ImageView) itemView.findViewById(R.id.category);
            this.cardViewViewHolder = (CardView) itemView.findViewById(R.id.cv);
            this.mensajeBG = (RelativeLayout) itemView.findViewById(R.id.mensajeBG);
            this.imagen = (ImageView) itemView.findViewById(R.id.category);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Chats currentItem = (Chats) getItem(position);


            //DatabaseReference reference = getRef(position);
            //String nick=currentItem.getNick();
            //Query Id = db.orderByChild("_id").limitToLast(1);
            //Query misChats = reference.getRoot().child("Chat").child("receptor").equalTo(nick);//No sube al root. Ver....

/*
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();//BB.DD.
            DatabaseReference misChats = firebaseDatabase.getReference(MisReferencias.REFERENCIA_CHATS);
            Query query = misChats.child("receptor").equalTo(nick);*/

            /*boolean completed = !currentItem.isCompleted();
            currentItem.setCompleted(completed);
            Map<String, Object> updates = new HashMap<String, Object>();
            updates.put("completed", completed);
            reference.updateChildren(updates);*/
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            Chats currentItem = (Chats) getItem(position);
            DatabaseReference reference = getRef(position);
            //reference.removeValue();

            if (emisor.equals(currentItem.getEmailEmisor())) {//Si el mensaje seleccionado es del usuario le deja borrarlo...
                borrar(reference, view);
            }


            return true;
        }


        private void borrar(final DatabaseReference reference, final View view) {

            AlertDialog.Builder dialogEliminar = new AlertDialog.Builder(view.getContext());

            //dialogEliminar.setIcon(android.R.drawable.ic_dialog_alert);
            dialogEliminar.setIcon(R.drawable.imagen2);
            dialogEliminar.setTitle("Eliminar mensaje");
            dialogEliminar.setMessage("¿Deseas eliminar este mensaje del Chat?");
            dialogEliminar.setCancelable(false);

            dialogEliminar.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int boton) {

                            reference.removeValue();

                            Toast.makeText(view.getContext(),
                                    "Mensaje eliminado",
                                    Toast.LENGTH_SHORT).show();


                        }
                    });

            dialogEliminar.setNegativeButton(android.R.string.no, null);

            dialogEliminar.show();
        }
    }


}
