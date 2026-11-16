package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Cita;
import com.mycompany.ciudadanosv.models.CitaDAO;
import com.mycompany.ciudadanosv.models.ReglaNegocioException;
import com.mycompany.ciudadanosv.models.Sede;
import com.mycompany.ciudadanosv.models.SedeDAO;
import com.mycompany.ciudadanosv.models.Tramite;
import com.mycompany.ciudadanosv.models.TramiteDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Agendar una cita de salud (MINSAL).
 * URL protegida: /app/citas/nueva?codigo=MINSAL-01&idSede=7&fecha=2026-11-18
 *   GET  -> el ciudadano elige tipo de atención, sede y día; se muestran los horarios con cupo
 *   POST -> reserva el horario (idCupo) y redirige a /app/citas
 *
 * Los filtros se envían por GET (cada cambio recarga la página): no necesita AJAX.
 */
public class CitaNuevaController extends HttpServlet {

    public static final String INSTITUCION = "MINSAL";
    private static final int DIAS_HABILES_A_MOSTRAR = 10;
    private static final String VISTA = "/WEB-INF/vistas/ciudadano/cita_nueva.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Csrf.asegurarToken(request);
        try {
            mostrar(request, response, null);
        } catch (SQLException ex) {
            log("Error cargando agenda de citas", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Csrf.esValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        int idCupo = entero(request.getParameter("idCupo"));
        try {
            Cita cita = new CitaDAO().reservar(usuario.getIdUsuario(), idCupo, request.getRemoteAddr());
            request.getSession().setAttribute("aviso", "Cita " + cita.getCodigo() + " agendada para el "
                    + cita.getFechaTexto() + " a las " + cita.getHoraTexto() + " (turno #" + cita.getNumeroTurno() + ").");
            response.sendRedirect(request.getContextPath() + "/app/citas");
        } catch (ReglaNegocioException ex) {
            try {
                mostrar(request, response, ex.getMessage());
            } catch (SQLException sqlEx) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (SQLException ex) {
            log("Error reservando cita", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Arma la página con lo elegido hasta ahora (tipo -> sede -> día -> horarios). */
    private void mostrar(HttpServletRequest request, HttpServletResponse response, String error)
            throws SQLException, ServletException, IOException {
        List<Tramite> tipos = new TramiteDAO().listarConCita(INSTITUCION);
        List<Sede> sedes = new SedeDAO().listarPorInstitucion(INSTITUCION);
        if (tipos.isEmpty() || sedes.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String codigo = request.getParameter("codigo");
        Tramite tipo = tipos.stream().filter(t -> t.getCodigo().equals(codigo)).findFirst().orElse(tipos.get(0));
        int idSede = entero(request.getParameter("idSede"));
        Sede sede = sedes.stream().filter(s -> s.getIdSede() == idSede).findFirst().orElse(null);

        // Próximos días hábiles (desde mañana)
        List<LocalDate> dias = new ArrayList<>();
        LocalDate d = LocalDate.now().plusDays(1);
        while (dias.size() < DIAS_HABILES_A_MOSTRAR) {
            if (CitaDAO.esDiaHabil(d)) {
                dias.add(d);
            }
            d = d.plusDays(1);
        }
        LocalDate fecha = null;
        try {
            LocalDate pedida = LocalDate.parse(request.getParameter("fecha") == null ? "" : request.getParameter("fecha"));
            if (dias.contains(pedida)) {
                fecha = pedida;
            }
        } catch (DateTimeParseException ex) {
            fecha = null;
        }

        Locale es = Locale.of("es", "SV");
        List<String[]> diasVista = new ArrayList<>();   // {yyyy-MM-dd, "lun", "17", "nov"}
        for (LocalDate dia : dias) {
            diasVista.add(new String[]{
                dia.toString(),
                dia.format(DateTimeFormatter.ofPattern("EEE", es)).replace(".", ""),
                String.valueOf(dia.getDayOfMonth()),
                dia.format(DateTimeFormatter.ofPattern("MMM", es)).replace(".", "")
            });
        }

        request.setAttribute("tipos", tipos);
        request.setAttribute("tipo", tipo);
        request.setAttribute("sedes", sedes);
        request.setAttribute("sede", sede);
        request.setAttribute("dias", diasVista);
        request.setAttribute("fecha", fecha == null ? "" : fecha.toString());
        if (fecha != null) {
            request.setAttribute("fechaTexto", fecha.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", es)));
        }
        if (sede != null && fecha != null) {
            request.setAttribute("cupos", new CitaDAO().listarCupos(sede.getIdSede(), tipo.getIdTramite(), fecha));
        }
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private static int entero(String v) {
        try {
            return v == null ? 0 : Integer.parseInt(v.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public String getServletInfo() {
        return "Agendar cita de salud";
    }
}
