package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.RequisitoDAO;
import com.mycompany.ciudadanosv.models.Tramite;
import com.mycompany.ciudadanosv.models.TramiteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Ficha de un trámite (requisitos, costo, plazo) antes de iniciar la solicitud.
 * URL protegida: /app/tramite?codigo=RNPN-DUI-01
 * En los siguientes pasos desde aquí se abrirá el formulario de cada trámite.
 */
public class TramiteController extends HttpServlet {

    /** Código del trámite -> formulario ya construido. Se agrega uno por cada paso nuevo. */
    private static final java.util.Map<String, String> FORMULARIOS = java.util.Map.ofEntries(
            java.util.Map.entry(SolicitudPartidaController.CODIGO_TRAMITE, "/app/solicitud/partida"),
            java.util.Map.entry("MUNI-01", "/app/solicitud/municipal?codigo=MUNI-01"),
            java.util.Map.entry("MUNI-02", "/app/solicitud/municipal?codigo=MUNI-02"),
            java.util.Map.entry("RNPN-DUI-01", "/app/solicitud/dui?codigo=RNPN-DUI-01"),
            java.util.Map.entry("RNPN-DUI-02", "/app/solicitud/dui?codigo=RNPN-DUI-02"),
            java.util.Map.entry("MINSAL-01", "/app/citas/nueva?codigo=MINSAL-01"),
            java.util.Map.entry("MINSAL-02", "/app/citas/nueva?codigo=MINSAL-02"),
            java.util.Map.entry("MINSAL-03", "/app/citas/nueva?codigo=MINSAL-03"),
            java.util.Map.entry("MINSAL-04", "/app/citas/nueva?codigo=MINSAL-04"),
            java.util.Map.entry("VMT-01", "/app/multas?tipo=dui"),
            java.util.Map.entry("VMT-02", "/app/multas?tipo=placa")
    );

    /** true si el trámite tiene un formulario programado (no se debe eliminar del catálogo). */
    public static boolean tieneFormulario(String codigo) {
        return codigo != null && FORMULARIOS.containsKey(codigo);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String codigo = request.getParameter("codigo");
        if (codigo == null || codigo.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/inicio#servicios");
            return;
        }
        try {
            Tramite tramite = new TramiteDAO().buscarPorCodigo(codigo.trim());
            if (tramite == null || "INACTIVO".equals(tramite.getEstado())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("tramite", tramite);
            request.setAttribute("requisitos", new RequisitoDAO().listarPorTramite(tramite.getIdTramite()));
            String formulario = FORMULARIOS.get(tramite.getCodigo());
            if (formulario != null) {
                request.setAttribute("urlFormulario", request.getContextPath() + formulario);
                if (formulario.startsWith("/app/citas")) {
                    request.setAttribute("textoBoton", "Agendar cita");
                    request.setAttribute("iconoBoton", "event_available");
                } else if (formulario.startsWith("/app/multas")) {
                    request.setAttribute("textoBoton", "Consultar multas");
                    request.setAttribute("iconoBoton", "search");
                }
            }
        } catch (SQLException ex) {
            log("No se pudo cargar el trámite " + codigo, ex);
            request.setAttribute("errorBD", "No se pudo cargar la información del trámite. Intenta de nuevo.");
        }
        request.getRequestDispatcher("/WEB-INF/vistas/ciudadano/tramite.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Ficha informativa de un trámite";
    }
}
