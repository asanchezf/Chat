package modelos;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Chats implements Serializable{
    private String emisor;
    private String fecha;
    private String mensaje;
    private String receptor;
    private String emailEmisor;
    @Exclude
    private boolean sentByMe;
    private long longFecha;
    @Exclude
    private boolean paraBorrar;
    private String imagen;
    private String rutaImagen;


    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Chats() {
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public Chats(String emisor, String fecha, String mensaje, String receptor, String emailEmisor, long longFecha, boolean paraBorrar, String imagen, String rutaImagen) {
        this.emisor = emisor;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.receptor = receptor;
        this.emailEmisor = emailEmisor;
        this.longFecha = longFecha;
        this.paraBorrar = paraBorrar;
        this.imagen = imagen;
        this.rutaImagen = rutaImagen;
    }

    public Chats(String emisor, String fecha, String mensaje, String receptor, String emailEmisor, long longFecha, boolean paraBorrar, String imagen) {
        this.emisor = emisor;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.receptor = receptor;
        this.emailEmisor = emailEmisor;
        this.longFecha = longFecha;
        this.paraBorrar = paraBorrar;
        this.imagen = imagen;
    }

    //SE UTILIZA CUANDO SE VA A CREAR UN MENSAJE PARA QUE SEA BORRADO AUTOMÁTICAMENTE POR EL SISTEMA
    public Chats(String emisor, String fecha, String mensaje, String receptor, String emailEmisor, long longFecha, boolean paraBorrar) {
        this.emisor = emisor;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.receptor = receptor;
        this.emailEmisor = emailEmisor;
        this.longFecha = longFecha;
        this.paraBorrar = paraBorrar;
    }
    //SE UTILIZA CUANDO SE VA A CREAR UN MENSAJE NORMAL
    public Chats(String emisor, String fecha, String mensaje, String receptor,String emailEmisor) {
        this.emisor = emisor;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.receptor = receptor;
        this.emailEmisor=emailEmisor;
    }

    public long getLongFecha() {
        return longFecha;
    }

    public void setLongFecha(long longFecha) {
        this.longFecha = longFecha;
    }

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

    public boolean isParaBorrar() {
        return paraBorrar;
    }

    public void setParaBorrar(boolean paraBorrar) {
        this.paraBorrar = paraBorrar;
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
