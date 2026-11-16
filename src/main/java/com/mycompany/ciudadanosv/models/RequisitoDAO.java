package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequisitoDAO {

    /** Requisitos de un trámite: primero los obligatorios. */
    public List<Requisito> listarPorTramite(int idTramite) throws SQLException {
        String sql = "SELECT id_requisito, id_tramite, nombre, descripcion, obligatorio, requiere_archivo, "
                + "formatos, tamano_max_mb, tipo_validacion "
                + "FROM requisito WHERE id_tramite = ? ORDER BY obligatorio DESC, id_requisito";
        List<Requisito> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTramite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Requisito r = new Requisito();
                    r.setIdRequisito(rs.getInt("id_requisito"));
                    r.setIdTramite(rs.getInt("id_tramite"));
                    r.setNombre(rs.getString("nombre"));
                    r.setDescripcion(rs.getString("descripcion"));
                    r.setObligatorio(rs.getBoolean("obligatorio"));
                    r.setRequiereArchivo(rs.getBoolean("requiere_archivo"));
                    r.setFormatos(rs.getString("formatos"));
                    r.setTamanoMaxMb(rs.getInt("tamano_max_mb"));
                    r.setTipoValidacion(rs.getString("tipo_validacion"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }
}
