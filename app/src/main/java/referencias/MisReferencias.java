package referencias;

public class MisReferencias {
//FIREBASE ES SINGLETON POR LO QUE NO EXTENDEMOS DE APPLICATION

    //Referencias para la primera versión:
    final public static String REFERENCIA_CONVERSACIONES = "Conversaciones";
    final public static String REFERENCIA_USUARIOS = "Usuarios";
    final public static String REFERENCIA_CHATS = "Chat";
    public static String USUARIO_CONECTADO="";
    //Recoger imágenes de la galería
    final public static int GALLERY_REQUEST = 1001;
    final public static String SERVER_KEY="AIzaSyAfidbHtEXmIvuUOrg1e0_Suotnsh3w09g";
    final public static String FCM_PUSH_URL="https://fcm.googleapis.com/fcm/send";
    //final public static String PUSH_URL_POST="http://petty.hol.es/WebServicesPush/EnviarNotificacionDesdePHP.php";
    final public static String PUSH_URL_POST="http://antonymail62.000webhostapp.com/WebServicesPush/EnviarNotificacionDesdePHP.php";
    final public static String PUSH_URL="http://petty.hol.es/WebServicesPush/notify.php";
    //URL del icono que hemos puesto en Firebase para cuando el usuario no ha cambiado el icono de su perfil
    final public static String URL_MI_ICONO_DEFAULT_FIREBASE="https://firebasestorage.googleapis.com/v0/b/chat-8addb.appspot.com/o/Images%2FAvatar%2Fdefault.png?alt=media&token=932dc535-3998-45c9-97e7-60be8e53dfb7";


    //Nuevas referencias a utilizar:
    final public static String SEPARATOR = "___";
    final public static String CHATS_PATH = "mischats";
    final public static String USERS_PATH = "misusers";
    final public static String CONTACTS_PATH = "miscontacts";



}
