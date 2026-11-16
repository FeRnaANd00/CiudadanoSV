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

/**
 * Confirmación después de enviar cualquier solicitud (muestra el folio).
 * URL protegida: /app/solicitud/enviada?folio=EXP-2026-00003
 * Solo muestra solicitudes del usuario en sesión.
 */
public class SolicitudEnviadaController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        String folio = request.getParameter("folio");
        if (folio == null || folio.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            Solicitud solicitud = new SolicitudDAO().buscarPorFolio(folio.trim(), usuario.getIdUsuario());
            if (solicitud == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("solicitud", solicitud);
        } catch (SQLException ex) {
            log("Error consultando solicitud " + folio, ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.getRequestDispatcher("/WEB-INF/vistas/ciudadano/solicitud_enviada.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Confirmación de solicitud enviada";
    }
}
