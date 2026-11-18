package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.TramiteAdminDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Parametros;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Catálogo de trámites (solo SUPER_ADMIN).
 * URL protegida: /admin/tramites?q=&area=&estado=
 */
public class AdminTramitesController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        if (!usuario.isSuperAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String q = Parametros.textoONulo(request, "q");
        Integer area = Parametros.entero(request, "area");
        String estado = Parametros.textoONulo(request, "estado");
        try {
            TramiteAdminDAO dao = new TramiteAdminDAO();
            request.setAttribute("tramites", dao.listar(q, area, estado));
            request.setAttribute("areas", dao.listarAreas());
        } catch (SQLException ex) {
            log("Error cargando el catálogo", ex);
            request.setAttribute("errorBD", "No se pudo cargar el catálogo. Intenta de nuevo.");
        }
        request.setAttribute("q", q);
        request.setAttribute("area", area);
        request.setAttribute("estado", estado);
        request.getRequestDispatcher("/WEB-INF/vistas/admin/tramites.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Catálogo de trámites (administración)";
    }
}
