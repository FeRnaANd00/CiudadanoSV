package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Citas y cupos (administración): bloques horarios por sede/trámite/día y agenda de la sede.
 * Operadores ven solo las sedes de su institución; el super admin, todas.
 */
public class CupoAdminDAO {

    public static final int CAPACIDAD_MAXIMA = 200;

    private static final LocalTime[][] BLOQUES_POR_DEFECTO = {
        {LocalTime.of(7, 0), LocalTime.of(9, 0)},
        {LocalTime.of(9, 0), LocalTime.of(11, 0)},
        {LocalTime.of(11, 0), LocalTime.of(13, 0)},
        {LocalTime.of(13, 0), LocalTime.of(15, 0)}
    };
    private static final int CAPACIDAD_POR_DEFECTO = 10;

    /** Un día del resumen semanal. */
    public static class DiaAgenda {
        private static final Locale ES = Locale.of("es", "SV");
        private final LocalDate fecha;
        private final int citas;

        public DiaAgenda(LocalDate fecha, int citas) {
            this.fecha = fecha;
            this.citas = citas;
        }

        public String getDiaSemana() {
            return fecha.format(DateTimeFormatter.ofPattern("EEE", ES)).replace(".", "");
        }

        public int getDiaNumero() { return fecha.getDayOfMonth(); }
        public String getFechaTexto() { return fecha.format(DateTimeFormatter.ofPattern("dd/MM/uuuu")); }
        public boolean isHoy() { return fecha.equals(LocalDate.now()); }
        public LocalDate getFecha() { return fecha; }
        public int getCitas() { return citas; }
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Sedes activas que el funcionario puede administrar. */
    public List<Sede> listarSedes(Usuario u) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT s.id_sede, s.id_institucion, s.id_municipio, s.nombre, s.horario, "
                + "       m.nombre AS municipio, t.siglas "
                + "FROM sede s JOIN municipio m ON m.id_municipio = s.id_municipio "
                + "JOIN institucion t ON t.id_institucion = s.id_institucion "
                + "WHERE s.activo = TRUE " + alcanceSede(u, params)
                + "ORDER BY t.siglas, s.nombre";
        List<Sede> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            SolicitudAdminDAO.asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sede s = new Sede();
                    s.setIdSede(rs.getInt("id_sede"));
                    s.setIdInstitucion(rs.getInt("id_institucion"));
                    s.setIdMunicipio(rs.getInt("id_municipio"));
                    s.setNombre(rs.getString("nombre"));
                    s.setHorario(rs.getString("horario"));
                    s.setMunicipioNombre(rs.getString("municipio"));
                    s.setInstitucionSiglas(rs.getString("siglas"));
                    lista.add(s);
                }
            }
        }
        return lista;
    }

    /** Trámites con cita de la institución de la sede. */
    public List<Tramite> listarTramitesDeSede(int idSede) throws SQLException {
        String sql = "SELECT t.id_tramite, t.codigo, t.nombre FROM tramite t "
                + "JOIN sede s ON s.id_institucion = t.id_institucion "
                + "WHERE s.id_sede = ? AND t.requiere_cita = TRUE AND t.estado <> 'INACTIVO' ORDER BY t.nombre";
        List<Tramite> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSede);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tramite t = new Tramite();
                    t.setIdTramite(rs.getInt("id_tramite"));
                    t.setCodigo(rs.getString("codigo"));
                    t.setNombre(rs.getString("nombre"));
                    lista.add(t);
                }
            }
        }
        return lista;
    }

    /** Bloques horarios de un día (sin crear nada). */
    public List<Cupo> listarBloques(int idSede, int idTramite, LocalDate fecha) throws SQLException {
        String sql = "SELECT id_cupo, id_sede, id_tramite, fecha, hora_inicio, hora_fin, capacidad, ocupados, habilitado "
                + "FROM cupo WHERE id_sede = ? AND id_tramite = ? AND fecha = ? ORDER BY hora_inicio";
        List<Cupo> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
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

    /** Citas (no canceladas) por día de la sede, desde una fecha, solo días hábiles. */
    public List<DiaAgenda> resumenDias(int idSede, LocalDate desde, int dias) throws SQLException {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate d = desde;
        while (fechas.size() < dias) {
            if (CitaDAO.esDiaHabil(d)) {
                fechas.add(d);
            }
            d = d.plusDays(1);
        }
        String sql = "SELECT COUNT(*) FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo "
                + "WHERE cu.id_sede = ? AND cu.fecha = ? AND ci.estado <> 'CANCELADA'";
        List<DiaAgenda> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (LocalDate f : fechas) {
                ps.setInt(1, idSede);
                ps.setDate(2, Date.valueOf(f));
                try (ResultSet rs = ps.executeQuery()) {
                    lista.add(new DiaAgenda(f, rs.next() ? rs.getInt(1) : 0));
                }
            }
        }
        return lista;
    }

    /** Agenda del día en la sede (todas las citas, o las de un trámite). */
    public List<Cita> agendaDelDia(int idSede, LocalDate fecha, Integer idTramite) throws SQLException {
        String sql = "SELECT ci.id_cita, ci.codigo, ci.id_usuario, ci.id_cupo, ci.fecha_hora, ci.numero_turno, "
                + "       ci.estado, t.nombre AS tramite, u.nombre_completo, u.dui, s.folio "
                + "FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo "
                + "JOIN tramite t ON t.id_tramite = cu.id_tramite "
                + "JOIN usuario u ON u.id_usuario = ci.id_usuario "
                + "LEFT JOIN solicitud s ON s.id_solicitud = ci.id_solicitud "
                + "WHERE cu.id_sede = ? AND cu.fecha = ? " + (idTramite != null ? "AND cu.id_tramite = ? " : "")
                + "ORDER BY ci.fecha_hora, ci.numero_turno";
        List<Cita> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSede);
            ps.setDate(2, Date.valueOf(fecha));
            if (idTramite != null) {
                ps.setInt(3, idTramite);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cita c = new Cita();
                    c.setIdCita(rs.getInt("id_cita"));
                    c.setCodigo(rs.getString("codigo"));
                    c.setIdUsuario(rs.getInt("id_usuario"));
                    c.setIdCupo(rs.getInt("id_cupo"));
                    c.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                    c.setNumeroTurno(rs.getInt("numero_turno"));
                    c.setEstado(rs.getString("estado"));
                    c.setTramiteNombre(rs.getString("tramite"));
                    c.setCiudadanoNombre(rs.getString("nombre_completo"));
                    c.setCiudadanoDui(rs.getString("dui"));
                    c.setFolioSolicitud(rs.getString("folio"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    /** true si el funcionario puede administrar esa sede. */
    public boolean sedeEnAlcance(Usuario u, int idSede) throws SQLException {
        List<Object> params = new ArrayList<>();
        params.add(idSede);
        String sql = "SELECT 1 FROM sede s WHERE s.id_sede = ? " + alcanceSede(u, params);
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            SolicitudAdminDAO.asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    /** Crea los 4 bloques por defecto de un día hábil que aún no tiene bloques. Devuelve cuántos creó. */
    public int generarBloques(Usuario u, int idSede, int idTramite, LocalDate fecha, String ip)
            throws SQLException, ReglaNegocioException {
        validarSedeYTramite(u, idSede, idTramite);
        validarFechaFutura(fecha);
        if (!CitaDAO.esDiaHabil(fecha)) {
            throw new ReglaNegocioException("Sábados y domingos no se generan bloques automáticos; agrégalos uno por uno.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                int creados = 0;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO cupo (id_sede, id_tramite, fecha, hora_inicio, hora_fin, capacidad) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
                    for (LocalTime[] b : BLOQUES_POR_DEFECTO) {
                        if (haySolape(con, idSede, idTramite, fecha, b[0], b[1], 0)) {
                            continue;
                        }
                        ps.setInt(1, idSede);
                        ps.setInt(2, idTramite);
                        ps.setDate(3, Date.valueOf(fecha));
                        ps.setTime(4, Time.valueOf(b[0]));
                        ps.setTime(5, Time.valueOf(b[1]));
                        ps.setInt(6, CAPACIDAD_POR_DEFECTO);
                        creados += ps.executeUpdate();
                    }
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_CUPOS",
                        creados + " bloques por defecto creados (sede " + idSede + ", " + fecha + ")", ip);
                con.commit();
                return creados;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Agrega un bloque horario. */
    public void crearBloque(Usuario u, int idSede, int idTramite, LocalDate fecha, LocalTime inicio, LocalTime fin,
            int capacidad, String ip) throws SQLException, ReglaNegocioException {
        validarSedeYTramite(u, idSede, idTramite);
        validarFechaFutura(fecha);
        if (inicio == null || fin == null || !inicio.isBefore(fin)) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        validarCapacidad(capacidad, 0);
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                if (haySolape(con, idSede, idTramite, fecha, inicio, fin, 0)) {
                    throw new ReglaNegocioException("Ese horario se cruza con otro bloque del mismo día.");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO cupo (id_sede, id_tramite, fecha, hora_inicio, hora_fin, capacidad) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, idSede);
                    ps.setInt(2, idTramite);
                    ps.setDate(3, Date.valueOf(fecha));
                    ps.setTime(4, Time.valueOf(inicio));
                    ps.setTime(5, Time.valueOf(fin));
                    ps.setInt(6, capacidad);
                    ps.executeUpdate();
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_CUPOS",
                        "Bloque " + inicio + "-" + fin + " creado (sede " + idSede + ", " + fecha + ")", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Cambia la capacidad (nunca por debajo de los turnos ya ocupados). */
    public void cambiarCapacidad(Usuario u, int idCupo, int capacidad, String ip) throws SQLException, ReglaNegocioException {
        Cupo c = buscarEnAlcance(u, idCupo);
        validarCapacidad(capacidad, c.getOcupados());
        actualizarCupo(u, "UPDATE cupo SET capacidad = ? WHERE id_cupo = ? AND ocupados <= ?",
                new int[]{capacidad, idCupo, capacidad},
                "Capacidad del bloque " + c.getHorarioTexto() + " (" + c.getFecha() + ") = " + capacidad, ip);
    }

    /** Habilita o deshabilita un bloque (las citas ya agendadas se mantienen). */
    public void cambiarHabilitado(Usuario u, int idCupo, boolean habilitado, String ip) throws SQLException, ReglaNegocioException {
        Cupo c = buscarEnAlcance(u, idCupo);
        actualizarCupo(u, "UPDATE cupo SET habilitado = ? WHERE id_cupo = ?",
                new int[]{habilitado ? 1 : 0, idCupo},
                "Bloque " + c.getHorarioTexto() + " (" + c.getFecha() + ") " + (habilitado ? "habilitado" : "deshabilitado"), ip);
    }

    /** Elimina un bloque sin turnos ocupados. */
    public void eliminarBloque(Usuario u, int idCupo, String ip) throws SQLException, ReglaNegocioException {
        Cupo c = buscarEnAlcance(u, idCupo);
        if (c.getOcupados() > 0) {
            throw new ReglaNegocioException("No se puede eliminar un bloque con citas; deshabilítalo en su lugar.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM cupo WHERE id_cupo = ? AND ocupados = 0 "
                        + "AND NOT EXISTS (SELECT 1 FROM cita WHERE cita.id_cupo = cupo.id_cupo)")) {
                    ps.setInt(1, idCupo);
                    if (ps.executeUpdate() == 0) {
                        throw new ReglaNegocioException("El bloque tiene citas registradas; deshabilítalo en su lugar.");
                    }
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_CUPOS",
                        "Bloque " + c.getHorarioTexto() + " (" + c.getFecha() + ") eliminado", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Bloquea (deshabilita) o desbloquea todos los bloques de un día para la sede y trámite. */
    public int cambiarDia(Usuario u, int idSede, int idTramite, LocalDate fecha, boolean habilitar, String ip)
            throws SQLException, ReglaNegocioException {
        validarSedeYTramite(u, idSede, idTramite);
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                int n;
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE cupo SET habilitado = ? WHERE id_sede = ? AND id_tramite = ? AND fecha = ?")) {
                    ps.setBoolean(1, habilitar);
                    ps.setInt(2, idSede);
                    ps.setInt(3, idTramite);
                    ps.setDate(4, Date.valueOf(fecha));
                    n = ps.executeUpdate();
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_CUPOS",
                        "Día " + fecha + " " + (habilitar ? "habilitado" : "bloqueado") + " (sede " + idSede + ")", ip);
                con.commit();
                return n;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Marca una cita del día como ATENDIDA o NO_ASISTIO. Si no asistió, se avisa al ciudadano. */
    public void registrarAtencion(Usuario u, int idCita, String resultado, String ip)
            throws SQLException, ReglaNegocioException {
        if (!"ATENDIDA".equals(resultado) && !"NO_ASISTIO".equals(resultado)) {
            throw new ReglaNegocioException("Resultado de atención no válido.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                int idUsuario;
                int idSede;
                String codigo;
                LocalDate fecha;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT ci.id_usuario, ci.codigo, ci.estado, cu.id_sede, cu.fecha FROM cita ci "
                        + "JOIN cupo cu ON cu.id_cupo = ci.id_cupo WHERE ci.id_cita = ? FOR UPDATE")) {
                    ps.setInt(1, idCita);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("La cita no existe.");
                        }
                        if (!"PROGRAMADA".equals(rs.getString("estado"))) {
                            throw new ReglaNegocioException("La cita ya no está programada.");
                        }
                        idUsuario = rs.getInt("id_usuario");
                        codigo = rs.getString("codigo");
                        idSede = rs.getInt("id_sede");
                        fecha = rs.getDate("fecha").toLocalDate();
                    }
                }
                if (!sedeEnAlcance(u, idSede)) {
                    throw new ReglaNegocioException("La cita no pertenece a una sede de tu institución.");
                }
                if (fecha.isAfter(LocalDate.now())) {
                    throw new ReglaNegocioException("Solo se registra la asistencia el día de la cita o después.");
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE cita SET estado = ? WHERE id_cita = ?")) {
                    ps.setString(1, resultado);
                    ps.setInt(2, idCita);
                    ps.executeUpdate();
                }
                if ("NO_ASISTIO".equals(resultado)) {
                    NotificacionDAO.crear(con, idUsuario, "CITA", "Inasistencia a la cita " + codigo,
                            "No se registró tu asistencia a la cita " + codigo + ". Puedes agendar una nueva cuando lo necesites.",
                            null, idCita);
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "ATENCION_CITA",
                        "Cita " + codigo + ": " + ("ATENDIDA".equals(resultado) ? "atendida" : "no asistió"), ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static String alcanceSede(Usuario u, List<Object> params) {
        if (u.isSuperAdmin()) {
            return "";
        }
        params.add(u.getIdInstitucion() == null ? -1 : u.getIdInstitucion());
        return "AND s.id_institucion = ? ";
    }

    private void validarSedeYTramite(Usuario u, int idSede, int idTramite) throws SQLException, ReglaNegocioException {
        if (!u.isPuedeGestionarCupos() || !sedeEnAlcance(u, idSede)) {
            throw new ReglaNegocioException("No puedes administrar los cupos de esa sede.");
        }
        boolean ok = listarTramitesDeSede(idSede).stream().anyMatch(t -> t.getIdTramite() == idTramite);
        if (!ok) {
            throw new ReglaNegocioException("Ese trámite no se atiende con cita en la sede elegida.");
        }
    }

    private static void validarFechaFutura(LocalDate fecha) throws ReglaNegocioException {
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("Elige una fecha de hoy en adelante.");
        }
        if (fecha.isAfter(LocalDate.now().plusMonths(6))) {
            throw new ReglaNegocioException("Solo se programan cupos hasta 6 meses adelante.");
        }
    }

    private static void validarCapacidad(int capacidad, int ocupados) throws ReglaNegocioException {
        if (capacidad < 1 || capacidad > CAPACIDAD_MAXIMA) {
            throw new ReglaNegocioException("La capacidad debe estar entre 1 y " + CAPACIDAD_MAXIMA + ".");
        }
        if (capacidad < ocupados) {
            throw new ReglaNegocioException("El bloque ya tiene " + ocupados + " turnos ocupados; la capacidad no puede ser menor.");
        }
    }

    private boolean haySolape(Connection con, int idSede, int idTramite, LocalDate fecha, LocalTime inicio,
            LocalTime fin, int excluirIdCupo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM cupo WHERE id_sede = ? AND id_tramite = ? AND fecha = ? "
                + "AND hora_inicio < ? AND hora_fin > ? AND id_cupo <> ? LIMIT 1")) {
            ps.setInt(1, idSede);
            ps.setInt(2, idTramite);
            ps.setDate(3, Date.valueOf(fecha));
            ps.setTime(4, Time.valueOf(fin));
            ps.setTime(5, Time.valueOf(inicio));
            ps.setInt(6, excluirIdCupo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Cupo buscarEnAlcance(Usuario u, int idCupo) throws SQLException, ReglaNegocioException {
        Cupo c;
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_cupo, id_sede, id_tramite, fecha, hora_inicio, hora_fin, capacidad, ocupados, habilitado "
                     + "FROM cupo WHERE id_cupo = ?")) {
            ps.setInt(1, idCupo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocioException("El bloque no existe.");
                }
                c = mapearCupo(rs);
            }
        }
        if (!u.isPuedeGestionarCupos() || !sedeEnAlcance(u, c.getIdSede())) {
            throw new ReglaNegocioException("No puedes administrar los cupos de esa sede.");
        }
        return c;
    }

    private void actualizarCupo(Usuario u, String sql, int[] valores, String descripcion, String ip)
            throws SQLException, ReglaNegocioException {
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (int i = 0; i < valores.length; i++) {
                        ps.setInt(i + 1, valores[i]);
                    }
                    if (ps.executeUpdate() == 0) {
                        throw new ReglaNegocioException("No se pudo actualizar el bloque (cambió mientras lo editabas).");
                    }
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_CUPOS", descripcion, ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
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
