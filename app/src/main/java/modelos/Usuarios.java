package modelos;

/**
 * Created by Usuario on 09/02/2017.
 */

public class Usuarios {

    private String nombre;
    private String nick;
    private String fecha;
    private String email;
    private boolean online;
    private String tokenNotify;
    private String image;
    private String chateaCon;
    private boolean escribe;
    private int timeBorrado;

    public final static boolean ONLINE = true;
    public final static boolean OFFLINE = false;

    public int getTimeBorrado() {
        return timeBorrado;
    }

    public void setTimeBorrado(int timeBorrado) {
        this.timeBorrado = timeBorrado;
    }

    public Usuarios() {
    }

    public Usuarios(String nombre, String nick, String fecha, String email, boolean online, String tokenNotify, String image, String chateaCon, boolean escribe) {
        this.nombre = nombre;
        this.nick = nick;
        this.fecha = fecha;
        this.email = email;
        this.online = online;
        this.tokenNotify = tokenNotify;
        this.image = image;
        this.chateaCon = chateaCon;
        this.escribe = escribe;
    }


    public Usuarios(String nombre, String nick, String fecha, String email, boolean online, String tokenNotify, String image, String chateaCon, boolean escribe, int timeBorrado) {
        this.nombre = nombre;
        this.nick = nick;
        this.fecha = fecha;
        this.email = email;
        this.online = online;
        this.tokenNotify = tokenNotify;
        this.image = image;
        this.chateaCon = chateaCon;
        this.escribe = escribe;
        this.timeBorrado = timeBorrado;
    }

    public boolean isEscribe() {
        return escribe;
    }

    public void setEscribe(boolean escribe) {
        this.escribe = escribe;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }



    public String getChateaCon() {
        return chateaCon;
    }

    public void setChateaCon(String chateaCon) {
        this.chateaCon = chateaCon;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTokenNotify() {
        return tokenNotify;
    }

    public void setTokenNotify(String tokenNotify) {
        this.tokenNotify = tokenNotify;
    }




    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
