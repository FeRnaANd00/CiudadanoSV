package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bandeja de notificaciones y bitácora del ciudadano.
 * Todas las consultas filtran por id_usuario: nadie puede ver ni tocar avisos de otro.
 */
public class NotificacionDAO {

    /** Tipos que se pueden usar como filtro en la bandeja. */
    public static final Set<String> TIPOS_FILTRO = Set.of("TRAMITE", "CITA", "SEGURIDAD");

    /**
     * Avisos no archivados del usuario, del más reciente al más antiguo.
     * @param tipo TRAMITE, CITA, SEGURIDAD o null para todos
     */
    public List<Notificacion> listar(int idUsuario, String tipo) throws SQLException {
        String sql = "SELECT n.id_notificacion, n.tipo, n.titulo, n.mensaje, n.id_solicitud, n.id_cita, "
                + "       n.leida, n.archivada, n.fecha, s.folio "
                + "FROM notificacion n "
                + "LEFT JOIN solicitud s ON s.id_solicitud = n.id_solicitud AND s.id_usuario = n.id_usuario "
                + "WHERE n.id_usuario = ? AND n.archivada = FALSE "
                + (tipo != null ? "AND n.tipo = ? " : "")
                + "ORDER BY n.fecha DESC, n.id_notificacion DESC LIMIT 100";
        List<Notificacion> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            if (tipo != null) {
                ps.setString(2, tipo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notificacion n = new Notificacion();
                    n.setIdNotificacion(rs.getInt("id_notificacion"));
                    n.setTipo(rs.getString("tipo"));
                    n.setTitulo(rs.getString("titulo"));
                    n.setMensaje(rs.getString("mensaje"));
                    n.setIdSolicitud((Integer) rs.getObject("id_solicitud", Integer.class));
                    n.setIdCita((Integer) rs.getObject("id_cita", Integer.class));
                    n.setLeida(rs.getBoolean("leida"));
                    n.setArchivada(rs.getBoolean("archivada"));
                    n.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    n.setFolioSolicitud(rs.getString("folio"));
                    lista.add(n);
                }
            }
        }
        return lista;
    }

    /** Cantidad de avisos (no archivados) por tipo, más "TODAS" y "NO_LEIDAS". */
    public Map<String, Integer> contarPorTipo(int idUsuario) throws SQLException {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        conteo.put("TODAS", 0);
        conteo.put("NO_LEIDAS", 0);
        for (String t : new String[]{"TRAMITE", "CITA", "SEGURIDAD"}) {
            conteo.put(t, 0);
        }
        String sql = "SELECT tipo, COUNT(*) AS total, SUM(leida = FALSE) AS no_leidas "
                + "FROM notificacion WHERE id_usuario = ? AND archivada = FALSE GROUP BY tipo";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int total = rs.getInt("total");
                    conteo.merge(rs.getString("tipo"), total, Integer::sum);
                    conteo.merge("TODAS", total, Integer::sum);
                    conteo.merge("NO_LEIDAS", rs.getInt("no_leidas"), Integer::sum);
                }
            }
        }
        return conteo;
    }

    /** Número para la campanita del menú. */
    public int contarNoLeidas(int idUsuario) throws SQLException {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM notificacion WHERE id_usuario = ? AND leida = FALSE AND archivada = FALSE")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Busca un aviso del usuario (null si no existe o es de otra persona). */
    public Notificacion buscar(int idUsuario, int idNotificacion) throws SQLException {
        String sql = "SELECT n.id_notificacion, n.tipo, n.id_solicitud, n.id_cita, n.leida, s.folio "
                + "FROM notificacion n "
                + "LEFT JOIN solicitud s ON s.id_solicitud = n.id_solicitud AND s.id_usuario = n.id_usuario "
                + "WHERE n.id_notificacion = ? AND n.id_usuario = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Notificacion n = new Notificacion();
                n.setIdNotificacion(rs.getInt("id_notificacion"));
                n.setTipo(rs.getString("tipo"));
                n.setIdSolicitud((Integer) rs.getObject("id_solicitud", Integer.class));
                n.setIdCita((Integer) rs.getObject("id_cita", Integer.class));
                n.setLeida(rs.getBoolean("leida"));
                n.setFolioSolicitud(rs.getString("folio"));
                return n;
            }
        }
    }

    /** Marca un aviso como leído. Devuelve false si no es del usuario. */
    public boolean marcarLeida(int idUsuario, int idNotificacion) throws SQLException {
        return actualizar("UPDATE notificacion SET leida = TRUE WHERE id_notificacion = ? AND id_usuario = ?",
                idNotificacion, idUsuario) > 0;
    }

    /** Archiva (oculta de la bandeja) un aviso; queda marcado como leído. */
    public boolean archivar(int idUsuario, int idNotificacion) throws SQLException {
        return actualizar("UPDATE notificacion SET leida = TRUE, archivada = TRUE "
                + "WHERE id_notificacion = ? AND id_usuario = ?", idNotificacion, idUsuario) > 0;
    }

    /** Marca como leídos todos los avisos del usuario (o solo los de un tipo). Devuelve cuántos cambió. */
    public int marcarTodasLeidas(int idUsuario, String tipo) throws SQLException {
        String sql = "UPDATE notificacion SET leida = TRUE WHERE id_usuario = ? AND leida = FALSE"
                + (tipo != null ? " AND tipo = ?" : "");
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            if (tipo != null) {
                ps.setString(2, tipo);
            }
            return ps.executeUpdate();
        }
    }

    /** Actividad del usuario (bitácora), de la más reciente a la más antigua. */
    public List<RegistroBitacora> listarBitacora(int idUsuario, int limite) throws SQLException {
        String sql = "SELECT id_bitacora, accion, descripcion, ip, fecha FROM bitacora "
                + "WHERE id_usuario = ? ORDER BY fecha DESC, id_bitacora DESC LIMIT ?";
        List<RegistroBitacora> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RegistroBitacora b = new RegistroBitacora();
                    b.setIdBitacora(rs.getLong("id_bitacora"));
                    b.setAccion(rs.getString("accion"));
                    b.setDescripcion(rs.getString("descripcion"));
                    b.setIp(rs.getString("ip"));
                    b.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    lista.add(b);
                }
            }
        }
        return lista;
    }

    // ------------------------------------------------------------------
    // Para usar dentro de las transacciones de otros DAO (perfil, administración...)

    /** Crea un aviso usando la conexión (y transacción) de quien llama. */
    public static void crear(Connection con, int idUsuario, String tipo, String titulo, String mensaje,
            Integer idSolicitud, Integer idCita) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO notificacion (id_usuario, tipo, titulo, mensaje, id_solicitud, id_cita) "
                + "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo);
            ps.setString(3, recortar(titulo, 120));
            ps.setString(4, recortar(mensaje, 500));
            if (idSolicitud == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, idSolicitud);
            }
            if (idCita == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, idCita);
            }
            ps.executeUpdate();
        }
    }

    /** Agrega una línea a la bitácora (solo INSERT: la bitácora no se modifica). */
    public static void registrarBitacora(Connection con, Integer idUsuario, String accion, String descripcion,
            String ip) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO bitacora (id_usuario, accion, descripcion, ip) VALUES (?, ?, ?, ?)")) {
            if (idUsuario == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, idUsuario);
            }
            ps.setString(2, accion);
            ps.setString(3, recortar(descripcion, 300));
            ps.setString(4, ip);
            ps.executeUpdate();
        }
    }

    /** Recorta al tamaño de la columna para que un texto largo no haga fallar la transacción. */
    private static String recortar(String texto, int max) {
        return texto == null || texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }

    private int actualizar(String sql, int idNotificacion, int idUsuario) throws SQLException {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate();
        }
    }
}
