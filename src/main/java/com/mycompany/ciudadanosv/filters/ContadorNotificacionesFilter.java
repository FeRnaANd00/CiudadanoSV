package com.mycompany.ciudadanosv.filters;

import com.mycompany.ciudadanosv.models.NotificacionDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Pone en la petición "notifNoLeidas" (número de la campanita del menú) cuando hay sesión.
 * Solo en peticiones GET, que son las que muestran una página. Mapeado en web.xml.
 */
public class ContadorNotificacionesFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if ("GET".equals(request.getMethod())) {
            HttpSession sesion = request.getSession(false);
            Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
            if (usuario != null) {
                try {
                    request.setAttribute("notifNoLeidas", new NotificacionDAO().contarNoLeidas(usuario.getIdUsuario()));
                } catch (SQLException ex) {
                    request.getServletContext().log("No se pudo contar las notificaciones", ex);
                }
            }
        }
        chain.doFilter(req, res);
    }
}
