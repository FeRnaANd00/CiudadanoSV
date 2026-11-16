package com.mycompany.ciudadanosv.controllers;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * "Mis solicitudes": todas las solicitudes del ciudadano con búsqueda y filtros.
 * URL protegida: /app/solicitudes?q=texto&area=ID&estado=PROCESO|APROBADA|RECHAZADA
 *
 * Un ciudadano tiene pocas solicitudes, así que se traen todas y se filtran aquí
 * (así también salen los contadores de cada filtro con una sola consulta).
 */
public class MisSolicitudesController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");

        List<Solicitud> todas;
        try {
            todas = new SolicitudDAO().listarPorUsuario(usuario.getIdUsuario());
        } catch (SQLException ex) {
            log("Error listando solicitudes", ex);
            request.setAttribute("errorBD", "No se pudieron cargar tus solicitudes. Intenta de nuevo.");
            request.getRequestDispatcher("/WEB-INF/vistas/ciudadano/mis_solicitudes.jsp").forward(request, response);
            return;
        }

        String q = limpiar(request.getParameter("q")).toLowerCase(Locale.ROOT);
        String estado = limpiar(request.getParameter("estado"));
        int area = entero(request.getParameter("area"));

        // Contadores y áreas presentes (sobre todas, sin filtrar)
        int enProceso = 0, aprobadas = 0, rechazadas = 0;
        Map<Integer, String> areas = new LinkedHashMap<>();
        for (Solicitud s : todas) {
            switch (s.getGrupoEstado()) {
                case "APROBADA" -> aprobadas++;
                case "RECHAZADA" -> rechazadas++;
                default -> enProceso++;
            }
            areas.putIfAbsent(s.getIdArea(), s.getAreaNombre());
        }

        // Aplicar filtros
        List<Solicitud> filtradas = new ArrayList<>();
        for (Solicitud s : todas) {
            boolean coincideTexto = q.isEmpty()
                    || s.getFolio().toLowerCase(Locale.ROOT).contains(q)
                    || s.getTramiteNombre().toLowerCase(Locale.ROOT).contains(q);
            boolean coincideEstado = estado.isEmpty() || estado.equals(s.getGrupoEstado());
            boolean coincideArea = area == 0 || area == s.getIdArea();
            if (coincideTexto && coincideEstado && coincideArea) {
                filtradas.add(s);
            }
        }

        request.setAttribute("solicitudes", filtradas);
        request.setAttribute("total", todas.size());
        request.setAttribute("enProceso", enProceso);
        request.setAttribute("aprobadas", aprobadas);
        request.setAttribute("rechazadas", rechazadas);
        request.setAttribute("areas", areas);
        request.setAttribute("hayFiltros", !q.isEmpty() || !estado.isEmpty() || area != 0);
        request.getRequestDispatcher("/WEB-INF/vistas/ciudadano/mis_solicitudes.jsp").forward(request, response);
    }

    private static String limpiar(String v) {
        return v == null ? "" : v.trim();
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
        return "Listado de solicitudes del ciudadano";
    }
}
