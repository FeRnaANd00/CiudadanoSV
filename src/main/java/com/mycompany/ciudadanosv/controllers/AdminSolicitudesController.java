package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.InstitucionDAO;
import com.mycompany.ciudadanosv.models.SolicitudAdminDAO;
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
 * Bandeja de solicitudes de la institución del funcionario (todas, para el super admin).
 * URL protegida: /admin/solicitudes?q=&estado=&institucion=&tramite=&mias=1&pagina=
 */
public class AdminSolicitudesController extends HttpServlet {

    private static final Set<String> ESTADOS = Set.of("ABIERTAS", "RECIBIDA", "EN_REVISION", "CITA_PROGRAMADA",
            "APROBADA", "RECHAZADA", "FINALIZADA");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");

        SolicitudAdminDAO.Filtro f = new SolicitudAdminDAO.Filtro();
        f.texto = Parametros.textoONulo(request, "q");
        String estado = Parametros.texto(request, "estado");
        // Por defecto la bandeja muestra lo que hay que atender
        f.estado = request.getParameter("estado") == null ? "ABIERTAS" : (ESTADOS.contains(estado) ? estado : null);
        f.idInstitucion = usuario.isSuperAdmin() ? Parametros.entero(request, "institucion") : null;
        f.idTramite = Parametros.entero(request, "tramite");
        f.soloMias = "1".equals(request.getParameter("mias"));
        f.pagina = Math.max(1, Parametros.entero(request, "pagina", 1));

        try {
            SolicitudAdminDAO dao = new SolicitudAdminDAO();
            int total = dao.contar(usuario, f);
            int paginas = Math.max(1, (total + SolicitudAdminDAO.POR_PAGINA - 1) / SolicitudAdminDAO.POR_PAGINA);
            if (f.pagina > paginas) {
                f.pagina = paginas;
            }
            request.setAttribute("solicitudes", dao.listar(usuario, f));
            request.setAttribute("total", total);
            request.setAttribute("paginas", paginas);
            request.setAttribute("indicadores", dao.indicadores(usuario));
            request.setAttribute("tramites", dao.listarTramitesFiltro(usuario));
            if (usuario.isSuperAdmin()) {
                request.setAttribute("instituciones", new InstitucionDAO().listar());
            }
        } catch (SQLException ex) {
            log("Error cargando la bandeja", ex);
            request.setAttribute("errorBD", "No se pudo cargar la bandeja. Intenta de nuevo.");
        }
        request.setAttribute("f", f);
        request.getRequestDispatcher("/WEB-INF/vistas/admin/solicitudes.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Bandeja de solicitudes (administración)";
    }
}
