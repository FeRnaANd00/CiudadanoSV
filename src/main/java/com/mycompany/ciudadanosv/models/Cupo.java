package com.mycompany.ciudadanosv.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Bloque horario con capacidad en una sede para un trámite (tabla cupo). */
public class Cupo {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("hh:mm a", Locale.of("es", "SV"));

    private int idCupo;
    private int idSede;
    private int idTramite;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int capacidad;
    private int ocupados;
    private boolean habilitado;

    public Cupo() {
    }

    public int getDisponibles() {
        return Math.max(0, capacidad - ocupados);
    }

    public boolean isLleno() {
        return getDisponibles() == 0;
    }

    /** "07:00 a. m. – 09:00 a. m." */
    public String getHorarioTexto() {
        return horaInicio.format(HORA) + " – " + horaFin.format(HORA);
    }

    /** Porcentaje ocupado (para la barra). */
    public int getPorcentajeOcupado() {
        return capacidad == 0 ? 100 : Math.round(ocupados * 100f / capacidad);
    }

    public int getIdCupo() { return idCupo; }
    public void setIdCupo(int idCupo) { this.idCupo = idCupo; }

    public int getIdSede() { return idSede; }
    public void setIdSede(int idSede) { this.idSede = idSede; }

    public int getIdTramite() { return idTramite; }
    public void setIdTramite(int idTramite) { this.idTramite = idTramite; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public int getOcupados() { return ocupados; }
    public void setOcupados(int ocupados) { this.ocupados = ocupados; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}
