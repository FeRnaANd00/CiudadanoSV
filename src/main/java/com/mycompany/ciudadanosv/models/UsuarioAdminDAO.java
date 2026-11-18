package com.mycompany.ciudadanosv.models;

import com.mycompany.ciudadanosv.util.Seguridad;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestión de usuarios (solo SUPER_ADMIN, permiso USUARIO_GESTIONAR).
 * Hay un único super admin: aquí solo se crean REVISOR y OPERADOR, cada uno con su institución.
 */
public class UsuarioAdminDAO {

    public static final int POR_PAGINA = 20;
    private static final int ROL_REVISOR = 2;
    private static final int ROL_OPERADOR = 3;

    private static final String SELECT = "SELECT u.id_usuario, r.nombre AS rol, u.dui, u.nombre_completo, u.correo, "
            + "       u.telefono, u.estado, u.id_institucion, i.siglas, u.id_sede, se.nombre AS sede, u.cargo, "
            + "       u.fecha_registro, u.ultimo_acceso, u.fecha_nacimiento, u.estado_civil, u.direccion, "
            + "       mu.nombre AS municipio, dep.nombre AS departamento, dis.nombre AS distrito, "
            + "       (SELECT COUNT(*) FROM solicitud s WHERE s.id_usuario = u.id_usuario) AS num_solicitudes "
            + "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol "
            + "LEFT JOIN institucion i ON i.id_institucion = u.id_institucion "
            + "LEFT JOIN sede se ON se.id_sede = u.id_sede "
            + "LEFT JOIN municipio mu ON mu.id_municipio = u.id_municipio "
            + "LEFT JOIN departamento dep ON dep.id_departamento = mu.id_departamento "
            + "LEFT JOIN distrito dis ON dis.id_distrito = u.id_distrito ";

    /** Directorio con filtros: texto (nombre, DUI o correo), rol, estado, institución; paginado. */
    public List<CuentaUsuario> listar(String texto, String rol, String estado, Integer idInstitucion, int pagina)
            throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = SELECT + where(texto, rol, estado, idInstitucion, params)
                + "ORDER BY (r.nombre = 'CIUDADANO'), u.nombre_completo LIMIT ? OFFSET ?";
        params.add(POR_PAGINA);
        params.add((Math.max(1, pagina) - 1) * POR_PAGINA);
        List<CuentaUsuario> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            SolicitudAdminDAO.asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public int contar(String texto, String rol, String estado, Integer idInstitucion) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM usuario u JOIN rol r ON r.id_rol = u.id_rol "
                + where(texto, rol, estado, idInstitucion, params);
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            SolicitudAdminDAO.asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Cuentas por rol y por estado (para los chips e indicadores). */
    public Map<String, Integer> resumen() throws SQLException {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (String k : new String[]{"TOTAL", "CIUDADANO", "REVISOR", "OPERADOR", "SUPER_ADMIN", "ACTIVO", "SUSPENDIDO", "EN_REVISION"}) {
            m.put(k, 0);
        }
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT r.nombre, u.estado, COUNT(*) FROM usuario u JOIN rol r ON r.id_rol = u.id_rol GROUP BY r.nombre, u.estado");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int n = rs.getInt(3);
                m.merge("TOTAL", n, Integer::sum);
                m.merge(rs.getString(1), n, Integer::sum);
                m.merge(rs.getString(2), n, Integer::sum);
            }
        }
        return m;
    }

    public CuentaUsuario buscar(int idUsuario) throws SQLException {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE u.id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Matriz de roles y permisos (solo lectura): rol -> lista de descripciones de permisos. */
    public Map<String, List<String>> matrizPermisos() throws SQLException {
        Map<String, List<String>> m = new LinkedHashMap<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT r.nombre, p.descripcion FROM rol r "
                     + "LEFT JOIN rol_permiso rp ON rp.id_rol = r.id_rol "
                     + "LEFT JOIN permiso p ON p.id_permiso = rp.id_permiso ORDER BY r.id_rol, p.id_permiso");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<String> permisos = m.computeIfAbsent(rs.getString(1), k -> new ArrayList<>());
                if (rs.getString(2) != null) {
                    permisos.add(rs.getString(2));
                }
            }
        }
        return m;
    }

    /** Sedes activas de una institución (para asignar la sede del funcionario). */
    public List<Sede> listarSedes() throws SQLException {
        List<Sede> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT s.id_sede, s.id_institucion, s.nombre, i.siglas FROM sede s "
                     + "JOIN institucion i ON i.id_institucion = s.id_institucion WHERE s.activo = TRUE ORDER BY i.siglas, s.nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Sede s = new Sede();
                s.setIdSede(rs.getInt(1));
                s.setIdInstitucion(rs.getInt(2));
                s.setNombre(rs.getString(3));
                s.setInstitucionSiglas(rs.getString(4));
                lista.add(s);
            }
        }
        return lista;
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    /** Crea un funcionario (REVISOR u OPERADOR) con contraseña inicial. Devuelve el id. */
    public int crearFuncionario(Usuario admin, CuentaUsuario c, String clave, String ip)
            throws SQLException, ReglaNegocioException {
        validarDatos(c, true);
        if (!Seguridad.claveEsSegura(clave)) {
            throw new ReglaNegocioException("La contraseña inicial debe tener mínimo 8 caracteres, un número, una mayúscula y un símbolo.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                validarUnicos(con, c, 0);
                validarSede(con, c);
                String salt = Seguridad.generarSalt();
                int id;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO usuario (id_rol, dui, nombre_completo, correo, telefono, clave_hash, clave_salt, "
                        + "id_institucion, id_sede, cargo, nivel_verificacion, ultimo_cambio_clave) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 2, NOW())", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idRol(c.getRol()));
                    ps.setString(2, c.getDui());
                    ps.setString(3, c.getNombreCompleto());
                    ps.setString(4, c.getCorreo());
                    ps.setString(5, c.getTelefono());
                    ps.setString(6, Seguridad.hashClave(salt, clave));
                    ps.setString(7, salt);
                    ps.setInt(8, c.getIdInstitucion());
                    setEnteroONulo(ps, 9, c.getIdSede());
                    ps.setString(10, c.getCargo());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                NotificacionDAO.crear(con, id, "SISTEMA", "Bienvenido(a) al portal administrativo",
                        "Tu cuenta de " + c.getRolTexto().toLowerCase() + " fue creada por el administrador. "
                        + "Te recomendamos cambiar tu contraseña inicial.", null, null);
                NotificacionDAO.registrarBitacora(con, admin.getIdUsuario(), "GESTION_USUARIO",
                        "Funcionario creado: " + c.getCorreo() + " (" + c.getRol() + ")", ip);
                con.commit();
                return id;
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Actualiza los datos de un funcionario (no del super admin ni de ciudadanos). */
    public void actualizarFuncionario(Usuario admin, CuentaUsuario c, String ip) throws SQLException, ReglaNegocioException {
        CuentaUsuario actual = buscar(c.getIdUsuario());
        if (actual == null || !actual.isFuncionario() || actual.isSuperAdmin()) {
            throw new ReglaNegocioException("Solo se editan cuentas de revisores y operadores.");
        }
        c.setDui(actual.getDui());   // el DUI no cambia
        validarDatos(c, false);
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                validarUnicos(con, c, c.getIdUsuario());
                validarSede(con, c);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario SET id_rol = ?, nombre_completo = ?, correo = ?, telefono = ?, id_institucion = ?, "
                        + "id_sede = ?, cargo = ? WHERE id_usuario = ?")) {
                    ps.setInt(1, idRol(c.getRol()));
                    ps.setString(2, c.getNombreCompleto());
                    ps.setString(3, c.getCorreo());
                    ps.setString(4, c.getTelefono());
                    ps.setInt(5, c.getIdInstitucion());
                    setEnteroONulo(ps, 6, c.getIdSede());
                    ps.setString(7, c.getCargo());
                    ps.setInt(8, c.getIdUsuario());
                    ps.executeUpdate();
                }
                NotificacionDAO.registrarBitacora(con, admin.getIdUsuario(), "GESTION_USUARIO",
                        "Funcionario actualizado: " + c.getCorreo() + " (" + c.getRol() + ", institución " + c.getIdInstitucion() + ")", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Suspende o reactiva una cuenta. El super admin no se puede suspender. */
    public void cambiarEstado(Usuario admin, int idUsuario, String estado, String ip) throws SQLException, ReglaNegocioException {
        if (!"ACTIVO".equals(estado) && !"SUSPENDIDO".equals(estado)) {
            throw new ReglaNegocioException("Estado no válido.");
        }
        CuentaUsuario c = buscar(idUsuario);
        if (c == null) {
            throw new ReglaNegocioException("La cuenta no existe.");
        }
        if (c.isSuperAdmin() || idUsuario == admin.getIdUsuario()) {
            throw new ReglaNegocioException("La cuenta del super administrador no se puede suspender.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE usuario SET estado = ? WHERE id_usuario = ?")) {
                    ps.setString(1, estado);
                    ps.setInt(2, idUsuario);
                    ps.executeUpdate();
                }
                if ("ACTIVO".equals(estado)) {
                    NotificacionDAO.crear(con, idUsuario, "SEGURIDAD", "Cuenta reactivada",
                            "Tu cuenta fue reactivada por el administrador del sistema.", null, null);
                }
                NotificacionDAO.registrarBitacora(con, admin.getIdUsuario(), "GESTION_USUARIO",
                        "Cuenta " + c.getCorreo() + " " + ("ACTIVO".equals(estado) ? "reactivada" : "suspendida"), ip);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Asigna una contraseña temporal (el usuario debería cambiarla al entrar). */
    public void restablecerClave(Usuario admin, int idUsuario, String clave, String ip) throws SQLException, ReglaNegocioException {
        if (!Seguridad.claveEsSegura(clave)) {
            throw new ReglaNegocioException("La contraseña temporal debe tener mínimo 8 caracteres, un número, una mayúscula y un símbolo.");
        }
        CuentaUsuario c = buscar(idUsuario);
        if (c == null) {
            throw new ReglaNegocioException("La cuenta no existe.");
        }
        if (c.isSuperAdmin()) {
            throw new ReglaNegocioException("La contraseña del super administrador no se restablece desde aquí.");
        }
        String salt = Seguridad.generarSalt();
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario SET clave_hash = ?, clave_salt = ?, ultimo_cambio_clave = NOW() WHERE id_usuario = ?")) {
                    ps.setString(1, Seguridad.hashClave(salt, clave));
                    ps.setString(2, salt);
                    ps.setInt(3, idUsuario);
                    ps.executeUpdate();
                }
                NotificacionDAO.crear(con, idUsuario, "SEGURIDAD", "Contraseña restablecida",
                        "El administrador restableció tu contraseña. Si no lo solicitaste, comunícate con soporte.", null, null);
                NotificacionDAO.registrarBitacora(con, admin.getIdUsuario(), "GESTION_USUARIO",
                        "Contraseña restablecida para " + c.getCorreo(), ip);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Veces que la cuenta aparece en solicitudes (como ciudadano o revisor), citas o historiales.
     * Si es 0 la cuenta nunca se usó y se puede eliminar sin romper registros.
     */
    public int contarUsos(int idUsuario) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM solicitud WHERE id_usuario = ? OR id_revisor = ?) "
                + "     + (SELECT COUNT(*) FROM cita WHERE id_usuario = ?) "
                + "     + (SELECT COUNT(*) FROM solicitud_historial WHERE id_usuario = ?)";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setInt(i, idUsuario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Elimina una cuenta que nunca se usó. Sus notificaciones y sesiones se borran con ella (ON DELETE CASCADE);
     * sus líneas de bitácora se conservan sin usuario (ON DELETE SET NULL) y queda constancia de quién la eliminó.
     */
    public void eliminar(Usuario admin, int idUsuario, String ip) throws SQLException, ReglaNegocioException {
        CuentaUsuario c = buscar(idUsuario);
        if (c == null) {
            throw new ReglaNegocioException("La cuenta no existe.");
        }
        if (c.isSuperAdmin() || idUsuario == admin.getIdUsuario()) {
            throw new ReglaNegocioException("La cuenta del super administrador no se puede eliminar.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                // Se vuelve a contar justo antes de borrar por si se usó mientras tanto
                if (contarUsos(idUsuario) > 0) {
                    throw new ReglaNegocioException("La cuenta ya tiene solicitudes, citas o historial: no se puede eliminar. Suspéndela.");
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuario WHERE id_usuario = ?")) {
                    ps.setInt(1, idUsuario);
                    ps.executeUpdate();
                }
                NotificacionDAO.registrarBitacora(con, admin.getIdUsuario(), "GESTION_USUARIO",
                        "Cuenta eliminada (sin uso): " + c.getCorreo() + " / DUI " + c.getDui() + " (" + c.getRol() + ")", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // ------------------------------------------------------------------
    private String where(String texto, String rol, String estado, Integer idInstitucion, List<Object> params) {
        StringBuilder w = new StringBuilder("WHERE 1 = 1 ");
        if (texto != null && !texto.isBlank()) {
            w.append("AND (u.nombre_completo LIKE ? OR u.correo LIKE ? OR REPLACE(u.dui, '-', '') LIKE ?) ");
            String t = "%" + texto.trim() + "%";
            params.add(t);
            params.add(t);
            params.add("%" + texto.replaceAll("\\D", "") + "%");
        }
        if (rol != null && !rol.isBlank()) {
            w.append("AND r.nombre = ? ");
            params.add(rol);
        }
        if (estado != null && !estado.isBlank()) {
            w.append("AND u.estado = ? ");
            params.add(estado);
        }
        if (idInstitucion != null) {
            w.append("AND u.id_institucion = ? ");
            params.add(idInstitucion);
        }
        return w.toString();
    }

    private void validarDatos(CuentaUsuario c, boolean nuevo) throws ReglaNegocioException {
        if (!"REVISOR".equals(c.getRol()) && !"OPERADOR".equals(c.getRol())) {
            throw new ReglaNegocioException("El rol debe ser Revisor u Operador (solo existe un super administrador).");
        }
        if (c.getNombreCompleto() == null || c.getNombreCompleto().length() < 5 || c.getNombreCompleto().length() > 120) {
            throw new ReglaNegocioException("Escribe el nombre completo (5 a 120 caracteres).");
        }
        if (nuevo && (c.getDui() == null || !c.getDui().matches("\\d{8}-\\d"))) {
            throw new ReglaNegocioException("El DUI debe tener el formato 00000000-0.");
        }
        if (c.getCorreo() == null || !c.getCorreo().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") || c.getCorreo().length() > 120) {
            throw new ReglaNegocioException("Escribe un correo electrónico válido.");
        }
        if (c.getTelefono() != null && !c.getTelefono().matches("\\d{4}-\\d{4}")) {
            throw new ReglaNegocioException("El teléfono debe tener el formato 7000-0000.");
        }
        if (c.getIdInstitucion() == null) {
            throw new ReglaNegocioException("Asigna la institución del funcionario.");
        }
        if (c.getCargo() != null && c.getCargo().length() > 100) {
            throw new ReglaNegocioException("El cargo admite máximo 100 caracteres.");
        }
    }

    private void validarUnicos(Connection con, CuentaUsuario c, int excluirId) throws SQLException, ReglaNegocioException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT dui, correo FROM usuario WHERE (dui = ? OR correo = ?) AND id_usuario <> ? LIMIT 1")) {
            ps.setString(1, c.getDui());
            ps.setString(2, c.getCorreo());
            ps.setInt(3, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new ReglaNegocioException(c.getCorreo().equalsIgnoreCase(rs.getString("correo"))
                            ? "Ya existe una cuenta con ese correo." : "Ya existe una cuenta con ese DUI.");
                }
            }
        }
    }

    private void validarSede(Connection con, CuentaUsuario c) throws SQLException, ReglaNegocioException {
        if (c.getIdSede() == null) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM sede WHERE id_sede = ? AND id_institucion = ?")) {
            ps.setInt(1, c.getIdSede());
            ps.setInt(2, c.getIdInstitucion());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocioException("La sede elegida no pertenece a la institución del funcionario.");
                }
            }
        }
    }

    private static int idRol(String rol) {
        return "OPERADOR".equals(rol) ? ROL_OPERADOR : ROL_REVISOR;
    }

    private static void setEnteroONulo(PreparedStatement ps, int i, Integer v) throws SQLException {
        if (v == null) {
            ps.setNull(i, Types.INTEGER);
        } else {
            ps.setInt(i, v);
        }
    }

    private CuentaUsuario mapear(ResultSet rs) throws SQLException {
        CuentaUsuario c = new CuentaUsuario();
        c.setIdUsuario(rs.getInt("id_usuario"));
        c.setRol(rs.getString("rol"));
        c.setDui(rs.getString("dui"));
        c.setNombreCompleto(rs.getString("nombre_completo"));
        c.setCorreo(rs.getString("correo"));
        c.setTelefono(rs.getString("telefono"));
        c.setEstado(rs.getString("estado"));
        int inst = rs.getInt("id_institucion");
        c.setIdInstitucion(rs.wasNull() ? null : inst);
        c.setInstitucionSiglas(rs.getString("siglas"));
        int sede = rs.getInt("id_sede");
        c.setIdSede(rs.wasNull() ? null : sede);
        c.setSedeNombre(rs.getString("sede"));
        c.setCargo(rs.getString("cargo"));
        c.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        Timestamp acceso = rs.getTimestamp("ultimo_acceso");
        c.setUltimoAcceso(acceso == null ? null : acceso.toLocalDateTime());
        c.setNumSolicitudes(rs.getInt("num_solicitudes"));
        java.sql.Date nacimiento = rs.getDate("fecha_nacimiento");
        c.setFechaNacimiento(nacimiento == null ? null : nacimiento.toLocalDate());
        c.setEstadoCivil(rs.getString("estado_civil"));
        c.setDireccion(rs.getString("direccion"));
        c.setMunicipioNombre(rs.getString("municipio"));
        c.setDepartamentoNombre(rs.getString("departamento"));
        c.setDistritoNombre(rs.getString("distrito"));
        return c;
    }
}
