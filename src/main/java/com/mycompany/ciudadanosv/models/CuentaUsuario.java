package com.mycompany.ciudadanosv.models;

import com.mycompany.ciudadanosv.util.Fechas;
import java.time.LocalDateTime;

/** Cuenta vista desde "Gestión de usuarios" (ciudadano o funcionario). */
public class CuentaUsuario {

    private int idUsuario;
    private String rol;
    private String dui;
    private String nombreCompleto;
    private String correo;
    private String telefono;
    private String estado;           // ACTIVO, SUSPENDIDO, EN_REVISION
    private Integer idInstitucion;
    private String institucionSiglas;
    private Integer idSede;
    private String sedeNombre;
    private String cargo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;
    private int numSolicitudes;
    // Datos personales (los edita la persona en "Mi perfil")
    private java.time.LocalDate fechaNacimiento;
    private String estadoCivil;
    private String direccion;
    private String municipioNombre;
    private String departamentoNombre;
    private String distritoNombre;

    public boolean isFuncionario() {
        return !"CIUDADANO".equals(rol);
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(rol);
    }

    public String getRolTexto() {
        Usuario u = new Usuario();
        u.setRol(rol);
        return u.getRolTexto();
    }

    public String getEstadoTexto() {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "ACTIVO" -> "Activo";
            case "SUSPENDIDO" -> "Suspendido";
            case "EN_REVISION" -> "En revisión";
            default -> estado;
        };
    }

    /** Iniciales para el avatar: "Douglas Morales" -> "DM". */
    public String getIniciales() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "?";
        }
        String[] p = nombreCompleto.trim().split("\\s+");
        return (p[0].substring(0, 1) + (p.length > 1 ? p[p.length > 2 ? 2 : 1].substring(0, 1) : "")).toUpperCase();
    }

    /** "18/03/1991 (35 años)" o "" si no la registró. */
    public String getFechaNacimientoTexto() {
        if (fechaNacimiento == null) {
            return "";
        }
        int edad = java.time.Period.between(fechaNacimiento, java.time.LocalDate.now()).getYears();
        return Fechas.formatear(fechaNacimiento) + " (" + edad + " años)";
    }

    public String getEstadoCivilTexto() {
        Perfil p = new Perfil();
        p.setEstadoCivil(estadoCivil);
        return p.getEstadoCivilTexto();
    }

    /** "Izalco, Sonsonate Este, Sonsonate". */
    public String getResidenciaTexto() {
        return Perfil.unir(distritoNombre, municipioNombre, departamentoNombre);
    }

    public String getFechaRegistroTexto() {
        return Fechas.formatearFechaHora(fechaRegistro);
    }

    public String getUltimoAccesoTexto() {
        return ultimoAcceso == null ? "Nunca" : Fechas.formatearFechaHora(ultimoAcceso);
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getDui() { return dui; }
    public void setDui(String dui) { this.dui = dui; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getIdInstitucion() { return idInstitucion; }
    public void setIdInstitucion(Integer idInstitucion) { this.idInstitucion = idInstitucion; }

    public String getInstitucionSiglas() { return institucionSiglas; }
    public void setInstitucionSiglas(String institucionSiglas) { this.institucionSiglas = institucionSiglas; }

    public Integer getIdSede() { return idSede; }
    public void setIdSede(Integer idSede) { this.idSede = idSede; }

    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String sedeNombre) { this.sedeNombre = sedeNombre; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public java.time.LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(java.time.LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(String estadoCivil) { this.estadoCivil = estadoCivil; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getMunicipioNombre() { return municipioNombre; }
    public void setMunicipioNombre(String municipioNombre) { this.municipioNombre = municipioNombre; }

    public String getDepartamentoNombre() { return departamentoNombre; }
    public void setDepartamentoNombre(String departamentoNombre) { this.departamentoNombre = departamentoNombre; }

    public String getDistritoNombre() { return distritoNombre; }
    public void setDistritoNombre(String distritoNombre) { this.distritoNombre = distritoNombre; }

    public int getNumSolicitudes() { return numSolicitudes; }
    public void setNumSolicitudes(int numSolicitudes) { this.numSolicitudes = numSolicitudes; }
}
