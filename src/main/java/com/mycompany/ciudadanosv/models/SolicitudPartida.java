package com.mycompany.ciudadanosv.models;

import java.time.LocalDate;

/** Datos propios de la solicitud de partida de nacimiento (tabla solicitud_partida). */
public class SolicitudPartida {

    /** Valores permitidos (columna ENUM) y su texto en pantalla. */
    public static final String[][] RELACIONES = {
        {"PERSONA_INSCRITA", "Soy la persona inscrita"},
        {"PADRE_MADRE_REPRESENTANTE", "Madre, padre o representante legal"},
        {"OTRA_AUTORIZADA", "Otra persona autorizada"}
    };
    public static final String[][] USOS = {
        {"TRAMITE_PERSONAL", "Trámite personal"},
        {"ESTUDIO", "Estudio"},
        {"OTRO", "Otro uso"}
    };

    private String nombreInscrito;
    private String relacion;
    private LocalDate fechaNacimiento;
    private int idMunicipioInscripcion;
    private String uso;
    private String municipioNombre;   // solo para mostrar en el detalle

    public SolicitudPartida() {
    }

    /** Texto en pantalla de un valor del ENUM (ej. ESTUDIO -> "Estudio"). */
    public static String texto(String[][] opciones, String valor) {
        for (String[] o : opciones) {
            if (o[0].equals(valor)) {
                return o[1];
            }
        }
        return valor;
    }

    public String getRelacionTexto() { return texto(RELACIONES, relacion); }
    public String getUsoTexto() { return texto(USOS, uso); }
    public String getFechaNacimientoTexto() { return com.mycompany.ciudadanosv.util.Fechas.formatear(fechaNacimiento); }

    public String getMunicipioNombre() { return municipioNombre; }
    public void setMunicipioNombre(String municipioNombre) { this.municipioNombre = municipioNombre; }

    public static boolean esValorValido(String[][] opciones, String valor) {
        for (String[] o : opciones) {
            if (o[0].equals(valor)) {
                return true;
            }
        }
        return false;
    }

    public String getNombreInscrito() { return nombreInscrito; }
    public void setNombreInscrito(String nombreInscrito) { this.nombreInscrito = nombreInscrito; }

    public String getRelacion() { return relacion; }
    public void setRelacion(String relacion) { this.relacion = relacion; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public int getIdMunicipioInscripcion() { return idMunicipioInscripcion; }
    public void setIdMunicipioInscripcion(int idMunicipioInscripcion) { this.idMunicipioInscripcion = idMunicipioInscripcion; }

    public String getUso() { return uso; }
    public void setUso(String uso) { this.uso = uso; }
}
