package adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.antonio.chat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import modelos.Chats;
import referencias.MisReferencias;

/**
 * Created by Usuario on 09/02/2017.
 */

public class AdaptadorChatsViewHolder extends FirebaseRecyclerAdapter<Chats, AdaptadorChatsViewHolder.ChatsViewHolder> {

    String emisor = MisReferencias.USUARIO_CONECTADO;

    public AdaptadorChatsViewHolder(int modelLayout, Query ref) {
        super(Chats.class, modelLayout, ChatsViewHolder.class, ref);

    }


    @Override
    public ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return super.onCreateViewHolder(parent, viewType);
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(mModelLayout, parent, false);
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
        long restoHoras=diffHoras % 60;
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
            return String.valueOf(inicio.substring(0,10));

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

    @Override
    protected void populateViewHolder(ChatsViewHolder viewHolder, Chats model, int position) {

        Context context;
        viewHolder.txtNombreviewHolder.setText(model.getEmisor());
        viewHolder.txtMensajeviewHolder.setText(model.getMensaje());
        //viewHolder.txtFechaviewHolder.setText(mifecha);

        //viewHolder.txtFechaviewHolder.setText(model.getFecha());

        //diferenciaFechas(model.getFecha(),dameFechaHoraActual())
        viewHolder.txtHoraviewHolder.setText(diferenciaFechas(model.getFecha(), dameFechaHoraActual()));

        //No se muestra el receptor...
        context = viewHolder.cardViewViewHolder.getContext();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.cardViewViewHolder.getLayoutParams();


        int milefMargin = 5;//incremento del margen izquierdo del cardview...
        int color = fetchColor(R.attr.cardBackgroundColor, context);//la Activity tiene style propio BocadillosChat
        //int anchoCard=viewHolder.cardViewViewHolder.getHeight();//No devuelve el dato pq aún no está construido el cardview

        viewHolder.imagenviewHolder.setImageResource(R.drawable.ic_chat);
        //int color=Resources.getSystem().getColor(R.color.md_blue_50);
        /*if (!model.isSentByMe()) {
            //gravity = Gravity.RIGHT;
            //color = fetchColor(R.attr.colorAccent);
            background=R.attr.colorAccent;
            milefMargin=300;
        }*/
        // Obtener la densidad, el alto y el ancho de pantalla
        Resources res =context.getResources();
        int densidad = res.getDisplayMetrics().densityDpi;
        int heightPixels=res.getDisplayMetrics().heightPixels;
        int widthPixels=res.getDisplayMetrics().widthPixels;


        //String emisor=MisReferencias.USUARIO_CONECTADO;
        String receptor = model.getEmailEmisor();

        if (emisor.equals(receptor)) {//SE TRATA DE UN MENSAJE PROPIO DEL USUARIO CONECTADO

            viewHolder.imagenviewHolder.setImageResource(R.drawable.imagen2);
            color = fetchColor(R.attr.colorAccent, context);//la Activity tiene style propio BocadillosChat. Es el color Accent del tema propio

            //TENEMOS EN CUENTA LAS DENSIDADES DE LAS PANTALLAS
            if(densidad>=420 && densidad<480) {//Nexus 5X.Densidad xxhdpi
                milefMargin = 475;//Máximo para Nexus 5X con 420dp de pantalla y cuando el cardview tenga 220dp de ancho
                //milefMargin = 245;//Máximo para el alpha con 320dp de pantalla y cuando el cardview tiene 220dp de ancho
               }
               else if(densidad>=480){
                milefMargin = 415;//Máximo para Pixel,Alcatel de Darío y cualquiera con mayor densidad

            }
            else if(densidad>240&& densidad<420 ){//Alpha. Densidad xhdpi
                milefMargin = 280;//Máximo para el alpha con 320dp de pantalla y cuando el cardview tiene 220dp de ancho
            }
            else {

                //Xperia
                if(widthPixels<500){
                    milefMargin = 205;//Máximo para Xperia E3 con 420dp de pantalla y cuando el cardview tenga 160dp de ancho
                }
                else {//S4mini
                    milefMargin = 250;
                }



            }


        }
        /*else{
            milefMargin+=10;}*/

        //SE QUITA EL COLOR DE FONDO
        viewHolder.cardViewViewHolder.setCardBackgroundColor(color);

        //viewHolder.cardViewViewHolder.setCardBackgroundColor(Resources.getSystem().getColor(R.color.md_black_1000_20));


        //params.gravity = gravity;
        params.leftMargin = milefMargin;
        //params.width=220;
        //params.gravity=gravity;
        //params.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        viewHolder.cardViewViewHolder.setLayoutParams(params);


    }


    private int fetchColor(int color, Context context) {


        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data,
                new int[]{color});
        int returnColor = a.getColor(0, 0);
        a.recycle();
        return returnColor;
    }

    class ChatsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
            View.OnLongClickListener {

        TextView txtNombreviewHolder;
        TextView txtMensajeviewHolder;
        //TextView txtFechaviewHolder;
        TextView txtHoraviewHolder;
        ImageView imagenviewHolder;
        CardView cardViewViewHolder;


        public ChatsViewHolder(View itemView) {
            super(itemView);

            this.txtNombreviewHolder = (TextView) itemView.findViewById(R.id.tv1);
            this.txtMensajeviewHolder = (TextView) itemView.findViewById(R.id.tv3);
            //this.txtFechaviewHolder = (TextView) itemView.findViewById(R.id.tv2);
            this.txtHoraviewHolder = (TextView) itemView.findViewById(R.id.tvHora);
            this.imagenviewHolder = (ImageView) itemView.findViewById(R.id.category);
            this.cardViewViewHolder = (CardView) itemView.findViewById(R.id.cv);


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
