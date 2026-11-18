package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Cita;
import com.mycompany.ciudadanosv.models.Cupo;
import com.mycompany.ciudadanosv.models.CupoAdminDAO;
import com.mycompany.ciudadanosv.models.ReglaNegocioException;
import com.mycompany.ciudadanosv.models.Sede;
import com.mycompany.ciudadanosv.models.Tramite;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Csrf;
import com.mycompany.ciudadanosv.util.Fechas;
import com.mycompany.ciudadanosv.util.Parametros;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Citas y cupos: bloques horarios por sede/trámite/día y agenda de la sede.
 * Solo OPERADOR y SUPER_ADMIN (permiso CUPO_GESTIONAR).
 * URL protegida: /admin/cupos?sede=1&tramite=1&fecha=DD/MM/AAAA
 *   POST accion=generar | crear (inicio, fin, capacidad) | capacidad (idCupo, capacidad)
 *        | habilitar / deshabilitar (idCupo) | eliminar (idCupo) | bloquearDia | habilitarDia
 *        | atender (idCita, resultado=ATENDIDA|NO_ASISTIO)
 */
public class AdminCuposController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        if (!usuario.isPuedeGestionarCupos()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Csrf.asegurarToken(request);
        LocalDate fecha = Fechas.leer(request.getParameter("fecha"));
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        try {
            CupoAdminDAO dao = new CupoAdminDAO();
            List<Sede> sedes = dao.listarSedes(usuario);
            request.setAttribute("sedes", sedes);
            Integer idSede = Parametros.entero(request, "sede");
            Sede sede = sedes.stream().filter(s -> idSede != null && s.getIdSede() == idSede).findFirst()
                    .orElse(sedes.isEmpty() ? null : sedes.get(0));
            if (sede != null) {
                List<Tramite> tramites = dao.listarTramitesDeSede(sede.getIdSede());
                Integer idTramite = Parametros.entero(request, "tramite");
                Tramite tramite = tramites.stream().filter(t -> idTramite != null && t.getIdTramite() == idTramite)
                        .findFirst().orElse(tramites.isEmpty() ? null : tramites.get(0));
                request.setAttribute("sede", sede);
                request.setAttribute("tramites", tramites);
                request.setAttribute("tramite", tramite);
                request.setAttribute("semana", dao.resumenDias(sede.getIdSede(), LocalDate.now(), 5));
                List<Cita> agenda = dao.agendaDelDia(sede.getIdSede(), fecha, null);
                request.setAttribute("agenda", agenda);
                if (tramite != null) {
                    List<Cupo> bloques = dao.listarBloques(sede.getIdSede(), tramite.getIdTramite(), fecha);
                    int capacidad = bloques.stream().mapToInt(Cupo::getCapacidad).sum();
                    int ocupados = bloques.stream().mapToInt(Cupo::getOcupados).sum();
                    request.setAttribute("bloques", bloques);
                    request.setAttribute("capacidadDia", capacidad);
                    request.setAttribute("ocupadosDia", ocupados);
                    request.setAttribute("ocupacionDia", capacidad == 0 ? 0 : Math.round(ocupados * 100f / capacidad));
                    request.setAttribute("diaBloqueado", !bloques.isEmpty() && bloques.stream().noneMatch(Cupo::isHabilitado));
                }
            }
        } catch (SQLException ex) {
            log("Error cargando cupos", ex);
            request.setAttribute("errorBD", "No se pudieron cargar los cupos. Intenta de nuevo.");
        }
        request.setAttribute("fecha", fecha);
        request.setAttribute("fechaTexto", Fechas.formatear(fecha));
        request.setAttribute("esPasado", fecha.isBefore(LocalDate.now()));
        request.setAttribute("diaHabil", com.mycompany.ciudadanosv.models.CitaDAO.esDiaHabil(fecha));
        request.setAttribute("fechaAnterior", Fechas.formatear(fecha.minusDays(1)));
        request.setAttribute("fechaSiguiente", Fechas.formatear(fecha.plusDays(1)));
        request.setAttribute("capacidadMaxima", CupoAdminDAO.CAPACIDAD_MAXIMA);
        request.getRequestDispatcher("/WEB-INF/vistas/admin/cupos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Csrf.esValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        if (!usuario.isPuedeGestionarCupos()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int idSede = Parametros.entero(request, "sede", 0);
        int idTramite = Parametros.entero(request, "tramite", 0);
        LocalDate fecha = Fechas.leer(request.getParameter("fecha"));
        String ip = request.getRemoteAddr();
        CupoAdminDAO dao = new CupoAdminDAO();
        String aviso = null;
        try {
            switch (Parametros.texto(request, "accion")) {
                case "generar" -> {
                    int n = dao.generarBloques(usuario, idSede, idTramite, fecha, ip);
                    aviso = n == 0 ? "El día ya tenía sus bloques." : n + " bloques creados con capacidad de 10 turnos.";
                }
                case "crear" -> {
                    dao.crearBloque(usuario, idSede, idTramite, fecha, hora(request, "inicio"), hora(request, "fin"),
                            Parametros.entero(request, "capacidad", 0), ip);
                    aviso = "Bloque agregado.";
                }
                case "capacidad" -> {
                    dao.cambiarCapacidad(usuario, Parametros.entero(request, "idCupo", 0),
                            Parametros.entero(request, "capacidad", 0), ip);
                    aviso = "Capacidad actualizada.";
                }
                case "habilitar", "deshabilitar" -> {
                    boolean habilitar = "habilitar".equals(request.getParameter("accion"));
                    dao.cambiarHabilitado(usuario, Parametros.entero(request, "idCupo", 0), habilitar, ip);
                    aviso = habilitar ? "Bloque habilitado." : "Bloque deshabilitado: ya no se pueden agendar turnos en él.";
                }
                case "eliminar" -> {
                    dao.eliminarBloque(usuario, Parametros.entero(request, "idCupo", 0), ip);
                    aviso = "Bloque eliminado.";
                }
                case "bloquearDia", "habilitarDia" -> {
                    boolean habilitar = "habilitarDia".equals(request.getParameter("accion"));
                    if (fecha == null) {
                        throw new ReglaNegocioException("Fecha no válida.");
                    }
                    int n = dao.cambiarDia(usuario, idSede, idTramite, fecha, habilitar, ip);
                    aviso = habilitar ? n + " bloques habilitados." : "Día bloqueado: " + n + " bloques deshabilitados. Las citas ya agendadas se mantienen.";
                }
                case "atender" -> {
                    String resultado = Parametros.texto(request, "resultado");
                    dao.registrarAtencion(usuario, Parametros.entero(request, "idCita", 0), resultado, ip);
                    aviso = "ATENDIDA".equals(resultado) ? "Cita marcada como atendida." : "Inasistencia registrada y notificada al ciudadano.";
                }
                default -> {
                    // acción desconocida
                }
            }
            if (aviso != null) {
                request.getSession().setAttribute("aviso", aviso);
            }
        } catch (ReglaNegocioException ex) {
            request.getSession().setAttribute("avisoError", ex.getMessage());
        } catch (SQLException ex) {
            log("Error actualizando cupos", ex);
            request.getSession().setAttribute("avisoError", "No se pudo guardar el cambio. Intenta de nuevo.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/cupos?sede=" + idSede + "&tramite=" + idTramite
                + "&fecha=" + URLEncoder.encode(Fechas.formatear(fecha == null ? LocalDate.now() : fecha), StandardCharsets.UTF_8));
    }

    private static LocalTime hora(HttpServletRequest request, String nombre) {
        try {
            return LocalTime.parse(Parametros.texto(request, nombre));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    @Override
    public String getServletInfo() {
        return "Citas y cupos (administración)";
    }
}
