package adaptadores;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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


public class AdaptadorUsuariosViewHolder extends FirebaseRecyclerAdapter<Usuarios, AdaptadorUsuariosViewHolder.UsuariosViewHolder> {

    private static final String LOGTAG = "android-fcm";
    private int miTiempoConfiguradoEmisor;
    private String miurlCompartido = "";
    private Uri miImageUri = null;


    public AdaptadorUsuariosViewHolder(int modelLayout, Query ref, int tiempoConfiguradoEmisor) {
        super(Usuarios.class, modelLayout, UsuariosViewHolder.class, ref);
        miTiempoConfiguradoEmisor = tiempoConfiguradoEmisor;

    }


    //Constructor cuando nos traen una URL desde un navegador web como mensaje a enviar
    public AdaptadorUsuariosViewHolder(int modelLayout, Query ref, String urlCompartido, int tiempoConfiguradoEmisor) {
        super(Usuarios.class, modelLayout, UsuariosViewHolder.class, ref);
        miurlCompartido = urlCompartido;
        miTiempoConfiguradoEmisor = tiempoConfiguradoEmisor;

    }

    //Constructor cuando nos traen una imagen desde la Galería
    public AdaptadorUsuariosViewHolder(int modelLayout, Query ref, Uri imageUri, int tiempoConfiguradoEmisor) {
        super(Usuarios.class, modelLayout, UsuariosViewHolder.class, ref);
        miTiempoConfiguradoEmisor = tiempoConfiguradoEmisor;
        miImageUri = imageUri;
    }

    @Override
    public UsuariosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(mModelLayout, parent, false);
        return new UsuariosViewHolder(view);
    }

    @Override
    protected void populateViewHolder(UsuariosViewHolder viewHolder, final Usuarios model, int position) {
        String IMAGE_DEFAULT = "default_image";
        String emailUsuario = MisReferencias.USUARIO_CONECTADO;
        String emailColeccion = model.getEmail();

        //Necesitamos Contexto para Glide
        Context context = viewHolder.imageAvatar.getContext();

        //MUESTRA A TODOS LOS USUARIOS MENOS EL PROPIO.
        if (!emailUsuario.equals(emailColeccion)) {
            viewHolder.cardViewViewHolder.setVisibility(View.VISIBLE);
            viewHolder.txtNombreviewHolder.setText(model.getNick());
            viewHolder.txtNickviewHolder.setText(model.getEmail());
            viewHolder.txtFechaviewHolder.setText(model.getFecha());

            //Traemos las imágenes del perfil de cada uno de los usuarios.
            if (model.getImage().equals(IMAGE_DEFAULT)) {
                viewHolder.imageAvatar.setImageResource(R.drawable.nuevo);
            } else {
                //Informamos la imagen con Glide:
                Glide.with(context)
                        .load(Uri.parse(model.getImage()))
                        //.placeholder(R.drawable.image_susti)//Imagen de sustitución mientras carga la imagen final. Contiene transición fade.
                        .error(R.drawable.image5)//Imagen de sustitución si se ha producido error de carga
                        .override(81,81)//Tamaño aplicado a la imagen. Tamaño en px. cuidado con los tamaños de las pantallas de los dispositivos.
                        .centerCrop()//Escalado de imagen para llenar siempre los límites establecidos en diseño
                        //.skipMemoryCache(true)//Omitiría la memoria caché. Por defecto está activada.
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)//Gestión de la caché de disco.
                        .into(viewHolder.imageAvatar);//dónde vamos a mostrar las imágenes
            }

            //Actualizacmos el estado de cada uno de los usuarios...
            boolean estado = model.isOnline();
            if (estado) {
                viewHolder.txtEstadoviewHolder.setText(R.string.estado);
                viewHolder.txtEstadoviewHolder.setVisibility(View.VISIBLE);
              } else {
                viewHolder.txtEstadoviewHolder.setVisibility(View.INVISIBLE);
            }

        }

        //CUANDO ES EL USUARIO CONECTADO NO SE MUESTRA.
        else if (emailUsuario.equals(emailColeccion)){
            //NO SE MUESTRA LA INFORMACION DEL USUARIO CONECTADO
            viewHolder.cardViewViewHolder.setVisibility(View.GONE);
        }



        //EVENTO PARA MOSTRAR LA IMAGEN DE PERFIL EN OTRA ACTIVITY
        //final String post_key= String.valueOf(getRef(position));//Devuelve la ruta
        final String post_key = getRef(position).getKey();//Devuelve el nombre
        viewHolder.imageAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(),"click en imagen " +post_key,Toast.LENGTH_SHORT).show();

                openActivityPerfil(v, post_key);

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
            implements View.OnClickListener {
        //View.OnLongClickListener {//13 DE NOVIEMBRE SE DESECHA EL ONCLIK

        TextView txtNombreviewHolder;
        TextView txtNickviewHolder;
        TextView txtFechaviewHolder;
        TextView txtEstadoviewHolder;
        ImageView imageAvatar;
        CardView cardViewViewHolder;

        UsuariosViewHolder(View itemView) {
            super(itemView);


            this.txtNombreviewHolder = (TextView) itemView.findViewById(R.id.text1);
            this.txtNickviewHolder = (TextView) itemView.findViewById(R.id.text2);
            this.txtFechaviewHolder = (TextView) itemView.findViewById(R.id.text3);
            this.txtEstadoviewHolder = (TextView) itemView.findViewById(R.id.textEstado);
            this.cardViewViewHolder = (CardView) itemView.findViewById(R.id.cvUsuarios);
            this.imageAvatar = (ImageView) itemView.findViewById(R.id.imagenAvatar);


            itemView.setOnClickListener(this);
            //itemView.setOnLongClickListener(this);

        }

        void openActivity(View view, String receptor, String email, String tokenNotify, String imagenToolbarReceptor) {

            //ABRIMOS LA ACTIVITY NORMALMENTE
            Intent abrirChat = new Intent(view.getContext(), Activity_chats.class);
            //LO quitamos pq no queremos que se ejecute el on Destroy de ActivityUsuarios
           /* abrirChat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
            abrirChat.putExtra("parametro_receptor", receptor);
            abrirChat.putExtra("parametro_receptor_email", email);
            abrirChat.putExtra("parametro_receptor_token", tokenNotify);
            abrirChat.putExtra("parametro_receptor_urlcompartido", miurlCompartido);
            abrirChat.putExtra("parametro_emisor_tiempoBorrado", miTiempoConfiguradoEmisor);
            abrirChat.putExtra("parametro_receptor_imagen", miImageUri);
            abrirChat.putExtra("parametro_receptor_avatar", imagenToolbarReceptor);
           /* abrirChat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                       | Intent.FLAG_ACTIVITY_CLEAR_TASK);*/


            Log.i(LOGTAG, "Paso AdaptadorUsuariosViewHolder emisor_tiempoBorrado" + miTiempoConfiguradoEmisor);
            Log.i(LOGTAG, "Paso AdaptadorUsuariosViewHolder " + miImageUri);//LO trae
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

        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Usuarios currentItem = (Usuarios) getItem(position);
            DatabaseReference reference = getRef(position);
            String receptor = currentItem.getNick();
            String email = currentItem.getEmail();
            String tokenNotify = currentItem.getTokenNotify();
            String imagenToolbarReceptor = currentItem.getImage();


            openActivity(view, receptor, email, tokenNotify, imagenToolbarReceptor);

        }


      /*  @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            DatabaseReference reference = getRef(position);
            reference.removeValue();
            return true;
        }*/
    }
}