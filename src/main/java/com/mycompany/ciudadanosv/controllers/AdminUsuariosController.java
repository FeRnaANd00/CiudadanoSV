package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.InstitucionDAO;
import com.mycompany.ciudadanosv.models.UsuarioAdminDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Parametros;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

/**
 * Directorio de usuarios (solo SUPER_ADMIN).
 * URL protegida: /admin/usuarios?q=&rol=&estado=&institucion=&pagina=
 */
public class AdminUsuariosController extends HttpServlet {

    private static final Set<String> ROLES = Set.of("CIUDADANO", "REVISOR", "OPERADOR", "SUPER_ADMIN");
    private static final Set<String> ESTADOS = Set.of("ACTIVO", "SUSPENDIDO", "EN_REVISION");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        if (!usuario.isSuperAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String q = Parametros.textoONulo(request, "q");
        String rol = ROLES.contains(Parametros.texto(request, "rol")) ? Parametros.texto(request, "rol") : null;
        String estado = ESTADOS.contains(Parametros.texto(request, "estado")) ? Parametros.texto(request, "estado") : null;
        Integer institucion = Parametros.entero(request, "institucion");
        int pagina = Math.max(1, Parametros.entero(request, "pagina", 1));
        try {
            UsuarioAdminDAO dao = new UsuarioAdminDAO();
            int total = dao.contar(q, rol, estado, institucion);
            int paginas = Math.max(1, (total + UsuarioAdminDAO.POR_PAGINA - 1) / UsuarioAdminDAO.POR_PAGINA);
            pagina = Math.min(pagina, paginas);
            request.setAttribute("cuentas", dao.listar(q, rol, estado, institucion, pagina));
            request.setAttribute("total", total);
            request.setAttribute("paginas", paginas);
            request.setAttribute("resumen", dao.resumen());
            request.setAttribute("matriz", dao.matrizPermisos());
            request.setAttribute("instituciones", new InstitucionDAO().listar());
        } catch (SQLException ex) {
            log("Error cargando usuarios", ex);
            request.setAttribute("errorBD", "No se pudo cargar el directorio de usuarios. Intenta de nuevo.");
        }
        request.setAttribute("q", q);
        request.setAttribute("rol", rol);
        request.setAttribute("estado", estado);
        request.setAttribute("institucion", institucion);
        request.setAttribute("pagina", pagina);
        request.getRequestDispatcher("/WEB-INF/vistas/admin/usuarios.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Gestión de usuarios (directorio)";
    }
}
