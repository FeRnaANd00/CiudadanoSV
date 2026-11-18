package com.mycompany.ciudadanosv.models;

/** Institución pública (RNPN, MINSAL, VMT...). */
public class Institucion {

    private int idInstitucion;
    private String siglas;
    private String nombre;
    private boolean activo;

    public int getIdInstitucion() { return idInstitucion; }
    public void setIdInstitucion(int idInstitucion) { this.idInstitucion = idInstitucion; }

    public String getSiglas() { return siglas; }
    public void setSiglas(String siglas) { this.siglas = siglas; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
