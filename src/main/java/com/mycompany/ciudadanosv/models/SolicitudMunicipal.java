package com.mycompany.ciudadanosv.models;

/** Datos de una solicitud municipal: solvencia o constancia de residencia (tabla solicitud_municipal). */
public class SolicitudMunicipal {

    private int idMunicipio;
    private String municipioNombre;
    private String domicilio;

    public SolicitudMunicipal() {
    }

    public int getIdMunicipio() { return idMunicipio; }
    public void setIdMunicipio(int idMunicipio) { this.idMunicipio = idMunicipio; }

    public String getMunicipioNombre() { return municipioNombre; }
    public void setMunicipioNombre(String municipioNombre) { this.municipioNombre = municipioNombre; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
}
