package com.mycompany.ciudadanosv.models;

/** Municipio con el nombre de su departamento (tablas municipio + departamento). */
public class Municipio {

    private int idMunicipio;
    private String nombre;
    private String departamento;

    public Municipio() {
    }

    public int getIdMunicipio() { return idMunicipio; }
    public void setIdMunicipio(int idMunicipio) { this.idMunicipio = idMunicipio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}
