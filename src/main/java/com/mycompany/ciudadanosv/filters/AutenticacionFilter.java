package com.mycompany.ciudadanosv.filters;

import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.models.UsuarioDAO;
import java.sql.SQLException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Protege las zonas privadas del portal (mapeado en web.xml):
 *   /app/*   -> requiere sesión iniciada (cualquier rol)
 *   /admin/* -> requiere sesión de funcionario (REVISOR, OPERADOR, SUPER_ADMIN)
 *
 * Sin sesión: redirige a /login?destino=<página pedida> para volver después.
 * Ciudadano intentando entrar a /admin: error 403 (acceso denegado).
 */
public class AutenticacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession sesion = request.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        String ruta = request.getServletPath();               // /app/... o /admin/...

        if (usuario == null) {
            String destino = ruta + (request.getPathInfo() == null ? "" : request.getPathInfo());
            if (request.getQueryString() != null) {
                destino += "?" + request.getQueryString();
            }
            response.sendRedirect(request.getContextPath() + "/login?destino="
                    + URLEncoder.encode(destino, StandardCharsets.UTF_8));
            return;
        }

        // Si el administrador la suspendió o le cambió rol/institución, se aplica de inmediato
        try {
            if (!new UsuarioDAO().refrescar(usuario)) {
                sesion.invalidate();
                response.sendRedirect(request.getContextPath() + "/login?suspendida=1");
                return;
            }
        } catch (SQLException ex) {
            request.getServletContext().log("No se pudo verificar la cuenta en sesión", ex);
        }

        if (ruta.startsWith("/admin") && !usuario.isFuncionario()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Páginas privadas: que el navegador no las guarde (botón "atrás" tras cerrar sesión)
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        chain.doFilter(req, res);
    }
}
