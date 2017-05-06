package adaptadores;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.antonio.chat.Activity_Perfiles;
import com.example.antonio.chat.Activity_chats;
import com.example.antonio.chat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import modelos.Usuarios;
import referencias.MisReferencias;


/**
 * Created by Usuario on 09/02/2017.
 */

public class AdaptadorUsuariosViewHolder extends FirebaseRecyclerAdapter<Usuarios, AdaptadorUsuariosViewHolder.UsuariosViewHolder> {

    private boolean pintar = true;
    //private static String  IMAGE_DEFAULT="default_image";


    public AdaptadorUsuariosViewHolder(int modelLayout, Query ref) {
        super(Usuarios.class, modelLayout, UsuariosViewHolder.class, ref);

    }

    @Override
    public UsuariosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(mModelLayout, parent, false);

        return new UsuariosViewHolder(view);
    }

    @Override
    protected void populateViewHolder(UsuariosViewHolder viewHolder, final Usuarios model, int position) {
         String  IMAGE_DEFAULT="default_image";

        String emailUsuario = MisReferencias.USUARIO_CONECTADO;
        String emailColeccion = model.getEmail();
        String nick=model.getNick();

        ViewGroup view = (ViewGroup) viewHolder.itemView;
        Resources res = view.getResources();

        //Necesitamos Contexto para Glide
        Context context=viewHolder.imageAvatar.getContext();

            //MUESTRA A TODOS LOS USUARIOS MENOS EL PROPIO. y que no sean nulos==ZUsuario Darío
            if (!emailUsuario.equals(emailColeccion)&& nick !=null ) {
                //viewHolder.txtNombreviewHolder.setText(String.format("Nick: %s", model.getNick()));
                viewHolder.txtNombreviewHolder.setText(model.getNick());
                //viewHolder.txtNickviewHolder.setText(String.format("Email: %s", model.getEmail()));
                viewHolder.txtNickviewHolder.setText(String.format(model.getEmail()));
                //viewHolder.txtFechaviewHolder.setText(String.format("Fecha de alta: %s", model.getFecha()));
                viewHolder.txtFechaviewHolder.setText(String.format(model.getFecha()));
                //viewHolder.imageAvatar.setImageURI(Uri.parse(model.getImage()));

                if(model.getImage().equals(IMAGE_DEFAULT)){

                    viewHolder.imageAvatar.setImageResource(R.drawable.nuevo);
                }
                else{
                //Informamos la imagen con Glide:
                Glide.with(context)
                        //.load("http://petty.hol.es/CasaRozas/"+model.getItem(position).getImagen())//Desde dónde cargamos las imágenes
                        .load(Uri.parse(model.getImage()))
                        //.placeholder(R.drawable.image_susti)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                        .error(R.drawable.image5)//Imagen de sustitución si se ha producido error de carga
                        //.override(600,400)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                        .centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                        //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                        .into(viewHolder.imageAvatar);//dónde vamos a mostrar las imágenes
                }


                boolean estado = model.isOnline();
                if (estado) {
                    viewHolder.txtEstadoviewHolder.setText(R.string.estado);
                    viewHolder.txtEstadoviewHolder.setVisibility(View.VISIBLE);
                }/*else if(!estado){
                viewHolder.txtEstadoviewHolder.setText("No conectado");
                viewHolder.txtEstadoviewHolder.setTextColor(res.getColor(R.color.md_red_400));
                viewHolder.txtEstadoviewHolder.setVisibility(View.VISIBLE);
            }*/

            } else {

                pintar = false;
                viewHolder.cardViewViewHolder.setVisibility(View.GONE);

            }

        //final String post_key= String.valueOf(getRef(position));//Devuelve la ruta
        final String post_key= getRef(position).getKey();//Devuelve el nombre
        viewHolder.imageAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(),"click en imagen " +post_key,Toast.LENGTH_SHORT).show();



                //Toast.makeText(v.getContext(),"click en imagen " +post_key,Toast.LENGTH_SHORT).show();

                openActivityPerfil(v,post_key);


            }
        });
    }

    private void openActivityPerfil(View v, String usuario) {

        Intent abrirPefil = new Intent(v.getContext(), Activity_Perfiles.class);

        abrirPefil.putExtra("parametro_db", usuario);
        abrirPefil.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        v.getContext().startActivity(abrirPefil);

    }


    class UsuariosViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
            View.OnLongClickListener {


        TextView txtNombreviewHolder;
        TextView txtNickviewHolder;
        TextView txtFechaviewHolder;
        TextView txtEstadoviewHolder;
        ImageView imageAvatar;
        CardView cardViewViewHolder;

         UsuariosViewHolder(View itemView) {
            super(itemView);


            if (pintar = true) {
                this.txtNombreviewHolder = (TextView) itemView.findViewById(R.id.text1);
                this.txtNickviewHolder = (TextView) itemView.findViewById(R.id.text2);
                this.txtFechaviewHolder = (TextView) itemView.findViewById(R.id.text3);
                this.txtEstadoviewHolder = (TextView) itemView.findViewById(R.id.textEstado);
                this.cardViewViewHolder = (CardView) itemView.findViewById(R.id.cvUsuarios);
                this.imageAvatar = (ImageView) itemView.findViewById(R.id.imagenAvatar);
                    /*this.cardViewViewHolder.setCardBackgroundColor(R.drawable.degradado);
                    this.cardViewViewHolder.setBackgroundColor(R.drawable.degradado);
                    this.cardViewViewHolder.setBackground(R.drawable.degradado);*/
            }

            itemView.setOnClickListener(this);
            //itemView.setOnLongClickListener(this);

        }

        public void openActivity(View view, String receptor, String email,String tokenNotify) {

            //ABRIMOS LA ACTIVITY NORMALMENTE
            Intent abrirChat = new Intent(view.getContext(), Activity_chats.class);
            //LO quitamos pq no queremos que se ejecute el on Destroy de ActivityUsuarios
            abrirChat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            abrirChat.putExtra("parametro_receptor", receptor);
            abrirChat.putExtra("parametro_receptor_email", email);
            abrirChat.putExtra("parametro_receptor_token", tokenNotify);
            view.getContext().startActivity(abrirChat);


    /*        Intent abrirChat = new Intent(view.getContext(), Activity_chats.class);
            String action = abrirChat.getAction();
            Uri data = abrirChat.getData();


            //VER::::::::
            //LO quitamos pq no queremos que se ejecute el on Destroy de ActivityUsuarios
            *//*abrirChat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);*//*

            abrirChat.putExtra("parametro_receptor", receptor);
            abrirChat.putExtra("parametro_receptor_email", email);
            abrirChat.putExtra("parametro_receptor_token", tokenNotify);
            view.getContext().startActivity(abrirChat);*/





            //ABRIMOS A RAIZ DE LA LLAMDA DESDE OTRA APP:
           /* String actionName= "recibir.action.chat.usuarios";
            Intent intent = new Intent(actionName);
            intent.putExtra("parametro_receptor", receptor);
            intent.putExtra("parametro_receptor_email", email);
            intent.putExtra("parametro_receptor_text", text);
            view.getContext().startActivity(intent);*/

        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Usuarios currentItem = (Usuarios) getItem(position);
            DatabaseReference reference = getRef(position);
            String receptor = currentItem.getNick();
            String email = currentItem.getEmail();
            String tokenNotify=currentItem.getTokenNotify();
            //Query Id = db.orderByChild("_id").limitToLast(1);
            //Query misChats = reference.getRoot().child("Chat").child("receptor").equalTo(nick);//No sube al root. Ver....


            /*FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();//BB.DD.
            DatabaseReference misChats = firebaseDatabase.getReference(MisReferencias.REFERENCIA_CHATS);
            Query query = misChats.child("receptor").equalTo(receptor);*/


            openActivity(view, receptor, email,tokenNotify);


            //Toast.makeText(this,"Sesión cerrada", Toast.LENGTH_SHORT).show();
           /* Intent intent = new Intent(UsuariosViewHolder.this, Activity_chats.class);
            startActivity(intent);*/





            /*boolean completed = !currentItem.isCompleted();
            currentItem.setCompleted(completed);
            Map<String, Object> updates = new HashMap<String, Object>();
            updates.put("completed", completed);
            reference.updateChildren(updates);*/
        }


        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            DatabaseReference reference = getRef(position);
            reference.removeValue();
            return true;
        }
    }


}
