package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SolicitudDAO {

    /**
     * Guarda una solicitud de partida de nacimiento en UNA transacción:
     *   solicitud + solicitud_partida + historial + notificación + bitácora.
     * Si algo falla, no se guarda nada (rollback).
     *
     * @return el folio generado (ej. EXP-2026-00003)
     */
    public String crearPartida(int idUsuario, int idTramite, SolicitudPartida datos, String ip)
            throws SQLException {
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                int idSolicitud = insertarSolicitud(con, idUsuario, idTramite, null);
                String folio = asignarFolio(con, idSolicitud);

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO solicitud_partida (id_solicitud, nombre_inscrito, relacion, "
                        + "fecha_nacimiento, id_municipio_inscripcion, uso) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, idSolicitud);
                    ps.setString(2, datos.getNombreInscrito());
                    ps.setString(3, datos.getRelacion());
                    ps.setDate(4, Date.valueOf(datos.getFechaNacimiento()));
                    ps.setInt(5, datos.getIdMunicipioInscripcion());
                    ps.setString(6, datos.getUso());
                    ps.executeUpdate();
                }

                registrarEnvio(con, idSolicitud, idUsuario, folio,
                        "Partida de nacimiento", ip);
                con.commit();
                return folio;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Guarda una solicitud municipal (solvencia o constancia de residencia) en una transacción.
     * @param idSede alcaldía del municipio (puede ser null si no hay sede registrada)
     */
    public String crearMunicipal(int idUsuario, Tramite tramite, Integer idSede, SolicitudMunicipal datos, String ip)
            throws SQLException {
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                int idSolicitud = insertarSolicitud(con, idUsuario, tramite.getIdTramite(), idSede);
                String folio = asignarFolio(con, idSolicitud);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO solicitud_municipal (id_solicitud, id_municipio, domicilio) VALUES (?, ?, ?)")) {
                    ps.setInt(1, idSolicitud);
                    ps.setInt(2, datos.getIdMunicipio());
                    ps.setString(3, datos.getDomicilio());
                    ps.executeUpdate();
                }
                registrarEnvio(con, idSolicitud, idUsuario, folio, tramite.getNombre(), ip);
                con.commit();
                return folio;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Guarda una solicitud de DUI con sus archivos en una transacción.
     * Los archivos se escriben en disco ANTES del commit; si la base falla, se borran.
     */
    public String crearDui(int idUsuario, Tramite tramite, int idSede, SolicitudDui datos,
            List<ArchivoSubido> archivos, java.nio.file.Path raizArchivos, String ip)
            throws SQLException, java.io.IOException {
        List<String> escritos = new ArrayList<>();
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                int idSolicitud = insertarSolicitud(con, idUsuario, tramite.getIdTramite(), idSede);
                String folio = asignarFolio(con, idSolicitud);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO solicitud_dui (id_solicitud, motivo, id_municipio_res) VALUES (?, ?, ?)")) {
                    ps.setInt(1, idSolicitud);
                    ps.setString(2, datos.getMotivo());
                    ps.setInt(3, datos.getIdMunicipioRes());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO documento (id_solicitud, id_requisito, nombre_archivo, ruta_archivo, "
                        + "tipo_mime, tamano_kb, hash_sha256) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    for (ArchivoSubido a : archivos) {
                        String ruta = com.mycompany.ciudadanosv.util.AlmacenArchivos.guardar(
                                raizArchivos, folio, a.getTipoMime(), a.getContenido());
                        escritos.add(ruta);
                        ps.setInt(1, idSolicitud);
                        ps.setInt(2, a.getIdRequisito());
                        ps.setString(3, a.getNombreOriginal());
                        ps.setString(4, ruta);
                        ps.setString(5, a.getTipoMime());
                        ps.setInt(6, a.getTamanoKb());
                        ps.setString(7, com.mycompany.ciudadanosv.util.AlmacenArchivos.sha256(a.getContenido()));
                        ps.executeUpdate();
                    }
                }
                registrarEnvio(con, idSolicitud, idUsuario, folio, tramite.getNombre(), ip);
                con.commit();
                return folio;
            } catch (SQLException | java.io.IOException ex) {
                con.rollback();
                for (String ruta : escritos) {
                    com.mycompany.ciudadanosv.util.AlmacenArchivos.borrarSilencioso(raizArchivos, ruta);
                }
                throw ex;
            }
        }
    }

    /** Datos municipales de la solicitud (null si no es municipal). */
    public SolicitudMunicipal buscarMunicipal(int idSolicitud) throws SQLException {
        String sql = "SELECT sm.id_municipio, sm.domicilio, m.nombre AS municipio "
                + "FROM solicitud_municipal sm JOIN municipio m ON m.id_municipio = sm.id_municipio "
                + "WHERE sm.id_solicitud = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                SolicitudMunicipal m = new SolicitudMunicipal();
                m.setIdMunicipio(rs.getInt("id_municipio"));
                m.setDomicilio(rs.getString("domicilio"));
                m.setMunicipioNombre(rs.getString("municipio"));
                return m;
            }
        }
    }

    /** Datos de DUI de la solicitud (null si no es de DUI). */
    public SolicitudDui buscarDui(int idSolicitud) throws SQLException {
        String sql = "SELECT sd.motivo, sd.id_municipio_res, m.nombre AS municipio, d.nombre AS departamento "
                + "FROM solicitud_dui sd JOIN municipio m ON m.id_municipio = sd.id_municipio_res "
                + "JOIN departamento d ON d.id_departamento = m.id_departamento "
                + "WHERE sd.id_solicitud = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                SolicitudDui d = new SolicitudDui();
                d.setMotivo(rs.getString("motivo"));
                d.setIdMunicipioRes(rs.getInt("id_municipio_res"));
                d.setMunicipioNombre(rs.getString("municipio"));
                d.setDepartamentoNombre(rs.getString("departamento"));
                return d;
            }
        }
    }

    /** Un documento con su ruta y el dueño de la solicitud (para descargarlo). Null si no existe. */
    public Documento buscarDocumento(int idDocumento) throws SQLException {
        String sql = "SELECT d.id_documento, d.id_solicitud, d.nombre_archivo, d.ruta_archivo, d.tipo_mime, "
                + "       d.tamano_kb, d.estado_cotejo, s.id_usuario "
                + "FROM documento d JOIN solicitud s ON s.id_solicitud = d.id_solicitud "
                + "WHERE d.id_documento = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Documento d = new Documento();
                d.setIdDocumento(rs.getInt("id_documento"));
                d.setIdSolicitud(rs.getInt("id_solicitud"));
                d.setNombreArchivo(rs.getString("nombre_archivo"));
                d.setRutaArchivo(rs.getString("ruta_archivo"));
                d.setTipoMime(rs.getString("tipo_mime"));
                d.setTamanoKb(rs.getInt("tamano_kb"));
                d.setEstadoCotejo(rs.getString("estado_cotejo"));
                d.setIdUsuarioDueno(rs.getInt("id_usuario"));
                return d;
            }
        }
    }

    /** Columnas comunes para listar y ver solicitudes (con trámite, área, institución, sede y revisor). */
    private static final String SELECT_SOLICITUD =
            "SELECT s.id_solicitud, s.folio, s.id_usuario, s.id_tramite, s.estado, s.observacion_ciudadano, "
            + "       s.fecha_creacion, s.fecha_actualizacion, "
            + "       t.nombre AS tramite, t.codigo, t.requiere_cita, "
            + "       a.id_area, a.nombre AS area, a.icono, "
            + "       i.siglas, i.nombre AS institucion, "
            + "       se.nombre AS sede, rv.nombre_completo AS revisor "
            + "FROM solicitud s "
            + "JOIN tramite t       ON t.id_tramite = s.id_tramite "
            + "JOIN area_servicio a ON a.id_area = t.id_area "
            + "JOIN institucion i   ON i.id_institucion = t.id_institucion "
            + "LEFT JOIN sede se    ON se.id_sede = s.id_sede "
            + "LEFT JOIN usuario rv ON rv.id_usuario = s.id_revisor ";

    /** Una solicitud del ciudadano por folio (null si no existe o no es suya). */
    public Solicitud buscarPorFolio(String folio, int idUsuario) throws SQLException {
        String sql = SELECT_SOLICITUD + "WHERE s.folio = ? AND s.id_usuario = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, folio);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Todas las solicitudes del ciudadano (sin borradores), de la más reciente a la más antigua. */
    public List<Solicitud> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = SELECT_SOLICITUD
                + "WHERE s.id_usuario = ? AND s.estado <> 'BORRADOR' "
                + "ORDER BY s.fecha_creacion DESC, s.id_solicitud DESC";
        List<Solicitud> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Historial visible para el ciudadano (sin observaciones internas), en orden cronológico. */
    public List<HistorialSolicitud> listarHistorial(int idSolicitud) throws SQLException {
        String sql = "SELECT h.estado_anterior, h.estado_nuevo, h.comentario, h.fecha, "
                + "       u.nombre_completo, r.nombre AS rol "
                + "FROM solicitud_historial h "
                + "LEFT JOIN usuario u ON u.id_usuario = h.id_usuario "
                + "LEFT JOIN rol r     ON r.id_rol = u.id_rol "
                + "WHERE h.id_solicitud = ? AND h.es_interno = FALSE "
                + "ORDER BY h.fecha, h.id_historial";
        List<HistorialSolicitud> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialSolicitud h = new HistorialSolicitud();
                    h.setEstadoAnterior(rs.getString("estado_anterior"));
                    h.setEstadoNuevo(rs.getString("estado_nuevo"));
                    h.setComentario(rs.getString("comentario"));
                    h.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    h.setUsuarioNombre(rs.getString("nombre_completo"));
                    h.setUsuarioRol(rs.getString("rol"));
                    lista.add(h);
                }
            }
        }
        return lista;
    }

    /** Documentos adjuntos de la solicitud. */
    public List<Documento> listarDocumentos(int idSolicitud) throws SQLException {
        String sql = "SELECT d.id_documento, d.id_solicitud, d.nombre_archivo, d.tipo_mime, d.tamano_kb, "
                + "       d.estado_cotejo, r.nombre AS requisito "
                + "FROM documento d LEFT JOIN requisito r ON r.id_requisito = d.id_requisito "
                + "WHERE d.id_solicitud = ? ORDER BY d.fecha_carga";
        List<Documento> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Documento d = new Documento();
                    d.setIdDocumento(rs.getInt("id_documento"));
                    d.setIdSolicitud(rs.getInt("id_solicitud"));
                    d.setNombreArchivo(rs.getString("nombre_archivo"));
                    d.setTipoMime(rs.getString("tipo_mime"));
                    d.setTamanoKb(rs.getInt("tamano_kb"));
                    d.setEstadoCotejo(rs.getString("estado_cotejo"));
                    d.setRequisitoNombre(rs.getString("requisito"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    /** Datos de la partida de nacimiento (null si la solicitud no es de partida). */
    public SolicitudPartida buscarPartida(int idSolicitud) throws SQLException {
        String sql = "SELECT p.nombre_inscrito, p.relacion, p.fecha_nacimiento, p.id_municipio_inscripcion, "
                + "       p.uso, m.nombre AS municipio "
                + "FROM solicitud_partida p JOIN municipio m ON m.id_municipio = p.id_municipio_inscripcion "
                + "WHERE p.id_solicitud = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                SolicitudPartida p = new SolicitudPartida();
                p.setNombreInscrito(rs.getString("nombre_inscrito"));
                p.setRelacion(rs.getString("relacion"));
                p.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
                p.setIdMunicipioInscripcion(rs.getInt("id_municipio_inscripcion"));
                p.setUso(rs.getString("uso"));
                p.setMunicipioNombre(rs.getString("municipio"));
                return p;
            }
        }
    }

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setFolio(rs.getString("folio"));
        s.setIdUsuario(rs.getInt("id_usuario"));
        s.setIdTramite(rs.getInt("id_tramite"));
        s.setEstado(rs.getString("estado"));
        s.setObservacionCiudadano(rs.getString("observacion_ciudadano"));
        s.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        s.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
        s.setTramiteNombre(rs.getString("tramite"));
        s.setTramiteCodigo(rs.getString("codigo"));
        s.setRequiereCita(rs.getBoolean("requiere_cita"));
        s.setIdArea(rs.getInt("id_area"));
        s.setAreaNombre(rs.getString("area"));
        s.setAreaIcono(rs.getString("icono"));
        s.setInstitucionSiglas(rs.getString("siglas"));
        s.setInstitucionNombre(rs.getString("institucion"));
        s.setSedeNombre(rs.getString("sede"));
        s.setRevisorNombre(rs.getString("revisor"));
        return s;
    }

    // ------------------------------------------------------------------
    // Piezas reutilizables para los demás trámites (municipal, DUI...)
    // ------------------------------------------------------------------

    /** Inserta la fila en solicitud (estado RECIBIDA) con un folio temporal. Devuelve el id. */
    int insertarSolicitud(Connection con, int idUsuario, int idTramite, Integer idSede) throws SQLException {
        String folioTemporal = "TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO solicitud (folio, id_usuario, id_tramite, id_sede, estado, declaracion_jurada) "
                + "VALUES (?, ?, ?, ?, 'RECIBIDA', TRUE)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, folioTemporal);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idTramite);
            if (idSede == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, idSede);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Folio definitivo a partir del id: EXP-2026-00012. Único porque el id es único. */
    String asignarFolio(Connection con, int idSolicitud) throws SQLException {
        String folio = String.format("EXP-%d-%05d", Year.now().getValue(), idSolicitud);
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE solicitud SET folio = ? WHERE id_solicitud = ?")) {
            ps.setString(1, folio);
            ps.setInt(2, idSolicitud);
            ps.executeUpdate();
        }
        return folio;
    }

    /** Historial (RECIBIDA) + notificación al ciudadano + bitácora. */
    void registrarEnvio(Connection con, int idSolicitud, int idUsuario, String folio,
            String nombreTramite, String ip) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO solicitud_historial (id_solicitud, estado_anterior, estado_nuevo, comentario, id_usuario) "
                + "VALUES (?, NULL, 'RECIBIDA', 'Solicitud enviada por el ciudadano', ?)")) {
            ps.setInt(1, idSolicitud);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO notificacion (id_usuario, tipo, titulo, mensaje, id_solicitud) "
                + "VALUES (?, 'TRAMITE', ?, ?, ?)")) {
            ps.setInt(1, idUsuario);
            ps.setString(2, "Solicitud recibida: " + nombreTramite);
            ps.setString(3, "Tu solicitud " + folio + " fue recibida y está pendiente de revisión.");
            ps.setInt(4, idSolicitud);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO bitacora (id_usuario, accion, descripcion, ip) VALUES (?, 'ENVIO_SOLICITUD', ?, ?)")) {
            ps.setInt(1, idUsuario);
            ps.setString(2, "Solicitud " + folio + " (" + nombreTramite + ") enviada");
            ps.setString(3, ip);
            ps.executeUpdate();
        }
    }
}
