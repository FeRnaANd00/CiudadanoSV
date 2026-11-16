package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MunicipioDAO {

    /** Todos los municipios, ordenados por departamento y nombre. */
    public List<Municipio> listar() throws SQLException {
        String sql = "SELECT m.id_municipio, m.nombre, d.nombre AS departamento "
                + "FROM municipio m JOIN departamento d ON d.id_departamento = m.id_departamento "
                + "ORDER BY d.nombre, m.nombre";
        List<Municipio> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Municipio m = new Municipio();
                m.setIdMunicipio(rs.getInt("id_municipio"));
                m.setNombre(rs.getString("nombre"));
                m.setDepartamento(rs.getString("departamento"));
                lista.add(m);
            }
        }
        return lista;
    }
}
