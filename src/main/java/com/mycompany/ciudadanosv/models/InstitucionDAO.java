package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InstitucionDAO {

    /** Todas las instituciones, por siglas. */
    public List<Institucion> listar() throws SQLException {
        List<Institucion> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_institucion, siglas, nombre, activo FROM institucion ORDER BY siglas");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Institucion i = new Institucion();
                i.setIdInstitucion(rs.getInt("id_institucion"));
                i.setSiglas(rs.getString("siglas"));
                i.setNombre(rs.getString("nombre"));
                i.setActivo(rs.getBoolean("activo"));
                lista.add(i);
            }
        }
        return lista;
    }
}
