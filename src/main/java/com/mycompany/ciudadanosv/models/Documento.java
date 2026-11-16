package com.mycompany.ciudadanosv.models;

/** Archivo adjunto a una solicitud (tabla documento). */
public class Documento {

    private int idDocumento;
    private int idSolicitud;
    private String nombreArchivo;
    private String tipoMime;
    private int tamanoKb;
    private String estadoCotejo;     // PENDIENTE, VALIDO, RECHAZADO
    private String requisitoNombre;
    private String rutaArchivo;      // relativa a la carpeta de archivos
    private int idUsuarioDueno;      // ciudadano dueño de la solicitud

    public Documento() {
    }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public int getIdUsuarioDueno() { return idUsuarioDueno; }
    public void setIdUsuarioDueno(int idUsuarioDueno) { this.idUsuarioDueno = idUsuarioDueno; }

    /** "1.2 MB" o "940 KB". */
    public String getTamanoTexto() {
        return tamanoKb >= 1024
                ? String.format(java.util.Locale.US, "%.1f MB", tamanoKb / 1024.0)
                : tamanoKb + " KB";
    }

    public boolean isPdf() {
        return "application/pdf".equals(tipoMime);
    }

    public String getEstadoCotejoTexto() {
        if (estadoCotejo == null) {
            return "";
        }
        return switch (estadoCotejo) {
            case "VALIDO" -> "Cotejado válido";
            case "RECHAZADO" -> "Rechazado";
            default -> "Pendiente de cotejo";
        };
    }

    public int getIdDocumento() { return idDocumento; }
    public void setIdDocumento(int idDocumento) { this.idDocumento = idDocumento; }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getTipoMime() { return tipoMime; }
    public void setTipoMime(String tipoMime) { this.tipoMime = tipoMime; }

    public int getTamanoKb() { return tamanoKb; }
    public void setTamanoKb(int tamanoKb) { this.tamanoKb = tamanoKb; }

    public String getEstadoCotejo() { return estadoCotejo; }
    public void setEstadoCotejo(String estadoCotejo) { this.estadoCotejo = estadoCotejo; }

    public String getRequisitoNombre() { return requisitoNombre; }
    public void setRequisitoNombre(String requisitoNombre) { this.requisitoNombre = requisitoNombre; }
}
