package com.mycompany.ciudadanosv.models;

import java.math.BigDecimal;

/** Trámite o servicio del catálogo (tabla tramite). */
public class Tramite {

    private int idTramite;
    private int idArea;
    private String areaNombre;
    private String institucionSiglas;
    private String institucionNombre;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal arancel;
    private Integer plazoHoras;
    private String modalidad;
    private boolean requiereCita;
    private boolean requiereBiometria;
    private String estado;

    public Tramite() {
    }

    /** true si el ciudadano ya puede solicitarlo (no está "Próximamente"). */
    public boolean isDisponible() {
        return "ACTIVO".equals(estado);
    }

    /** true si no tiene costo. */
    public boolean isGratuito() {
        return arancel == null || arancel.signum() == 0;
    }

    public int getIdTramite() { return idTramite; }
    public void setIdTramite(int idTramite) { this.idTramite = idTramite; }

    public int getIdArea() { return idArea; }
    public void setIdArea(int idArea) { this.idArea = idArea; }

    public String getAreaNombre() { return areaNombre; }
    public void setAreaNombre(String areaNombre) { this.areaNombre = areaNombre; }

    public String getInstitucionSiglas() { return institucionSiglas; }
    public void setInstitucionSiglas(String institucionSiglas) { this.institucionSiglas = institucionSiglas; }

    public String getInstitucionNombre() { return institucionNombre; }
    public void setInstitucionNombre(String institucionNombre) { this.institucionNombre = institucionNombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getArancel() { return arancel; }
    public void setArancel(BigDecimal arancel) { this.arancel = arancel; }

    public Integer getPlazoHoras() { return plazoHoras; }
    public void setPlazoHoras(Integer plazoHoras) { this.plazoHoras = plazoHoras; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public boolean isRequiereCita() { return requiereCita; }
    public void setRequiereCita(boolean requiereCita) { this.requiereCita = requiereCita; }

    public boolean isRequiereBiometria() { return requiereBiometria; }
    public void setRequiereBiometria(boolean requiereBiometria) { this.requiereBiometria = requiereBiometria; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
