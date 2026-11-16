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
    private static final java.util.Map<String, String> FORMULARIOS = java.util.Map.of(
            SolicitudPartidaController.CODIGO_TRAMITE, "/app/solicitud/partida",
            "MUNI-01", "/app/solicitud/municipal?codigo=MUNI-01",
            "MUNI-02", "/app/solicitud/municipal?codigo=MUNI-02",
            "RNPN-DUI-01", "/app/solicitud/dui?codigo=RNPN-DUI-01",
            "RNPN-DUI-02", "/app/solicitud/dui?codigo=RNPN-DUI-02"
    );

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
