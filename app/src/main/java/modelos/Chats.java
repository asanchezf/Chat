package modelos;

import com.google.firebase.database.Exclude;

/**
 * Created by Usuario on 09/02/2017.
 */

public class Chats {


    private String emisor;
    private String fecha;
    private String mensaje;
    private String receptor;
    private String emailEmisor;
    @Exclude
    private boolean sentByMe;

    public boolean isSentByMe() {
        return sentByMe;
    }

    public void setSentByMe(boolean sentByMe) {
        this.sentByMe = sentByMe;
    }

    public String getEmailEmisor() {
        return emailEmisor;
    }

    public void setEmailEmisor(String emailEmisor) {
        this.emailEmisor = emailEmisor;
    }

    public Chats() {

    }

    public Chats(String emisor, String fecha, String mensaje, String receptor,String emailEmisor) {
        this.emisor = emisor;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.receptor = receptor;
        this.emailEmisor=emailEmisor;
    }

    public String getEmisor() {
        return emisor;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getReceptor() {
        return receptor;
    }

    public void setReceptor(String receptor) {
        this.receptor = receptor;
    }
}
