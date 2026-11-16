package com.mycompany.ciudadanosv.models;

/** Datos de una solicitud de renovación o reposición de DUI (tabla solicitud_dui). */
public class SolicitudDui {

    /** Valores del ENUM motivo y su texto en pantalla. */
    public static final String[][] MOTIVOS = {
        {"VENCIMIENTO", "Vencimiento de vigencia (8 años)"},
        {"DETERIORO", "Deterioro visible o daño del chip/plástico"},
        {"EXTRAVIO_ROBO", "Extravío o robo con constancia"},
        {"MODIFICACION_DATOS", "Modificación de datos personales o estado familiar"}
    };

    private String motivo;
    private int idMunicipioRes;
    private String municipioNombre;
    private String departamentoNombre;

    public SolicitudDui() {
    }

    public String getMotivoTexto() {
        return SolicitudPartida.texto(MOTIVOS, motivo);
    }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public int getIdMunicipioRes() { return idMunicipioRes; }
    public void setIdMunicipioRes(int idMunicipioRes) { this.idMunicipioRes = idMunicipioRes; }

    public String getMunicipioNombre() { return municipioNombre; }
    public void setMunicipioNombre(String municipioNombre) { this.municipioNombre = municipioNombre; }

    public String getDepartamentoNombre() { return departamentoNombre; }
    public void setDepartamentoNombre(String departamentoNombre) { this.departamentoNombre = departamentoNombre; }
}
