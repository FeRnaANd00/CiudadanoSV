package com.mycompany.ciudadanosv.models;

/** Requisito de un trámite (tabla requisito). */
public class Requisito {

    private int idRequisito;
    private int idTramite;
    private String nombre;
    private String descripcion;
    private boolean obligatorio;
    private boolean requiereArchivo;
    private String formatos;
    private int tamanoMaxMb;
    private String tipoValidacion;   // MANUAL, AUTOMATICA, PRESENCIAL

    public Requisito() {
    }

    public int getIdRequisito() { return idRequisito; }
    public void setIdRequisito(int idRequisito) { this.idRequisito = idRequisito; }

    public int getIdTramite() { return idTramite; }
    public void setIdTramite(int idTramite) { this.idTramite = idTramite; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isObligatorio() { return obligatorio; }
    public void setObligatorio(boolean obligatorio) { this.obligatorio = obligatorio; }

    public boolean isRequiereArchivo() { return requiereArchivo; }
    public void setRequiereArchivo(boolean requiereArchivo) { this.requiereArchivo = requiereArchivo; }

    public String getFormatos() { return formatos; }
    public void setFormatos(String formatos) { this.formatos = formatos; }

    public int getTamanoMaxMb() { return tamanoMaxMb; }
    public void setTamanoMaxMb(int tamanoMaxMb) { this.tamanoMaxMb = tamanoMaxMb; }

    public String getTipoValidacion() { return tipoValidacion; }
    public void setTipoValidacion(String tipoValidacion) { this.tipoValidacion = tipoValidacion; }
}
