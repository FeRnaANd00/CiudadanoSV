package com.mycompany.ciudadanosv.models;

/** Sede de atención (Duicentro, Unidad de Salud, Alcaldía...). Tabla sede. */
public class Sede {

    private int idSede;
    private int idInstitucion;
    private int idMunicipio;
    private String nombre;
    private String municipioNombre;
    private String horario;
    private String institucionSiglas;

    public Sede() {
    }

    public int getIdSede() { return idSede; }
    public void setIdSede(int idSede) { this.idSede = idSede; }

    public int getIdInstitucion() { return idInstitucion; }
    public void setIdInstitucion(int idInstitucion) { this.idInstitucion = idInstitucion; }

    public int getIdMunicipio() { return idMunicipio; }
    public void setIdMunicipio(int idMunicipio) { this.idMunicipio = idMunicipio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMunicipioNombre() { return municipioNombre; }
    public void setMunicipioNombre(String municipioNombre) { this.municipioNombre = municipioNombre; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getInstitucionSiglas() { return institucionSiglas; }
    public void setInstitucionSiglas(String institucionSiglas) { this.institucionSiglas = institucionSiglas; }
}
