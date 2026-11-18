package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.EstadisticaDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Inicio del portal administrativo = panel estadístico (solo funcionarios; lo protege AutenticacionFilter).
 * Revisores y operadores ven los datos de su institución; el super admin, los de todo el portal.
 * URL: /admin/inicio
 */
public class AdminInicioController extends HttpServlet {

    private static final int DIAS_TENDENCIA = 14;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        try {
            EstadisticaDAO dao = new EstadisticaDAO();
            request.setAttribute("kpi", dao.indicadores(usuario));
            request.setAttribute("porArea", dao.porArea(usuario));
            request.setAttribute("porEstado", dao.porEstado(usuario));
            request.setAttribute("topTramites", dao.topTramites(usuario));
            request.setAttribute("porDia", dao.porDia(usuario, DIAS_TENDENCIA));
            request.setAttribute("diasTendencia", DIAS_TENDENCIA);
        } catch (SQLException ex) {
            log("Error cargando estadísticas", ex);
            request.setAttribute("errorBD", "No se pudieron cargar las estadísticas. Intenta de nuevo.");
        }
        request.getRequestDispatcher("/WEB-INF/vistas/admin/inicio.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Panel estadístico del portal administrativo";
    }
}
