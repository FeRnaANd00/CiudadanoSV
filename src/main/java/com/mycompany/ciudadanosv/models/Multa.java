package com.mycompany.ciudadanosv.models;

import com.mycompany.ciudadanosv.util.Fechas;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Esquela / infracción de tránsito (tablas multa + tipo_infraccion). */
public class Multa {

    private int idMulta;
    private String folio;
    private String codigoInfraccion;     // TO-01
    private String descripcion;
    private String gravedad;             // LEVE, GRAVE, MUY_GRAVE
    private String duiInfractor;
    private String placa;
    private LocalDateTime fechaHora;
    private String lugar;
    private String agente;
    private BigDecimal monto;
    private LocalDate fechaVencimiento;
    private String estado;               // PENDIENTE, PAGADA, EN_APELACION, ANULADA
    private String reciboPago;
    private LocalDateTime fechaPago;

    public Multa() {
    }

    public boolean isPendiente() {
        return "PENDIENTE".equals(estado);
    }

    /** Pendiente y ya pasó la fecha límite. */
    public boolean isVencida() {
        return isPendiente() && fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    /** "Vence en 5 días", "Vence hoy" o "Vencida hace 3 días". */
    public String getVencimientoTexto() {
        if (!isPendiente() || fechaVencimiento == null) {
            return "";
        }
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
        if (dias > 1) {
            return "Vence en " + dias + " días";
        }
        if (dias == 1) {
            return "Vence mañana";
        }
        if (dias == 0) {
            return "Vence hoy";
        }
        return "Vencida hace " + (-dias) + (dias == -1 ? " día" : " días");
    }

    public String getGravedadTexto() {
        if (gravedad == null) {
            return "";
        }
        return switch (gravedad) {
            case "LEVE" -> "Infracción leve";
            case "GRAVE" -> "Infracción grave";
            case "MUY_GRAVE" -> "Infracción muy grave";
            default -> gravedad;
        };
    }

    public String getEstadoTexto() {
        if (estado == null) {
            return "";
        }
        if (isVencida()) {
            return "Vencida";
        }
        return switch (estado) {
            case "PENDIENTE" -> "Pendiente";
            case "PAGADA" -> "Pagada";
            case "EN_APELACION" -> "En apelación";
            case "ANULADA" -> "Anulada";
            default -> estado;
        };
    }

    public String getFechaHoraTexto() {
        return Fechas.formatearFechaHora(fechaHora);
    }

    public String getFechaVencimientoTexto() {
        return Fechas.formatear(fechaVencimiento);
    }

    public String getFechaPagoTexto() {
        return Fechas.formatearFechaHora(fechaPago);
    }

    public int getIdMulta() { return idMulta; }
    public void setIdMulta(int idMulta) { this.idMulta = idMulta; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public String getCodigoInfraccion() { return codigoInfraccion; }
    public void setCodigoInfraccion(String codigoInfraccion) { this.codigoInfraccion = codigoInfraccion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getGravedad() { return gravedad; }
    public void setGravedad(String gravedad) { this.gravedad = gravedad; }

    public String getDuiInfractor() { return duiInfractor; }
    public void setDuiInfractor(String duiInfractor) { this.duiInfractor = duiInfractor; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public String getAgente() { return agente; }
    public void setAgente(String agente) { this.agente = agente; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getReciboPago() { return reciboPago; }
    public void setReciboPago(String reciboPago) { this.reciboPago = reciboPago; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}
