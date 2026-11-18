package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DistritoDAO {

    /** Todos los distritos, por municipio y nombre (la vista filtra según el municipio elegido). */
    public List<Distrito> listar() throws SQLException {
        List<Distrito> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_distrito, id_municipio, nombre FROM distrito ORDER BY id_municipio, nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Distrito d = new Distrito();
                d.setIdDistrito(rs.getInt("id_distrito"));
                d.setIdMunicipio(rs.getInt("id_municipio"));
                d.setNombre(rs.getString("nombre"));
                lista.add(d);
            }
        }
        return lista;
    }
}
