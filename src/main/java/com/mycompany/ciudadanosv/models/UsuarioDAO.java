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
                + "       u.correo, u.telefono, u.estado, u.clave_hash, u.clave_salt, u.id_institucion, i.siglas "
                + "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol "
                + "LEFT JOIN institucion i ON i.id_institucion = u.id_institucion "
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
                int idInst = rs.getInt("id_institucion");
                u.setIdInstitucion(rs.wasNull() ? null : idInst);
                u.setInstitucionSiglas(rs.getString("siglas"));
                return u;
            }
        }
    }

    /**
     * Vuelve a leer rol, institución y estado del usuario en sesión.
     * Devuelve false si la cuenta ya no existe o no está ACTIVA (hay que cerrar la sesión).
     */
    public boolean refrescar(Usuario u) throws SQLException {
        String sql = "SELECT r.nombre AS rol, u.id_rol, u.estado, u.nombre_completo, u.correo, u.telefono, "
                + "       u.id_institucion, i.siglas "
                + "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol "
                + "LEFT JOIN institucion i ON i.id_institucion = u.id_institucion WHERE u.id_usuario = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, u.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"ACTIVO".equals(rs.getString("estado"))) {
                    return false;
                }
                u.setRol(rs.getString("rol"));
                u.setIdRol(rs.getInt("id_rol"));
                u.setEstado(rs.getString("estado"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setCorreo(rs.getString("correo"));
                u.setTelefono(rs.getString("telefono"));
                int idInst = rs.getInt("id_institucion");
                u.setIdInstitucion(rs.wasNull() ? null : idInst);
                u.setInstitucionSiglas(rs.getString("siglas"));
                return true;
            }
        }
    }

    // ------------------------------------------------------------------
    // Mi perfil
    // ------------------------------------------------------------------

    /** Datos del perfil del usuario (null si no existe). */
    public Perfil buscarPerfil(int idUsuario) throws SQLException {
        String sql = "SELECT u.id_usuario, u.dui, u.nombre_completo, u.correo, u.telefono, u.direccion, "
                + "       u.id_municipio, m.nombre AS municipio, dep.nombre AS departamento, "
                + "       u.id_distrito, dis.nombre AS distrito, u.estado_civil, u.fecha_nacimiento, "
                + "       u.nivel_verificacion, u.fecha_registro, u.ultimo_cambio_clave "
                + "FROM usuario u LEFT JOIN municipio m ON m.id_municipio = u.id_municipio "
                + "LEFT JOIN departamento dep ON dep.id_departamento = m.id_departamento "
                + "LEFT JOIN distrito dis ON dis.id_distrito = u.id_distrito "
                + "WHERE u.id_usuario = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Perfil p = new Perfil();
                p.setIdUsuario(rs.getInt("id_usuario"));
                p.setDui(rs.getString("dui"));
                p.setNombreCompleto(rs.getString("nombre_completo"));
                p.setCorreo(rs.getString("correo"));
                p.setTelefono(rs.getString("telefono"));
                p.setDireccion(rs.getString("direccion"));
                int idMunicipio = rs.getInt("id_municipio");
                p.setIdMunicipio(rs.wasNull() ? null : idMunicipio);
                p.setMunicipioNombre(rs.getString("municipio"));
                p.setDepartamentoNombre(rs.getString("departamento"));
                int idDistrito = rs.getInt("id_distrito");
                p.setIdDistrito(rs.wasNull() ? null : idDistrito);
                p.setDistritoNombre(rs.getString("distrito"));
                p.setEstadoCivil(rs.getString("estado_civil"));
                java.sql.Date nacimiento = rs.getDate("fecha_nacimiento");
                p.setFechaNacimiento(nacimiento == null ? null : nacimiento.toLocalDate());
                p.setNivelVerificacion(rs.getInt("nivel_verificacion"));
                p.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
                java.sql.Timestamp cambio = rs.getTimestamp("ultimo_cambio_clave");
                p.setUltimoCambioClave(cambio == null ? null : cambio.toLocalDateTime());
                return p;
            }
        }
    }

    /**
     * Actualiza los datos de contacto y personales (no nombre ni DUI).
     * Si cambia el correo, avisa con una notificación de seguridad. Todo en una transacción.
     */
    public void actualizarPerfil(Perfil p, String ip) throws SQLException, ReglaNegocioException {
        if (p.getTelefono() != null && !p.getTelefono().matches("\\d{4}-\\d{4}")) {
            throw new ReglaNegocioException("El teléfono debe tener el formato 7000-0000.");
        }
        if (p.getCorreo() == null || !p.getCorreo().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                || p.getCorreo().length() > 120) {
            throw new ReglaNegocioException("Escribe un correo electrónico válido.");
        }
        if (p.getDireccion() != null && p.getDireccion().length() > 200) {
            throw new ReglaNegocioException("La dirección admite máximo 200 caracteres.");
        }
        if (p.getEstadoCivil() != null
                && !java.util.Set.of("SOLTERO", "CASADO", "DIVORCIADO", "VIUDO", "ACOMPANADO").contains(p.getEstadoCivil())) {
            throw new ReglaNegocioException("Elige un estado civil válido.");
        }
        if (p.getFechaNacimiento() != null && (p.getFechaNacimiento().isAfter(java.time.LocalDate.now().minusYears(18))
                || p.getFechaNacimiento().isBefore(java.time.LocalDate.of(1900, 1, 1)))) {
            throw new ReglaNegocioException("La fecha de nacimiento no es válida (debes ser mayor de edad).");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                String correoAnterior;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT correo FROM usuario WHERE id_usuario = ? FOR UPDATE")) {
                    ps.setInt(1, p.getIdUsuario());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("La cuenta no existe.");
                        }
                        correoAnterior = rs.getString(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 1 FROM usuario WHERE correo = ? AND id_usuario <> ?")) {
                    ps.setString(1, p.getCorreo());
                    ps.setInt(2, p.getIdUsuario());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new ReglaNegocioException("Ese correo ya está registrado en otra cuenta.");
                        }
                    }
                }
                // El distrito debe pertenecer al municipio elegido
                if (p.getIdDistrito() != null) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT 1 FROM distrito WHERE id_distrito = ? AND id_municipio = ?")) {
                        ps.setInt(1, p.getIdDistrito());
                        ps.setInt(2, p.getIdMunicipio() == null ? -1 : p.getIdMunicipio());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new ReglaNegocioException("El distrito elegido no pertenece al municipio.");
                            }
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario SET correo = ?, telefono = ?, direccion = ?, id_municipio = ?, "
                        + "estado_civil = ?, fecha_nacimiento = ?, id_distrito = ? WHERE id_usuario = ?")) {
                    ps.setString(1, p.getCorreo());
                    ps.setString(2, p.getTelefono());
                    ps.setString(3, p.getDireccion());
                    if (p.getIdMunicipio() == null) {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(4, p.getIdMunicipio());
                    }
                    ps.setString(5, p.getEstadoCivil());
                    ps.setDate(6, p.getFechaNacimiento() == null ? null : java.sql.Date.valueOf(p.getFechaNacimiento()));
                    if (p.getIdDistrito() == null) {
                        ps.setNull(7, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(7, p.getIdDistrito());
                    }
                    ps.setInt(8, p.getIdUsuario());
                    ps.executeUpdate();
                }
                if (!p.getCorreo().equalsIgnoreCase(correoAnterior)) {
                    NotificacionDAO.crear(con, p.getIdUsuario(), "SEGURIDAD", "Correo de la cuenta actualizado",
                            "El correo de tu cuenta cambió de " + correoAnterior + " a " + p.getCorreo()
                            + ". Si no fuiste tú, cambia tu contraseña.", null, null);
                }
                NotificacionDAO.registrarBitacora(con, p.getIdUsuario(), "ACTUALIZAR_PERFIL",
                        "Datos de perfil actualizados", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Cambia la contraseña verificando la actual. Genera un salt nuevo, notifica y registra en la bitácora. */
    public void cambiarClave(int idUsuario, String actual, String nueva, String confirmacion, String ip)
            throws SQLException, ReglaNegocioException {
        if (actual == null || actual.isEmpty()) {
            throw new ReglaNegocioException("Escribe tu contraseña actual.");
        }
        if (nueva == null || !nueva.equals(confirmacion)) {
            throw new ReglaNegocioException("La nueva contraseña y su confirmación no coinciden.");
        }
        if (!Seguridad.claveEsSegura(nueva)) {
            throw new ReglaNegocioException("La nueva contraseña debe tener mínimo 8 caracteres, un número, una mayúscula y un símbolo.");
        }
        if (nueva.equals(actual)) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta de la actual.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT clave_hash, clave_salt FROM usuario WHERE id_usuario = ? FOR UPDATE")) {
                    ps.setInt(1, idUsuario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !Seguridad.verificarClave(actual, rs.getString("clave_salt"), rs.getString("clave_hash"))) {
                            throw new ReglaNegocioException("La contraseña actual no es correcta.");
                        }
                    }
                }
                String salt = Seguridad.generarSalt();
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario SET clave_hash = ?, clave_salt = ?, ultimo_cambio_clave = NOW() WHERE id_usuario = ?")) {
                    ps.setString(1, Seguridad.hashClave(salt, nueva));
                    ps.setString(2, salt);
                    ps.setInt(3, idUsuario);
                    ps.executeUpdate();
                }
                NotificacionDAO.crear(con, idUsuario, "SEGURIDAD", "Contraseña actualizada",
                        "Cambiaste la contraseña de tu cuenta. Si no fuiste tú, comunícate con soporte.", null, null);
                NotificacionDAO.registrarBitacora(con, idUsuario, "CAMBIO_CLAVE", "Contraseña cambiada desde Mi perfil", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
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
