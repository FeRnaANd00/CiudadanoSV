package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Cita;
import com.mycompany.ciudadanosv.models.CitaDAO;
import com.mycompany.ciudadanosv.models.ReglaNegocioException;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * "Mis citas": próximas citas e historial; permite cancelar.
 * URL protegida: /app/citas
 *   GET              -> listado
 *   POST accion=cancelar&idCita=..  -> cancela y vuelve al listado
 */
public class MisCitasController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Csrf.asegurarToken(request);
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        try {
            List<Cita> proximas = new ArrayList<>();
            List<Cita> historial = new ArrayList<>();
            for (Cita c : new CitaDAO().listarPorUsuario(usuario.getIdUsuario())) {
                (c.isProxima() ? proximas : historial).add(c);
            }
            request.setAttribute("proximas", proximas);
            request.setAttribute("historial", historial);
        } catch (SQLException ex) {
            log("Error listando citas", ex);
            request.setAttribute("errorBD", "No se pudieron cargar tus citas. Intenta de nuevo.");
        }
        request.getRequestDispatcher("/WEB-INF/vistas/ciudadano/mis_citas.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Csrf.esValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        if ("cancelar".equals(request.getParameter("accion"))) {
            int idCita;
            try {
                idCita = Integer.parseInt(request.getParameter("idCita"));
            } catch (NumberFormatException | NullPointerException ex) {
                idCita = 0;
            }
            try {
                new CitaDAO().cancelar(usuario.getIdUsuario(), idCita, request.getRemoteAddr());
                request.getSession().setAttribute("aviso", "La cita fue cancelada y el turno quedó libre.");
            } catch (ReglaNegocioException ex) {
                request.getSession().setAttribute("avisoError", ex.getMessage());
            } catch (SQLException ex) {
                log("Error cancelando cita", ex);
                request.getSession().setAttribute("avisoError", "No se pudo cancelar la cita. Intenta de nuevo.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/app/citas");
    }

    @Override
    public String getServletInfo() {
        return "Citas del ciudadano";
    }
}
