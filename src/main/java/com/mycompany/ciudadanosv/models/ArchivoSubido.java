package com.mycompany.ciudadanosv.models;

/** Archivo recibido en un formulario, ya validado, listo para guardarse. */
public class ArchivoSubido {

    private final int idRequisito;
    private final String nombreOriginal;
    private final String tipoMime;      // detectado por el contenido, no por la extensión
    private final byte[] contenido;

    public ArchivoSubido(int idRequisito, String nombreOriginal, String tipoMime, byte[] contenido) {
        this.idRequisito = idRequisito;
        this.nombreOriginal = nombreOriginal;
        this.tipoMime = tipoMime;
        this.contenido = contenido;
    }

    public int getIdRequisito() { return idRequisito; }
    public String getNombreOriginal() { return nombreOriginal; }
    public String getTipoMime() { return tipoMime; }
    public byte[] getContenido() { return contenido; }

    public int getTamanoKb() {
        return (int) Math.max(1, Math.round(contenido.length / 1024.0));
    }
}
