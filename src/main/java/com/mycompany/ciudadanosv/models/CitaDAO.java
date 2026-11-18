package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CitaDAO {

    /** Bloques que se crean solos cuando un día aún no tiene cupos (el admin los ajusta en el paso 11). */
    private static final LocalTime[][] BLOQUES_POR_DEFECTO = {
        {LocalTime.of(7, 0), LocalTime.of(9, 0)},
        {LocalTime.of(9, 0), LocalTime.of(11, 0)},
        {LocalTime.of(11, 0), LocalTime.of(13, 0)},
        {LocalTime.of(13, 0), LocalTime.of(15, 0)}
    };
    private static final int CAPACIDAD_POR_DEFECTO = 10;

    /**
     * Cupos de una sede/trámite en un día. Si ese día hábil todavía no tiene cupos,
     * crea los bloques por defecto (INSERT IGNORE: no duplica si ya existen).
     */
    public List<Cupo> listarCupos(int idSede, int idTramite, LocalDate fecha) throws SQLException {
        try (Connection con = Conexion.conectar()) {
            if (esDiaHabil(fecha)) {
                crearBloquesSiFaltan(con, idSede, idTramite, fecha);
            }
            List<Cupo> lista = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_cupo, id_sede, id_tramite, fecha, hora_inicio, hora_fin, capacidad, ocupados, habilitado "
                    + "FROM cupo WHERE id_sede = ? AND id_tramite = ? AND fecha = ? ORDER BY hora_inicio")) {
                ps.setInt(1, idSede);
                ps.setInt(2, idTramite);
                ps.setDate(3, Date.valueOf(fecha));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearCupo(rs));
                    }
                }
            }
            return lista;
        }
    }

    /** Crea los 4 bloques por defecto solo si el día no tiene ningún cupo para esa sede y trámite. */
    private void crearBloquesSiFaltan(Connection con, int idSede, int idTramite, LocalDate fecha) throws SQLException {
        try (PreparedStatement existe = con.prepareStatement(
                "SELECT 1 FROM cupo WHERE id_sede = ? AND id_tramite = ? AND fecha = ? LIMIT 1")) {
            existe.setInt(1, idSede);
            existe.setInt(2, idTramite);
            existe.setDate(3, Date.valueOf(fecha));
            try (ResultSet rs = existe.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO cupo (id_sede, id_tramite, fecha, hora_inicio, hora_fin, capacidad) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (LocalTime[] b : BLOQUES_POR_DEFECTO) {
                ps.setInt(1, idSede);
                ps.setInt(2, idTramite);
                ps.setDate(3, Date.valueOf(fecha));
                ps.setTime(4, Time.valueOf(b[0]));
                ps.setTime(5, Time.valueOf(b[1]));
                ps.setInt(6, CAPACIDAD_POR_DEFECTO);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Reserva un turno en el cupo, en una transacción con bloqueo de fila (FOR UPDATE):
     * si dos personas reservan el último turno al mismo tiempo, solo una lo obtiene.
     *
     * @return la cita creada (con código, turno y hora)
     * @throws ReglaNegocioException si el cupo está lleno, ya pasó, o el ciudadano ya tiene cita
     */
    public Cita reservar(int idUsuario, int idCupo, String ip) throws SQLException, ReglaNegocioException {
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                Cita cita = reservarEn(con, idUsuario, idCupo, null, ip);
                con.commit();
                return cita;
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Reserva usando la conexión (y la transacción) de quien llama; no hace commit.
     * La usa también la administración para la cita biométrica de un trámite (idSolicitud).
     */
    Cita reservarEn(Connection con, int idUsuario, int idCupo, Integer idSolicitud, String ip)
            throws SQLException, ReglaNegocioException {
        Cupo cupo;
        String nombreTramite;
        String nombreSede;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT c.id_cupo, c.id_sede, c.id_tramite, c.fecha, c.hora_inicio, c.hora_fin, c.capacidad, "
                + "       c.ocupados, c.habilitado, t.nombre AS tramite, s.nombre AS sede "
                + "FROM cupo c JOIN tramite t ON t.id_tramite = c.id_tramite "
                + "JOIN sede s ON s.id_sede = c.id_sede "
                + "WHERE c.id_cupo = ? FOR UPDATE")) {
            ps.setInt(1, idCupo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocioException("El horario seleccionado no existe.");
                }
                cupo = mapearCupo(rs);
                nombreTramite = rs.getString("tramite");
                nombreSede = rs.getString("sede");
            }
        }
        if (!cupo.isHabilitado()) {
            throw new ReglaNegocioException("Ese horario no está habilitado.");
        }
        if (!cupo.getFecha().isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("Solo puedes agendar a partir de mañana.");
        }
        if (cupo.isLleno()) {
            throw new ReglaNegocioException("Ese horario se acaba de llenar. Elige otro.");
        }

        // Una sola cita activa por tipo de atención
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ci.codigo FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo "
                + "WHERE ci.id_usuario = ? AND cu.id_tramite = ? AND ci.estado = 'PROGRAMADA' AND ci.fecha_hora > NOW() LIMIT 1")) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, cupo.getIdTramite());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new ReglaNegocioException("Ya tienes una cita programada para este servicio ("
                            + rs.getString(1) + "). Cancélala si quieres cambiar la fecha.");
                }
            }
        }
        // No dos citas en el mismo bloque horario
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo "
                + "WHERE ci.id_usuario = ? AND ci.estado = 'PROGRAMADA' AND cu.fecha = ? "
                + "AND cu.hora_inicio < ? AND cu.hora_fin > ? LIMIT 1")) {
            ps.setInt(1, idUsuario);
            ps.setDate(2, Date.valueOf(cupo.getFecha()));
            ps.setTime(3, Time.valueOf(cupo.getHoraFin()));
            ps.setTime(4, Time.valueOf(cupo.getHoraInicio()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new ReglaNegocioException("Ya tienes otra cita en ese mismo horario.");
                }
            }
        }

        // Turno y hora estimada: el bloque se reparte entre su capacidad
        int turno = cupo.getOcupados() + 1;
        long minutosBloque = Duration.between(cupo.getHoraInicio(), cupo.getHoraFin()).toMinutes();
        long minutosPorTurno = Math.max(1, minutosBloque / Math.max(1, cupo.getCapacidad()));
        LocalDateTime fechaHora = LocalDateTime.of(cupo.getFecha(), cupo.getHoraInicio())
                .plusMinutes((turno - 1) * minutosPorTurno);

        int idCita;
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO cita (codigo, id_usuario, id_cupo, fecha_hora, numero_turno, estado, id_solicitud) "
                + "VALUES (?, ?, ?, ?, ?, 'PROGRAMADA', ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            ps.setInt(2, idUsuario);
            ps.setInt(3, idCupo);
            ps.setTimestamp(4, Timestamp.valueOf(fechaHora));
            ps.setInt(5, turno);
            if (idSolicitud == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, idSolicitud);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                idCita = keys.getInt(1);
            }
        }
        String codigo = String.format("CIT-%d-%05d", Year.now().getValue(), idCita);
        try (PreparedStatement ps = con.prepareStatement("UPDATE cita SET codigo = ? WHERE id_cita = ?")) {
            ps.setString(1, codigo);
            ps.setInt(2, idCita);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE cupo SET ocupados = ocupados + 1 WHERE id_cupo = ? AND ocupados < capacidad")) {
            ps.setInt(1, idCupo);
            if (ps.executeUpdate() == 0) {
                throw new ReglaNegocioException("Ese horario se acaba de llenar. Elige otro.");
            }
        }

        Cita cita = new Cita();
        cita.setIdCita(idCita);
        cita.setCodigo(codigo);
        cita.setFechaHora(fechaHora);
        cita.setNumeroTurno(turno);
        cita.setTramiteNombre(nombreTramite);
        cita.setSedeNombre(nombreSede);
        cita.setEstado("PROGRAMADA");

        notificarYRegistrar(con, idUsuario, idCita, "Cita agendada: " + nombreTramite,
                "Tu cita " + codigo + " es el " + cita.getFechaTexto() + " a las " + cita.getHoraTexto()
                + " en " + nombreSede + ". Turno #" + turno + ".",
                "AGENDAR_CITA", "Cita " + codigo + " agendada (" + nombreTramite + ")"
                + (idSolicitud != null ? " por la institución" : ""), ip);
        return cita;
    }

    /** Cancela una cita propia (hasta 2 horas antes) y libera el turno. */
    public void cancelar(int idUsuario, int idCita, String ip) throws SQLException, ReglaNegocioException {
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                Cita cita = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT ci.id_cita, ci.codigo, ci.id_cupo, ci.fecha_hora, ci.estado, t.nombre AS tramite "
                        + "FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo "
                        + "JOIN tramite t ON t.id_tramite = cu.id_tramite "
                        + "WHERE ci.id_cita = ? AND ci.id_usuario = ? FOR UPDATE")) {
                    ps.setInt(1, idCita);
                    ps.setInt(2, idUsuario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            cita = new Cita();
                            cita.setIdCita(rs.getInt("id_cita"));
                            cita.setCodigo(rs.getString("codigo"));
                            cita.setIdCupo(rs.getInt("id_cupo"));
                            cita.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                            cita.setEstado(rs.getString("estado"));
                            cita.setTramiteNombre(rs.getString("tramite"));
                        }
                    }
                }
                if (cita == null) {
                    throw new ReglaNegocioException("No se encontró la cita.");
                }
                if (!cita.isCancelable()) {
                    throw new ReglaNegocioException("Esta cita ya no se puede cancelar (mínimo 2 horas antes).");
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE cita SET estado = 'CANCELADA' WHERE id_cita = ?")) {
                    ps.setInt(1, idCita);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE cupo SET ocupados = ocupados - 1 WHERE id_cupo = ? AND ocupados > 0")) {
                    ps.setInt(1, cita.getIdCupo());
                    ps.executeUpdate();
                }
                notificarYRegistrar(con, idUsuario, idCita, "Cita cancelada: " + cita.getTramiteNombre(),
                        "Cancelaste la cita " + cita.getCodigo() + " del " + cita.getFechaTexto() + ".",
                        "CANCELAR_CITA", "Cita " + cita.getCodigo() + " cancelada", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Citas del ciudadano: primero las próximas, luego el historial. */
    public List<Cita> listarPorUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT ci.id_cita, ci.codigo, ci.id_usuario, ci.id_cupo, ci.fecha_hora, ci.numero_turno, "
                + "       ci.ventanilla, ci.estado, t.nombre AS tramite, s.nombre AS sede, s.direccion, i.siglas "
                + "FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo "
                + "JOIN tramite t ON t.id_tramite = cu.id_tramite "
                + "JOIN sede s ON s.id_sede = cu.id_sede "
                + "JOIN institucion i ON i.id_institucion = s.id_institucion "
                + "WHERE ci.id_usuario = ? "
                + "ORDER BY (ci.estado = 'PROGRAMADA' AND ci.fecha_hora > NOW()) DESC, "
                + "         CASE WHEN ci.estado = 'PROGRAMADA' AND ci.fecha_hora > NOW() THEN ci.fecha_hora END ASC, "
                + "         ci.fecha_hora DESC";
        List<Cita> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cita c = new Cita();
                    c.setIdCita(rs.getInt("id_cita"));
                    c.setCodigo(rs.getString("codigo"));
                    c.setIdUsuario(rs.getInt("id_usuario"));
                    c.setIdCupo(rs.getInt("id_cupo"));
                    c.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                    c.setNumeroTurno(rs.getInt("numero_turno"));
                    c.setVentanilla(rs.getString("ventanilla"));
                    c.setEstado(rs.getString("estado"));
                    c.setTramiteNombre(rs.getString("tramite"));
                    c.setSedeNombre(rs.getString("sede"));
                    c.setSedeDireccion(rs.getString("direccion"));
                    c.setInstitucionSiglas(rs.getString("siglas"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    public static boolean esDiaHabil(LocalDate fecha) {
        return fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    // ------------------------------------------------------------------
    private void notificarYRegistrar(Connection con, int idUsuario, int idCita, String titulo, String mensaje,
            String accion, String descripcion, String ip) throws SQLException {
        NotificacionDAO.crear(con, idUsuario, "CITA", titulo, mensaje, null, idCita);
        NotificacionDAO.registrarBitacora(con, idUsuario, accion, descripcion, ip);
    }

    private Cupo mapearCupo(ResultSet rs) throws SQLException {
        Cupo c = new Cupo();
        c.setIdCupo(rs.getInt("id_cupo"));
        c.setIdSede(rs.getInt("id_sede"));
        c.setIdTramite(rs.getInt("id_tramite"));
        c.setFecha(rs.getDate("fecha").toLocalDate());
        c.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        c.setHoraFin(rs.getTime("hora_fin").toLocalTime());
        c.setCapacidad(rs.getInt("capacidad"));
        c.setOcupados(rs.getInt("ocupados"));
        c.setHabilitado(rs.getBoolean("habilitado"));
        return c;
    }
}
