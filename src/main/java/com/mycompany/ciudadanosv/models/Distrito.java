package com.mycompany.ciudadanosv.models;

/** Distrito (antiguo municipio) dentro de un municipio. */
public class Distrito {

    private int idDistrito;
    private int idMunicipio;
    private String nombre;

    public Distrito() {
    }

    public int getIdDistrito() { return idDistrito; }
    public void setIdDistrito(int idDistrito) { this.idDistrito = idDistrito; }

    public int getIdMunicipio() { return idMunicipio; }
    public void setIdMunicipio(int idMunicipio) { this.idMunicipio = idMunicipio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
