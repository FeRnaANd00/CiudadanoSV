package com.mycompany.ciudadanosv.models;

import com.mycompany.ciudadanosv.util.Fechas;
import java.time.LocalDateTime;

/** Solicitud o expediente de un ciudadano (tabla solicitud). */
public class Solicitud {

    private int idSolicitud;
    private String folio;
    private int idUsuario;
    private int idTramite;
    private String tramiteNombre;
    private String tramiteCodigo;
    private boolean requiereCita;
    private int idArea;
    private String areaNombre;
    private String areaIcono;
    private String institucionSiglas;
    private String institucionNombre;
    private String sedeNombre;
    private String revisorNombre;
    private String estado;
    private String observacionCiudadano;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public Solicitud() {
    }

    /** Texto de un estado (también lo usa el historial). */
    public static String textoEstado(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "BORRADOR" -> "Borrador";
            case "RECIBIDA" -> "Recibida";
            case "EN_REVISION" -> "En revisión";
            case "CITA_PROGRAMADA" -> "Cita programada";
            case "APROBADA" -> "Aprobada";
            case "RECHAZADA" -> "Rechazada";
            case "FINALIZADA" -> "Finalizada";
            default -> estado;
        };
    }

    /** Explicación corta de cada estado para la línea de tiempo. */
    public static String descripcionEstado(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "RECIBIDA" -> "La institución recibió tu solicitud.";
            case "EN_REVISION" -> "Un revisor está validando tus datos y documentos.";
            case "CITA_PROGRAMADA" -> "Debes presentarte a la cita asignada.";
            case "APROBADA" -> "Tu trámite fue aprobado y el documento está listo.";
            case "RECHAZADA" -> "La solicitud no pudo aprobarse. Revisa las observaciones.";
            case "FINALIZADA" -> "El trámite se completó y se entregó el documento.";
            default -> "";
        };
    }

    public String getEstadoTexto() {
        return textoEstado(estado);
    }

    /** Grupo para filtros y colores: PROCESO, APROBADA, RECHAZADA. */
    public String getGrupoEstado() {
        if ("APROBADA".equals(estado) || "FINALIZADA".equals(estado)) {
            return "APROBADA";
        }
        if ("RECHAZADA".equals(estado)) {
            return "RECHAZADA";
        }
        return "PROCESO";
    }

    /** Avance aproximado (0-100) para la barra de progreso. */
    public int getProgreso() {
        if (estado == null) {
            return 0;
        }
        return switch (estado) {
            case "RECIBIDA" -> 25;
            case "EN_REVISION" -> 50;
            case "CITA_PROGRAMADA" -> 75;
            case "APROBADA", "FINALIZADA", "RECHAZADA" -> 100;
            default -> 0;
        };
    }

    public String getFechaCreacionTexto() {
        return Fechas.formatearFechaHora(fechaCreacion);
    }

    public String getFechaActualizacionTexto() {
        return Fechas.formatearFechaHora(fechaActualizacion);
    }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdTramite() { return idTramite; }
    public void setIdTramite(int idTramite) { this.idTramite = idTramite; }

    public String getTramiteNombre() { return tramiteNombre; }
    public void setTramiteNombre(String tramiteNombre) { this.tramiteNombre = tramiteNombre; }

    public String getTramiteCodigo() { return tramiteCodigo; }
    public void setTramiteCodigo(String tramiteCodigo) { this.tramiteCodigo = tramiteCodigo; }

    public boolean isRequiereCita() { return requiereCita; }
    public void setRequiereCita(boolean requiereCita) { this.requiereCita = requiereCita; }

    public int getIdArea() { return idArea; }
    public void setIdArea(int idArea) { this.idArea = idArea; }

    public String getAreaNombre() { return areaNombre; }
    public void setAreaNombre(String areaNombre) { this.areaNombre = areaNombre; }

    public String getAreaIcono() { return areaIcono; }
    public void setAreaIcono(String areaIcono) { this.areaIcono = areaIcono; }

    public String getInstitucionSiglas() { return institucionSiglas; }
    public void setInstitucionSiglas(String institucionSiglas) { this.institucionSiglas = institucionSiglas; }

    public String getInstitucionNombre() { return institucionNombre; }
    public void setInstitucionNombre(String institucionNombre) { this.institucionNombre = institucionNombre; }

    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String sedeNombre) { this.sedeNombre = sedeNombre; }

    public String getRevisorNombre() { return revisorNombre; }
    public void setRevisorNombre(String revisorNombre) { this.revisorNombre = revisorNombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacionCiudadano() { return observacionCiudadano; }
    public void setObservacionCiudadano(String observacionCiudadano) { this.observacionCiudadano = observacionCiudadano; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
