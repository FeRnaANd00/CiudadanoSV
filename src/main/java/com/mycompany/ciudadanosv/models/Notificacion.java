package com.mycompany.ciudadanosv.models;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Aviso de la bandeja del ciudadano (tabla notificacion). */
public class Notificacion {

    private static final Locale ES = Locale.of("es", "SV");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("hh:mm a", ES);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d MMM uuuu", ES);

    private int idNotificacion;
    private String tipo;            // TRAMITE, CITA, SEGURIDAD, SISTEMA
    private String titulo;
    private String mensaje;
    private Integer idSolicitud;
    private Integer idCita;
    private String folioSolicitud;  // para el enlace "Ver trámite"
    private boolean leida;
    private boolean archivada;
    private LocalDateTime fecha;

    /** Icono Material según el tipo. */
    public String getIcono() {
        if (tipo == null) {
            return "notifications";
        }
        return switch (tipo) {
            case "TRAMITE" -> "folder_open";
            case "CITA" -> "event_upcoming";
            case "SEGURIDAD" -> "shield_lock";
            default -> "campaign";
        };
    }

    /** "Hace 5 min", "Hace 2 h", "Ayer, 04:45 p. m." o "14 nov 2024". */
    public String getCuandoTexto() {
        if (fecha == null) {
            return "";
        }
        LocalDateTime ahora = LocalDateTime.now();
        long minutos = Duration.between(fecha, ahora).toMinutes();
        if (minutos < 1) {
            return "Hace un momento";
        }
        if (minutos < 60) {
            return "Hace " + minutos + " min";
        }
        LocalDate dia = fecha.toLocalDate();
        if (dia.equals(ahora.toLocalDate())) {
            return minutos < 6 * 60 ? "Hace " + (minutos / 60) + " h" : "Hoy, " + fecha.format(HORA);
        }
        if (dia.equals(ahora.toLocalDate().minusDays(1))) {
            return "Ayer, " + fecha.format(HORA);
        }
        return fecha.format(FECHA).replace(".", "");
    }

    /** Hay una página a la que llevar al ciudadano. */
    public boolean isConEnlace() {
        return folioSolicitud != null || idCita != null;
    }

    public String getTextoEnlace() {
        return folioSolicitud != null ? "Ver trámite" : (idCita != null ? "Ver mis citas" : "");
    }

    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public Integer getIdCita() { return idCita; }
    public void setIdCita(Integer idCita) { this.idCita = idCita; }

    public String getFolioSolicitud() { return folioSolicitud; }
    public void setFolioSolicitud(String folioSolicitud) { this.folioSolicitud = folioSolicitud; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public boolean isArchivada() { return archivada; }
    public void setArchivada(boolean archivada) { this.archivada = archivada; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
