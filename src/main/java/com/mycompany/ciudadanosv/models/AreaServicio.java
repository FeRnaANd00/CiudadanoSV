package com.mycompany.ciudadanosv.models;

import java.util.ArrayList;
import java.util.List;

/** Área del catálogo (Salud Pública, Transporte VMT...) con sus trámites. */
public class AreaServicio {

    private int idArea;
    private String nombre;
    private String icono;
    private int orden;
    private final List<Tramite> tramites = new ArrayList<>();

    public AreaServicio() {
    }

    public int getIdArea() { return idArea; }
    public void setIdArea(int idArea) { this.idArea = idArea; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** Nombre del icono de Material Symbols (badge, local_hospital...). */
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public List<Tramite> getTramites() { return tramites; }
}
