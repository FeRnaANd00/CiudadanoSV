package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TramiteDAO {

    /** Busca un trámite por su código (ej. RNPN-DUI-01), con su área e institución. Null si no existe. */
    public Tramite buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT t.id_tramite, t.id_area, a.nombre AS area, i.siglas, i.nombre AS institucion, "
                + "       t.codigo, t.nombre, t.descripcion, t.arancel, t.plazo_horas, t.modalidad, "
                + "       t.requiere_cita, t.requiere_biometria, t.estado "
                + "FROM tramite t "
                + "JOIN area_servicio a ON a.id_area = t.id_area "
                + "JOIN institucion i   ON i.id_institucion = t.id_institucion "
                + "WHERE t.codigo = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
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
    }
}
