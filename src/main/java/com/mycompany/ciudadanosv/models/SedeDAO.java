package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SedeDAO {

    private static final String SELECT = "SELECT s.id_sede, s.id_institucion, s.id_municipio, s.nombre, "
            + "s.horario, m.nombre AS municipio "
            + "FROM sede s JOIN municipio m ON m.id_municipio = s.id_municipio "
            + "JOIN institucion i ON i.id_institucion = s.id_institucion ";

    /** Sedes activas de una institución (ej. "RNPN" -> Duicentros). */
    public List<Sede> listarPorInstitucion(String siglas) throws SQLException {
        String sql = SELECT + "WHERE i.siglas = ? AND s.activo = TRUE ORDER BY s.nombre";
        List<Sede> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, siglas);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Primera sede activa de una institución en un municipio (ej. la alcaldía del municipio). Null si no hay. */
    public Sede buscarPorInstitucionYMunicipio(String siglas, int idMunicipio) throws SQLException {
        String sql = SELECT + "WHERE i.siglas = ? AND s.id_municipio = ? AND s.activo = TRUE ORDER BY s.id_sede LIMIT 1";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, siglas);
            ps.setInt(2, idMunicipio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private Sede mapear(ResultSet rs) throws SQLException {
        Sede s = new Sede();
        s.setIdSede(rs.getInt("id_sede"));
        s.setIdInstitucion(rs.getInt("id_institucion"));
        s.setIdMunicipio(rs.getInt("id_municipio"));
        s.setNombre(rs.getString("nombre"));
        s.setHorario(rs.getString("horario"));
        s.setMunicipioNombre(rs.getString("municipio"));
        return s;
    }
}
