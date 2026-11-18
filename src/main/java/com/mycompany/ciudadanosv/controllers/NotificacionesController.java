package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Notificacion;
import com.mycompany.ciudadanosv.models.NotificacionDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Bandeja de notificaciones y bitácora del ciudadano.
 * URL protegida: /app/notificaciones
 *   GET  ?tipo=TRAMITE|CITA|SEGURIDAD   -> bandeja (filtrada)
 *   GET  ?vista=historial                -> bitácora de actividad
 *   POST accion=leer&id=..               -> marca un aviso como leído
 *   POST accion=archivar&id=..           -> lo quita de la bandeja
 *   POST accion=todas[&tipo=..]          -> marca todos (o los del filtro) como leídos
 *   POST accion=abrir&id=..              -> lo marca leído y lleva al trámite o a la cita
 */
public class NotificacionesController extends HttpServlet {

    private static final int LIMITE_BITACORA = 60;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Csrf.asegurarToken(request);
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        boolean historial = "historial".equals(request.getParameter("vista"));
        String tipo = tipoValido(request.getParameter("tipo"));
        NotificacionDAO dao = new NotificacionDAO();
        try {
            request.setAttribute("conteo", dao.contarPorTipo(usuario.getIdUsuario()));
            if (historial) {
                request.setAttribute("bitacora", dao.listarBitacora(usuario.getIdUsuario(), LIMITE_BITACORA));
            } else {
                request.setAttribute("notificaciones", dao.listar(usuario.getIdUsuario(), tipo));
            }
        } catch (SQLException ex) {
            log("Error cargando notificaciones", ex);
            request.setAttribute("errorBD", "No se pudieron cargar tus notificaciones. Intenta de nuevo.");
        }
        request.setAttribute("vista", historial ? "historial" : "bandeja");
        request.setAttribute("tipo", tipo);
        request.getRequestDispatcher("/WEB-INF/vistas/ciudadano/notificaciones.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Csrf.esValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        String accion = request.getParameter("accion");
        String tipo = tipoValido(request.getParameter("tipo"));
        int id = entero(request.getParameter("id"));
        NotificacionDAO dao = new NotificacionDAO();
        String volver = request.getContextPath() + "/app/notificaciones"
                + (tipo != null ? "?tipo=" + tipo : "");
        try {
            switch (accion == null ? "" : accion) {
                case "leer" -> dao.marcarLeida(usuario.getIdUsuario(), id);
                case "archivar" -> {
                    if (dao.archivar(usuario.getIdUsuario(), id)) {
                        request.getSession().setAttribute("aviso", "La notificación fue archivada.");
                    }
                }
                case "todas" -> {
                    int n = dao.marcarTodasLeidas(usuario.getIdUsuario(), tipo);
                    request.getSession().setAttribute("aviso", n == 0
                            ? "No tenías notificaciones pendientes de leer."
                            : (n == 1 ? "1 notificación marcada como leída." : n + " notificaciones marcadas como leídas."));
                }
                case "abrir" -> {
                    Notificacion n = dao.buscar(usuario.getIdUsuario(), id);
                    if (n != null) {
                        dao.marcarLeida(usuario.getIdUsuario(), id);
                        if (n.getFolioSolicitud() != null) {
                            volver = request.getContextPath() + "/app/solicitud?folio="
                                    + URLEncoder.encode(n.getFolioSolicitud(), StandardCharsets.UTF_8);
                        } else if (n.getIdCita() != null) {
                            volver = request.getContextPath() + "/app/citas";
                        }
                    }
                }
                default -> {
                    // acción desconocida: solo vuelve a la bandeja
                }
            }
        } catch (SQLException ex) {
            log("Error actualizando notificaciones", ex);
            request.getSession().setAttribute("avisoError", "No se pudo actualizar la notificación. Intenta de nuevo.");
        }
        response.sendRedirect(volver);
    }

    private static String tipoValido(String tipo) {
        return tipo != null && NotificacionDAO.TIPOS_FILTRO.contains(tipo) ? tipo : null;
    }

    private static int entero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException | NullPointerException ex) {
            return 0;
        }
    }

    @Override
    public String getServletInfo() {
        return "Notificaciones y bitácora del ciudadano";
    }
}
