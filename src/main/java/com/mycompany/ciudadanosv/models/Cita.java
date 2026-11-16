package com.mycompany.ciudadanosv.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Cita presencial de un ciudadano (tabla cita). */
public class Cita {

    private static final Locale ES = Locale.of("es", "SV");

    private int idCita;
    private String codigo;
    private int idUsuario;
    private int idCupo;
    private LocalDateTime fechaHora;
    private int numeroTurno;
    private String ventanilla;
    private String estado;        // PROGRAMADA, EN_ATENCION, ATENDIDA, CANCELADA, NO_ASISTIO
    private String tramiteNombre;
    private String sedeNombre;
    private String sedeDireccion;
    private String institucionSiglas;

    public Cita() {
    }

    /** Programada y todavía no llega la hora. */
    public boolean isProxima() {
        return "PROGRAMADA".equals(estado) && fechaHora != null && fechaHora.isAfter(LocalDateTime.now());
    }

    /** Se puede cancelar hasta 2 horas antes. */
    public boolean isCancelable() {
        return isProxima() && fechaHora.isAfter(LocalDateTime.now().plusHours(2));
    }

    /** "Hoy", "Mañana" o "En 5 días". */
    public String getCuandoTexto() {
        if (fechaHora == null) {
            return "";
        }
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaHora.toLocalDate());
        if (dias == 0) {
            return "Hoy";
        }
        if (dias == 1) {
            return "Mañana";
        }
        return dias > 1 ? "En " + dias + " días" : "";
    }

    /** "lunes, 17 de noviembre de 2026" */
    public String getFechaTexto() {
        return fechaHora == null ? "" : fechaHora.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' uuuu", ES));
    }

    /** "10:24 a. m." */
    public String getHoraTexto() {
        return fechaHora == null ? "" : fechaHora.format(DateTimeFormatter.ofPattern("hh:mm a", ES));
    }

    public String getDiaNumero() {
        return fechaHora == null ? "" : String.valueOf(fechaHora.getDayOfMonth());
    }

    public String getMesCorto() {
        return fechaHora == null ? "" : fechaHora.format(DateTimeFormatter.ofPattern("MMM", ES)).replace(".", "").toUpperCase(ES);
    }

    public String getEstadoTexto() {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "PROGRAMADA" -> isProxima() ? "Programada" : "Vencida";
            case "EN_ATENCION" -> "En atención";
            case "ATENDIDA" -> "Atendida";
            case "CANCELADA" -> "Cancelada";
            case "NO_ASISTIO" -> "No asistió";
            default -> estado;
        };
    }

    public int getIdCita() { return idCita; }
    public void setIdCita(int idCita) { this.idCita = idCita; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdCupo() { return idCupo; }
    public void setIdCupo(int idCupo) { this.idCupo = idCupo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public int getNumeroTurno() { return numeroTurno; }
    public void setNumeroTurno(int numeroTurno) { this.numeroTurno = numeroTurno; }

    public String getVentanilla() { return ventanilla; }
    public void setVentanilla(String ventanilla) { this.ventanilla = ventanilla; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTramiteNombre() { return tramiteNombre; }
    public void setTramiteNombre(String tramiteNombre) { this.tramiteNombre = tramiteNombre; }

    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String sedeNombre) { this.sedeNombre = sedeNombre; }

    public String getSedeDireccion() { return sedeDireccion; }
    public void setSedeDireccion(String sedeDireccion) { this.sedeDireccion = sedeDireccion; }

    public String getInstitucionSiglas() { return institucionSiglas; }
    public void setInstitucionSiglas(String institucionSiglas) { this.institucionSiglas = institucionSiglas; }
}
