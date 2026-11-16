package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.AreaServicio;
import com.mycompany.ciudadanosv.models.AreaServicioDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Landing page pública: muestra el catálogo de servicios.
 * URL: /inicio
 */
public class InicioController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AreaServicioDAO dao = new AreaServicioDAO();
        try {
            List<AreaServicio> areas = dao.listarConTramites();
            int totalTramites = 0;
            for (AreaServicio a : areas) {
                totalTramites += a.getTramites().size();
            }
            request.setAttribute("areas", areas);
            request.setAttribute("totalAreas", areas.size());
            request.setAttribute("totalTramites", totalTramites);
            request.setAttribute("totalSedes", dao.contarSedesActivas());
        } catch (SQLException ex) {
            log("No se pudo cargar el catálogo de servicios", ex);
            request.setAttribute("errorBD",
                    "No se pudo conectar con la base de datos ciudadanosv_bd. "
                    + "Verifica que MariaDB esté encendido y los datos de Conexion.java.");
        }

        // Mensaje de una sola vez (ej. después de iniciar sesión)
        Object aviso = request.getSession().getAttribute("aviso");
        if (aviso != null) {
            request.setAttribute("aviso", aviso);
            request.getSession().removeAttribute("aviso");
        }

        request.getRequestDispatcher("/WEB-INF/vistas/inicio.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Landing page con el catálogo de servicios";
    }
}
