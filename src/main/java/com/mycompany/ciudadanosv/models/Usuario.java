package com.mycompany.ciudadanosv.models;

/**
 * Usuario del sistema (ciudadano o funcionario).
 * Nunca guarda la contraseña: este objeto es el que va en la sesión.
 */
public class Usuario {

    private int idUsuario;
    private int idRol;
    private String rol;            // CIUDADANO, REVISOR, OPERADOR, SUPER_ADMIN
    private String dui;
    private String nombreCompleto;
    private String correo;
    private String telefono;
    private String estado;

    public Usuario() {
    }

    /** Primer nombre, para saludar: "Hola, Carlos". */
    public String getPrimerNombre() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "";
        }
        return nombreCompleto.trim().split("\\s+")[0];
    }

    public boolean isCiudadano() {
        return "CIUDADANO".equals(rol);
    }

    /** Revisor, operador o super admin: puede entrar a /admin. */
    public boolean isFuncionario() {
        return "REVISOR".equals(rol) || "OPERADOR".equals(rol) || "SUPER_ADMIN".equals(rol);
    }

    /** Nombre del rol para mostrar en pantalla. */
    public String getRolTexto() {
        if (rol == null) {
            return "";
        }
        return switch (rol) {
            case "CIUDADANO" -> "Ciudadano";
            case "REVISOR" -> "Revisor técnico";
            case "OPERADOR" -> "Operador";
            case "SUPER_ADMIN" -> "Super administrador";
            default -> rol;
        };
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

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
}
