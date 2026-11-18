package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bandeja de solicitudes y resolución (administración).
 *
 * Alcance: el SUPER_ADMIN ve todas las instituciones; revisores y operadores solo
 * las solicitudes de trámites de su institución (usuario.id_institucion).
 * Todo cambio de estado se registra en el historial, se notifica al ciudadano y queda en la bitácora.
 */
public class SolicitudAdminDAO {

    public static final int POR_PAGINA = 15;

    private static final Set<String> ESTADOS_FINALES = Set.of("APROBADA", "RECHAZADA", "FINALIZADA");

    private static final String SELECT =
            "SELECT s.id_solicitud, s.folio, s.id_usuario, s.id_tramite, s.id_sede, s.id_revisor, s.estado, "
            + "       s.observacion_ciudadano, s.observacion_interna, s.fecha_creacion, s.fecha_actualizacion, "
            + "       s.fecha_resolucion, "
            + "       t.nombre AS tramite, t.codigo, t.requiere_cita, t.requiere_biometria, t.id_institucion, "
            + "       a.id_area, a.nombre AS area, a.icono, i.siglas, i.nombre AS institucion, "
            + "       se.nombre AS sede, rv.nombre_completo AS revisor, "
            + "       c.nombre_completo AS ciudadano, c.dui, c.correo, c.telefono, c.fecha_nacimiento, c.direccion, "
            + "       cm.nombre AS c_municipio, cdep.nombre AS c_departamento, cdis.nombre AS c_distrito "
            + "FROM solicitud s "
            + "JOIN tramite t       ON t.id_tramite = s.id_tramite "
            + "JOIN area_servicio a ON a.id_area = t.id_area "
            + "JOIN institucion i   ON i.id_institucion = t.id_institucion "
            + "JOIN usuario c       ON c.id_usuario = s.id_usuario "
            + "LEFT JOIN sede se    ON se.id_sede = s.id_sede "
            + "LEFT JOIN usuario rv ON rv.id_usuario = s.id_revisor "
            + "LEFT JOIN municipio cm ON cm.id_municipio = c.id_municipio "
            + "LEFT JOIN departamento cdep ON cdep.id_departamento = cm.id_departamento "
            + "LEFT JOIN distrito cdis ON cdis.id_distrito = c.id_distrito ";

    /** Filtros de la bandeja (todos opcionales). */
    public static class Filtro {
        public String texto;          // folio o DUI (con o sin guion)
        public String estado;         // estado exacto, o "ABIERTAS"
        public Integer idInstitucion; // solo el super admin puede elegirla
        public Integer idTramite;
        public boolean soloMias;      // asignadas al funcionario
        public int pagina = 1;

        // Getters para las vistas (EL solo lee propiedades con get/is)
        public String getTexto() { return texto; }
        public String getEstado() { return estado; }
        public Integer getIdInstitucion() { return idInstitucion; }
        public Integer getIdTramite() { return idTramite; }
        public boolean isSoloMias() { return soloMias; }
        public int getPagina() { return pagina; }
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Página de la bandeja según filtros y alcance del funcionario. */
    public List<Solicitud> listar(Usuario funcionario, Filtro f) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = construirWhere(funcionario, f, params);
        String sql = SELECT + where
                + "ORDER BY (s.estado IN ('RECIBIDA','EN_REVISION','CITA_PROGRAMADA')) DESC, s.fecha_creacion ASC "
                + "LIMIT ? OFFSET ?";
        params.add(POR_PAGINA);
        params.add((Math.max(1, f.pagina) - 1) * POR_PAGINA);
        List<Solicitud> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Total de resultados con los mismos filtros (para la paginación). */
    public int contar(Usuario funcionario, Filtro f) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM solicitud s JOIN tramite t ON t.id_tramite = s.id_tramite "
                + "JOIN usuario c ON c.id_usuario = s.id_usuario " + construirWhere(funcionario, f, params);
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Indicadores de la bandeja dentro del alcance: cantidad por estado, "ABIERTAS",
     * "PRIORIDAD" (abiertas con más de 24 h) y "SIN_ASIGNAR".
     */
    public Map<String, Integer> indicadores(Usuario funcionario) throws SQLException {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (String e : new String[]{"TOTAL", "ABIERTAS", "PRIORIDAD", "SIN_ASIGNAR",
            "RECIBIDA", "EN_REVISION", "CITA_PROGRAMADA", "APROBADA", "RECHAZADA", "FINALIZADA"}) {
            m.put(e, 0);
        }
        List<Object> params = new ArrayList<>();
        String sql = "SELECT s.estado, COUNT(*) AS total, "
                + "  SUM(s.fecha_creacion < NOW() - INTERVAL 24 HOUR) AS viejas, "
                + "  SUM(s.id_revisor IS NULL) AS sin_asignar "
                + "FROM solicitud s JOIN tramite t ON t.id_tramite = s.id_tramite "
                + "WHERE s.estado <> 'BORRADOR' " + alcance(funcionario, params)
                + "GROUP BY s.estado";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString("estado");
                    int total = rs.getInt("total");
                    m.merge(estado, total, Integer::sum);
                    m.merge("TOTAL", total, Integer::sum);
                    if (!ESTADOS_FINALES.contains(estado)) {
                        m.merge("ABIERTAS", total, Integer::sum);
                        m.merge("PRIORIDAD", rs.getInt("viejas"), Integer::sum);
                        m.merge("SIN_ASIGNAR", rs.getInt("sin_asignar"), Integer::sum);
                    }
                }
            }
        }
        return m;
    }

    /** Una solicitud por folio dentro del alcance del funcionario (null si no existe o no le corresponde). */
    public Solicitud buscar(Usuario funcionario, String folio) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(folio);
        String sql = SELECT + "WHERE s.folio = ? AND s.estado <> 'BORRADOR' " + alcance(funcionario, params);
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Historial completo, incluidas las observaciones internas. */
    public List<HistorialSolicitud> listarHistorialCompleto(int idSolicitud) throws SQLException {
        String sql = "SELECT h.estado_anterior, h.estado_nuevo, h.comentario, h.es_interno, h.fecha, "
                + "       u.nombre_completo, r.nombre AS rol "
                + "FROM solicitud_historial h "
                + "LEFT JOIN usuario u ON u.id_usuario = h.id_usuario "
                + "LEFT JOIN rol r     ON r.id_rol = u.id_rol "
                + "WHERE h.id_solicitud = ? ORDER BY h.fecha DESC, h.id_historial DESC";
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
                    h.setInterno(rs.getBoolean("es_interno"));
                    h.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    h.setUsuarioNombre(rs.getString("nombre_completo"));
                    h.setUsuarioRol(rs.getString("rol"));
                    lista.add(h);
                }
            }
        }
        return lista;
    }

    /** Citas ligadas a la solicitud (cita biométrica). */
    public List<Cita> listarCitas(int idSolicitud) throws SQLException {
        String sql = "SELECT ci.id_cita, ci.codigo, ci.fecha_hora, ci.numero_turno, ci.estado, "
                + "       se.nombre AS sede "
                + "FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo JOIN sede se ON se.id_sede = cu.id_sede "
                + "WHERE ci.id_solicitud = ? ORDER BY ci.fecha_hora DESC";
        List<Cita> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cita c = new Cita();
                    c.setIdCita(rs.getInt("id_cita"));
                    c.setCodigo(rs.getString("codigo"));
                    c.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                    c.setNumeroTurno(rs.getInt("numero_turno"));
                    c.setEstado(rs.getString("estado"));
                    c.setSedeNombre(rs.getString("sede"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    /** Trámites que aparecen en el filtro de la bandeja (los de la institución, o todos para el super admin). */
    public List<Tramite> listarTramitesFiltro(Usuario funcionario) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT t.id_tramite, t.codigo, t.nombre, i.siglas FROM tramite t "
                + "JOIN institucion i ON i.id_institucion = t.id_institucion "
                + "WHERE EXISTS (SELECT 1 FROM solicitud s WHERE s.id_tramite = t.id_tramite) "
                + alcance(funcionario, params) + "ORDER BY i.siglas, t.nombre";
        List<Tramite> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tramite t = new Tramite();
                    t.setIdTramite(rs.getInt("id_tramite"));
                    t.setCodigo(rs.getString("codigo"));
                    t.setNombre(rs.getString("nombre"));
                    t.setInstitucionSiglas(rs.getString("siglas"));
                    lista.add(t);
                }
            }
        }
        return lista;
    }

    // ------------------------------------------------------------------
    // Acciones (cada una en su transacción)
    // ------------------------------------------------------------------

    /**
     * Cambia el estado: valida la transición, guarda la observación para el ciudadano,
     * asigna al funcionario si nadie la tenía, registra historial, notifica y deja bitácora.
     */
    public void cambiarEstado(Usuario funcionario, String folio, String estadoNuevo, String comentario, String ip)
            throws SQLException, ReglaNegocioException {
        if (!funcionario.isPuedeResolver()) {
            throw new ReglaNegocioException("Tu rol no permite resolver solicitudes.");
        }
        Solicitud s = buscar(funcionario, folio);
        if (s == null) {
            throw new ReglaNegocioException("La solicitud no existe o no pertenece a tu institución.");
        }
        if (!s.getEstadosSiguientes().contains(estadoNuevo)) {
            throw new ReglaNegocioException("No se puede pasar de \"" + s.getEstadoTexto() + "\" a \""
                    + Solicitud.textoEstado(estadoNuevo) + "\".");
        }
        if (comentario != null && comentario.length() > 500) {
            throw new ReglaNegocioException("La observación admite máximo 500 caracteres.");
        }
        if ("RECHAZADA".equals(estadoNuevo) && (comentario == null || comentario.length() < 10)) {
            throw new ReglaNegocioException("Para rechazar escribe al ciudadano el motivo (mínimo 10 caracteres).");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                // WHERE estado = actual: si otro funcionario la cambió al mismo tiempo, no se pisa
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE solicitud SET estado = ?, "
                        + "  observacion_ciudadano = COALESCE(?, observacion_ciudadano), "
                        + "  id_revisor = COALESCE(id_revisor, ?), "
                        + "  fecha_resolucion = IF(? IN ('APROBADA','RECHAZADA'), NOW(), fecha_resolucion) "
                        + "WHERE id_solicitud = ? AND estado = ?")) {
                    ps.setString(1, estadoNuevo);
                    ps.setString(2, comentario);
                    ps.setInt(3, funcionario.getIdUsuario());
                    ps.setString(4, estadoNuevo);
                    ps.setInt(5, s.getIdSolicitud());
                    ps.setString(6, s.getEstado());
                    if (ps.executeUpdate() == 0) {
                        throw new ReglaNegocioException("Otro funcionario modificó esta solicitud. Recarga e intenta de nuevo.");
                    }
                }
                cerrarCitasPendientes(con, s.getIdSolicitud(), estadoNuevo);
                insertarHistorial(con, s.getIdSolicitud(), s.getEstado(), estadoNuevo,
                        comentario != null ? comentario : textoCambioPorDefecto(estadoNuevo), false, funcionario.getIdUsuario());
                NotificacionDAO.crear(con, s.getIdUsuario(), "TRAMITE",
                        tituloNotificacion(estadoNuevo, s.getTramiteNombre()),
                        "Tu solicitud " + s.getFolio() + " cambió a \"" + Solicitud.textoEstado(estadoNuevo) + "\"."
                        + (comentario != null ? " Observación: " + recortar(comentario, 300) : ""),
                        s.getIdSolicitud(), null);
                NotificacionDAO.registrarBitacora(con, funcionario.getIdUsuario(), "CAMBIO_ESTADO",
                        "Solicitud " + s.getFolio() + ": " + s.getEstadoTexto() + " -> " + Solicitud.textoEstado(estadoNuevo), ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Observación interna: queda en el historial (solo funcionarios) y en la ficha; no se notifica al ciudadano. */
    public void agregarObservacionInterna(Usuario funcionario, String folio, String texto, String ip)
            throws SQLException, ReglaNegocioException {
        if (texto == null || texto.length() < 3) {
            throw new ReglaNegocioException("Escribe la observación interna.");
        }
        if (texto.length() > 500) {
            throw new ReglaNegocioException("La observación admite máximo 500 caracteres.");
        }
        Solicitud s = buscar(funcionario, folio);
        if (s == null) {
            throw new ReglaNegocioException("La solicitud no existe o no pertenece a tu institución.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE solicitud SET observacion_interna = ? WHERE id_solicitud = ?")) {
                    ps.setString(1, texto);
                    ps.setInt(2, s.getIdSolicitud());
                    ps.executeUpdate();
                }
                insertarHistorial(con, s.getIdSolicitud(), s.getEstado(), s.getEstado(), texto, true, funcionario.getIdUsuario());
                NotificacionDAO.registrarBitacora(con, funcionario.getIdUsuario(), "OBSERVACION_INTERNA",
                        "Observación interna en " + s.getFolio(), ip);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** El funcionario toma la solicitud (queda como revisor asignado). */
    public void asignarme(Usuario funcionario, String folio, String ip) throws SQLException, ReglaNegocioException {
        if (!funcionario.isPuedeResolver()) {
            throw new ReglaNegocioException("Tu rol no permite tomar solicitudes.");
        }
        Solicitud s = buscar(funcionario, folio);
        if (s == null) {
            throw new ReglaNegocioException("La solicitud no existe o no pertenece a tu institución.");
        }
        if (!s.isAbierta()) {
            throw new ReglaNegocioException("La solicitud ya está cerrada.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE solicitud SET id_revisor = ? WHERE id_solicitud = ?")) {
                    ps.setInt(1, funcionario.getIdUsuario());
                    ps.setInt(2, s.getIdSolicitud());
                    ps.executeUpdate();
                }
                insertarHistorial(con, s.getIdSolicitud(), s.getEstado(), s.getEstado(),
                        "Asignada a " + funcionario.getNombreCompleto(), true, funcionario.getIdUsuario());
                NotificacionDAO.registrarBitacora(con, funcionario.getIdUsuario(), "ASIGNAR_SOLICITUD",
                        "Tomó la solicitud " + s.getFolio(), ip);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Cotejo de un documento: VALIDO o RECHAZADO (o PENDIENTE para deshacer). */
    public void cotejarDocumento(Usuario funcionario, String folio, int idDocumento, String estado, String ip)
            throws SQLException, ReglaNegocioException {
        if (!funcionario.isPuedeResolver()) {
            throw new ReglaNegocioException("Tu rol no permite cotejar documentos.");
        }
        if (!Set.of("VALIDO", "RECHAZADO", "PENDIENTE").contains(estado)) {
            throw new ReglaNegocioException("Estado de cotejo no válido.");
        }
        Solicitud s = buscar(funcionario, folio);
        if (s == null) {
            throw new ReglaNegocioException("La solicitud no existe o no pertenece a tu institución.");
        }
        if (!s.isAbierta()) {
            throw new ReglaNegocioException("La solicitud ya está cerrada; no se pueden cambiar sus documentos.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                String nombre;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT nombre_archivo FROM documento WHERE id_documento = ? AND id_solicitud = ?")) {
                    ps.setInt(1, idDocumento);
                    ps.setInt(2, s.getIdSolicitud());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("El documento no pertenece a esta solicitud.");
                        }
                        nombre = rs.getString(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE documento SET estado_cotejo = ? WHERE id_documento = ?")) {
                    ps.setString(1, estado);
                    ps.setInt(2, idDocumento);
                    ps.executeUpdate();
                }
                NotificacionDAO.registrarBitacora(con, funcionario.getIdUsuario(), "COTEJO_DOCUMENTO",
                        "Documento " + nombre + " de " + s.getFolio() + " marcado como " + estado, ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Programa la cita biométrica del ciudadano en la sede de la solicitud y la pasa a CITA_PROGRAMADA.
     * Reserva el turno, cambia el estado, historial, notificaciones y bitácora: todo o nada.
     */
    public Cita programarCita(Usuario funcionario, String folio, int idCupo, String ip)
            throws SQLException, ReglaNegocioException {
        if (!funcionario.isPuedeResolver() && !funcionario.isPuedeGestionarCupos()) {
            throw new ReglaNegocioException("Tu rol no permite programar citas.");
        }
        Solicitud s = buscar(funcionario, folio);
        if (s == null) {
            throw new ReglaNegocioException("La solicitud no existe o no pertenece a tu institución.");
        }
        if (!s.isPuedeProgramarCita()) {
            throw new ReglaNegocioException("Solo se programa la cita biométrica de trámites con biometría que están en revisión.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                // El cupo debe ser de la sede y del trámite de la solicitud
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 1 FROM cupo WHERE id_cupo = ? AND id_sede = ? AND id_tramite = ?")) {
                    ps.setInt(1, idCupo);
                    ps.setInt(2, s.getIdSede());
                    ps.setInt(3, s.getIdTramite());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("El horario no corresponde a la sede del trámite.");
                        }
                    }
                }
                Cita cita = new CitaDAO().reservarEn(con, s.getIdUsuario(), idCupo, s.getIdSolicitud(), ip);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE solicitud SET estado = 'CITA_PROGRAMADA', id_revisor = COALESCE(id_revisor, ?) "
                        + "WHERE id_solicitud = ? AND estado = 'EN_REVISION'")) {
                    ps.setInt(1, funcionario.getIdUsuario());
                    ps.setInt(2, s.getIdSolicitud());
                    if (ps.executeUpdate() == 0) {
                        throw new ReglaNegocioException("Otro funcionario modificó esta solicitud. Recarga e intenta de nuevo.");
                    }
                }
                String detalle = "Cita " + cita.getCodigo() + " el " + cita.getFechaTexto() + " a las "
                        + cita.getHoraTexto() + " en " + cita.getSedeNombre() + " (turno #" + cita.getNumeroTurno() + ").";
                insertarHistorial(con, s.getIdSolicitud(), "EN_REVISION", "CITA_PROGRAMADA",
                        "Cita biométrica programada. " + detalle, false, funcionario.getIdUsuario());
                NotificacionDAO.crear(con, s.getIdUsuario(), "TRAMITE",
                        "Cita biométrica programada: " + s.getTramiteNombre(),
                        "Tu solicitud " + s.getFolio() + " requiere que te presentes. " + detalle
                        + " Lleva tu DUI anterior o la denuncia de extravío.", s.getIdSolicitud(), null);
                NotificacionDAO.registrarBitacora(con, funcionario.getIdUsuario(), "PROGRAMAR_CITA",
                        "Cita " + cita.getCodigo() + " para la solicitud " + s.getFolio(), ip);
                con.commit();
                return cita;
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** Condición de alcance por institución (vacía para el super admin). */
    static String alcance(Usuario funcionario, List<Object> params) {
        if (funcionario.isSuperAdmin()) {
            return "";
        }
        // Funcionario sin institución asignada: no ve nada
        params.add(funcionario.getIdInstitucion() == null ? -1 : funcionario.getIdInstitucion());
        return "AND t.id_institucion = ? ";
    }

    private String construirWhere(Usuario funcionario, Filtro f, List<Object> params) {
        StringBuilder w = new StringBuilder("WHERE s.estado <> 'BORRADOR' ");
        w.append(alcance(funcionario, params));
        if (f.texto != null && !f.texto.isBlank()) {
            String t = f.texto.trim().toUpperCase();
            String soloDigitos = t.replaceAll("\\D", "");
            w.append("AND (s.folio LIKE ? OR REPLACE(c.dui, '-', '') = ?) ");
            params.add("%" + t + "%");
            params.add(soloDigitos);
        }
        if ("ABIERTAS".equals(f.estado)) {
            w.append("AND s.estado IN ('RECIBIDA','EN_REVISION','CITA_PROGRAMADA') ");
        } else if (f.estado != null && !f.estado.isBlank()) {
            w.append("AND s.estado = ? ");
            params.add(f.estado);
        }
        if (f.idInstitucion != null && funcionario.isSuperAdmin()) {
            w.append("AND t.id_institucion = ? ");
            params.add(f.idInstitucion);
        }
        if (f.idTramite != null) {
            w.append("AND s.id_tramite = ? ");
            params.add(f.idTramite);
        }
        if (f.soloMias) {
            w.append("AND s.id_revisor = ? ");
            params.add(funcionario.getIdUsuario());
        }
        return w.toString();
    }

    static void asignar(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof Integer n) {
                ps.setInt(i + 1, n);
            } else {
                ps.setString(i + 1, String.valueOf(p));
            }
        }
    }

    private void insertarHistorial(Connection con, int idSolicitud, String anterior, String nuevo,
            String comentario, boolean interno, int idUsuario) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO solicitud_historial (id_solicitud, estado_anterior, estado_nuevo, comentario, es_interno, id_usuario) "
                + "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, idSolicitud);
            ps.setString(2, anterior);
            ps.setString(3, nuevo);
            ps.setString(4, recortar(comentario, 500));
            ps.setBoolean(5, interno);
            ps.setInt(6, idUsuario);
            ps.executeUpdate();
        }
    }

    /**
     * Citas biométricas aún programadas de la solicitud:
     * al aprobar quedan ATENDIDAS; al rechazar se cancelan y se libera el turno.
     */
    private void cerrarCitasPendientes(Connection con, int idSolicitud, String estadoNuevo) throws SQLException {
        if ("RECHAZADA".equals(estadoNuevo)) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE cupo cu JOIN cita ci ON ci.id_cupo = cu.id_cupo "
                    + "SET cu.ocupados = GREATEST(cu.ocupados - 1, 0), ci.estado = 'CANCELADA' "
                    + "WHERE ci.id_solicitud = ? AND ci.estado = 'PROGRAMADA'")) {
                ps.setInt(1, idSolicitud);
                ps.executeUpdate();
            }
        } else if ("APROBADA".equals(estadoNuevo)) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE cita SET estado = 'ATENDIDA' WHERE id_solicitud = ? AND estado = 'PROGRAMADA'")) {
                ps.setInt(1, idSolicitud);
                ps.executeUpdate();
            }
        }
    }

    private static String textoCambioPorDefecto(String estado) {
        return switch (estado) {
            case "EN_REVISION" -> "La solicitud fue tomada para revisión";
            case "APROBADA" -> "Solicitud aprobada";
            case "FINALIZADA" -> "Documento entregado; trámite finalizado";
            default -> null;
        };
    }

    private static String tituloNotificacion(String estado, String tramite) {
        return recortar(switch (estado) {
            case "APROBADA" -> "Solicitud aprobada: ";
            case "RECHAZADA" -> "Solicitud rechazada: ";
            case "FINALIZADA" -> "Trámite finalizado: ";
            default -> "Cambio de estado: ";
        } + tramite, 120);
    }

    private static String recortar(String texto, int max) {
        return texto == null || texto.length() <= max ? texto : texto.substring(0, max);
    }

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setFolio(rs.getString("folio"));
        s.setIdUsuario(rs.getInt("id_usuario"));
        s.setIdTramite(rs.getInt("id_tramite"));
        int idSede = rs.getInt("id_sede");
        s.setIdSede(rs.wasNull() ? null : idSede);
        int idRevisor = rs.getInt("id_revisor");
        s.setIdRevisor(rs.wasNull() ? null : idRevisor);
        s.setEstado(rs.getString("estado"));
        s.setObservacionCiudadano(rs.getString("observacion_ciudadano"));
        s.setObservacionInterna(rs.getString("observacion_interna"));
        s.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        s.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
        Timestamp res = rs.getTimestamp("fecha_resolucion");
        s.setFechaResolucion(res == null ? null : res.toLocalDateTime());
        s.setTramiteNombre(rs.getString("tramite"));
        s.setTramiteCodigo(rs.getString("codigo"));
        s.setRequiereCita(rs.getBoolean("requiere_cita"));
        s.setRequiereBiometria(rs.getBoolean("requiere_biometria"));
        s.setIdInstitucion(rs.getInt("id_institucion"));
        s.setIdArea(rs.getInt("id_area"));
        s.setAreaNombre(rs.getString("area"));
        s.setAreaIcono(rs.getString("icono"));
        s.setInstitucionSiglas(rs.getString("siglas"));
        s.setInstitucionNombre(rs.getString("institucion"));
        s.setSedeNombre(rs.getString("sede"));
        s.setRevisorNombre(rs.getString("revisor"));
        s.setCiudadanoNombre(rs.getString("ciudadano"));
        s.setCiudadanoDui(rs.getString("dui"));
        s.setCiudadanoCorreo(rs.getString("correo"));
        s.setCiudadanoTelefono(rs.getString("telefono"));
        CuentaUsuario datos = new CuentaUsuario();
        java.sql.Date nacimiento = rs.getDate("fecha_nacimiento");
        datos.setFechaNacimiento(nacimiento == null ? null : nacimiento.toLocalDate());
        datos.setDistritoNombre(rs.getString("c_distrito"));
        datos.setMunicipioNombre(rs.getString("c_municipio"));
        datos.setDepartamentoNombre(rs.getString("c_departamento"));
        s.setCiudadanoFechaNacimiento(datos.getFechaNacimientoTexto());
        String lugar = datos.getResidenciaTexto();
        String direccion = rs.getString("direccion");
        if (direccion == null || direccion.isBlank()) {
            s.setCiudadanoResidencia(lugar);
        } else {
            s.setCiudadanoResidencia(lugar.isEmpty() ? direccion : lugar + " — " + direccion);
        }
        return s;
    }
}
