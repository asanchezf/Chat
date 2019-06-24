package referencias;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import modelos.Usuarios;


public class FirebaseHelper {
    //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    private DatabaseReference dataReference;
    private static FirebaseDatabase mData;

//private FirebaseDatabase dataReference;

    private final static String SEPARATOR = "___";
    private final static String CHATS_PATH = "chats";
    private final static String USERS_PATH = "users";
    private final static String CONTACTS_PATH = "contacts";

    final private static String REFERENCIA_CHATS = "Chat";
    final private static String REFERENCIA_USUARIOS = "Usuarios";

    private static class SingletonHolder {
        private static final FirebaseHelper INSTANCE = new FirebaseHelper();
    }

    public static FirebaseHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private FirebaseHelper(){

        dataReference = FirebaseDatabase.getInstance().getReference();//Correcto ANTES

        //dataReference=FirebaseHelper.getDatabasePersistence().getReference();//Ver. Utilizando la persistencia
   }
    public  static FirebaseDatabase getDatabasePersistence(){


        if (mData == null) {

            mData = FirebaseDatabase.getInstance();
            mData.setPersistenceEnabled(true);
        }
        return mData;

    }

    public int getDevuelveTiempoBorrado(String usuarioChat){
        /*DatabaseReference database1;
        DatabaseReference db;
        database1 = FirebaseDatabase.getInstance().getReference();
        db = database1.getRoot().child(REFERENCIA_USUARIOS).child(usuario);*/
        final int[] tiempoConfiguradoReceptor = new int[1];
        dataReference = FirebaseDatabase.getInstance().getReference().child(REFERENCIA_USUARIOS).child(usuarioChat);
        dataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                   // Toast.makeText(Activity_chats.this, "NO hay datos...", Toast.LENGTH_SHORT).show();

                }

                Usuarios usuarios = dataSnapshot.getValue(Usuarios.class);
                /*timeBorrado[0] =usuarios.getTimeBorrado();
                tiempoConfiguradoReceptor= timeBorrado[0];*/
                 tiempoConfiguradoReceptor[0] = usuarios.getTimeBorrado();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return tiempoConfiguradoReceptor[0];
    }

    public DatabaseReference getDataReference() {
        return dataReference;
    }

    public String getAuthUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = null;
        if (user != null) {
            email = user.getEmail();
        }
        return email;
    }

    private DatabaseReference getUserReference(String email){
        DatabaseReference userReference = null;
        if (email != null) {
            String emailKey = email.replace(".", "_");
            userReference = dataReference.getRoot().child(USERS_PATH).child(emailKey);
        }
        return userReference;
    }

    private DatabaseReference getMyUserReference() {
        return getUserReference(getAuthUserEmail());
    }

    private DatabaseReference getContactsReference(String email){
        return getUserReference(email).child(CONTACTS_PATH);
    }

    private DatabaseReference getMyContactsReference(){
        return getContactsReference(getAuthUserEmail());
    }

    private DatabaseReference getOneContactReference(String mainEmail, String childEmail){
        String childKey = childEmail.replace(".","_");
        return getUserReference(mainEmail).child(CONTACTS_PATH).child(childKey);
    }

    public DatabaseReference getChatsReference(String nick_usuario, String receiver){
        //String keySender = getAuthUserEmail().replace(".","_");
        //String keyReceiver = receiver.replace(".","_");

        String keyChat = nick_usuario + SEPARATOR + receiver;
        if (nick_usuario.compareTo(receiver) > 0) {
            keyChat = receiver + SEPARATOR + nick_usuario;
        }
        return dataReference.getRoot().child(REFERENCIA_CHATS).child(keyChat);
    }


    public DatabaseReference getDatosReceptor(String receptor){

        //String keyReceptor = receptor;

        return dataReference.getRoot().child(REFERENCIA_USUARIOS).child(receptor);
    }


    public DatabaseReference getDatosEmisor(String emisor){

       return dataReference.getRoot().child(REFERENCIA_USUARIOS).child(emisor);
    }


    public void changeUserConnectionStatus(boolean online) {
        if (getMyUserReference() != null) {
            Map<String, Object> updates = new HashMap<String, Object>();
            updates.put("online", online);
            getMyUserReference().updateChildren(updates);

            notifyContactsOfConnectionChange(online);
        }
    }

    private void notifyContactsOfConnectionChange(final boolean online, final boolean signoff) {
        final String myEmail = getAuthUserEmail();
        getMyContactsReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String email = child.getKey();
                    DatabaseReference reference = getOneContactReference(email, myEmail);
                    reference.setValue(online);
                }
                if (signoff){
                    FirebaseAuth.getInstance().signOut();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    private void notifyContactsOfConnectionChange(boolean online) {
        notifyContactsOfConnectionChange(online, false);
    }

    /*public void signOff(){
        notifyContactsOfConnectionChange(User.OFFLINE, true);
    }*/
}
