package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TramiteDAO {

    private static final String SELECT = "SELECT t.id_tramite, t.id_area, a.nombre AS area, i.siglas, i.nombre AS institucion, "
            + "       t.codigo, t.nombre, t.descripcion, t.arancel, t.plazo_horas, t.modalidad, "
            + "       t.requiere_cita, t.requiere_biometria, t.estado "
            + "FROM tramite t "
            + "JOIN area_servicio a ON a.id_area = t.id_area "
            + "JOIN institucion i   ON i.id_institucion = t.id_institucion ";

    /** Busca un trámite por su código (ej. RNPN-DUI-01), con su área e institución. Null si no existe. */
    public Tramite buscarPorCodigo(String codigo) throws SQLException {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE t.codigo = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Trámites activos que se atienden con cita de una institución (ej. "MINSAL" -> tipos de atención médica). */
    public List<Tramite> listarConCita(String siglasInstitucion) throws SQLException {
        String sql = SELECT + "WHERE i.siglas = ? AND t.requiere_cita = TRUE AND t.estado = 'ACTIVO' ORDER BY t.id_tramite";
        List<Tramite> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, siglasInstitucion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Tramite mapear(ResultSet rs) throws SQLException {
        Tramite t = new Tramite();
        t.setIdTramite(rs.getInt("id_tramite"));
        t.setIdArea(rs.getInt("id_area"));
        t.setAreaNombre(rs.getString("area"));
        t.setInstitucionSiglas(rs.getString("siglas"));
        t.setInstitucionNombre(rs.getString("institucion"));
        t.setCodigo(rs.getString("codigo"));
        t.setNombre(rs.getString("nombre"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setArancel(rs.getBigDecimal("arancel"));
        int plazo = rs.getInt("plazo_horas");
        t.setPlazoHoras(rs.wasNull() ? null : plazo);
        t.setModalidad(rs.getString("modalidad"));
        t.setRequiereCita(rs.getBoolean("requiere_cita"));
        t.setRequiereBiometria(rs.getBoolean("requiere_biometria"));
        t.setEstado(rs.getString("estado"));
        return t;
    }
}
