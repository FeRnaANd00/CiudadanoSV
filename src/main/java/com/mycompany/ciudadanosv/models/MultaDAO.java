package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MultaDAO {

    private static final String SELECT = "SELECT m.id_multa, m.folio, ti.codigo, ti.descripcion, ti.gravedad, "
            + "       m.dui_infractor, m.placa, m.fecha_hora, m.lugar, m.agente, m.monto, m.fecha_vencimiento, "
            + "       m.estado, m.recibo_pago, m.fecha_pago "
            + "FROM multa m JOIN tipo_infraccion ti ON ti.id_tipo_infraccion = m.id_tipo_infraccion ";
    private static final String ORDEN = " ORDER BY (m.estado = 'PENDIENTE') DESC, m.fecha_hora DESC";

    /** Multas asociadas a un DUI (licencia vinculada). */
    public List<Multa> listarPorDui(String dui) throws SQLException {
        return listar(SELECT + "WHERE m.dui_infractor = ?" + ORDEN, dui);
    }

    /** Multas de una placa. Compara sin guion: "P123456" encuentra "P123-456". */
    public List<Multa> listarPorPlaca(String placaSinGuion) throws SQLException {
        return listar(SELECT + "WHERE REPLACE(UPPER(m.placa), '-', '') = ?" + ORDEN, placaSinGuion);
    }

    private List<Multa> listar(String sql, String valor) throws SQLException {
        List<Multa> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Multa m = new Multa();
                    m.setIdMulta(rs.getInt("id_multa"));
                    m.setFolio(rs.getString("folio"));
                    m.setCodigoInfraccion(rs.getString("codigo"));
                    m.setDescripcion(rs.getString("descripcion"));
                    m.setGravedad(rs.getString("gravedad"));
                    m.setDuiInfractor(rs.getString("dui_infractor"));
                    m.setPlaca(rs.getString("placa"));
                    m.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                    m.setLugar(rs.getString("lugar"));
                    m.setAgente(rs.getString("agente"));
                    m.setMonto(rs.getBigDecimal("monto"));
                    Date venc = rs.getDate("fecha_vencimiento");
                    m.setFechaVencimiento(venc == null ? null : venc.toLocalDate());
                    m.setEstado(rs.getString("estado"));
                    m.setReciboPago(rs.getString("recibo_pago"));
                    Timestamp pago = rs.getTimestamp("fecha_pago");
                    m.setFechaPago(pago == null ? null : pago.toLocalDateTime());
                    lista.add(m);
                }
            }
        }
        return lista;
    }
}
