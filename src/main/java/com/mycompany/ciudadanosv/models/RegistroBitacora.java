package com.mycompany.ciudadanosv.models;

import com.mycompany.ciudadanosv.util.Fechas;
import java.time.LocalDateTime;

/** Una línea de la bitácora (registro de actividad, solo lectura). */
public class RegistroBitacora {

    private long idBitacora;
    private String accion;        // LOGIN, ENVIO_SOLICITUD, CARGA_ARCHIVO, AGENDAR_CITA...
    private String descripcion;
    private String ip;
    private LocalDateTime fecha;

    /** Nombre legible de la acción. */
    public String getAccionTexto() {
        if (accion == null) {
            return "";
        }
        return switch (accion) {
            case "LOGIN" -> "Inicio de sesión";
            case "ENVIO_SOLICITUD" -> "Envío de solicitud";
            case "CARGA_ARCHIVO" -> "Carga de archivo";
            case "AGENDAR_CITA" -> "Cita agendada";
            case "CANCELAR_CITA" -> "Cita cancelada";
            case "ACTUALIZAR_PERFIL" -> "Actualización de datos de perfil";
            case "CAMBIO_CLAVE" -> "Cambio de contraseña";
            case "CONFIGURAR_SEGURIDAD" -> "Configuración de seguridad";
            case "REVOCAR_SESION" -> "Sesión revocada";
            case "CAMBIO_ESTADO" -> "Cambio de estado de solicitud";
            case "OBSERVACION_INTERNA" -> "Observación interna";
            case "ASIGNAR_SOLICITUD" -> "Solicitud asignada";
            case "COTEJO_DOCUMENTO" -> "Cotejo de documento";
            case "PROGRAMAR_CITA" -> "Cita biométrica programada";
            case "GESTION_CUPOS" -> "Gestión de cupos";
            case "ATENCION_CITA" -> "Atención de cita";
            case "GESTION_TRAMITE" -> "Catálogo de trámites";
            case "GESTION_USUARIO" -> "Gestión de usuarios";
            default -> accion.replace('_', ' ').toLowerCase();
        };
    }

    public String getIcono() {
        if (accion == null) {
            return "history";
        }
        return switch (accion) {
            case "LOGIN" -> "login";
            case "ENVIO_SOLICITUD" -> "send";
            case "CARGA_ARCHIVO" -> "upload_file";
            case "AGENDAR_CITA", "CANCELAR_CITA" -> "calendar_month";
            case "ACTUALIZAR_PERFIL" -> "person";
            case "CAMBIO_CLAVE", "CONFIGURAR_SEGURIDAD", "REVOCAR_SESION" -> "shield_lock";
            case "CAMBIO_ESTADO", "ASIGNAR_SOLICITUD" -> "gavel";
            case "OBSERVACION_INTERNA" -> "lock";
            case "COTEJO_DOCUMENTO" -> "fact_check";
            case "PROGRAMAR_CITA", "GESTION_CUPOS", "ATENCION_CITA" -> "calendar_month";
            case "GESTION_TRAMITE" -> "category";
            case "GESTION_USUARIO" -> "group";
            default -> "history";
        };
    }

    public String getFechaTexto() {
        return Fechas.formatearFechaHora(fecha);
    }

    public long getIdBitacora() { return idBitacora; }
    public void setIdBitacora(long idBitacora) { this.idBitacora = idBitacora; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
