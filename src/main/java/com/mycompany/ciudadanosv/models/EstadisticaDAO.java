package com.mycompany.ciudadanosv.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Panel estadístico de la administración.
 * Revisores y operadores ven los números de su institución; el super admin, los de todo el portal.
 */
public class EstadisticaDAO {

    /** Una barra de un gráfico: etiqueta, valor y porcentaje respecto al máximo (para el ancho). */
    public static class Barra {
        private final String etiqueta;
        private final String detalle;
        private final int valor;
        private int porcentaje;

        public Barra(String etiqueta, String detalle, int valor) {
            this.etiqueta = etiqueta;
            this.detalle = detalle;
            this.valor = valor;
        }

        public String getEtiqueta() { return etiqueta; }
        public String getDetalle() { return detalle; }
        public int getValor() { return valor; }
        public int getPorcentaje() { return porcentaje; }
    }

    /** Indicadores principales. */
    public Map<String, Object> indicadores(Usuario u) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        String tiempo = "—";
        List<Object> p = new ArrayList<>();
        String sql = "SELECT COUNT(*) AS total, "
                + "  SUM(s.estado IN ('RECIBIDA','EN_REVISION','CITA_PROGRAMADA')) AS abiertas, "
                + "  SUM(s.estado IN ('APROBADA','FINALIZADA')) AS aprobadas, "
                + "  SUM(s.estado = 'RECHAZADA') AS rechazadas, "
                + "  SUM(s.fecha_creacion >= CURDATE() - INTERVAL 30 DAY) AS ultimos30, "
                + "  AVG(CASE WHEN s.fecha_resolucion IS NOT NULL "
                + "      THEN TIMESTAMPDIFF(HOUR, s.fecha_creacion, s.fecha_resolucion) END) AS horas_resolucion "
                + "FROM solicitud s JOIN tramite t ON t.id_tramite = s.id_tramite "
                + "WHERE s.estado <> 'BORRADOR' " + SolicitudAdminDAO.alcance(u, p);
        try (Connection con = Conexion.conectar()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                SolicitudAdminDAO.asignar(ps, p);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int total = rs.getInt("total");
                    int aprobadas = rs.getInt("aprobadas");
                    int rechazadas = rs.getInt("rechazadas");
                    m.put("TOTAL", total);
                    m.put("ABIERTAS", rs.getInt("abiertas"));
                    m.put("APROBADAS", aprobadas);
                    m.put("RECHAZADAS", rechazadas);
                    m.put("ULTIMOS30", rs.getInt("ultimos30"));
                    double horas = rs.getDouble("horas_resolucion");
                    m.put("HORAS_RESOLUCION", rs.wasNull() ? -1 : Math.round(horas));
                    long h = Math.round(horas);
                    tiempo = rs.wasNull() ? "—" : (h < 48 ? h + " h" : Math.round(horas / 24) + " días");
                    int resueltas = aprobadas + rechazadas;
                    m.put("TASA_APROBACION", resueltas == 0 ? -1 : Math.round(aprobadas * 100f / resueltas));
                }
            }
            // Citas de hoy y de los próximos 7 días (sedes del alcance)
            List<Object> p2 = new ArrayList<>();
            String sqlCitas = "SELECT SUM(cu.fecha = CURDATE()) AS hoy, "
                    + "  SUM(cu.fecha BETWEEN CURDATE() AND CURDATE() + INTERVAL 7 DAY) AS semana "
                    + "FROM cita ci JOIN cupo cu ON cu.id_cupo = ci.id_cupo JOIN sede s ON s.id_sede = cu.id_sede "
                    + "WHERE ci.estado <> 'CANCELADA' " + alcanceSede(u, p2);
            try (PreparedStatement ps = con.prepareStatement(sqlCitas)) {
                SolicitudAdminDAO.asignar(ps, p2);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    m.put("CITAS_HOY", rs.getInt("hoy"));
                    m.put("CITAS_SEMANA", rs.getInt("semana"));
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM usuario u JOIN rol r ON r.id_rol = u.id_rol WHERE r.nombre = 'CIUDADANO'");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                m.put("CIUDADANOS", rs.getInt(1));
            }
        }
        m.put("TIEMPO_RESOLUCION", tiempo);
        return m;
    }

    /** Solicitudes por área de servicio (demanda). */
    public List<Barra> porArea(Usuario u) throws SQLException {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT a.nombre, COUNT(s.id_solicitud) AS n FROM solicitud s "
                + "JOIN tramite t ON t.id_tramite = s.id_tramite JOIN area_servicio a ON a.id_area = t.id_area "
                + "WHERE s.estado <> 'BORRADOR' " + SolicitudAdminDAO.alcance(u, p)
                + "GROUP BY a.id_area, a.nombre ORDER BY n DESC";
        return consultarBarras(sql, p);
    }

    /** Solicitudes por estado, en el orden del flujo. */
    public List<Barra> porEstado(Usuario u) throws SQLException {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT s.estado, COUNT(*) AS n FROM solicitud s JOIN tramite t ON t.id_tramite = s.id_tramite "
                + "WHERE s.estado <> 'BORRADOR' " + SolicitudAdminDAO.alcance(u, p)
                + "GROUP BY s.estado ORDER BY FIELD(s.estado,'RECIBIDA','EN_REVISION','CITA_PROGRAMADA','APROBADA','FINALIZADA','RECHAZADA')";
        List<Barra> crudas = consultarBarras(sql, p);
        List<Barra> lista = new ArrayList<>();
        for (Barra b : crudas) {
            lista.add(new Barra(Solicitud.textoEstado(b.getEtiqueta()), b.getEtiqueta(), b.getValor()));
        }
        escalar(lista);
        return lista;
    }

    /** Trámites más solicitados (top 5). */
    public List<Barra> topTramites(Usuario u) throws SQLException {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT t.nombre, COUNT(*) AS n FROM solicitud s JOIN tramite t ON t.id_tramite = s.id_tramite "
                + "WHERE s.estado <> 'BORRADOR' " + SolicitudAdminDAO.alcance(u, p)
                + "GROUP BY t.id_tramite, t.nombre ORDER BY n DESC LIMIT 5";
        return consultarBarras(sql, p);
    }

    /** Solicitudes recibidas por día en los últimos N días (incluye días en cero). */
    public List<Barra> porDia(Usuario u, int dias) throws SQLException {
        LocalDate desde = LocalDate.now().minusDays(dias - 1L);
        List<Object> p = new ArrayList<>();
        p.add(Date.valueOf(desde).toString());
        String sql = "SELECT DATE(s.fecha_creacion) AS dia, COUNT(*) AS n FROM solicitud s "
                + "JOIN tramite t ON t.id_tramite = s.id_tramite "
                + "WHERE s.estado <> 'BORRADOR' AND s.fecha_creacion >= ? " + SolicitudAdminDAO.alcance(u, p)
                + "GROUP BY DATE(s.fecha_creacion)";
        Map<LocalDate, Integer> conteo = new LinkedHashMap<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            SolicitudAdminDAO.asignar(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    conteo.put(rs.getDate("dia").toLocalDate(), rs.getInt("n"));
                }
            }
        }
        DateTimeFormatter corto = DateTimeFormatter.ofPattern("d MMM", Locale.of("es", "SV"));
        DateTimeFormatter largo = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.of("es", "SV"));
        List<Barra> lista = new ArrayList<>();
        for (LocalDate d = desde; !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            lista.add(new Barra(d.format(corto).replace(".", ""), d.format(largo), conteo.getOrDefault(d, 0)));
        }
        escalar(lista);
        return lista;
    }

    // ------------------------------------------------------------------
    private List<Barra> consultarBarras(String sql, List<Object> params) throws SQLException {
        List<Barra> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            SolicitudAdminDAO.asignar(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Barra(rs.getString(1), null, rs.getInt(2)));
                }
            }
        }
        escalar(lista);
        return lista;
    }

    /** Porcentaje de cada barra respecto a la mayor (la mayor = 100%). */
    private static void escalar(List<Barra> lista) {
        int max = lista.stream().mapToInt(Barra::getValor).max().orElse(0);
        for (Barra b : lista) {
            b.porcentaje = max == 0 ? 0 : Math.round(b.valor * 100f / max);
        }
    }

    private static String alcanceSede(Usuario u, List<Object> params) {
        if (u.isSuperAdmin()) {
            return "";
        }
        params.add(u.getIdInstitucion() == null ? -1 : u.getIdInstitucion());
        return "AND s.id_institucion = ? ";
    }
}
