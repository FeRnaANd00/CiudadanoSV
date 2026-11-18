package com.mycompany.ciudadanosv.models;

import com.mycompany.ciudadanosv.util.Fechas;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/** Datos de "Mi perfil" (tabla usuario). Nombre y DUI no se editan: son datos registrales. */
public class Perfil {

    private int idUsuario;
    private String dui;
    private String nombreCompleto;
    private String correo;
    private String telefono;
    private String direccion;
    private Integer idMunicipio;
    private String municipioNombre;
    private String departamentoNombre;
    private Integer idDistrito;
    private String distritoNombre;
    private String estadoCivil;       // SOLTERO, CASADO, DIVORCIADO, VIUDO, ACOMPANADO
    private LocalDate fechaNacimiento;
    private int nivelVerificacion;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoCambioClave;

    public Perfil() {
    }

    public String getEstadoCivilTexto() {
        if (estadoCivil == null) {
            return "";
        }
        return switch (estadoCivil) {
            case "SOLTERO" -> "Soltero(a)";
            case "CASADO" -> "Casado(a)";
            case "DIVORCIADO" -> "Divorciado(a)";
            case "VIUDO" -> "Viudo(a)";
            case "ACOMPANADO" -> "Acompañado(a)";
            default -> estadoCivil;
        };
    }

    /** "Izalco, Sonsonate Este, Sonsonate" (lo que haya registrado). */
    public String getResidenciaTexto() {
        return Perfil.unir(distritoNombre, municipioNombre, departamentoNombre);
    }

    /** Une las partes no vacías con ", ". */
    static String unir(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p != null && !p.isBlank()) {
                sb.append(sb.length() > 0 ? ", " : "").append(p);
            }
        }
        return sb.toString();
    }

    /** "18/03/1991" para el campo con máscara. */
    public String getFechaNacimientoTexto() {
        return Fechas.formatear(fechaNacimiento);
    }

    /** Edad en años (0 si no hay fecha). */
    public int getEdad() {
        return fechaNacimiento == null ? 0 : Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public String getFechaRegistroTexto() {
        return Fechas.formatearFechaHora(fechaRegistro);
    }

    /** "Hoy", "Hace 5 días", "Hace 4 meses" o "Nunca". */
    public String getUltimoCambioClaveTexto() {
        if (ultimoCambioClave == null) {
            return "Nunca";
        }
        long dias = ChronoUnit.DAYS.between(ultimoCambioClave.toLocalDate(), LocalDate.now());
        if (dias == 0) {
            return "Hoy";
        }
        if (dias < 30) {
            return "Hace " + dias + (dias == 1 ? " día" : " días");
        }
        long meses = dias / 30;
        return "Hace " + meses + (meses == 1 ? " mes" : " meses");
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getDui() { return dui; }
    public void setDui(String dui) { this.dui = dui; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Integer getIdMunicipio() { return idMunicipio; }
    public void setIdMunicipio(Integer idMunicipio) { this.idMunicipio = idMunicipio; }

    public String getMunicipioNombre() { return municipioNombre; }
    public void setMunicipioNombre(String municipioNombre) { this.municipioNombre = municipioNombre; }

    public String getDepartamentoNombre() { return departamentoNombre; }
    public void setDepartamentoNombre(String departamentoNombre) { this.departamentoNombre = departamentoNombre; }

    public Integer getIdDistrito() { return idDistrito; }
    public void setIdDistrito(Integer idDistrito) { this.idDistrito = idDistrito; }

    public String getDistritoNombre() { return distritoNombre; }
    public void setDistritoNombre(String distritoNombre) { this.distritoNombre = distritoNombre; }

    public String getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(String estadoCivil) { this.estadoCivil = estadoCivil; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public int getNivelVerificacion() { return nivelVerificacion; }
    public void setNivelVerificacion(int nivelVerificacion) { this.nivelVerificacion = nivelVerificacion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getUltimoCambioClave() { return ultimoCambioClave; }
    public void setUltimoCambioClave(LocalDateTime ultimoCambioClave) { this.ultimoCambioClave = ultimoCambioClave; }
}
