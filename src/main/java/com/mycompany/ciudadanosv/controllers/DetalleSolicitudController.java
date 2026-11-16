package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.HistorialSolicitud;
import com.mycompany.ciudadanosv.models.Solicitud;
import com.mycompany.ciudadanosv.models.SolicitudDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Detalle de una solicitud: datos, línea de tiempo, observaciones y documentos.
 * URL protegida: /app/solicitud?folio=EXP-2026-00003
 * Solo el dueño de la solicitud puede verla (si no, 404).
 */
public class DetalleSolicitudController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        String folio = request.getParameter("folio");
        if (folio == null || folio.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/app/solicitudes");
            return;
        }

        try {
            SolicitudDAO dao = new SolicitudDAO();
            Solicitud s = dao.buscarPorFolio(folio.trim(), usuario.getIdUsuario());
            if (s == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            List<HistorialSolicitud> historial = dao.listarHistorial(s.getIdSolicitud());

            request.setAttribute("solicitud", s);
            request.setAttribute("historial", historial);
            request.setAttribute("pendientes", pasosPendientes(s, historial));
            request.setAttribute("documentos", dao.listarDocumentos(s.getIdSolicitud()));
            request.setAttribute("partida", dao.buscarPartida(s.getIdSolicitud()));
            request.setAttribute("municipal", dao.buscarMunicipal(s.getIdSolicitud()));
            request.setAttribute("dui", dao.buscarDui(s.getIdSolicitud()));
        } catch (SQLException ex) {
            log("Error consultando detalle de " + folio, ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.getRequestDispatcher("/WEB-INF/vistas/ciudadano/detalle_solicitud.jsp").forward(request, response);
    }

    /**
     * Pasos que aún faltan según el flujo normal del trámite:
     *   RECIBIDA -> EN_REVISION -> (CITA_PROGRAMADA si requiere cita) -> APROBADA
     * Cada elemento: {estado, texto, descripción}.
     */
    private List<String[]> pasosPendientes(Solicitud s, List<HistorialSolicitud> historial) {
        List<String[]> pendientes = new ArrayList<>();
        if ("RECHAZADA".equals(s.getEstado()) || "APROBADA".equals(s.getEstado())
                || "FINALIZADA".equals(s.getEstado())) {
            return pendientes;
        }
        List<String> flujo = new ArrayList<>(List.of("RECIBIDA", "EN_REVISION"));
        if (s.isRequiereCita()) {
            flujo.add("CITA_PROGRAMADA");
        }
        flujo.add("APROBADA");

        int actual = flujo.indexOf(s.getEstado());
        for (int i = actual + 1; i < flujo.size(); i++) {
            String e = flujo.get(i);
            boolean yaOcurrio = historial.stream().anyMatch(h -> e.equals(h.getEstadoNuevo()));
            if (!yaOcurrio) {
                pendientes.add(new String[]{e, Solicitud.textoEstado(e), Solicitud.descripcionEstado(e)});
            }
        }
        return pendientes;
    }

    @Override
    public String getServletInfo() {
        return "Detalle de una solicitud del ciudadano";
    }
}
