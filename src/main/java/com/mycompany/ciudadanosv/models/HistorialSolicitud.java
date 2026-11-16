package com.mycompany.ciudadanosv.models;

import com.mycompany.ciudadanosv.util.Fechas;
import java.time.LocalDateTime;

/** Un cambio de estado de la solicitud (tabla solicitud_historial). */
public class HistorialSolicitud {

    private String estadoAnterior;
    private String estadoNuevo;
    private String comentario;
    private String usuarioNombre;   // quién hizo el cambio
    private String usuarioRol;
    private LocalDateTime fecha;

    public HistorialSolicitud() {
    }

    public String getEstadoTexto() {
        return Solicitud.textoEstado(estadoNuevo);
    }

    public String getFechaTexto() {
        return Fechas.formatearFechaHora(fecha);
    }

    /** true si lo hizo un funcionario (para mostrar "Revisado por ..."). */
    public boolean isHechoPorFuncionario() {
        return usuarioRol != null && !"CIUDADANO".equals(usuarioRol);
    }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getUsuarioRol() { return usuarioRol; }
    public void setUsuarioRol(String usuarioRol) { this.usuarioRol = usuarioRol; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
