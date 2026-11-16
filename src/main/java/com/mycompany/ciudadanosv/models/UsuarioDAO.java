package com.mycompany.ciudadanosv.models;

import com.mycompany.ciudadanosv.util.Seguridad;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    private static final int ROL_CIUDADANO = 1;

    /**
     * Valida el inicio de sesión con DUI o correo + contraseña.
     * Devuelve el usuario si las credenciales son correctas y la cuenta está ACTIVA;
     * si no, devuelve null.
     */
    public Usuario autenticar(String identificador, String clave) throws SQLException {
        String sql = "SELECT u.id_usuario, u.id_rol, r.nombre AS rol, u.dui, u.nombre_completo, "
                + "       u.correo, u.telefono, u.estado, u.clave_hash, u.clave_salt "
                + "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol "
                + "WHERE u.dui = ? OR u.correo = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identificador);
            ps.setString(2, identificador);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (!Seguridad.verificarClave(clave, rs.getString("clave_salt"), rs.getString("clave_hash"))) {
                    return null;
                }
                if (!"ACTIVO".equals(rs.getString("estado"))) {
                    return null;
                }
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setIdRol(rs.getInt("id_rol"));
                u.setRol(rs.getString("rol"));
                u.setDui(rs.getString("dui"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setCorreo(rs.getString("correo"));
                u.setTelefono(rs.getString("telefono"));
                u.setEstado(rs.getString("estado"));
                return u;
            }
        }
    }

    /** Guarda la fecha del último acceso y deja constancia en la bitácora. */
    public void registrarAcceso(int idUsuario, String ip) throws SQLException {
        try (Connection con = Conexion.conectar();
             PreparedStatement upd = con.prepareStatement(
                     "UPDATE usuario SET ultimo_acceso = NOW() WHERE id_usuario = ?");
             PreparedStatement bit = con.prepareStatement(
                     "INSERT INTO bitacora (id_usuario, accion, descripcion, ip) VALUES (?, 'LOGIN', ?, ?)")) {
            upd.setInt(1, idUsuario);
            upd.executeUpdate();
            bit.setInt(1, idUsuario);
            bit.setString(2, "Inicio de sesión en el portal web");
            bit.setString(3, ip);
            bit.executeUpdate();
        }
    }

    /** true si ya hay una cuenta con ese DUI o ese correo. */
    public boolean existeDuiOCorreo(String dui, String correo) throws SQLException {
        String sql = "SELECT 1 FROM usuario WHERE dui = ? OR correo = ? LIMIT 1";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dui);
            ps.setString(2, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Registra un ciudadano nuevo. La contraseña se guarda con salt + SHA-256. */
    public void registrarCiudadano(Usuario u, String clave) throws SQLException {
        String salt = Seguridad.generarSalt();
        String sql = "INSERT INTO usuario (id_rol, dui, nombre_completo, correo, telefono, "
                + "clave_hash, clave_salt, ultimo_cambio_clave) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ROL_CIUDADANO);
            ps.setString(2, u.getDui());
            ps.setString(3, u.getNombreCompleto());
            ps.setString(4, u.getCorreo());
            ps.setString(5, u.getTelefono());
            ps.setString(6, Seguridad.hashClave(salt, clave));
            ps.setString(7, salt);
            ps.executeUpdate();
        }
    }
}
