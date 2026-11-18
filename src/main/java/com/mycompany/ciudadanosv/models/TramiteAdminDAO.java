package com.mycompany.ciudadanosv.models;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Catálogo de trámites y requisitos (solo SUPER_ADMIN, permiso SERVICIO_GESTIONAR).
 * Los formularios del ciudadano leen los requisitos de la base, así que los cambios se ven al instante.
 */
public class TramiteAdminDAO {

    public static final Set<String> MODALIDADES = Set.of("PRESENCIAL", "DIGITAL", "MIXTA");
    public static final Set<String> ESTADOS = Set.of("ACTIVO", "EN_REVISION", "PROXIMAMENTE", "INACTIVO");
    public static final Set<String> VALIDACIONES = Set.of("MANUAL", "AUTOMATICA", "PRESENCIAL");
    public static final Set<String> FORMATOS = Set.of("PDF", "JPG", "PNG");

    private static final String SELECT = "SELECT t.id_tramite, t.id_area, a.nombre AS area, t.id_institucion, "
            + "       i.siglas, i.nombre AS institucion, t.codigo, t.nombre, t.descripcion, t.arancel, t.plazo_horas, "
            + "       t.modalidad, t.requiere_cita, t.requiere_biometria, t.estado, "
            + "       (SELECT COUNT(*) FROM requisito r WHERE r.id_tramite = t.id_tramite) AS num_requisitos, "
            + "       (SELECT COUNT(*) FROM solicitud s WHERE s.id_tramite = t.id_tramite) AS num_solicitudes "
            + "FROM tramite t JOIN area_servicio a ON a.id_area = t.id_area "
            + "JOIN institucion i ON i.id_institucion = t.id_institucion ";

    /** Catálogo con filtros opcionales (texto en código/nombre, área, estado). */
    public List<Tramite> listar(String texto, Integer idArea, String estado) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            sql.append("AND (t.codigo LIKE ? OR t.nombre LIKE ?) ");
            params.add("%" + texto.trim() + "%");
            params.add("%" + texto.trim() + "%");
        }
        if (idArea != null) {
            sql.append("AND t.id_area = ? ");
            params.add(idArea);
        }
        if (estado != null && ESTADOS.contains(estado)) {
            sql.append("AND t.estado = ? ");
            params.add(estado);
        }
        sql.append("ORDER BY a.orden, t.nombre");
        List<Tramite> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            SolicitudAdminDAO.asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Tramite buscar(int idTramite) throws SQLException {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE t.id_tramite = ?")) {
            ps.setInt(1, idTramite);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<AreaServicio> listarAreas() throws SQLException {
        List<AreaServicio> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement("SELECT id_area, nombre, icono FROM area_servicio ORDER BY orden");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AreaServicio a = new AreaServicio();
                a.setIdArea(rs.getInt("id_area"));
                a.setNombre(rs.getString("nombre"));
                a.setIcono(rs.getString("icono"));
                lista.add(a);
            }
        }
        return lista;
    }

    /** Valida y guarda (crea si idTramite = 0). Devuelve el id. */
    public int guardar(Usuario u, Tramite t, String ip) throws SQLException, ReglaNegocioException {
        validar(t);
        boolean nuevo = t.getIdTramite() == 0;
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                if (nuevo) {
                    try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM tramite WHERE codigo = ?")) {
                        ps.setString(1, t.getCodigo());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                throw new ReglaNegocioException("Ya existe un trámite con el código " + t.getCodigo() + ".");
                            }
                        }
                    }
                }
                String sql = nuevo
                        ? "INSERT INTO tramite (id_area, id_institucion, nombre, descripcion, arancel, plazo_horas, modalidad, "
                          + "requiere_cita, requiere_biometria, estado, codigo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        : "UPDATE tramite SET id_area = ?, id_institucion = ?, nombre = ?, descripcion = ?, arancel = ?, "
                          + "plazo_horas = ?, modalidad = ?, requiere_cita = ?, requiere_biometria = ?, estado = ? "
                          + "WHERE id_tramite = ?";
                int id = t.getIdTramite();
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, t.getIdArea());
                    ps.setInt(2, t.getIdInstitucion());
                    ps.setString(3, t.getNombre());
                    ps.setString(4, t.getDescripcion());
                    ps.setBigDecimal(5, t.getArancel());
                    if (t.getPlazoHoras() == null) {
                        ps.setNull(6, Types.INTEGER);
                    } else {
                        ps.setInt(6, t.getPlazoHoras());
                    }
                    ps.setString(7, t.getModalidad());
                    ps.setBoolean(8, t.isRequiereCita());
                    ps.setBoolean(9, t.isRequiereBiometria());
                    ps.setString(10, t.getEstado());
                    if (nuevo) {
                        ps.setString(11, t.getCodigo());
                    } else {
                        ps.setInt(11, t.getIdTramite());
                    }
                    if (ps.executeUpdate() == 0) {
                        throw new ReglaNegocioException("El trámite no existe.");
                    }
                    if (nuevo) {
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            keys.next();
                            id = keys.getInt(1);
                        }
                    }
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_TRAMITE",
                        (nuevo ? "Trámite creado: " : "Trámite actualizado: ") + t.getCodigo() + " (" + t.getEstado() + ")", ip);
                con.commit();
                return id;
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Agrega (idRequisito = 0) o modifica un requisito del trámite. */
    public void guardarRequisito(Usuario u, Requisito r, String ip) throws SQLException, ReglaNegocioException {
        validarRequisito(r);
        boolean nuevo = r.getIdRequisito() == 0;
        String sql = nuevo
                ? "INSERT INTO requisito (nombre, descripcion, obligatorio, requiere_archivo, formatos, tamano_max_mb, "
                  + "tipo_validacion, id_tramite) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE requisito SET nombre = ?, descripcion = ?, obligatorio = ?, requiere_archivo = ?, formatos = ?, "
                  + "tamano_max_mb = ?, tipo_validacion = ? WHERE id_tramite = ? AND id_requisito = ?";
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, r.getNombre());
                    ps.setString(2, r.getDescripcion());
                    ps.setBoolean(3, r.isObligatorio());
                    ps.setBoolean(4, r.isRequiereArchivo());
                    ps.setString(5, r.getFormatos());
                    ps.setInt(6, r.getTamanoMaxMb());
                    ps.setString(7, r.getTipoValidacion());
                    ps.setInt(8, r.getIdTramite());
                    if (!nuevo) {
                        ps.setInt(9, r.getIdRequisito());
                    }
                    if (ps.executeUpdate() == 0) {
                        throw new ReglaNegocioException("El requisito no existe.");
                    }
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_TRAMITE",
                        (nuevo ? "Requisito agregado: " : "Requisito actualizado: ") + r.getNombre()
                        + " (trámite " + r.getIdTramite() + ")", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Quita un requisito. Los documentos ya subidos se conservan (quedan sin requisito asociado). */
    public void eliminarRequisito(Usuario u, int idTramite, int idRequisito, String ip) throws SQLException, ReglaNegocioException {
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE documento d JOIN requisito r ON r.id_requisito = d.id_requisito "
                        + "SET d.id_requisito = NULL WHERE r.id_requisito = ? AND r.id_tramite = ?")) {
                    ps.setInt(1, idRequisito);
                    ps.setInt(2, idTramite);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM requisito WHERE id_requisito = ? AND id_tramite = ?")) {
                    ps.setInt(1, idRequisito);
                    ps.setInt(2, idTramite);
                    if (ps.executeUpdate() == 0) {
                        throw new ReglaNegocioException("El requisito no existe.");
                    }
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_TRAMITE",
                        "Requisito " + idRequisito + " eliminado (trámite " + idTramite + ")", ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Copia los requisitos de otro trámite (se agregan a los que ya tenga). Devuelve cuántos copió. */
    public int copiarRequisitos(Usuario u, int idDestino, int idOrigen, String ip) throws SQLException, ReglaNegocioException {
        if (idDestino == idOrigen) {
            throw new ReglaNegocioException("Elige un trámite distinto como origen.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                int n;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO requisito (id_tramite, nombre, descripcion, obligatorio, requiere_archivo, formatos, "
                        + "tamano_max_mb, tipo_validacion) "
                        + "SELECT ?, nombre, descripcion, obligatorio, requiere_archivo, formatos, tamano_max_mb, tipo_validacion "
                        + "FROM requisito WHERE id_tramite = ? ORDER BY id_requisito")) {
                    ps.setInt(1, idDestino);
                    ps.setInt(2, idOrigen);
                    n = ps.executeUpdate();
                }
                if (n == 0) {
                    throw new ReglaNegocioException("El trámite de origen no tiene requisitos.");
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_TRAMITE",
                        n + " requisitos copiados del trámite " + idOrigen + " al " + idDestino, ip);
                con.commit();
                return n;
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /** Solicitudes + citas que usan el trámite. Si es 0 nunca se usó. */
    public int contarUsos(int idTramite) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM solicitud WHERE id_tramite = ?) "
                + "     + (SELECT COUNT(*) FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo WHERE cu.id_tramite = ?)";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTramite);
            ps.setInt(2, idTramite);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Elimina un trámite que nunca se usó, junto con sus requisitos y sus bloques de cupos vacíos.
     * @param tieneFormulario true si el trámite tiene un formulario programado (no se elimina: lo usa el código)
     */
    public void eliminar(Usuario u, int idTramite, boolean tieneFormulario, String ip)
            throws SQLException, ReglaNegocioException {
        Tramite t = buscar(idTramite);
        if (t == null) {
            throw new ReglaNegocioException("El trámite no existe.");
        }
        if (tieneFormulario) {
            throw new ReglaNegocioException("Este trámite tiene un formulario programado en el portal; márcalo como Inactivo en lugar de eliminarlo.");
        }
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                if (contarUsos(idTramite) > 0) {
                    throw new ReglaNegocioException("El trámite ya tiene solicitudes o citas: no se puede eliminar. Márcalo como Inactivo.");
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM cupo WHERE id_tramite = ?")) {
                    ps.setInt(1, idTramite);
                    ps.executeUpdate();
                }
                // requisito tiene ON DELETE CASCADE
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM tramite WHERE id_tramite = ?")) {
                    ps.setInt(1, idTramite);
                    ps.executeUpdate();
                }
                NotificacionDAO.registrarBitacora(con, u.getIdUsuario(), "GESTION_TRAMITE",
                        "Trámite eliminado (sin uso): " + t.getCodigo() + " - " + t.getNombre(), ip);
                con.commit();
            } catch (SQLException | ReglaNegocioException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // ------------------------------------------------------------------
    private void validar(Tramite t) throws ReglaNegocioException {
        if (t.getCodigo() == null || !t.getCodigo().matches("[A-Z0-9]+(-[A-Z0-9]+)*") || t.getCodigo().length() > 20) {
            throw new ReglaNegocioException("El código debe tener letras mayúsculas, números y guiones (ej. MINED-04), máximo 20.");
        }
        if (t.getNombre() == null || t.getNombre().length() < 5 || t.getNombre().length() > 120) {
            throw new ReglaNegocioException("El nombre debe tener entre 5 y 120 caracteres.");
        }
        if (t.getDescripcion() != null && t.getDescripcion().length() > 300) {
            throw new ReglaNegocioException("La descripción admite máximo 300 caracteres.");
        }
        if (t.getArancel() == null || t.getArancel().signum() < 0 || t.getArancel().compareTo(new BigDecimal("9999.99")) > 0
                || t.getArancel().scale() > 2) {
            throw new ReglaNegocioException("El arancel debe ser un monto entre $0.00 y $9,999.99.");
        }
        if (t.getPlazoHoras() != null && (t.getPlazoHoras() < 1 || t.getPlazoHoras() > 2000)) {
            throw new ReglaNegocioException("El plazo debe estar entre 1 y 2000 horas (o vacío).");
        }
        if (!MODALIDADES.contains(t.getModalidad()) || !ESTADOS.contains(t.getEstado())) {
            throw new ReglaNegocioException("Modalidad o estado no válido.");
        }
        if (t.getIdArea() <= 0 || t.getIdInstitucion() <= 0) {
            throw new ReglaNegocioException("Elige el área y la institución.");
        }
        if (t.isRequiereBiometria() && !t.isRequiereCita()) {
            throw new ReglaNegocioException("Un trámite con biometría también requiere cita.");
        }
    }

    private void validarRequisito(Requisito r) throws ReglaNegocioException {
        if (r.getNombre() == null || r.getNombre().length() < 3 || r.getNombre().length() > 150) {
            throw new ReglaNegocioException("El nombre del requisito debe tener entre 3 y 150 caracteres.");
        }
        if (r.getDescripcion() != null && r.getDescripcion().length() > 250) {
            throw new ReglaNegocioException("La descripción del requisito admite máximo 250 caracteres.");
        }
        if (!VALIDACIONES.contains(r.getTipoValidacion())) {
            throw new ReglaNegocioException("Tipo de validación no válido.");
        }
        if (r.isRequiereArchivo()) {
            if (r.getFormatos() == null || r.getFormatos().isEmpty()
                    || !FORMATOS.containsAll(Arrays.asList(r.getFormatos().split(",")))) {
                throw new ReglaNegocioException("Marca al menos un formato permitido (PDF, JPG o PNG).");
            }
            if (r.getTamanoMaxMb() < 1 || r.getTamanoMaxMb() > 5) {
                throw new ReglaNegocioException("El tamaño máximo debe estar entre 1 y 5 MB.");
            }
        } else {
            r.setFormatos(null);
            r.setTamanoMaxMb(5);
        }
    }

    private Tramite mapear(ResultSet rs) throws SQLException {
        Tramite t = new Tramite();
        t.setIdTramite(rs.getInt("id_tramite"));
        t.setIdArea(rs.getInt("id_area"));
        t.setAreaNombre(rs.getString("area"));
        t.setIdInstitucion(rs.getInt("id_institucion"));
        t.setInstitucionSiglas(rs.getString("siglas"));
        t.setInstitucionNombre(rs.getString("institucion"));
        t.setCodigo(rs.getString("codigo"));
        t.setNombre(rs.getString("nombre"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setArancel(rs.getBigDecimal("arancel"));
        int plazo = rs.getInt("plazo_horas");
        t.setPlazoHoras(rs.wasNull() ? null : plazo);
        t.setModalidad(rs.getString("modalidad"));
        t.setRequiereCita(rs.getBoolean("requiere_cita"));
        t.setRequiereBiometria(rs.getBoolean("requiere_biometria"));
        t.setEstado(rs.getString("estado"));
        t.setNumRequisitos(rs.getInt("num_requisitos"));
        t.setNumSolicitudes(rs.getInt("num_solicitudes"));
        return t;
    }
}
