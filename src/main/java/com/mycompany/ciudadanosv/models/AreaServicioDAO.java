package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AreaServicioDAO {

    /**
     * Devuelve las áreas del catálogo, cada una con sus trámites visibles
     * (ACTIVO o PROXIMAMENTE), en el orden definido en la base de datos.
     */
    public List<AreaServicio> listarConTramites() throws SQLException {
        String sql = "SELECT a.id_area, a.nombre AS area, a.icono, a.orden, "
                + "       t.id_tramite, t.codigo, t.nombre AS tramite, t.descripcion, "
                + "       t.arancel, t.modalidad, t.requiere_cita, t.estado "
                + "FROM area_servicio a "
                + "LEFT JOIN tramite t ON t.id_area = a.id_area "
                + "      AND t.estado IN ('ACTIVO', 'PROXIMAMENTE') "
                + "ORDER BY a.orden, t.estado, t.id_tramite";

        Map<Integer, AreaServicio> areas = new LinkedHashMap<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idArea = rs.getInt("id_area");
                AreaServicio area = areas.get(idArea);
                if (area == null) {
                    area = new AreaServicio();
                    area.setIdArea(idArea);
                    area.setNombre(rs.getString("area"));
                    area.setIcono(rs.getString("icono"));
                    area.setOrden(rs.getInt("orden"));
                    areas.put(idArea, area);
                }
                if (rs.getObject("id_tramite") != null) {
                    Tramite t = new Tramite();
                    t.setIdTramite(rs.getInt("id_tramite"));
                    t.setIdArea(idArea);
                    t.setCodigo(rs.getString("codigo"));
                    t.setNombre(rs.getString("tramite"));
                    t.setDescripcion(rs.getString("descripcion"));
                    t.setArancel(rs.getBigDecimal("arancel"));
                    t.setModalidad(rs.getString("modalidad"));
                    t.setRequiereCita(rs.getBoolean("requiere_cita"));
                    t.setEstado(rs.getString("estado"));
                    area.getTramites().add(t);
                }
            }
        }
        return new ArrayList<>(areas.values());
    }

    /** Número de sedes de atención activas (Duicentros, Unidades de Salud...). */
    public int contarSedesActivas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM sede WHERE activo = TRUE";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
